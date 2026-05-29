package hexborne.game.assets;

import hexborne.game.items.ItemConfig;
import hexborne.game.items.ItemDefinition;
import hexborne.game.items.ItemType;

import java.util.ArrayList;
import java.util.List;

public final class AssetConfig {
    private AssetConfig() {
    }

    public static List<AssetDefinition> gameAssets() {
        List<AssetDefinition> assets = new ArrayList<>();
        assets.add(new AssetDefinition("ui.main-menu", "/assets/ui/main-menu.png"));
        assets.add(new AssetDefinition("ui.sub-menu", "/assets/ui/sub-menu.png"));
        assets.add(new AssetDefinition("ui.health-bar", "/assets/ui/health-bar.png"));
        assets.add(new AssetDefinition("ui.mana-bar", "/assets/ui/mana-bar.png"));
        assets.add(new AssetDefinition("ui.item-container", "/assets/ui/item container.png"));
        assets.add(new AssetDefinition("ui.item-container-locked", "/assets/ui/item-container-locked.png"));
        assets.add(new AssetDefinition("map.arena", "/assets/map/arena.png"));
        assets.add(new AssetDefinition("map.collision", "/assets/map/collision.png"));
        assets.add(new AssetDefinition("chest.opening", "/assets/chest/chest.png"));
        assets.add(new AssetDefinition("enemy.blue-slime", "/assets/enemies/ASSET/single_sheet/blue_slime.png"));
        assets.add(new AssetDefinition("enemy.green-slime", "/assets/enemies/ASSET/single_sheet/green_slime.png"));
        assets.add(new AssetDefinition("enemy.ghost", "/assets/enemies/ASSET/single_sheet/ghost.png"));
        assets.add(new AssetDefinition("enemy.skeleton", "/assets/enemies/ASSET/single_sheet/skeleton.png"));
        assets.add(new AssetDefinition("enemy.skeleton-swordsman", "/assets/enemies/ASSET/single_sheet/skeleton_swordman.png"));
        assets.add(new AssetDefinition("enemy.wizard", "/assets/enemies/ASSET/single_sheet/wizard.png"));
        addCharacterAssets(assets);

        for (ItemDefinition item : ItemConfig.allItems()) {
            assets.add(new AssetDefinition(item.getAlias(), item.getAssetPath()));
            if (item.getType() == ItemType.STAFF) {
                assets.add(new AssetDefinition(projectileAlias(item), projectilePath(item)));
            }
        }

        return List.copyOf(assets);
    }

    private static String projectileAlias(ItemDefinition staff) {
        return staff.getAlias().replace("gear_staves_", "gear_projectiles_");
    }

    private static String projectilePath(ItemDefinition staff) {
        String staffPath = staff.getAssetPath();
        int fileNameStart = staffPath.lastIndexOf('/') + 1;
        return "/assets/gear/projectiles/" + staffPath.substring(fileNameStart);
    }

    private static void addCharacterAssets(List<AssetDefinition> assets) {
        String[] directions = {"south", "east", "north", "west"};
        for (String direction : directions) {
            assets.add(new AssetDefinition("player.idle." + direction, "/assets/characters/walk/rotations/" + direction + ".png"));
            assets.add(new AssetDefinition("player.attack.idle." + direction, "/assets/characters/attack/rotations/" + direction + ".png"));
        }

        for (String direction : directions) {
            for (int frame = 0; frame < 6; frame++) {
                String frameName = String.format("frame_%03d.png", frame);
                assets.add(new AssetDefinition(
                        "player.walk." + direction + "." + frame,
                        "/assets/characters/walk/animations/animation-35fae6c0/" + direction + "/" + frameName
                ));
                assets.add(new AssetDefinition(
                        "player.damage." + direction + "." + frame,
                        "/assets/characters/taking_damage/animations/Taking_Punch-2dbd7b16/" + direction + "/" + frameName
                ));
            }

            for (int frame = 0; frame < 9; frame++) {
                String frameName = String.format("frame_%03d.png", frame);
                assets.add(new AssetDefinition(
                        "player.attack." + direction + "." + frame,
                        "/assets/characters/attack/animations/make_it_attack_animation_just_like_swing_a_staff_i-2f971098/" + direction + "/" + frameName
                ));
            }
        }
    }
}
