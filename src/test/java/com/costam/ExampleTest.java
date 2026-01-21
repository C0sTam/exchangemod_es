package com.costam;

import org.junit.Test;
import static org.junit.Assert.*;
import com.costam.exchangebot.client.util.PriceFormatter;

/**
 * Testy dla rzeczywistego kodu - klasa PriceFormatter z ExchangeBota
 *
 * Te testy sprawdzają rzeczywistą funkcjonalność formatowania cen
 * (np. "100k" = 100000, "1.5m" = 1500000)
 */
public class ExampleTest {

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
        // Liczba z "mln" (miliony)
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
    public void testFormatRemovesTrailingZeros() {
        // Zera końcowe powinny być usunięte
        assertEquals("1k", PriceFormatter.formatPrice(1_000.0));
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
}

