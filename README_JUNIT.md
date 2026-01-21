# JUnit Testowanie Jednostkowe - ExchangeBot

## 📌 Przegląd

Projekt **ExchangeBot** został skonfigurowany z pełną obsługą testów jednostkowych przy użyciu:
- **JUnit 4.13.2** - Framework do testowania jednostkowego
- **Hamcrest 2.2** - Matcher library dla bardziej czytelnych asercji

---

## 🎯 Szybki start

### Dla osób chcących się szybko dowiedzieć:
👉 **Przeczytaj**: [`JUNIT_QUICKSTART.md`](JUNIT_QUICKSTART.md)

### Dla osób chcących kompletną dokumentację:
👉 **Przeczytaj**: [`JUNIT_SETUP.md`](JUNIT_SETUP.md)

### Dla użytkowników IntelliJ IDEA:
👉 **Przeczytaj**: [`JUNIT_INTELLIJ_SETUP.md`](JUNIT_INTELLIJ_SETUP.md)

### Dla listy asercji i podsumowania:
👉 **Przeczytaj**: [`JUNIT_SUMMARY.md`](JUNIT_SUMMARY.md)

---

## ✨ Co zostało zrobione

### 1️⃣ Konfiguracja Gradle
```gradle
dependencies {
    // ... pozostałe zależności ...
    
    // JUnit Testing Dependencies
    testImplementation "junit:junit:4.13.2"
    testImplementation "org.hamcrest:hamcrest:2.2"
}
```

### 2️⃣ Struktura katalogów
```
src/test/
└── java/
    └── com/costam/
        ├── ExampleTest.java          (Podstawowe asercje)
        ├── AdvancedExampleTest.java  (Zaawansowane cechy)
        └── TestTemplate.java         (Szablon dla nowych testów)
```

### 3️⃣ Klasy testowe

#### **ExampleTest.java**
Demonstruje podstawowe asercje JUnit:
- `assertEquals()` - sprawdzenie równości
- `assertNotNull()` - sprawdzenie czy nie jest null
- `assertTrue()`/`assertFalse()` - sprawdzenie warunku

#### **AdvancedExampleTest.java**
Demonstruje zaawansowane funkcjonalności:
- Adnotacja `@Before` - inicjalizacja przed każdym testem
- Testy z `@Test(expected=Exception.class)` 
- Testowanie wyjątków
- Testy liczb ujemnych

#### **TestTemplate.java**
Szablon do szybkiego tworzenia nowych testów:
- Komentarze objaśniające każdą sekcję
- Przykłady best practices
- Gotowy do skopiowania

---

## 🚀 Uruchamianie testów

### Z linii poleceń
```powershell
# Uruchom wszystkie testy
.\gradlew test

# Uruchom konkretny test
.\gradlew test --tests com.costam.ExampleTest

# Uruchom konkretną metodę
.\gradlew test --tests com.costam.ExampleTest.testBasicAddition

# Czyszczenie i rebuild
.\gradlew clean test
```

### Z IntelliJ IDEA
1. Otwórz klasę testową
2. Naciśnij `Ctrl + Shift + F10` - uruchomi wszystkie testy w klasie
3. Na pojedynczej metodzie: `Ctrl + Shift + F10` - uruchomi tylko ten test
4. Debug: `Ctrl + Shift + D` - uruchomi test w trybie debugowania

### Raport testów
Po uruchomieniu testów raport HTML znajduje się w:
```
build/reports/tests/test/index.html
```

---

## 📝 Pisanie nowych testów

### Krok 1: Stwórz nową klasę
```java
package com.costam;

import org.junit.Test;
import static org.junit.Assert.*;

public class MyFeatureTest {
    
}
```

### Krok 2: Dodaj metodę testową
```java
@Test
public void testWhatYourCodeDoes() {
    // Arrange - przygotowanie
    int input = 5;
    
    // Act - wykonanie
    int result = myFunction(input);
    
    // Assert - asercja
    assertEquals(10, result);
}
```

### Krok 3: Uruchom test
```powershell
.\gradlew test --tests com.costam.MyFeatureTest.testWhatYourCodeDoes
```

---

## 📚 Dostępne asercje

| Asercja | Przykład |
|---------|----------|
| `assertEquals()` | `assertEquals(4, 2+2);` |
| `assertNotEquals()` | `assertNotEquals(5, 2+2);` |
| `assertTrue()` | `assertTrue(true);` |
| `assertFalse()` | `assertFalse(false);` |
| `assertNull()` | `assertNull(null);` |
| `assertNotNull()` | `assertNotNull("tekst");` |
| `assertSame()` | `assertSame(obj1, obj1);` |
| `assertNotSame()` | `assertNotSame(obj1, obj2);` |
| `fail()` | `fail("Test nie powiódł się");` |

