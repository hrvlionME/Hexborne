package hexborne.game.items;

import java.util.List;

public final class ItemSetDefinition {
    private final int index;
    private final String name;
    private final String bonusName;
    private final String bonusDescription;
    private final ItemDefinition staff;
    private final ItemDefinition spell;
    private final ItemDefinition robe;
    private final ItemDefinition ring;

    public ItemSetDefinition(
            int index,
            String name,
            String bonusName,
            String bonusDescription,
            ItemDefinition staff,
            ItemDefinition spell,
            ItemDefinition robe,
            ItemDefinition ring
    ) {
        this.index = index;
        this.name = name;
        this.bonusName = bonusName;
        this.bonusDescription = bonusDescription;
        this.staff = staff;
        this.spell = spell;
        this.robe = robe;
        this.ring = ring;
    }

    public int getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }

    public String getBonusName() {
        return bonusName;
    }

    public String getBonusDescription() {
        return bonusDescription;
    }

    public ItemDefinition getStaff() {
        return staff;
    }

    public ItemDefinition getSpell() {
        return spell;
    }

    public ItemDefinition getRobe() {
        return robe;
    }

    public ItemDefinition getRing() {
        return ring;
    }

    public List<ItemDefinition> getItems() {
        return List.of(staff, spell, robe, ring);
    }
}
