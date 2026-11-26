import argparse
import hashlib
import io
import json
import time
import zipfile
from pathlib import Path

import numpy as np
from ai_edge_litert.interpreter import Interpreter
from PIL import Image


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--trashnet", type=Path, required=True)
    parser.add_argument(
        "--model",
        type=Path,
        default=Path(__file__).resolve().parents[1]
        / "app/src/main/assets/models/waste_mobilenet_v2_fp16.tflite",
    )
    parser.add_argument("--output", type=Path, required=True)
    args = parser.parse_args()
    labels = args.model.with_name("waste_labels.txt").read_text(encoding="utf-8").splitlines()
    interpreter = Interpreter(model_path=str(args.model), num_threads=2)
    interpreter.allocate_tensors()
    model_input = interpreter.get_input_details()[0]
    model_output = interpreter.get_output_details()[0]
    results = []
    with zipfile.ZipFile(args.trashnet) as archive:
        for category in ("cardboard", "glass", "metal", "paper", "plastic", "trash"):
            images = sorted(
                name
                for name in archive.namelist()
                if f"/{category}/" in name
                and name.lower().endswith(".jpg")
                and not name.startswith("__MACOSX")
            )
            if len(images) < 10:
                raise ValueError(f"Not enough images for {category}")
            for name in images[:10]:
                with Image.open(io.BytesIO(archive.read(name))) as source:
                    photo = source.convert("RGB").resize((224, 224), Image.Resampling.BILINEAR)
                pixels = np.asarray(photo, dtype=np.float32) / 127.5 - 1
                interpreter.set_tensor(model_input["index"], pixels[None])
                start = time.perf_counter()
                interpreter.invoke()
                elapsed_ms = (time.perf_counter() - start) * 1000
                scores = interpreter.get_tensor(model_output["index"])[0]
                ranking = np.argsort(scores)[::-1]
                results.append(
                    {
                        "file": name,
                        "expected": category,
                        "predicted": labels[int(ranking[0])],
                        "score": float(scores[ranking[0]]),
                        "gap": float(scores[ranking[0]] - scores[ranking[1]]),
                        "inference_ms": elapsed_ms,
                    }
                )
    correct = sum(row["expected"] == row["predicted"] for row in results)
    report = {
        "purpose": "Integration smoke check, not an independent holdout evaluation",
        "model_sha256": hashlib.sha256(args.model.read_bytes()).hexdigest(),
        "archive_sha256": hashlib.sha256(args.trashnet.read_bytes()).hexdigest(),
        "correct": correct,
        "total": len(results),
        "results": results,
    }
    args.output.write_text(json.dumps(report, indent=2) + "\n", encoding="utf-8")
    print(f"Smoke check: {correct}/{len(results)} category matches")


if __name__ == "__main__":
    main()
