package hexborne.game.assets;

public final class AssetDefinition {
    private final String alias;
    private final String path;

    public AssetDefinition(String alias, String path) {
        this.alias = alias;
        this.path = path;
    }

    public String getAlias() {
        return alias;
    }

    public String getPath() {
        return path;
    }
}
