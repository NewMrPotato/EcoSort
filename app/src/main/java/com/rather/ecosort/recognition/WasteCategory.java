package com.rather.ecosort.recognition;

import com.rather.ecosort.R;

public enum WasteCategory {
    BATTERY("battery", R.string.waste_battery, R.string.waste_battery_advice),
    BIOLOGICAL("biological", R.string.waste_biological, R.string.waste_biological_advice),
    CARDBOARD("cardboard", R.string.waste_cardboard, R.string.waste_cardboard_advice),
    CLOTHES("clothes", R.string.waste_clothes, R.string.waste_clothes_advice),
    GLASS("glass", R.string.waste_glass, R.string.waste_glass_advice),
    METAL("metal", R.string.waste_metal, R.string.waste_metal_advice),
    PAPER("paper", R.string.waste_paper, R.string.waste_paper_advice),
    PLASTIC("plastic", R.string.waste_plastic, R.string.waste_plastic_advice),
    SHOES("shoes", R.string.waste_shoes, R.string.waste_shoes_advice),
    TRASH("trash", R.string.waste_trash, R.string.waste_trash_advice);

    public final String modelLabel;
    public final int nameResource;
    public final int adviceResource;

    WasteCategory(String modelLabel, int nameResource, int adviceResource) {
        this.modelLabel = modelLabel;
        this.nameResource = nameResource;
        this.adviceResource = adviceResource;
    }
}
