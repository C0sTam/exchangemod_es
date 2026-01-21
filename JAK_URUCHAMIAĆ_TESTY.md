# 🚀 Jak uruchamiać testy - PRAKTYCZNY PORADNIK

## 🎯 Najszybszy sposób - Otwórz raport HTML

Po uruchomieniu testów odwiedź plik raportu (otwórz w przeglądarce):
```
C:\Users\konto\Documents\exchangebot-all\exchangemod_es\build\reports\tests\test\index.html
```

**To pokazuje wszystkie wyniki testów w ładnym formacie!**

---

## 💻 Polecenia w PowerShell

### 1. Uruchom WSZYSTKIE testy i zobacz raport
```powershell
cd "C:\Users\konto\Documents\exchangebot-all\exchangemod_es"
.\gradlew test
start build/reports/tests/test/index.html
```

### 2. Uruchom testy JEDNEJ klasy
```powershell
.\gradlew test --tests com.costam.ExampleTest
start build/reports/tests/test/index.html
```

### 3. Uruchom JEDEN konkretny test
```powershell
# Test testBasicAddition z klasy ExampleTest
.\gradlew test --tests com.costam.ExampleTest.testBasicAddition
start build/reports/tests/test/index.html
```

### 4. Uruchom testy z dokładnymi informacjami w konsoli
```powershell
.\gradlew test --tests com.costam.ExampleTest -i
```

### 5. Czyszczenie i ponowne uruchomienie
```powershell
.\gradlew clean test
start build/reports/tests/test/index.html
```

---

## 🎨 Najlepiej - Z IntelliJ IDEA

Jest to NAJSZYBSZY i NAJPROSTSZY sposób!

### Uruchom test z IDE:

**Wszystkie testy w klasie:**
1. Otwórz plik testowy (np. `ExampleTest.java`)
2. Kliknij na nazwę klasy
3. Naciśnij `Ctrl + Shift + F10`
4. Wyniki pojawią się w panelu poniżej

**Jeden konkretny test:**
1. Otwórz plik testowy
2. Kliknij na metodę testową (np. `testBasicAddition`)
3. Naciśnij `Ctrl + Shift + F10`
4. Wynik pojawi się w panelu

**Debugowanie testu:**
1. Postaw punkt przerwania (kliknij obok numeru linii)
2. Naciśnij `Ctrl + Shift + D` na tescie
3. Przejdź przez kod krok po kroku

---

## 📊 Czytanie raportu HTML

Otwórz raport w przeglądarce:
```
build/reports/tests/test/index.html
```

Zobaczysz:
- ✅ Ile testów przeszło
- ❌ Ile testów nie przeszło
- ⏱️ Ile czasu zajęły
- 📝 Szczegóły każdego testu
- 🔗 Linki do poszczególnych klas testowych

---

## 🌟 SZYBKIE SKRÓTY

| Co chcę | Komenda |
|---------|---------|
| Uruchomić test | `.\gradlew test` |
| Zobaczyć wynik | `start build/reports/tests/test/index.html` |
| Wyczyścić build | `.\gradlew clean` |
| Rebuild | `.\gradlew clean test` |
| Jeden test | `.\gradlew test --tests com.costam.ExampleTest.testBasicAddition` |
| Jedna klasa | `.\gradlew test --tests com.costam.ExampleTest` |

---

## 📋 PRZYKŁAD - Pełny workflow

```powershell
# 1. Przejdź do katalogu projektu
cd "C:\Users\konto\Documents\exchangebot-all\exchangemod_es"

# 2. Uruchom testy
.\gradlew test

# 3. Otwórz raport
start build/reports/tests/test/index.html

# 4. Przegląd wyników w przeglądarce
```

**GOTOWE! 🎉**

---

## 🔍 Co sprawdzić w raporcie

1. **Test Summary** - Podsumowanie (ile testów, ile się powiodło)
2. **Duration** - Jak długo trwały testy
3. **Packages** - Testów pogrupowane po pakietach
4. **Classes** - Testów pogrupowane po klasach
5. **Szczegóły** - Kliknij na test aby zobaczyć dokładne informacje

---

## ⚠️ Jeśli test się nie pojawia w raporcie

1. Sprawdź czy klasa testowa kończy się na `Test` (np. `ExampleTest`)
2. Sprawdź czy metody mają `@Test` (ctrl+click na `@Test` aby zobaczyć czy jest zaimportowana)
3. Sprawdź czy metoda jest publiczna: `public void testXxx()`
4. Uruchom: `.\gradlew clean test`

---

## 🎓 PODSUMOWANIE

**Najszybszy sposób:**
```powershell
.\gradlew test
start build/reports/tests/test/index.html
```

**Najłatwiejszy sposób (z IDE):**
- Otwórz test w IntelliJ
- Naciśnij `Ctrl + Shift + F10`
- Wynik pojawi się natychmiast

**Jeden konkretny test:**
```powershell
.\gradlew test --tests com.costam.ExampleTest.testBasicAddition
```

---

Gotowy? Spróbuj teraz! 🚀

