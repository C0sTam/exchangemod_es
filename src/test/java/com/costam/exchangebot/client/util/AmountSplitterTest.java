package com.costam.exchangebot.client.util;

import org.junit.Test;
import java.util.List;
import static org.junit.Assert.*;

/**
 * Testy dla klasy AmountSplitter - rzeczywisty kod z ExchangeBota
 * 
 * Testuje dzielenie dużych ilości na mniejsze części (max 100k na raz)
 */
public class AmountSplitterTest {

    @Test
    public void testSplitAmountSmall() {
        // Liczba mniejsza niż 100k - powinno być 1 część
        List<Long> result = AmountSplitter.splitAmount(50_000);
        
        assertEquals("Powinno być 1 część", 1, result.size());
        assertEquals("Część powinna mieć wartość 50000", 50_000L, (long) result.get(0));
    }

    @Test
    public void testSplitAmountExactly100k() {
        // Dokładnie 100k - powinno być 1 część
        List<Long> result = AmountSplitter.splitAmount(100_000);
        
        assertEquals("Powinno być 1 część", 1, result.size());
        assertEquals("Część powinna mieć wartość 100000", 100_000L, (long) result.get(0));
    }

    @Test
    public void testSplitAmountSlightlyAbove100k() {
        // Liczba nieco powyżej 100k - powinno być 2 części
        List<Long> result = AmountSplitter.splitAmount(100_001);
        
        assertEquals("Powinno być 2 części", 2, result.size());
        assertEquals("Pierwsza część powinna mieć 100000", 100_000L, (long) result.get(0));
        assertEquals("Druga część powinna mieć 1", 1L, (long) result.get(1));
    }

    @Test
    public void testSplitAmount200k() {
        // 200k - powinno być 2 równe części
        List<Long> result = AmountSplitter.splitAmount(200_000);
        
        assertEquals("Powinno być 2 części", 2, result.size());
        assertEquals("Pierwsza część: 100000", 100_000L, (long) result.get(0));
        assertEquals("Druga część: 100000", 100_000L, (long) result.get(1));
    }

    @Test
    public void testSplitAmount250k() {
        // 250k - powinno być 3 części: 100k + 100k + 50k
        List<Long> result = AmountSplitter.splitAmount(250_000);
        
        assertEquals("Powinno być 3 części", 3, result.size());
        assertEquals("Pierwsza część: 100000", 100_000L, (long) result.get(0));
        assertEquals("Druga część: 100000", 100_000L, (long) result.get(1));
        assertEquals("Trzecia część: 50000", 50_000L, (long) result.get(2));
    }

    @Test
    public void testSplitAmountLarge() {
        // 1 milion - powinno być 10 części po 100k
        List<Long> result = AmountSplitter.splitAmount(1_000_000);
        
        assertEquals("Powinno być 10 części", 10, result.size());
        for (Long part : result) {
            assertEquals("Każda część powinna mieć 100000", 100_000L, (long) part);
        }
    }

    @Test
    public void testSplitAmountVeryLarge() {
        // 2.5 miliona
        List<Long> result = AmountSplitter.splitAmount(2_500_000);
        
        assertEquals("Powinno być 25 części", 25, result.size());
        
        // Wszystkie części oprócz ostatniej powinny być 100k
        for (int i = 0; i < 24; i++) {
            assertEquals("Część " + i + " powinna mieć 100000", 100_000L, (long) result.get(i));
        }
        
        // Ostatnia część
        assertEquals("Ostatnia część powinna mieć 100000", 100_000L, (long) result.get(24));
    }

    @Test
    public void testSplitAmountOne() {
        // 1 - najmniejsza liczba
        List<Long> result = AmountSplitter.splitAmount(1);
        
        assertEquals("Powinno być 1 część", 1, result.size());
        assertEquals("Część powinna mieć 1", 1L, (long) result.get(0));
    }

    @Test
    public void testSplitAmountZero() {
        // 0 - powinno być puste
        List<Long> result = AmountSplitter.splitAmount(0);
        
        assertEquals("Powinno być 0 części", 0, result.size());
    }

    @Test
    public void testSplitAmountSumEqualsOriginal() {
        // Suma wszystkich części powinna równać się oryginalnej liczbie
        long original = 345_678;
        List<Long> parts = AmountSplitter.splitAmount(original);
        
        long sum = 0;
        for (Long part : parts) {
            sum += part;
        }
        
        assertEquals("Suma części powinna równać się oryginałowi", original, sum);
    }

    @Test
    public void testSplitAmountAllPartsNotEmpty() {
        // Żadna część nie powinna być pusta
        List<Long> result = AmountSplitter.splitAmount(150_000);
        
        for (Long part : result) {
            assertTrue("Każda część powinna być > 0", part > 0);
        }
    }

    @Test
    public void testSplitAmountAllPartsMaximum() {
        // Żadna część nie powinna przekraczać 100k
        List<Long> result = AmountSplitter.splitAmount(500_000);
        
        for (Long part : result) {
            assertTrue("Żadna część nie powinna przekraczać 100000", part <= 100_000);
        }
    }

    @Test
    public void testSplitAmountRealisticGameValue() {
        // Realistyczna wartość z gry - gracz ma 1.5 miliona i chce sprzedać
        List<Long> result = AmountSplitter.splitAmount(1_500_000);
        
        assertEquals("Powinno być 15 części", 15, result.size());
        assertEquals("Każda część powinna być 100k", 100_000L, (long) result.get(0));
        
        // Suma powinna być poprawna
        long sum = result.stream().mapToLong(Long::longValue).sum();
        assertEquals("Suma powinna być 1.5 miliona", 1_500_000L, sum);
    }

    @Test
    public void testSplitAmountOddNumber() {
        // Liczba nieparzysta - 123_456
        List<Long> result = AmountSplitter.splitAmount(123_456);
        
        // Powinna być 2 części: 100k + 23456
        assertEquals("Powinno być 2 części", 2, result.size());
        assertEquals("Pierwsza część: 100000", 100_000L, (long) result.get(0));
        assertEquals("Druga część: 23456", 23_456L, (long) result.get(1));
    }
}

