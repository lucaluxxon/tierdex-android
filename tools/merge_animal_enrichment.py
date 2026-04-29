import json
from pathlib import Path


def main() -> None:
    project_root = Path(__file__).resolve().parents[1]
    animals_path = project_root / "app" / "src" / "main" / "assets" / "animals.json"
    enrichment_dir = project_root / "data" / "enrichment"

    animals = json.loads(animals_path.read_text(encoding="utf-8"))
    animal_by_id = {
        animal.get("id"): animal
        for animal in animals
        if isinstance(animal, dict) and animal.get("id")
    }

    updated_ids = set()
    missing_ids = []

    for enrichment_path in sorted(enrichment_dir.glob("*.json")):
        enrichment_items = json.loads(enrichment_path.read_text(encoding="utf-8"))
        if not isinstance(enrichment_items, list):
            raise ValueError(f"{enrichment_path} must contain a JSON array")

        for item in enrichment_items:
            if not isinstance(item, dict):
                continue

            animal_id = item.get("id")
            if not animal_id:
                continue

            target = animal_by_id.get(animal_id)
            if target is None:
                missing_ids.append((enrichment_path.name, animal_id))
                continue

            changed = False
            for key, value in item.items():
                if key == "id":
                    continue
                target[key] = value
                changed = True

            if changed:
                updated_ids.add(animal_id)

    animals_path.write_text(
        json.dumps(animals, ensure_ascii=False, indent=2) + "\n",
        encoding="utf-8",
    )

    print(f"Updated {len(updated_ids)} animals")
    for filename, animal_id in missing_ids:
        print(f"Warning: id '{animal_id}' from {filename} not found in animals.json")


if __name__ == "__main__":
    main()
