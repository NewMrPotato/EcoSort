package com.rather.ecosort;

import com.rather.ecosort.models.Placemark;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

final class CollectionPoints {
    private static final Pattern SEPARATORS = Pattern.compile("[^\\p{L}\\p{N}]+");

    private static final String[] DEMO_ROWS = {
            "60.938836|76.565343|Пункт приёма вторсырья Эко-Пункт|ул. Ленина, 16",
            "60.951186|76.559995|Пункт приёма одежды|ул. Мира, 11/2",
            "60.946178|76.578106|Пункт приёма вторсырья|ул. Мира, 29А",
            "60.935877|76.539806|Пункт приёма вторсырья|панель №20",
            "60.941541|76.557583|Контейнер для батареек и ламп|ул. Ленина, 7, корп. 1",
            "60.931442|76.551727|Контейнер для ПЭТ-бутылок ТрансСервис|ул. Менделеева, 2",
            "60.932713|76.549977|Контейнер для сбора макулатуры|ул. Менделеева, 7",
            "60.935330|76.585101|Пункт приёма одежды на благотворительность|ул. Ленина, 38А",
            "60.940700|76.594134|Контейнер для вторсырья|ул. Чапаева, 22",
            "60.937612|76.539843|Пункт приёма опасных отходов и шин|ул. Ленина, 4П, стр. 12",
            "60.941727|76.557812|Контейнер для ПЭТ-бутылок ТрансСервис|ул. Ленина, 7, корп. 1",
            "60.937904|76.551399|Контейнер для батареек|ул. Кузоваткина, 14",
            "60.937950|76.536092|Контейнер для ПЭТ-бутылок|ул. Ленина, 2П, стр. 12",
            "60.967223|76.534906|Пункт приёма шин и металлолома|Индустриальная ул., 54",
            "60.940138|76.591120|Контейнер для крышечек|ул. Мира, 43С1",
            "60.944286|76.565609|Контейнер для батареек и ламп|ул. Маршала Жукова, 8",
            "60.960698|76.535580|Пункт приёма ПЭТ-бутылок Нзум Юнипак|Индустриальная ул., 46, стр. 19",
            "60.957968|76.533081|Пункт приёма вторсырья Эко-Мир|панель № 15",
            "60.935681|76.617437|Контейнер для батареек и ламп|улица Романтиков",
            "60.946697|76.576438|Контейнер для вторсырья|улица Комсомольское Озеро",
            "60.952581|76.503444|СанСет|ул. Авиаторов, 2",
            "60.945330|76.607244|Контейнер для вторсырья|Интернациональная ул., 53",
            "60.940513|76.623643|Контейнер раздельного сбора отходов|Интернациональная ул., 38А",
            "60.989803|76.465773|Контейнер для пластиковых бутылок и алюминиевых банок РО Югра Экология|район НВ ГПЗ"
    };

    private CollectionPoints() {
    }

    static List<Placemark> demo() {
        List<Placemark> points = new ArrayList<>(DEMO_ROWS.length);
        for (String row : DEMO_ROWS) {
            String[] fields = row.split("\\|", 4);
            points.add(new Placemark(Double.parseDouble(fields[0]), Double.parseDouble(fields[1]),
                    fields[2], fields[3], fields[2]));
        }
        return Collections.unmodifiableList(points);
    }

    static List<Placemark> search(List<Placemark> points, String query) {
        String normalizedQuery = normalize(query);
        if (normalizedQuery.isEmpty()) {
            return points;
        }

        String[] terms = normalizedQuery.split(" ");
        List<Placemark> matches = new ArrayList<>();
        for (Placemark point : points) {
            String searchableText = normalize(point.getTitle() + " " + point.getContent());
            if (matchesAllTerms(searchableText, terms)) {
                matches.add(point);
            }
        }
        return matches;
    }

    private static boolean matchesAllTerms(String searchableText, String[] terms) {
        for (String term : terms) {
            if (!searchableText.contains(term)) {
                return false;
            }
        }
        return true;
    }

    private static String normalize(String text) {
        String folded = text.toLowerCase(Locale.ROOT).replace('ё', 'е');
        return SEPARATORS.matcher(folded).replaceAll(" ").trim();
    }
}
