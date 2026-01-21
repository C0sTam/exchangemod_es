package com.costam;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Przykładowa klasa testowa demonstrująca bardziej zaawansowane funcjonalności JUnit
 */
public class AdvancedExampleTest {

    private Calculator calculator;

    /**
     * Metoda wykonywana przed każdym testem
     */
    @Before
    public void setUp() {
        calculator = new Calculator();
    }

    @Test
    public void testAddition() {
        int result = calculator.add(5, 3);
        assertEquals("5 + 3 powinno dać 8", 8, result);
    }

    @Test
    public void testSubtraction() {
        int result = calculator.subtract(10, 4);
        assertEquals("10 - 4 powinno dać 6", 6, result);
    }

    @Test
    public void testMultiplication() {
        int result = calculator.multiply(6, 7);
        assertEquals("6 * 7 powinno dać 42", 42, result);
    }

    @Test
    public void testDivision() {
        int result = calculator.divide(20, 4);
        assertEquals("20 / 4 powinno dać 5", 5, result);
    }

    @Test(expected = ArithmeticException.class)
    public void testDivisionByZero() {
        calculator.divide(10, 0);
    }

    @Test
    public void testNegativeNumbers() {
        int result = calculator.add(-5, -3);
        assertEquals("-5 + (-3) powinno dać -8", -8, result);
    }

    @Test
    public void testZero() {
        int result = calculator.add(0, 0);
        assertEquals("0 + 0 powinno dać 0", 0, result);
    }

    /**
     * Prosta klasa kalkulator dla celów demonstracyjnych
     */
    private static class Calculator {
        public int add(int a, int b) {
            return a + b;
        }

        public int subtract(int a, int b) {
            return a - b;
        }

        public int multiply(int a, int b) {
            return a * b;
        }

        public int divide(int a, int b) {
            if (b == 0) {
                throw new ArithmeticException("Dzielenie przez zero!");
            }
            return a / b;
        }
    }
}

