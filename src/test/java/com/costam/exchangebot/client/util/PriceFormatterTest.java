package com.costam.exchangebot.client.util;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Testy dla klasy PriceFormatter - rzeczywisty kod z ExchangeBota
 *
 * Testuje parsowanie i formatowanie cen (np. "100k" = 100000)
 */
public class PriceFormatterTest {

    // ===== TESTY PARSOWANIA CENY (parsePrice) =====

    @Test
    public void testParseSimpleNumber() {
        // Podstawowa liczba bez mnożnika
        assertEquals(100.0, PriceFormatter.parsePrice("100"), 0.01);
    }

    @Test
    public void testParseNumberWithK() {
        // Liczba z "k" (tysiące) - np. "100k" = 100000
        assertEquals(100_000.0, PriceFormatter.parsePrice("100k"), 0.01);
    }

    @Test
    public void testParseNumberWithM() {
        // Liczba z "m" (miliony) - np. "1.5m" = 1500000
        assertEquals(1_500_000.0, PriceFormatter.parsePrice("1.5m"), 0.01);
    }

    @Test
    public void testParseNumberWithMln() {
        // Liczba z "mln" (miliony) - np. "2mln" = 2000000
        assertEquals(2_000_000.0, PriceFormatter.parsePrice("2mln"), 0.01);
    }

    @Test
    public void testParseNumberWithMld() {
        // Liczba z "mld" (miliardy) - np. "1.5mld" = 1500000000
        assertEquals(1_500_000_000.0, PriceFormatter.parsePrice("1.5mld"), 0.01);
    }

    @Test
    public void testParseWithCommaAsDecimal() {
        // Przecinek jako separator dziesiętny (europejski format)
        assertEquals(1_500.0, PriceFormatter.parsePrice("1,5k"), 0.01);
    }

    @Test
    public void testParseWithSpaces() {
        // Spacje na początku/końcu powinny być ignorowane
        assertEquals(100_000.0, PriceFormatter.parsePrice("  100k  "), 0.01);
    }

    @Test
    public void testParseCaseInsensitive() {
        // Duże i małe litery powinny działać tak samo
        assertEquals(100_000.0, PriceFormatter.parsePrice("100K"), 0.01);
        assertEquals(1_000_000.0, PriceFormatter.parsePrice("1M"), 0.01);
    }

    @Test
    public void testParseInvalidString() {
        // Nieprawidłowy tekst zwraca -1
        assertEquals(-1, PriceFormatter.parsePrice("abc"), 0.01);
    }

    @Test
    public void testParseEmptyString() {
        // Pusty string zwraca -1
        assertEquals(-1, PriceFormatter.parsePrice(""), 0.01);
    }

    @Test
    public void testParseNullString() {
        // null zwraca -1
        assertEquals(-1, PriceFormatter.parsePrice(null), 0.01);
    }

    @Test
    public void testParseDecimalNumber() {
        // Liczby dziesiętne
        assertEquals(123.45, PriceFormatter.parsePrice("123.45"), 0.01);
    }

    @Test
    public void testParseDecimalWithK() {
        // Liczba dziesiętna z mnożnikiem
        assertEquals(123_450.0, PriceFormatter.parsePrice("123.45k"), 0.01);
    }

    // ===== TESTY FORMATOWANIA CENY (formatPrice) =====

    @Test
    public void testFormatSmallNumber() {
        // Liczba mniejsza od 1000 - bez mnożnika
        assertEquals("100", PriceFormatter.formatPrice(100.0));
    }

    @Test
    public void testFormatNumbersAboveThousand() {
        // Liczba powyżej 1000 - formatowana z "k"
        assertEquals("100k", PriceFormatter.formatPrice(100_000.0));
    }

    @Test
    public void testFormatNumbersAboveMillion() {
        // Liczba powyżej miliona - formatowana z "m"
        assertEquals("1.5m", PriceFormatter.formatPrice(1_500_000.0));
    }

    @Test
    public void testFormatNumbersAboveBillion() {
        // Liczba powyżej miliarda - formatowana z "mld"
        assertEquals("1.5mld", PriceFormatter.formatPrice(1_500_000_000.0));
    }

    @Test
    public void testFormatRemovesTrailingZeros() {
        // Zera końcowe powinny być usunięte
        assertEquals("1k", PriceFormatter.formatPrice(1_000.0));
        assertEquals("1.5m", PriceFormatter.formatPrice(1_500_000.0));
    }

    @Test
    public void testFormatNegativeNumber() {
        // Liczby ujemne powinny być obsługiwane
        assertEquals("-100k", PriceFormatter.formatPrice(-100_000.0));
    }

    @Test
    public void testFormatZero() {
        // Zero
        assertEquals("0", PriceFormatter.formatPrice(0.0));
    }

    @Test
    public void testFormatDecimal() {
        // Liczby dziesiętne
        assertEquals("123.45", PriceFormatter.formatPrice(123.45));
    }

    @Test
    public void testFormatDecimalWithK() {
        // Liczba dziesiętna z mnożnikiem
        assertEquals("1.23k", PriceFormatter.formatPrice(1_234.56));
    }

    // ===== TESTY ROUND-TRIP (parse -> format -> parse) =====

    @Test
    public void testRoundTripSimpleNumber() {
        // Parse -> Format -> Parse = powinna być ta sama liczba
        double original = 100_000.0;
        String formatted = PriceFormatter.formatPrice(original);
        double reparsed = PriceFormatter.parsePrice(formatted);
        assertEquals(original, reparsed, 0.01);
    }

    @Test
    public void testRoundTripDecimal() {
        // Liczby dziesiętne mogą mieć małe różnice z powodu zaokrąglenia
        double original = 1_234_567.89;
        String formatted = PriceFormatter.formatPrice(original);
        double reparsed = PriceFormatter.parsePrice(formatted);
        // Tolerancja 1% z powodu zaokrąglenia
        assertEquals(original, reparsed, original * 0.01);
    }

    @Test
    public void testRoundTripLargeNumber() {
        double original = 500_000_000.0;
        String formatted = PriceFormatter.formatPrice(original);
        double reparsed = PriceFormatter.parsePrice(formatted);
        assertEquals(original, reparsed, 1);
    }
}

