package com.rather.ecosort;

import com.rather.ecosort.models.Placemark;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

final class CollectionPoints {
    private static final Pattern SEPARATORS = Pattern.compile("[^\\p{L}\\p{N}]+");

    private CollectionPoints() {
    }

    static List<Placemark> fromRows(String[] rows) {
        List<Placemark> points = new ArrayList<>(rows.length);
        for (String row : rows) {
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
