# Konfiguracja JUnit w IntelliJ IDEA

## ✅ Automatyczne rozpoznanie

IntelliJ IDEA automatycznie rozpozna testy JUnit dzięki:
1. Adnotacji `@Test` z pakietu `org.junit`
2. Umieszczeniu testów w katalogu `src/test/java`
3. Nazwie klasy kończącej się na `Test`

## 🎯 Uruchamianie testów z IDE

### Uruchomienie całej klasy testowej
1. Otwórz klasę testową (np. `ExampleTest.java`)
2. Kliknij prawym przyciskiem myszy na nazwie klasy
3. Wybierz "Run 'ExampleTest'"
4. Lub naciśnij `Ctrl + Shift + F10` (Windows/Linux)

### Uruchomienie pojedynczego testu
1. Umieść kursor na metodzie testowej (oznaczonej `@Test`)
2. Naciśnij `Ctrl + Shift + F10`
3. Lub kliknij na zieloną ikonę strzałki obok numeru linii

### Debugowanie testu
1. Postaw punkt przerwania (breakpoint) w kodzie testu
2. Kliknij prawym przyciskiem na test
3. Wybierz "Debug 'TestClassName.testMethod'"
4. Lub naciśnij `Ctrl + Shift + D`

## 📊 Wyświetlanie wyników testów

Po uruchomieniu testów pojawi się:
- **Test Results Panel** - dokładne wyniki każdego testu
- **Console Output** - logowanie i komunikaty debugowania
- **Code Coverage** (jeśli włączone)

## 🔧 Konfiguracja Run/Debug Configuration

1. `Run` → `Edit Configurations...`
2. Kliknij `+` aby dodać nową konfigurację
3. Wybierz `JUnit`
4. Ustaw:
   - **Name**: `All Tests` (lub dowolna nazwa)
   - **Test kind**: `All in package` lub `Class`
   - **Package**: `com.costam`
   - **Search for tests**: `Whole project`
5. Kliknij `OK`

## 📈 Code Coverage

1. `Run` → `Run 'ExampleTest' with Coverage`
2. Lub `Ctrl + Alt + F9` (Windows/Linux)
3. W oknie Coverage zobaczysz:
   - Które linie kodu są testowane
   - Procent pokrycia kodem

## 🎨 Wskazówki IDE

IntelliJ IDEA oferuje:
- **Quick Fix** (`Alt + Enter`) - sugestie generowania testów
- **Generate Test** (`Ctrl + Shift + T`) - szybkie tworzenie testów dla klasy
- **Intention Actions** - automatyczne uzupełnianie asercji
- **Test Navigation** - szybka nawigacja między kodem a testami (`Ctrl + Shift + T`)

## 📋 Skróty klawiszowe

| Akcja | Skrót |
|-------|-------|
| Uruchom testy | `Ctrl + Shift + F10` |
| Debuguj test | `Ctrl + Shift + D` |
| Run with Coverage | `Ctrl + Alt + F9` |
| Przejdź do testu | `Ctrl + Shift + T` |
| Wygeneruj test | `Ctrl + Shift + T` (na klasie) |

## 🚀 Szybkie szablony (Live Templates)

Możesz utworzyć szablony dla szybkiego tworzenia testów:

1. `File` → `Settings` → `Editor` → `Live Templates`
2. Wybierz grupę `Java`
3. Kliknij `+` i utwórz nowy szablon

Przykład szablonu:
```
@Test
public void $NAME$() {
    $END$
}
```

---
Teraz możesz wygodnie pracować z testami JUnit w IntelliJ IDEA!

