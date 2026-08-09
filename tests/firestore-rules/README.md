# Firestore baseline rules tests

Diese Tests dokumentieren den aktuellen Sicherheitszustand der Firestore-Regeln fuer XP- und Quest-Zustaende.

- Node-Version: Node.js 20+ (lokal geprueft mit `v24.14.1`)
- Java-Version: Java 21+ fuer den aktuellen Firestore-Emulator
- Lokale Projekt-ID: `tierdex-rules-test`
- Produktivprojekt: wird nicht verwendet

## Installation

```bash
npm install
```

## Testlauf

```bash
npm test
```

Wenn lokal standardmaessig nur Java 18 verfuegbar ist, muss fuer den Testlauf ein Java-21-Runtime aktiv sein, zum Beispiel die Android-Studio-JBR.

Die Tests laden immer direkt `../../firestore.rules` und pruefen damit den echten Repository-Stand. Dokumente fuer Lesetests werden nur in einem regelumgehenden Setup-Kontext angelegt.

Tests mit `BASELINE VULNERABILITY` bestaetigen bewusst aktuell erlaubte, aber fachlich unsichere Schreibzugriffe. Diese Tests muessen bei der spaeteren Sicherheitsumstellung gezielt angepasst oder invertiert werden.
