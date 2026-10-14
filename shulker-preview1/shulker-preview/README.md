# Shulker Preview (Fabric, Minecraft 1.21.10)

Zeigt den Inhalt von Shulkerboxen direkt an, ohne sie zu öffnen. Reine Client-Mod.

## Status ehrlich

- **Ziel-Version:** Minecraft Java **1.21.10**, Fabric Loader >= 0.17.3, Fabric API 0.138.4+1.21.10, Java 21.
- **Nicht getestet:** Der Code konnte in der Entwicklungsumgebung nicht kompiliert und nicht im Spiel getestet werden (Fabric-/Minecraft-Server dort gesperrt). Es gibt deshalb **keine fertige JAR** und keine behauptete Versionsunterstützung außer 1.21.10 als Ziel. Gebaut wird bei dir mit `build.bat`.
- Falls beim Build ein Fehler kommt (die Minecraft-GUI-API ändert sich oft): Fehlermeldung kopieren und schicken, dann wird es angepasst.

## Bauen (Windows)

1. Java 21 (JDK) installieren.
2. Ordner entpacken, `build.bat` doppelklicken (erster Lauf lädt Gradle, Minecraft und Fabric, dauert einige Minuten).
3. JAR aus `build\libs\` (ohne `-sources`) in `%appdata%\.minecraft\mods` kopieren, zusammen mit Fabric API.

## Funktionen

**Inventar (und Creative-Inventar, Kisten usw.)**
- Maus über Shulkerbox: Panel mit Titel (z. B. "Black Shulker Box", nach Spracheinstellung) und 9x3-Slotraster, Original-Item-Renderer (Texturen, Glitzern, Anzahl, Haltbarkeitsbalken).
- Maus weg: Vorschau verschwindet. Keine Taste nötig.
- **Shift halten:** Panel wird fixiert. Maus auf ein Item im Panel: normaler Minecraft-Tooltip (Name, Verzauberungen mit Level, Haltbarkeit, Attribute).
- **Shift loslassen:** Detailansicht sofort weg, normale Vorschau bleibt, solange die Shulkerbox angeschaut wird.
- Klicks auf das fixierte Panel werden blockiert, damit nichts darunter verschoben wird. Die Mod verändert, verschiebt oder dupliziert selbst keine Items.
- Der normale Tooltip der Shulkerbox wird durch das Panel ersetzt (Titel ist enthalten), wie im Screenshot.

**Spielwelt (Fadenkreuz auf platzierter Shulkerbox)**
- Panel unter dem Fadenkreuz, aktualisiert sich laufend.
- **Einzelspieler / LAN-Host:** Inhalt kommt aus der integrierten Server-Welt.
- **Multiplayer:** Vanilla-Server senden den Inhalt platzierter Shulkerboxen in der Regel nicht an den Client. Dann zeigt die Mod ein leeres Raster mit dem Hinweis "Leer oder Inhalt vom Server nicht übermittelt". Es werden nie Items erfunden. Daran lässt sich ohne Server-Unterstützung nichts ändern.
- Loot-Tabellen werden nicht generiert (keine Weltveränderung); unaufgedeckte Loot-Shulker erscheinen leer.
- Shift-Detail gibt es in der Spielwelt nicht (es gibt keinen Mauszeiger).

## Unterstützte Version

| Minecraft | Plattform | Abhängigkeiten | Status |
|---|---|---|---|
| 1.21.10 | Fabric | Loader >= 0.17.3, Fabric API, Java 21 | Quellcode fertig, **nicht kompiliert/getestet** |

Weitere Versionen (z. B. 1.21.11) folgen erst nach erfolgreichem Build der ersten.

## Bekannte Einschränkungen / Risiken

- Nicht getestet (siehe oben). Wahrscheinlichste Fehlerquellen: Mixin-Namen `drawMouseoverTooltip` / `onMouseClick`, `DrawContext.createNewRootLayer()`, `ContainerComponent.copyTo`.
- Das Panel ersetzt den Vanilla-Tooltip von Shulkerboxen komplett (Lore-Zeilen werden nicht angezeigt).
- Andere Tooltip-Mods für Shulkerboxen können sich überschneiden; dann eine davon entfernen.
- Farben/Optik sind dem Screenshot nachempfunden, aber nicht pixelgenau (kein Resource-Pack-Textur-Abgleich).

## Projektstruktur

- `ShulkerPreviewMod` Einstiegspunkt (Screen-Events, HUD-Element)
- `PreviewController` Inventar-Logik inkl. Shift-Fixierung
- `WorldPreview` Fadenkreuz-Vorschau
- `PanelRenderer` Zeichnen des Panels
- `ShulkerData` Inhalt aus Item-Daten lesen
- `mixin/` Zugriff auf fokussierten Slot, Tooltip ersetzen, Klicks blockieren
