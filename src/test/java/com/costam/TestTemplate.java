package com.costam;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Szablon dla nowych testów jednostkowych
 *
 * Skopiuj tę klasę i zmień:
 * - Nazwę klasy na [ClassNameToTest]Test
 * - Zawartość metod testowych na rzeczywiste testy
 */
public class TestTemplate {

    // Zmienne instancji używane w testach
    // private YourClass instance;

    /**
     * Uruchamia się przed każdym testem
     * Użyj do inicjalizacji obiektów testowych
     */
    @Before
    public void setUp() {
        // instance = new YourClass();
    }

    /**
     * Uruchamia się po każdym teście
     * Użyj do czyszczenia zasobów
     */
    @After
    public void tearDown() {
        // instance = null;
    }

    /**
     * Szablon testu
     * Zmień na rzeczywisty test
     */
    @Test
    public void testSomething() {
        // Prepare (Przygotowanie)
        // Utwórz dane testowe

        // Act (Wykonanie)
        // Wykonaj badaną operację

        // Assert (Asercja)
        // Zweryfikuj wynik
        assertTrue("To jest zawsze prawdą dla szablonu", true);
    }

    /**
     * Przykład testu z oczekiwanym wyjątkiem
     */
    @Test(expected = IllegalArgumentException.class)
    public void testExceptionHandling() {
        // Kod, który powinien wyrzucić IllegalArgumentException
        throw new IllegalArgumentException("Zły argument");
    }

    /**
     * Przykład testu z komunikatem błędu
     */
    @Test
    public void testWithMessage() {
        int result = 2 + 2;
        assertEquals("Matematyka: 2 + 2 = 4", 4, result);
    }

    /**
     * Przykład testu parametryzowanego (wymaga @RunWith(Parameterized.class))
     * oraz dodatkowych zależności
     */
    @Test
    public void testMultipleValues() {
        int[] values = {1, 2, 3, 4, 5};
        for (int value : values) {
            assertTrue("Wartość powinna być większa od 0", value > 0);
        }
    }
}

