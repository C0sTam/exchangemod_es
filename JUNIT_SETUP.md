# Testy Jednostkowe (JUnit) - ExchangeBot

## Konfiguracja

Projekt został skonfigurowany z obsługą testów jednostkowych JUnit 4. Zależności testowe zostały dodane do pliku `build.gradle`:

```gradle
testImplementation "junit:junit:4.13.2"
testImplementation "org.hamcrest:hamcrest:2.2"
```

## Struktura katalogów testów

```
src/test/
└── java/
    └── com/
        └── costam/
            └── ExampleTest.java
```

## Uruchamianie testów

### Uruchomienie wszystkich testów
```bash
./gradlew test
```

### Uruchomienie konkretnego testu
```bash
./gradlew test --tests com.costam.ExampleTest
```

### Uruchomienie konkretnej metody testowej
```bash
./gradlew test --tests com.costam.ExampleTest.testBasicAddition
```

## Tworzenie nowych testów

Wszystkie testy powinny być umieszczane w katalogu `src/test/java/com/costam/`.

Przykładowa struktura klasy testowej:

```java
package com.costam;

import org.junit.Test;
import static org.junit.Assert.*;

public class MyTest {
    
    @Test
    public void testSomething() {
        // Kod testu
        assertEquals("Oczekiwana wartość", "rzeczywista wartość");
    }
}
```

## Dostępne asercje JUnit

- `assertEquals(expected, actual)` - sprawdzenie równości wartości
- `assertNotEquals(unexpected, actual)` - sprawdzenie nierówności wartości
- `assertTrue(condition)` - sprawdzenie, czy warunek jest true
- `assertFalse(condition)` - sprawdzenie, czy warunek jest false
- `assertNull(object)` - sprawdzenie, czy obiekt jest null
- `assertNotNull(object)` - sprawdzenie, czy obiekt nie jest null
- `assertSame(expected, actual)` - sprawdzenie tożsamości referencji
- `assertNotSame(unexpected, actual)` - sprawdzenie różności referencji
- `fail()` - niezawodnie niepowodzenie testu

## Przykłady testów

W pliku `ExampleTest.java` znajdują się przykładowe testy demonstrujące podstawowe funkcjonalności JUnit.

## Raporty testów

Po uruchomieniu testów raport HTML znajduje się w:
```
build/reports/tests/test/index.html
```

Otwórz ten plik w przeglądarce, aby zobaczyć szczegółowy raport testów.

## Integracja z IDE

Większość IDE (IntelliJ IDEA, Eclipse, VS Code) automatycznie rozpoznaje testy JUnit i pozwala na ich uruchamianie bezpośrednio z editora.

