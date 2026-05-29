package hexborne.game.items;

public enum ItemType {
    STAFF("Staves"),
    SPELL("Spells"),
    ROBE("Robes"),
    RING("Rings");

    private final String label;

    ItemType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
