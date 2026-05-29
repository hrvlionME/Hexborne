package hexborne.game.persistence;

import hexborne.game.models.GameModel;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

public final class GameSave {
    private static final Path SAVE_PATH = Path.of(System.getProperty("user.home"), ".hexborne", "save.properties");
    private static final String GEAR_SEPARATOR = "\\|";
    private static final String GEAR_JOINER = "|";

    private GameSave() {
    }

    public static void load(GameModel model) {
        if (!Files.exists(SAVE_PATH)) {
            System.out.println("No save file found: " + SAVE_PATH);
            return;
        }

        Properties properties = new Properties();
        try (InputStream stream = Files.newInputStream(SAVE_PATH)) {
            properties.load(stream);
        } catch (IOException exception) {
            System.out.println("Failed to load save: " + exception.getMessage());
            return;
        }

        model.setSelectedCharacter(properties.getProperty("selectedCharacter", model.getSelectedCharacter()));
        model.setGear(
                properties.getProperty("staff", model.getStaff()),
                properties.getProperty("robe", model.getRobe()),
                properties.getProperty("ring", model.getRing()),
                properties.getProperty("spell", model.getSpell())
        );
        model.setCurrency(readInt(properties, "currency", model.getCurrency()));
        model.setExperience(readInt(properties, "experience", model.getExperience()));
        model.setUnlockedGear(readGear(properties.getProperty("unlockedGear", "")));
        System.out.println("Loaded save: " + SAVE_PATH);
    }

    public static void save(GameModel model) {
        Properties properties = new Properties();
        properties.setProperty("selectedCharacter", model.getSelectedCharacter());
        properties.setProperty("staff", model.getStaff());
        properties.setProperty("robe", model.getRobe());
        properties.setProperty("ring", model.getRing());
        properties.setProperty("spell", model.getSpell());
        properties.setProperty("currency", Integer.toString(model.getCurrency()));
        properties.setProperty("experience", Integer.toString(model.getExperience()));
        properties.setProperty("unlockedGear", String.join(GEAR_JOINER, model.getUnlockedGear()));

        try {
            Files.createDirectories(SAVE_PATH.getParent());
            try (OutputStream stream = Files.newOutputStream(SAVE_PATH)) {
                properties.store(stream, "Hexborne save data");
            }
            System.out.println("Saved game: " + SAVE_PATH);
        } catch (IOException exception) {
            System.out.println("Failed to save game: " + exception.getMessage());
        }
    }

    private static int readInt(Properties properties, String key, int fallback) {
        try {
            return Integer.parseInt(properties.getProperty(key, Integer.toString(fallback)));
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }

    private static Set<String> readGear(String value) {
        Set<String> gear = new HashSet<>();
        if (value.trim().isEmpty()) {
            return gear;
        }

        Arrays.stream(value.split(GEAR_SEPARATOR))
                .map(String::trim)
                .filter(item -> !item.isEmpty())
                .forEach(gear::add);
        return gear;
    }
}
