package com.rather.ecosort;

import com.rather.ecosort.models.Placemark;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class CollectionPointsTest {
    private final Placemark batteries = new Placemark(60.9, 76.5,
            "Пункт приёма батареек", "ул. Ленина, 7");
    private final Placemark paper = new Placemark(60.8, 76.6,
            "Контейнер для макулатуры", "ул. Ленина, 16");
    private final List<Placemark> points = Arrays.asList(batteries, paper);

    @Test
    public void searchesTitleAndAddressIgnoringCaseAndPunctuation() {
        assertEquals(Collections.singletonList(batteries),
                CollectionPoints.search(points, " ПРИЕМА,   Ленина "));
        assertEquals(Collections.singletonList(paper),
                CollectionPoints.search(points, "Ленина, 16"));
    }

    @Test
    public void emptyQueryShowsAllPointsInOrder() {
        assertEquals(points, CollectionPoints.search(points, "   "));
    }

    @Test
    public void returnsNoResultsWhenTermsMatchDifferentPoints() {
        assertEquals(Collections.emptyList(), CollectionPoints.search(points, "батареек 16"));
    }
}
