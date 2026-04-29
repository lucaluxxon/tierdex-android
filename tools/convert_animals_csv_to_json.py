import csv
import json
from pathlib import Path


FIELDS = [
    "id",
    "group",
    "subgroup",
    "germanName",
    "latinName",
    "habitat",
    "distribution",
    "rarity",
]


def main() -> None:
    project_root = Path(__file__).resolve().parents[1]
    assets_dir = project_root / "app" / "src" / "main" / "assets"
    csv_path = assets_dir / "tierlistegesamt.csv"
    json_path = assets_dir / "animals.json"

    with csv_path.open("r", encoding="utf-8-sig", newline="") as csv_file:
        sample = csv_file.read(4096)
        csv_file.seek(0)
        dialect = csv.Sniffer().sniff(sample, delimiters=";,")
        reader = csv.DictReader(csv_file, dialect=dialect)

        animals = []
        for row in reader:
            animal = {field: (row.get(field, "") or "").strip() for field in FIELDS}
            if animal["id"]:
                animals.append(animal)

    with json_path.open("w", encoding="utf-8") as json_file:
        json.dump(animals, json_file, ensure_ascii=False, indent=2)
        json_file.write("\n")

    print(f"Converted {len(animals)} animals to {json_path}")


if __name__ == "__main__":
    main()
