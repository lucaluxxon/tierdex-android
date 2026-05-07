# Tierdex: XP- und Levelsystem

Stand: 07.05.2026

Diese Datei dokumentiert den aktuellen Plan für das XP- und Levelsystem von Tierdex. Sie dient als fachliche Grundlage für spätere Codex-Prompts und technische Umsetzungsschritte.

## 1. Grundidee

Das XP-System soll Tierdex langfristig motivierender machen, ohne den Sammelgedanken zu verzerren. Der wichtigste Fortschritt soll aus echter Aktivität, Artenvielfalt, sauber dokumentierten Funden, Quests und später Challenges entstehen.

Die Levelkurve soll am Anfang bewusst belohnend sein. Spätere Level dürfen deutlich schwerer werden und sollen langfristig durch größere Quests, viele Funde, viele verschiedene Arten, Fotos, Standortdaten und spätere Challenges erreichbar sein.

Wichtig: Neue unterschiedliche Tierarten werden mit steigendem Fortschritt natürlicherweise schwerer zu finden. Deshalb soll die Levelkurve nicht hart exponentiell sein, sondern sanft progressiv.

## 2. Basis-XP

### 2.1 Täglicher Login

| Aktion | XP |
|---|---:|
| Täglicher Login | 2 XP |

Der tägliche Login soll pro Nutzer und Datum gespeichert werden, zum Beispiel:

```text
daily_login:2026-05-07
```

Dadurch darf der tägliche Login am selben Tag nicht mehrfach XP auslösen.

### 2.2 Fund-XP pro Tierart

Ein konkreter Tierfund soll nur bei den ersten drei Funden derselben Art XP geben. Ab dem vierten Fund derselben Art gibt es keine Basis-XP mehr.

| Fund derselben Art | Basis-XP |
|---|---:|
| 1. Fund einer Art | 10 XP |
| 2. Fund derselben Art | 5 XP |
| 3. Fund derselben Art | 3 XP |
| ab 4. Fund derselben Art | 0 XP |

Diese Staffelung soll Wiederholungsfunde belohnen, aber neue Arten weiterhin klar attraktiver machen.

### 2.3 Foto- und Standortbonus

Foto- und Standort-XP zählen auch beim zweiten und dritten Fund derselben Art, aber nur solange der Fund selbst XP-berechtigt ist. Ab dem vierten Fund derselben Art gibt es auch keine Foto- oder Standort-XP mehr.

| Zusatz beim XP-berechtigten Fund | XP |
|---|---:|
| Fund mit Foto | +3 XP |
| Fund mit Standort | +3 XP |
| Fund mit Notiz | 0 XP |

Notizen sollen vorerst keine Basis-XP geben, damit keine sehr kurzen oder bedeutungslosen Notizen nur wegen XP eingetragen werden. Notiz-Quests können später individuell bewertet werden.

### 2.4 Beispielrechnung für einen einzelnen Fund

| Situation | XP |
|---|---:|
| 1. Fund einer Art ohne Foto und Standort | 10 XP |
| 1. Fund einer Art mit Foto und Standort | 16 XP |
| 2. Fund derselben Art mit Foto und Standort | 11 XP |
| 3. Fund derselben Art mit Foto und Standort | 9 XP |
| 4. Fund derselben Art mit Foto und Standort | 0 XP |

## 3. Quest-XP

Quest-XP sollen nur einmalig pro erreichten Questschritt vergeben werden. Dafür braucht jede Queststufe eine stabile ID bzw. einen stabilen Award-Key.

### 3.1 Tiergruppen-Quests

Tiergruppen-Quests zählen nur unterschiedliche Tierarten innerhalb einer Gruppe, nicht einzelne Funde.

Beispiel:

| Eingetragene Funde | Zählt für Vogelarten-Quest |
|---|---:|
| 10x Amsel | 1 Vogelart |
| 5x Amsel und 5x Taube | 2 Vogelarten |
| Amsel, Taube, Rotkehlchen, Elster, Star | 5 Vogelarten |

XP-Regel:

```text
XP = Zielzahl x 10
```

Beispiele:

| Queststufe | XP |
|---|---:|
| 1 verschiedene Vogelart finden | 10 XP |
| 5 verschiedene Vogelarten finden | 50 XP |
| 10 verschiedene Vogelarten finden | 100 XP |
| 25 verschiedene Vogelarten finden | 250 XP |
| 50 verschiedene Vogelarten finden | 500 XP |

