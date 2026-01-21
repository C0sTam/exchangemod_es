# 🚀 JUnit - Quick Start

## Co zostało zrobione?

✅ **JUnit zainstalowany i skonfigurowany w projekcie ExchangeBot**

### 📦 Dodane komponenty:
- JUnit 4.13.2 (framework testowania)
- Hamcrest 2.2 (matcher library)
- Katalog testów: `src/test/java/com/costam/`
- 3 przykładowe klasy testowe
- Kompletna dokumentacja

## 🏃 Szybki start

### 1. Uruchamianie testów z linii poleceń
```powershell
cd "C:\Users\konto\Documents\exchangebot-all\exchangemod_es"
.\gradlew test
```

### 2. Uruchamianie testów z IntelliJ IDEA
- Otwórz plik testowy (np. `ExampleTest.java`)
- Naciśnij `Ctrl + Shift + F10` aby uruchomić
- Lub kliknij zieloną ikonę strzałki obok nazwy testu

### 3. Tworzenie nowego testu
1. Skopiuj `TestTemplate.java`
2. Zmień nazwę na `MyFeatureTest.java`
3. Napisz swoje testy wewnątrz `@Test` metod
4. Uruchom testy

## 📁 Struktura projektu

```
src/
├── main/
│   └── resources/
│       └── fabric.mod.json
├── client/
│   ├── java/
│   └── resources/
└── test/
    └── java/
        └── com/costam/
            ├── ExampleTest.java           ← Podstawowe przykłady
            ├── AdvancedExampleTest.java   ← Zaawansowane przykłady
            └── TestTemplate.java          ← Szablon dla nowych testów
```

## 📝 Przykład testu

```java
package com.costam;

import org.junit.Test;
import static org.junit.Assert.*;

public class MyTest {
    @Test
    public void testAddition() {
        int result = 2 + 2;
        assertEquals(4, result);
    }
}
```

## 🔍 Przydatne asercje

```java
assertEquals(oczekiwana, rzeczywista);
assertTrue(warunek);
assertFalse(warunek);
assertNull(obiekt);
assertNotNull(obiekt);
fail("komunikat");
```

## 📊 Wyświetlanie raportu testów

Po uruchomieniu testów raport HTML znajduje się w:
```
build/reports/tests/test/index.html
```

Otwórz plik w przeglądarce.

## 📚 Dodatkowe zasoby

- **JUNIT_SETUP.md** - Kompletna dokumentacja konfiguracji
- **JUNIT_SUMMARY.md** - Podsumowanie i lista asercji
- **JUNIT_INTELLIJ_SETUP.md** - Poradnik dla IntelliJ IDEA

## ⚙️ Modyfikacja build.gradle

Zależności testowe znajdują się w sekcji `dependencies`:

```gradle
testImplementation "junit:junit:4.13.2"
testImplementation "org.hamcrest:hamcrest:2.2"
```

## 🆘 Troubleshooting

### Testy się nie kompilują
```powershell
.\gradlew clean compileTestJava
```

### Chcę zresetować buidl
```powershell
.\gradlew clean build
```

### Wyświetlenie szczegółowych informacji
```powershell
.\gradlew test --stacktrace
```

---

**Status**: ✅ Gotowe do użytku

Więcej informacji w dokumentach markdown: JUNIT_SETUP.md, JUNIT_SUMMARY.md, JUNIT_INTELLIJ_SETUP.md