---

## 🏷️ Dostępne adnotacje

| Adnotacja | Opis |
|-----------|------|
| `@Test` | Oznacza metodę jako test |
| `@Before` | Uruchamia się przed każdym testem |
| `@After` | Uruchamia się po każdym teście |
| `@BeforeClass` | Uruchamia się raz na początku |
| `@AfterClass` | Uruchamia się raz na końcu |
| `@Ignore` | Pomija test |
| `@Test(expected=Exception.class)` | Oczekuje konkretnego wyjątku |

---

## 💡 Best Practices

### 1. Trzy sekcje testu (AAA Pattern)
```java
@Test
public void testSomething() {
    // Arrange - przygotowanie danych
    int a = 5;
    int b = 3;
    
    // Act - wykonanie badanej funkcjonalności
    int result = a + b;
    
    // Assert - weryfikacja wyniku
    assertEquals(8, result);
}
```

### 2. Nazewnictwo testów
- Nazwa klasy: `[NazwaKlasyDoTestowania]Test`
- Nazwa metody: `test[Co robi test]`

```java
public class CalculatorTest {
    @Test
    public void testAdditionWithPositiveNumbers() { }
    
    @Test
    public void testAdditionWithNegativeNumbers() { }
    
    @Test
    public void testDivisionByZeroThrowsException() { }
}
```

### 3. One assertion per test (gdzie to możliwe)
```java
// ✅ Dobrze - jeden test dla jednego warunku
@Test
public void testAdditionReturnsCorrectResult() {
    assertEquals(8, calculator.add(5, 3));
}

// ❌ Złe - wiele asercji w jednym teście
@Test
public void testCalculations() {
    assertEquals(8, calculator.add(5, 3));
    assertEquals(2, calculator.subtract(5, 3));
    assertEquals(15, calculator.multiply(5, 3));
}
```

### 4. Używaj setUp() i tearDown()
```java
private Calculator calculator;

@Before
public void setUp() {
    calculator = new Calculator();
}

@After
public void tearDown() {
    calculator = null;
}
```

---

## 🔧 Troubleshooting

### Problem: "Testy nie kompilują się"
```powershell
.\gradlew clean compileTestJava --stacktrace
```

### Problem: "JUnit nie znajduje testów"
- Upewnij się, że klasa kończy się na `Test`
- Upewnij się, że metody mają adnotację `@Test`
- Upewnij się, że znajdują się w `src/test/java`

### Problem: "Import JUnit się nie powodzie"
```powershell
.\gradlew build
```

---

## 📋 Lista plików

| Plik | Opis |
|------|------|
| `JUNIT_QUICKSTART.md` | 🚀 Szybki start - czytaj PIERWSZY |
| `JUNIT_SETUP.md` | 📖 Kompletna dokumentacja konfiguracji |
| `JUNIT_SUMMARY.md` | 📊 Podsumowanie i lista asercji |
| `JUNIT_INTELLIJ_SETUP.md` | 🎨 Poradnik dla IntelliJ IDEA |
| `src/test/java/com/costam/ExampleTest.java` | 📝 Przykłady podstawowe |
| `src/test/java/com/costam/AdvancedExampleTest.java` | 🚀 Przykłady zaawansowane |
| `src/test/java/com/costam/TestTemplate.java` | 📋 Szablon dla nowych testów |

---

## 🎓 Dodatkowe zasoby

### Oficjalna dokumentacja
- [JUnit 4 Official Documentation](https://junit.org/junit4/)
- [Hamcrest Matchers](https://hamcrest.org/JavaHamcrest/)

### Tutorials
- [JUnit Tutorial for Beginners](https://www.tutorialspoint.com/junit/)
- [Testing with JUnit 4](https://www.mkyong.com/unittest/junit-4-tutorial/)

---

## ✅ Checklist dla nowych testów

- [ ] Klasa testowa kończy się na `Test`
- [ ] Metody testowe mają adnotację `@Test`
- [ ] Każdy test ma jasną, opisową nazwę
- [ ] Test testuje jedną rzecz
- [ ] Test ma sekcje: Arrange, Act, Assert
- [ ] Testy są niezależne od siebie
- [ ] Testy się kompilują: `.\gradlew test`
- [ ] Testy przechodzą: `BUILD SUCCESSFUL`

---

## 📞 Szybka pomoc

```powershell
# Uruchom testy
.\gradlew test

# Sprawdź raport
start build/reports/tests/test/index.html

# Czyszczenie
.\gradlew clean

# Rebuild
.\gradlew clean test
```

---

**Status**: ✅ JUnit zainstalowany i gotowy do użytku

**Data konfiguracji**: 2026-01-21
**Wersja JUnit**: 4.13.2
**Projekt**: ExchangeBot (Fabric Mod for Minecraft 1.21.4)

