package hexborne.game.assets;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public final class AssetManager {
    private final Map<String, BufferedImage> images = new HashMap<>();

    public void loadGameAssets() {
        images.clear();

        for (AssetDefinition asset : AssetConfig.gameAssets()) {
            try (InputStream stream = AssetManager.class.getResourceAsStream(asset.getPath())) {
                if (stream == null) {
                    System.out.println("Asset missing: " + asset.getAlias() + " -> " + asset.getPath());
                    continue;
                }

                images.put(asset.getAlias(), ImageIO.read(stream));
                System.out.println("Loaded asset: " + asset.getAlias());
            } catch (IOException exception) {
                System.out.println("Failed to load asset: " + asset.getAlias() + " -> " + exception.getMessage());
            }
        }
    }

    public BufferedImage image(String alias) {
        return images.get(alias);
    }

    public boolean hasImage(String alias) {
        return images.containsKey(alias);
    }
}