Diese Logik gilt analog für alle Tiergruppen, zum Beispiel Vögel, Fische, Säugetiere, Amphibien und Reptilien.

### 3.2 Gesamtfundanzahl-Quest

Die Gesamtfundanzahl-Quest zählt alle eingetragenen Funde, also auch Wiederholungsfunde derselben Art.

Beispiel:

| Eingetragene Funde | Zählt für Gesamtfundanzahl |
|---|---:|
| 5x Amsel und 5x Taube | 10 Funde |

XP-Regel:

```text
XP = Zielzahl x 10
```

Beispiele:

| Queststufe | XP |
|---|---:|
| 1 Fund insgesamt | 10 XP |
| 5 Funde insgesamt | 50 XP |
| 10 Funde insgesamt | 100 XP |
| 25 Funde insgesamt | 250 XP |
| 50 Funde insgesamt | 500 XP |
| 100 Funde insgesamt | 1000 XP |

Diese Quest belohnt allgemeine Aktivität in der App.

### 3.3 Gesamtarten- bzw. Prozentquest

Die Prozentquest orientiert sich am Fortschrittsbalken auf der Startseite. Sie zählt den Anteil unterschiedlicher gefundener Arten am gesamten Tierdex.

XP-Regel:

| Fortschritt am gesamten Tierdex | XP |
|---|---:|
| 10% aller Tiere gefunden | 100 XP |
| 20% aller Tiere gefunden | 200 XP |
| 30% aller Tiere gefunden | 300 XP |
| 40% aller Tiere gefunden | 400 XP |
| 50% aller Tiere gefunden | 500 XP |
| 60% aller Tiere gefunden | 600 XP |
| 70% aller Tiere gefunden | 700 XP |
| 80% aller Tiere gefunden | 800 XP |
| 90% aller Tiere gefunden | 900 XP |
| 100% aller Tiere gefunden | 1000 XP |

Die Prozentquest ist langfristig gedacht. Bei ungefähr 900 Tieren erreicht ein Nutzer mit 50 verschiedenen Arten noch keine 10%.

### 3.4 Andere Questarten

Andere Questarten, zum Beispiel Fotoquests, Standortquests, Notizquests, Spezialarten oder soziale Quests, bekommen später individuelle XP-Werte.

Regel für die spätere Planung:

Wenn eine neue Questart noch keinen XP-Wert hat, soll der Nutzer gezielt gefragt werden, bevor Codex die XP-Werte fest einbaut.

## 4. Soziale XP

Soziale XP sollen bewusst niedrig bleiben und täglich begrenzt werden.

| Aktion | XP | Tageslimit |
|---|---:|---:|
| Fund eines anderen Nutzers liken | 2 XP | maximal 3 XP-vergütete Likes pro Tag |
| Kommentar schreiben | 2 XP | maximal 3 XP-vergütete Kommentare pro Tag |

Das bedeutet: Pro Tag können höchstens 3 Likes und 3 Kommentare XP auslösen. Weitere Likes oder Kommentare bleiben möglich, geben aber keine XP mehr.

## 5. Challenges

Challenges werden beim XP-System vorerst zurückgestellt, bis das Challenge-System existiert.

Später sollen Challenges eine wichtige Rolle für höhere Level spielen. Sie können Tages-, Wochen-, Saison- oder Spezialziele enthalten und individuell mit XP bewertet werden.

## 6. Verhalten beim Löschen, Sync und Import

### 6.1 XP werden beim Löschen nicht abgezogen

Wenn ein Fund gelöscht wird, werden bereits erhaltene XP nicht wieder abgezogen.

Wichtig ist aber: Bereits vergebene XP dürfen nicht erneut vergeben werden, wenn derselbe Fund oder dieselbe Fundstufe später wieder auftaucht.

### 6.2 Rückwirkende XP

Bestehende Funde sollen beim Einführen des XP-Systems einmalig rückwirkend XP erhalten.

Das darf nur mit sauberer Absicherung über persistente Award-Keys passieren, damit Sync, Import, App-Neustart oder erneutes Speichern keine doppelten XP erzeugen.

### 6.3 Award-Keys

Jede XP-Vergabe braucht einen stabilen Schlüssel. Beispiele:

```text
daily_login:2026-05-07
finding_xp:amsel:1
finding_xp:amsel:2
finding_xp:amsel:3
group_quest:voegel:10
total_findings:50
total_species_percent:10
social_like:2026-05-07:1
social_comment:2026-05-07:1
```

Technische Zielregel:

XP werden nur vergeben, wenn der jeweilige Award-Key noch nicht als vergeben gespeichert ist.

