package hexborne.game.items;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class ItemConfig {
    private static final ItemAsset[] STAVES = {
            item("apprentice staff.png", 0), item("wanderer's staff.png", 1), item("dawnspire wand.png", 2), item("arcane staff.png", 3),
            item("ember branch.png", 4), item("bloodreaper staff.png", 5), item("grave keeper.png", 6), item("sunforged staff.png", 7),
            item("verdant staff.png", 8), item("nightveil staff.png", 9), item("frostwoven staff.png", 10), item("phoenix soul.png", 11),
            item("voidcaller.png", 12), item("thornbinder.png", 13), item("ashfire rod.png", 14), item("stormcaller.png", 15)
    };

    private static final ItemAsset[] SPELLS = {
            item("worn grimoire.png", 0), item("traveler's codex.png", 1), item("druid's pages.png", 2), item("sunfire sigil.png", 3),
            item("witchfire.png", 4), item("bloodhound tome.png", 5), item("obsidian secret.png", 6), item("book of ascension.png", 7),
            item("verdant hex.png", 8), item("abyssal grimoire.png", 9), item("frost sigil.png", 10), item("radiant blessing.png", 11),
            item("void fragment.png", 12), item("soul of nature.png", 13), item("ember core.png", 14), item("hexkeeper's codex.png", 15)
    };

    private static final ItemAsset[] ROBES = {
            item("scholar shirt.png", 0), item("wildkeeper tunic.png", 1), item("celestial vestments.png", 2), item("arcane mantle.png", 3),
            item("emberlord robe.png", 4), item("bloodwoven garb.png", 5), item("gravewoven tunic.png", 6), item("sunflare tunic.png", 7),
            item("verdant vestments.png", 8), item("nightveil garb.png", 9), item("frostweaver robe.png", 10), item("phoenix robe.png", 11),
            item("voidcaller robe.png", 12), item("thornbound garb.png", 13), item("ashen cloak.png", 14), item("robe of hexforged.png", 15)
    };

    private static final ItemAsset[] RINGS = {
            item("apprentice band.png", 0), item("soulkeeper ring.png", 1), item("celestial loop.png", 2), item("arcanebound ring.png", 3),
            item("ember ring.png", 4), item("blood sigil.png", 5), item("ring of gravekeeper.png", 6), item("ring of storms.png", 7),
            item("verdant ring.png", 8), item("nightveil ring.png", 9), item("frostbound ring.png", 10), item("phoenix sigil.png", 11),
            item("void ring.png", 12), item("ring of thorns.png", 13), item("ring of volcanic ashes.png", 14), item("hexforged ring.png", 15)
    };

    private ItemConfig() {
    }

    public static List<ItemDefinition> allItems() {
        List<ItemDefinition> items = new ArrayList<>();
        addCategory(items, ItemType.STAFF, "staves", STAVES);
        addCategory(items, ItemType.SPELL, "books", SPELLS);
        addCategory(items, ItemType.ROBE, "robes", ROBES);
        addCategory(items, ItemType.RING, "rings", RINGS);
        return List.copyOf(items);
    }

    public static List<ItemDefinition> itemsByType(ItemType type) {
        List<ItemDefinition> result = new ArrayList<>();
        for (ItemDefinition item : allItems()) {
            if (item.getType() == type) {
                result.add(item);
            }
        }
        return result;
    }

    public static List<ItemSetDefinition> sets() {
        List<ItemDefinition> staves = buildCategory(ItemType.STAFF, "staves", STAVES);
        List<ItemDefinition> spells = buildCategory(ItemType.SPELL, "books", SPELLS);
        List<ItemDefinition> robes = buildCategory(ItemType.ROBE, "robes", ROBES);
        List<ItemDefinition> rings = buildCategory(ItemType.RING, "rings", RINGS);
        List<ItemSetDefinition> sets = new ArrayList<>();

        for (int i = 0; i < SET_NAMES.length; i++) {
            sets.add(new ItemSetDefinition(
                    i,
                    SET_NAMES[i],
                    setBonusName(i),
                    setBonusDescription(i),
                    findBySetId(staves, i, ItemType.STAFF),
                    findBySetId(spells, i, ItemType.SPELL),
                    findBySetId(robes, i, ItemType.ROBE),
                    findBySetId(rings, i, ItemType.RING)
            ));
        }

        return List.copyOf(sets);
    }

    private static ItemDefinition findBySetId(List<ItemDefinition> items, int setId, ItemType itemType) {
        for (ItemDefinition item : items) {
            if (item.getSetId() == setId) {
                return item;
            }
        }

        throw new IllegalStateException("Missing " + itemType + " for set id " + setId);
    }

    private static void addCategory(List<ItemDefinition> items, ItemType type, String folder, ItemAsset[] assets) {
        items.addAll(buildCategory(type, folder, assets));
    }

    private static List<ItemDefinition> buildCategory(ItemType type, String folder, ItemAsset[] assets) {
        List<ItemDefinition> items = new ArrayList<>();
        for (int i = 0; i < assets.length; i++) {
            String file = assets[i].file();
            String itemName = titleCase(file.substring(0, file.length() - 4));
            String alias = "gear_" + folder + "_" + slug(file.substring(0, file.length() - 4));
            String path = "/assets/gear/" + folder + "/" + file;
            items.add(createItem(alias, assets[i].setId(), itemName, type, path, i));
        }
        return items;
    }

    private static final String[] SET_NAMES = {
            "Apprentice Set", "Wanderer Set", "Dawnspire Set", "Arcane Set",
            "Ember Set", "Bloodhound Set", "Gravekeeper Set", "Sunforged Set",
            "Verdant Set", "Nightveil Set", "Frostwoven Set", "Phoenix Set",
            "Voidcaller Set", "Thornbound Set", "Ashfire Set", "Hexforged Set"
    };

    private static String setBonusName(int index) {
        String[] names = {
                "First Lesson", "Traveler's Luck", "Dawn Focus", "Arcane Flow",
                "Ember Surge", "Blood Pact", "Grave Oath", "Sunflare",
                "Verdant Pulse", "Night Cloak", "Frost Ward", "Phoenix Rise",
                "Void Hunger", "Thorn Guard", "Ashen Spark", "Hexforged Rhythm"
        };
        return names[index];
    }

    private static String setBonusDescription(int index) {
        String[] descriptions = {
                "Complete set: +8% experience gained.",
                "Complete set: +10% movement speed and +10% luck.",
                "Complete set: first shot after moving deals bonus damage.",
                "Complete set: spells travel faster and deal +6% damage.",
                "Complete set: every fifth projectile explodes in a small area.",
                "Complete set: recover a small amount of health after each wave.",
                "Complete set: defeated enemies have a higher chest currency value.",
                "Complete set: boss damage taken is reduced.",
                "Complete set: health pickups are stronger.",
                "Complete set: gain dodge chance while below half health.",
                "Complete set: enemies near you move slower.",
                "Complete set: revive once per run with partial health.",
                "Complete set: projectiles pierce one additional enemy.",
                "Complete set: contact damage reflects back to minions.",
                "Complete set: projectiles burn enemies for bonus damage.",
                "Complete set: attack speed increases while standing still."
        };
        return descriptions[index];
    }

    private static ItemDefinition createItem(String alias, int setId, String name, ItemType type, String path, int index) {
        int tier = index / 4 + 1;
        int flavor = index % 4;

        int damage = type == ItemType.STAFF || type == ItemType.SPELL ? tier * 3 + flavor : flavor;
        int attackSpeed = type == ItemType.STAFF || type == ItemType.SPELL ? tier + flavor : 0;
        int defense = type == ItemType.ROBE || type == ItemType.RING ? tier * 2 + flavor : 0;
        int maxHealth = type == ItemType.ROBE ? tier * 12 + flavor * 3 : type == ItemType.RING ? tier * 5 : 0;
        int movementSpeed = type == ItemType.ROBE ? flavor : type == ItemType.STAFF ? tier : 0;
        int luck = type == ItemType.RING || type == ItemType.SPELL ? tier + flavor : 0;

        return new ItemDefinition(
                alias,
                setId,
                name,
                type,
                path,
                descriptionFor(type, name, tier),
                damage,
                attackSpeed,
                defense,
                maxHealth,
                movementSpeed,
                luck
        );
    }

    private static String descriptionFor(ItemType type, String name, int tier) {
        return switch (type) {
            case STAFF -> name + " channels tier " + tier + " magic into direct projectile damage.";
            case SPELL -> name + " alters spell behavior and improves magical pressure during waves.";
            case ROBE -> name + " protects the caster and changes survival stats.";
            case RING -> name + " adds passive run modifiers and utility bonuses.";
        };
    }

    private static String slug(String value) {
        return value.toLowerCase(Locale.ROOT)
                .replace("'", "")
                .replace(" ", "_");
    }

    private static String titleCase(String value) {
        String[] words = value.split(" ");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (!result.isEmpty()) {
                result.append(' ');
            }
            result.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return result.toString();
    }

    private static ItemAsset item(String file, int setId) {
        return new ItemAsset(file, setId);
    }

    private record ItemAsset(String file, int setId) {
    }
}
