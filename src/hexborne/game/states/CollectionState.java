package hexborne.game.states;

import hexborne.engine.render.GameWindow;
import hexborne.engine.states.GameState;
import hexborne.engine.states.State;
import hexborne.engine.ui.Button;
import hexborne.engine.ui.Ui;
import hexborne.game.Game;
import hexborne.game.items.ItemConfig;
import hexborne.game.items.ItemDefinition;
import hexborne.game.items.ItemSetDefinition;
import hexborne.game.models.Models;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public final class CollectionState extends State {
    private final List<ItemSetDefinition> sets = ItemConfig.sets();
    private final List<ItemHitbox> itemHitboxes = new ArrayList<>();
    private Button backButton;
    private ItemSetDefinition selectedSet;
    private ItemDefinition selectedItem;

    public CollectionState(Models models) {
        super(models);
    }

    @Override
    protected GameState getStateId() {
        return GameState.COLLECTION;
    }

    @Override
    protected void setupEvents() {
    }

    @Override
    protected void modelChanges() {
        models.game().setLastLoadedState(GameState.COLLECTION);
        if (selectedSet == null) {
            selectedSet = sets.get(0);
            selectedItem = selectedSet.getStaff();
        }
    }

    @Override
    protected void setupBlocks() {
        backButton = new Button(0, 0, 1, 1, "Back", () -> Game.get().changeState(GameState.GAME_MENU));
    }

    @Override
    protected void onBlocksComplete() {
        // Collection waits for clicks.
    }

    @Override
    public void render(Graphics2D g) {
        layoutBackButton();
        itemHitboxes.clear();

        drawBackground(g);

        g.setFont(Ui.font(Font.BOLD, GameWindow.HEIGHT / 15));
        g.setColor(new Color(232, 231, 202));
        Ui.drawCentered(g, "Collection", GameWindow.WIDTH / 2, GameWindow.HEIGHT / 10);

        g.setFont(Ui.font(Font.PLAIN, GameWindow.HEIGHT / 42));
        g.setColor(new Color(157, 188, 177));
        Ui.drawCentered(g, "Gear archive by type. Select an item to inspect its stats and matching bonus.", GameWindow.WIDTH / 2, GameWindow.HEIGHT / 7);

        int margin = Math.max(24, GameWindow.WIDTH / 35);
        int detailHeight = Math.max(180, GameWindow.HEIGHT / 4);
        int gridTop = GameWindow.HEIGHT / 5;
        int gridBottom = GameWindow.HEIGHT - detailHeight - GameWindow.HEIGHT / 24;
        drawItemGrid(g, margin, gridTop, GameWindow.WIDTH - margin * 2, gridBottom - gridTop);
        drawDetails(g, margin, GameWindow.HEIGHT - detailHeight, GameWindow.WIDTH - margin * 2, detailHeight - GameWindow.HEIGHT / 35);

        backButton.render(g);
    }

    private void drawItemGrid(Graphics2D g, int x, int y, int width, int height) {
        String[] labels = {"Staves", "Spells", "Robes", "Rings"};
        int rows = labels.length;
        int columns = sets.size();
        int rowSections = (int) Math.ceil(sets.size() / (double) columns);
        int labelWidth = Math.max(64, GameWindow.WIDTH / 20);
        int gap = Math.max(4, GameWindow.WIDTH / 260);
        int rowGap = Math.max(10, GameWindow.HEIGHT / 85);
        int sectionGap = Math.max(4, GameWindow.HEIGHT / 160);
        int cellWidth = Math.max(56, (width - labelWidth - gap * (columns - 1)) / columns);
        int cellHeight = Math.max(56, (height - rowGap * (rows - 1) - sectionGap * rows * (rowSections - 1)) / (rows * rowSections));

        for (int row = 0; row < rows; row++) {
            int rowY = y + row * (cellHeight * rowSections + sectionGap * (rowSections - 1) + rowGap);
            g.setFont(Ui.font(Font.BOLD, Math.max(13, cellHeight / 4)));
            g.setColor(new Color(157, 188, 177));
            g.drawString(labels[row], x, rowY + (cellHeight * rowSections + sectionGap * (rowSections - 1)) / 2 + g.getFontMetrics().getAscent() / 2 - 2);

            for (int index = 0; index < sets.size(); index++) {
                int section = index / columns;
                int column = index % columns;
                ItemSetDefinition set = sets.get(index);
                ItemDefinition item = itemForRow(set, row);
                int cellX = x + labelWidth + column * (cellWidth + gap);
                int rowSectionY = rowY + section * (cellHeight + sectionGap);
                drawItemBox(g, set, item, cellX, rowSectionY, cellWidth, cellHeight);
            }
        }
    }

    private void drawBackground(Graphics2D g) {
        BufferedImage background = Game.get().getAssets().image("ui.sub-menu");
        if (background != null) {
            g.drawImage(background, 0, 0, GameWindow.WIDTH, GameWindow.HEIGHT, null);
            return;
        }

        g.setColor(new Color(8, 12, 20));
        g.fillRect(0, 0, GameWindow.WIDTH, GameWindow.HEIGHT);
    }

    private ItemDefinition itemForRow(ItemSetDefinition set, int row) {
        return switch (row) {
            case 0 -> set.getStaff();
            case 1 -> set.getSpell();
            case 2 -> set.getRobe();
            default -> set.getRing();
        };
    }

    private void drawItemBox(Graphics2D g, ItemSetDefinition set, ItemDefinition item, int slotX, int slotY, int slotWidth, int slotHeight) {
        int width = Math.min((int) (slotHeight * 1.35), (int) (slotWidth * 0.86));
        int height = Math.min((int) (width / 1.35), (int) (slotHeight * 0.86));
        width = (int) (height * 1.35);
        int x = slotX + (slotWidth - width) / 2;
        int y = slotY + (slotHeight - height) / 2;
        int iconSize = Math.max(24, Math.min((int) (width * 0.52), (int) (height * 0.56)));
        boolean selected = item == selectedItem;
        boolean unlocked = models.game().isGearUnlocked(item.getName());
        BufferedImage container = Game.get().getAssets().image(unlocked ? "ui.item-container" : "ui.item-container-locked");
        if (container != null) {
            Graphics2D pixelGraphics = (Graphics2D) g.create();
            pixelGraphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            pixelGraphics.drawImage(container, x, y, width, height, null);
            pixelGraphics.dispose();
        } else {
            g.setColor(selected ? new Color(26, 55, 58) : new Color(18, 30, 43));
            g.fillRoundRect(x, y, width, height, 10, 10);
        }

        if (selected) {
            g.setColor(new Color(118, 201, 176, 95));
            g.fillRoundRect(x + 4, y + 4, width - 8, height - 8, 8, 8);
        }
        if (!unlocked) {
            return;
        }

        itemHitboxes.add(new ItemHitbox(new Rectangle(x, y, width, height), set, item));

        int iconX = x + width / 2 - iconSize / 2;
        int iconBaseline = y + height / 2 + iconSize / 2;
        drawItemIcon(g, item, iconX, iconBaseline, iconSize);
    }

    private void drawItemIcon(Graphics2D g, ItemDefinition item, int x, int baseline, int size) {
        BufferedImage image = Game.get().getAssets().image(item.getAlias());
        if (image != null) {
            Ui.drawPixelImageCenteredTrimmed(g, image, x + size / 2, baseline - size / 2, size);
            return;
        }

        g.setColor(new Color(80, 95, 110));
        g.fillRect(x, baseline - size, size, size);
        g.setColor(new Color(232, 231, 202));
        g.drawString("?", x + size / 2 - 4, baseline - size / 2 + 5);
    }

    private void drawDetails(Graphics2D g, int x, int y, int width, int height) {
        g.setColor(new Color(16, 25, 36));
        g.fillRoundRect(x, y, width, height, 18, 18);
        g.setColor(new Color(82, 120, 125));
        g.drawRoundRect(x, y, width, height, 18, 18);

        if (selectedSet == null || selectedItem == null) {
            return;
        }

        int iconSize = Math.max(72, GameWindow.HEIGHT / 8);
        int iconX = x + GameWindow.WIDTH / 45;
        int iconBaseline = y + height / 2 + iconSize / 2;
        drawItemIcon(g, selectedItem, iconX, iconBaseline, iconSize);

        int textX = iconX + iconSize + GameWindow.WIDTH / 35;
        g.setFont(Ui.font(Font.BOLD, GameWindow.HEIGHT / 31));
        g.setColor(new Color(232, 231, 202));
        g.drawString(selectedItem.getName() + "  [" + selectedItem.getType().getLabel() + "]", textX, y + GameWindow.HEIGHT / 22);

        g.setFont(Ui.font(Font.PLAIN, GameWindow.HEIGHT / 48));
        g.setColor(new Color(196, 202, 188));
        g.drawString(selectedItem.getDescription(), textX, y + GameWindow.HEIGHT / 12);

        g.setFont(Ui.font(Font.BOLD, GameWindow.HEIGHT / 50));
        g.setColor(new Color(118, 201, 176));
        g.drawString(statsText(), textX, y + GameWindow.HEIGHT / 8);

        g.setFont(Ui.font(Font.BOLD, GameWindow.HEIGHT / 42));
        g.setColor(new Color(232, 231, 202));
        g.drawString(setDisplayName(selectedSet) + " Bonus: " + selectedSet.getBonusName(), textX, y + GameWindow.HEIGHT / 6);

        g.setFont(Ui.font(Font.PLAIN, GameWindow.HEIGHT / 48));
        g.setColor(new Color(196, 202, 188));
        g.drawString(selectedSet.getBonusDescription(), textX, y + GameWindow.HEIGHT / 5);
    }

    private String statsText() {
        return "DMG +" + selectedItem.getDamage()
                + "   AS +" + selectedItem.getAttackSpeed()
                + "   DEF +" + selectedItem.getDefense()
                + "   HP +" + selectedItem.getMaxHealth()
                + "   SPD +" + selectedItem.getMovementSpeed()
                + "   LUCK +" + selectedItem.getLuck();
    }

    @Override
    public void mousePressed(MouseEvent event) {
        layoutBackButton();
        if (backButton.click(event.getX(), event.getY())) {
            return;
        }

        for (ItemHitbox hitbox : itemHitboxes) {
            if (hitbox.bounds.contains(event.getX(), event.getY())) {
                selectedSet = hitbox.set;
                selectedItem = hitbox.item;
                return;
            }
        }
    }

    @Override
    protected void removeEvents() {
    }

    @Override
    protected void exitState() {
    }

    private void layoutBackButton() {
        int width = Math.max(160, GameWindow.WIDTH / 9);
        int height = Math.max(44, GameWindow.HEIGHT / 16);
        backButton.setBounds(GameWindow.WIDTH - width - GameWindow.WIDTH / 40, GameWindow.HEIGHT / 28, width, height);
    }

    private String setDisplayName(ItemSetDefinition set) {
        String name = set.getName();
        return name.endsWith(" Set") ? name.substring(0, name.length() - 4) : name;
    }

    private static final class ItemHitbox {
        private final Rectangle bounds;
        private final ItemSetDefinition set;
        private final ItemDefinition item;

        private ItemHitbox(Rectangle bounds, ItemSetDefinition set, ItemDefinition item) {
            this.bounds = bounds;
            this.set = set;
            this.item = item;
        }
    }

}