## 7. Levelkurve

Die Levelkurve soll sanft progressiv sein.

Grundprinzip:

- Level 1 bis 5: schneller Einstieg und frühe Erfolgserlebnisse
- Level 6 bis 15: aktiver Gelegenheitsnutzer mit vielen normalen Funden
- Level 16 bis 25: regelmäßiger Nutzer mit klar sichtbarer Sammlung
- Level 26 bis 35: sehr aktiver Nutzer mit vielen Arten und Zusatzdaten
- Level 36 bis 50: Langzeitspieler mit jahrelanger Nutzung, vielen Arten, vielen Funden, Quests, Fotos, Standorten und später Challenges
- Level 51 bis 60: sehr langfristige Spitzentitel für besonders aktive Nutzer über viele Jahre

Grobe Orientierung für XP bis zum nächsten Level:

| Levelschritt | XP bis zum nächsten Level |
|---|---:|
| Level 1 -> 2 | ca. 50 XP |
| Level 2 -> 3 | ca. 75 XP |
| Level 3 -> 4 | ca. 100 XP |
| Level 4 -> 5 | ca. 125 XP |
| Level 5 -> 6 | ca. 150 XP |
| Level 10 -> 11 | ca. 400 XP |
| Level 20 -> 21 | ca. 1000 XP |
| Level 30 -> 31 | ca. 1800 XP |
| Level 40 -> 41 | ca. 2800 XP |

Level 50 oder höher soll nicht bedeuten, dass ein Nutzer alle Tiere gefunden hat. Diese Level sollen eher anzeigen, dass der Nutzer langfristig sehr aktiv war, viele Funde dokumentiert und große Ziele abgeschlossen hat.

## 8. Leveltitel

Die Leveltitel sollen später im Profil auftauchen können. Sie sollen eine natürliche Sammel- und Forschungsprogression abbilden, leicht spielerisch wirken, aber nicht zu albern sein.

| Level | Titel |
|---:|---|
| 1 | Natur-Neuling |
| 5 | Spurenleser |
| 10 | Feldforscher |
| 15 | Artenkenner |
| 20 | Tierkundiger |
| 25 | Naturbeobachter |
| 30 | Wildnisforscher |
| 35 | Artenexperte |
| 40 | Tierdex-Meister |
| 45 | Natur-Chronist |
| 50 | Wandelndes Tier-Lexikon |
| 55 | Tier-König |
| 60 | Legende des Tierdex |

Die Titel ab Level 50 sind als langfristige Spitzentitel gedacht. Sie sollen nicht durch kurze Aktivität erreichbar sein, sondern durch viele Arten, viele Gesamtfunde, Fotos, Standorte, Quests und spätere Challenges über längere Zeit.

## 9. Beispiel: Gelegentlicher Nutzer nach drei Monaten

Annahme:

- 3 Monate Nutzung
- ca. 25 Logins
- 50 verschiedene Tierarten gefunden
- ca. 65 Gesamtfunde wegen einiger Dopplungen
- viele Funde mit Foto und Standort
- mehrere kleine und mittlere Gruppenquests abgeschlossen
- noch keine 10%-Prozentquest erreicht

Grobe XP-Schätzung:

| Quelle | XP grob |
|---|---:|
| Logins | ca. 50 XP |
| neue Arten | ca. 500 XP |
| Wiederholungsfunde | ca. 65 XP |
| Foto- und Standortboni | ca. 200-250 XP |
| Gesamtfundquests bis 50 Funde | ca. 910 XP |
| Tiergruppenquests | ca. 500-700 XP |
| Prozentquest | 0 XP |

Ergebnis:

Ein gelegentlicher Nutzer mit 50 verschiedenen Arten nach drei Monaten läge ungefähr bei 2200 bis 2500 XP und damit grob bei Level 11 bis 12.

Das wird als passend bewertet: Der Nutzer hat sichtbaren Fortschritt, ist aber noch weit entfernt von hohen Langzeit-Leveln.

## 10. Offene Punkte

Noch offen für spätere Planung:

1. Konkrete Leveltabelle bis Level 60 ausformulieren.
2. Leveltitel im Profil optisch und technisch einbauen.
3. Spätere Challenge-XP definieren, sobald Challenges konzipiert sind.
4. XP-Werte für weitere Questarten individuell festlegen.
5. Technische Speicherstruktur für XP, Level, Award-Keys und rückwirkende Vergabe festlegen.
6. Anzeige im Profil und eventuell auf der Startseite gestalten.

