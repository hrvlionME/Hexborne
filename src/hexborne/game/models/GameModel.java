package hexborne.game.models;

import hexborne.engine.states.GameState;
import hexborne.game.items.ItemConfig;
import hexborne.game.items.ItemDefinition;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public final class GameModel {
    private static final boolean TEMP_UNLOCK_ALL_GEAR = false;
    private static final Set<String> DEFAULT_UNLOCKED_GEAR = Set.of(
            "Apprentice Staff",
            "Worn Grimoire",
            "Scholar Shirt",
            "Apprentice Band"
    );

    private GameState lastLoadedState = GameState.LOADING;
    private String selectedCharacter = "Mage";
    private String staff = "Apprentice Staff";
    private String robe = "Scholar Shirt";
    private String ring = "Apprentice Band";
    private String spell = "Worn Grimoire";
    private final Set<String> unlockedGear = new HashSet<>(DEFAULT_UNLOCKED_GEAR);
    private int currency;
    private int experience;
    private int lastRunCurrencyGain;
    private int lastRunExperienceGain;
    private String lastUnlockedGear;

    public GameState getLastLoadedState() {
        return lastLoadedState;
    }

    public void setLastLoadedState(GameState lastLoadedState) {
        this.lastLoadedState = lastLoadedState;
    }

    public String getSelectedCharacter() {
        return selectedCharacter;
    }

    public void setSelectedCharacter(String selectedCharacter) {
        this.selectedCharacter = selectedCharacter;
    }

    public String getStaff() {
        return staff;
    }

    public String getRobe() {
        return robe;
    }

    public String getRing() {
        return ring;
    }

    public String getSpell() {
        return spell;
    }

    public void setGear(String staff, String robe, String ring, String spell) {
        this.staff = staff;
        this.robe = robe;
        this.ring = ring;
        this.spell = spell;
    }

    public boolean isGearUnlocked(String itemName) {
        return TEMP_UNLOCK_ALL_GEAR || unlockedGear.contains(itemName);
    }

    public void unlockGear(String itemName) {
        unlockedGear.add(itemName);
    }

    public Set<String> getUnlockedGear() {
        return Set.copyOf(unlockedGear);
    }

    public void setUnlockedGear(Set<String> itemNames) {
        unlockedGear.clear();
        unlockedGear.addAll(DEFAULT_UNLOCKED_GEAR);
        unlockedGear.addAll(itemNames);
    }

    public ItemDefinition unlockRandomGear(Random random) {
        List<ItemDefinition> lockedItems = new ArrayList<>();
        for (ItemDefinition item : ItemConfig.allItems()) {
            if (!unlockedGear.contains(item.getName())) {
                lockedItems.add(item);
            }
        }

        if (lockedItems.isEmpty()) {
            lastUnlockedGear = null;
            return null;
        }

        ItemDefinition item = lockedItems.get(random.nextInt(lockedItems.size()));
        unlockGear(item.getName());
        lastUnlockedGear = item.getName();
        return item;
    }

    public String getLastUnlockedGear() {
        return lastUnlockedGear;
    }

    public int getCurrency() {
        return currency;
    }

    public int getExperience() {
        return experience;
    }

    public void setCurrency(int currency) {
        this.currency = Math.max(0, currency);
    }

    public void setExperience(int experience) {
        this.experience = Math.max(0, experience);
    }

    public int getLastRunCurrencyGain() {
        return lastRunCurrencyGain;
    }

    public int getLastRunExperienceGain() {
        return lastRunExperienceGain;
    }

    public void finishRun(int currencyGain, int experienceGain) {
        this.lastRunCurrencyGain = currencyGain;
        this.lastRunExperienceGain = experienceGain;
        this.currency += currencyGain;
        this.experience += experienceGain;
    }
}
