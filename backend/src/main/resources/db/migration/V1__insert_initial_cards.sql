CREATE TABLE IF NOT EXISTS cards (
                                     id UUID PRIMARY KEY,
                                     name TEXT NOT NULL,
                                     element TEXT NOT NULL,
                                     phase TEXT NOT NULL,
                                     attack INT NOT NULL,
                                     life INT NOT NULL,
                                     defense INT NOT NULL,
                                     rarity TEXT NOT NULL,
                                     description TEXT
);
INSERT INTO avatar_schema.cards (id, name, element, phase, attack, life, defense, rarity, description)
VALUES
    ('b1e5e3d1-8f6f-4f77-9c91-111111111111', 'Carta 1 da Tribo FIRE', 'FIRE', 'YOUNG', 55, 60, 40, 'COMMON', 'Uma carta da fase YOUNG com afinidade elemental FIRE e raridade COMMON.'),
    ('c2a6d4e2-7b8f-4a88-8b92-222222222222', 'Carta 2 da Tribo WATER', 'WATER', 'ADULT', 70, 80, 50, 'RARE', 'Uma carta da fase ADULT com afinidade elemental WATER e raridade RARE.'),
    ('d3b7e5f3-9c9f-4b99-9c93-333333333333', 'Carta 3 da Tribo EARTH', 'EARTH', 'COMMON', 60, 65, 45, 'COMMON', 'Uma carta da fase COMMON com afinidade elemental EARTH e raridade COMMON.'),
    ('e4c8f6f4-0d0f-4c00-9d94-444444444444', 'Carta 4 da Tribo AIR', 'AIR', 'MASTER', 90, 100, 70, 'LEGENDARY', 'Uma carta da fase MASTER com afinidade elemental AIR e raridade LEGENDARY.'),
    ('f5d9a7a5-1e1f-4d11-9e95-555555555555', 'Carta 5 da Tribo LIGHTNING', 'LIGHTNING', 'ADULT', 75, 85, 55, 'EPIC', 'Uma carta da fase ADULT com afinidade elemental LIGHTNING e raridade EPIC.'),
    ('a6e0b8f6-2f2f-4e22-9f96-666666666666', 'Carta 6 da Tribo BLOOD', 'BLOOD', 'YOUNG', 50, 55, 45, 'COMMON', 'Uma carta da fase YOUNG com afinidade elemental BLOOD e raridade COMMON.'),
    ('b7f1a9a7-3d3f-4f33-9d97-777777777777', 'Carta 7 da Tribo METAL', 'METAL', 'ADULT', 65, 75, 55, 'RARE', 'Uma carta da fase ADULT com afinidade elemental METAL e raridade RARE.'),
    ('c8d2c0a8-4b4f-4f44-9f98-888888888888', 'Carta 8 da Tribo AVATAR', 'AVATAR', 'MASTER', 95, 110, 80, 'LEGENDARY', 'Uma carta da fase MASTER com afinidade elemental AVATAR e raridade LEGENDARY.'),
    ('d9f3f1f9-5f5f-4a55-9f99-999999999999', 'Carta 9 da Tribo FIRE', 'FIRE', 'COMMON', 55, 60, 40, 'COMMON', 'Uma carta da fase COMMON com afinidade elemental FIRE e raridade COMMON.'),
    ('e0f4f2b0-6f6f-4f66-9f10-101010101010', 'Carta 10 da Tribo WATER', 'WATER', 'YOUNG', 60, 65, 45, 'COMMON', 'Uma carta da fase YOUNG com afinidade elemental WATER e raridade COMMON.');
