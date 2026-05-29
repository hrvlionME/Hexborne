package hexborne.game.items;

public final class ItemDefinition {
    private final String alias;
    private final int setId;
    private final String name;
    private final ItemType type;
    private final String assetPath;
    private final String description;
    private final int damage;
    private final int attackSpeed;
    private final int defense;
    private final int maxHealth;
    private final int movementSpeed;
    private final int luck;

    public ItemDefinition(
            String alias,
            int setId,
            String name,
            ItemType type,
            String assetPath,
            String description,
            int damage,
            int attackSpeed,
            int defense,
            int maxHealth,
            int movementSpeed,
            int luck
    ) {
        this.alias = alias;
        this.setId = setId;
        this.name = name;
        this.type = type;
        this.assetPath = assetPath;
        this.description = description;
        this.damage = damage;
        this.attackSpeed = attackSpeed;
        this.defense = defense;
        this.maxHealth = maxHealth;
        this.movementSpeed = movementSpeed;
        this.luck = luck;
    }

    public String getAlias() {
        return alias;
    }

    public int getSetId() {
        return setId;
    }

    public String getName() {
        return name;
    }

    public ItemType getType() {
        return type;
    }

    public String getAssetPath() {
        return assetPath;
    }

    public String getDescription() {
        return description;
    }

    public int getDamage() {
        return damage;
    }

    public int getAttackSpeed() {
        return attackSpeed;
    }

    public int getDefense() {
        return defense;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public int getMovementSpeed() {
        return movementSpeed;
    }

    public int getLuck() {
        return luck;
    }
}
