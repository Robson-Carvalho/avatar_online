#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import uuid
import random

NUM_CARDS = 10000
OUTPUT_FILE = "generetade_cards"

elements = ["WATER", "FIRE", "EARTH", "AIR", "BLOOD", "METAL", "LIGHTNING", "AVATAR"]
phases = ["COMMON", "YOUNG", "ADULT", "MASTER"]
rarities = ["COMMON", "RARE", "EPIC", "LEGENDARY"]

def random_stats(phase):
    """Gera stats baseadas na fase para deixar mais variado"""
    base = {
        "COMMON": (40, 60),
        "YOUNG": (50, 70),
        "ADULT": (60, 90),
        "MASTER": (80, 120)
    }
    min_val, max_val = base[phase]
    attack = random.randint(min_val, max_val)
    life = random.randint(min_val, max_val)
    defense = random.randint(min_val//2, max_val//2)
    return attack, life, defense

with open(OUTPUT_FILE, "w", encoding="utf-8") as f:
    f.write("INSERT INTO avatar_schema.cards (id, name, element, phase, attack, life, defense, rarity, description)\nVALUES\n")

    values = []
    for i in range(1, NUM_CARDS + 1):
        card_id = str(uuid.uuid4())
        element = random.choice(elements)
        phase = random.choice(phases)
        rarity = random.choice(rarities)
        attack, life, defense = random_stats(phase)
        name = f"Carta {i} da Tribo {element}"
        description = f"Uma carta da fase {phase} com afinidade elemental {element} e raridade {rarity}."

        values.append(f"('{card_id}', '{name}', '{element}', '{phase}', {attack}, {life}, {defense}, '{rarity}', '{description}')")

    f.write(",\n".join(values))
    f.write(";\n")

print(f"{NUM_CARDS} cartas geradas e salvas em {OUTPUT_FILE}")
