#!/bin/bash
# Build and run Nine Men's Morris
cd "$(dirname "$0")"

echo "=== Kompilimi ==="
mkdir -p out
javac -d out src/model/*.java src/view/*.java src/Main.java

if [ $? -eq 0 ]; then
    echo "=== Kompilimi u krye me sukses! ==="
    echo "=== Duke nisur lojën... ==="
    java -cp out Main
else
    echo "=== GABIM gjatë kompilimit ==="
fi
