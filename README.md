# Nine Men's Morris (Mühle) — Java

Lojë tavoline klasike me ndërfaqe grafike Swing.

## Struktura e Projektit

```
NineMensMorris/
├── src/
│   ├── Main.java                  ← Pika hyrëse
│   ├── model/
│   │   ├── Board.java             ← Tabela (24 nyje, mullinjët, fqinjët)
│   │   ├── Game.java              ← Logjika kryesore e lojës
│   │   ├── GameState.java         ← Enum: PLACING, MOVING, FLYING, REMOVING, GAME_OVER
│   │   └── Player.java            ← Lojtari (emri, ngjyra, gurët)
│   └── view/
│       ├── GameWindow.java        ← JFrame kryesor
│       ├── BoardPanel.java        ← Vizualizimi i tabelës (grafika)
│       └── StatusPanel.java       ← Paneli anësor (info, mesazhe)
├── run.sh                         ← Script kompilimi + ekzekutimi
└── README.md
```

## Si ta Ekzekutosh

### Opsioni 1 — Script (Linux/Mac)
```bash
chmod +x run.sh
./run.sh
```

### Opsioni 2 — Manualisht
```bash
mkdir -p out
javac -d out src/model/*.java src/view/*.java src/Main.java
java -cp out Main
```

### Opsioni 3 — IntelliJ IDEA
1. File → Open → zgjidh dosjen `NineMensMorris`
2. File → Project Structure → Sources → shto `src` si Sources Root
3. Krijo Run Configuration: Main class = `Main`
4. Run!

## Rregullat

| Faza | Përshkrim |
|------|-----------|
| **VENDOSJA** | Vendos 9 gurë nga radha. Mulli = hiq 1 gur armiku |
| **LËVIZJA** | Lëviz te fqinjët. Mulli = hiq 1 gur armiku |
| **FLUTURIMI** | Kur ke 3 gurë, mund të shkosh kudo |
| **FITORJA** | Armiku ka < 3 gurë, ose nuk mund të lëvizë |

## Kërkesat
- Java 17+ (për switch expressions)
