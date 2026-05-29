package hexborne.game.blocks;

import hexborne.engine.blocks.Block;
import hexborne.engine.render.GameWindow;
import hexborne.engine.ui.Button;
import hexborne.engine.ui.Ui;
import hexborne.game.Game;
import hexborne.game.items.ItemConfig;
import hexborne.game.items.ItemDefinition;
import hexborne.game.items.ItemType;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public final class GearSelectBlock extends Block {
    private final List<ArrowHitbox> arrowHitboxes = new ArrayList<>();
    private List<ItemDefinition> staves;
    private List<ItemDefinition> spells;
    private List<ItemDefinition> robes;
    private List<ItemDefinition> rings;
    private Button confirm;
    private ItemDefinition selectedStaff;
    private ItemDefinition selectedSpell;
    private ItemDefinition selectedRobe;
    private ItemDefinition selectedRing;

    public GearSelectBlock() {
        super("Choose Gear");
    }

    @Override
    public void start() {
        staves = unlockedItemsByType(ItemType.STAFF);
        spells = unlockedItemsByType(ItemType.SPELL);
        robes = unlockedItemsByType(ItemType.ROBE);
        rings = unlockedItemsByType(ItemType.RING);
        selectedStaff = itemByName(staves, models.game().getStaff());
        selectedSpell = itemByName(spells, models.game().getSpell());
        selectedRobe = itemByName(robes, models.game().getRobe());
        selectedRing = itemByName(rings, models.game().getRing());
        equipSelectedGear();
        confirm = new Button(0, 0, 1, 1, "Confirm Gear", this::end);
    }

    @Override
    public void render(Graphics2D g) {
        layoutButton();
        arrowHitboxes.clear();
        drawBackground(g);
        g.setFont(Ui.font(Font.BOLD, GameWindow.HEIGHT / 14));
        g.setColor(new Color(232, 231, 202));
        Ui.drawCentered(g, "Gear Select", GameWindow.WIDTH / 2, GameWindow.HEIGHT / 6);

        int slotWidth = Math.max(140, GameWindow.WIDTH / 8);
        int slotHeight = Math.max(220, GameWindow.HEIGHT / 3);
        int gap = Math.max(18, GameWindow.WIDTH / 50);
        int totalWidth = slotWidth * 4 + gap * 3;
        int startX = GameWindow.WIDTH / 2 - totalWidth / 2;
        int y = GameWindow.HEIGHT / 3;

        drawSlot(g, startX, y, slotWidth, slotHeight, "Staff", selectedStaff, new Color(151, 105, 61), Slot.STAFF);
        drawSlot(g, startX + slotWidth + gap, y, slotWidth, slotHeight, "Spell", selectedSpell, new Color(78, 164, 232), Slot.SPELL);
        drawSlot(g, startX + (slotWidth + gap) * 2, y, slotWidth, slotHeight, "Robe", selectedRobe, new Color(111, 71, 161), Slot.ROBE);
        drawSlot(g, startX + (slotWidth + gap) * 3, y, slotWidth, slotHeight, "Ring", selectedRing, new Color(214, 164, 62), Slot.RING);
        confirm.render(g);
    }

    private void drawSlot(Graphics2D g, int x, int y, int width, int height, String title, ItemDefinition item, Color color, Slot slot) {
        g.setColor(new Color(25, 35, 48));
        g.fillRoundRect(x, y, width, height, 18, 18);
        int iconSize = Math.min(width / 2, height / 3);
        int iconX = x + width / 2 - iconSize / 2;
        int iconBaseline = y + height / 8 + iconSize;
        drawItemIcon(g, item, iconX, iconBaseline, iconSize, color);
        g.setColor(new Color(232, 231, 202));
        g.setFont(Ui.font(Font.BOLD, height / 11));
        Ui.drawCentered(g, title, x + width / 2, y + height * 11 / 20);
        g.setFont(Ui.font(Font.PLAIN, height / 15));
        Ui.drawCentered(g, item.getName(), x + width / 2, y + height * 13 / 20);

        int chevronWidth = Math.max(34, width / 4);
        int chevronHeight = Math.max(28, height / 9);
        int chevronY = y + height * 4 / 5;
        drawChevron(g, x + width / 8, chevronY, chevronWidth, chevronHeight, "<", slot, -1);
        drawChevron(g, x + width - width / 8 - chevronWidth, chevronY, chevronWidth, chevronHeight, ">", slot, 1);
    }

    private void drawChevron(Graphics2D g, int x, int y, int width, int height, String label, Slot slot, int direction) {
        Rectangle bounds = new Rectangle(x, y, width, height);
        arrowHitboxes.add(new ArrowHitbox(bounds, slot, direction));
        g.setColor(new Color(14, 20, 30, 225));
        g.fillRoundRect(x, y, width, height, 10, 10);
        g.setColor(new Color(82, 120, 125));
        g.drawRoundRect(x, y, width, height, 10, 10);
        g.setColor(new Color(232, 231, 202));
        g.setFont(Ui.font(Font.BOLD, Math.max(18, height - 4)));
        Ui.drawCentered(g, label, x + width / 2, y + height - 7);
    }

    private void drawItemIcon(Graphics2D g, ItemDefinition item, int x, int baseline, int size, Color fallbackColor) {
        BufferedImage image = Game.get().getAssets().image(item.getAlias());
        if (image != null) {
            Ui.drawPixelImageCenteredTrimmed(g, image, x + size / 2, baseline - size / 2, size);
            return;
        }

        g.setColor(fallbackColor);
        g.fillOval(x, baseline - size, size, size);
    }

    private void drawBackground(Graphics2D g) {
        BufferedImage background = Game.get().getAssets().image("ui.sub-menu");
        if (background != null) {
            g.drawImage(background, 0, 0, GameWindow.WIDTH, GameWindow.HEIGHT, null);
            return;
        }

        g.setColor(new Color(10, 13, 22));
        g.fillRect(0, 0, GameWindow.WIDTH, GameWindow.HEIGHT);
    }

    @Override
    public void mousePressed(MouseEvent event) {
        layoutButton();
        for (ArrowHitbox hitbox : arrowHitboxes) {
            if (hitbox.bounds.contains(event.getX(), event.getY())) {
                cycle(hitbox.slot, hitbox.direction);
                return;
            }
        }
        confirm.click(event.getX(), event.getY());
    }

    @Override
    public void cleanup() {
    }

    private void layoutButton() {
        int width = Math.max(260, GameWindow.WIDTH / 5);
        int height = Math.max(50, GameWindow.HEIGHT / 14);
        confirm.setBounds(GameWindow.WIDTH / 2 - width / 2, GameWindow.HEIGHT * 5 / 6, width, height);
    }

    private void cycle(Slot slot, int direction) {
        switch (slot) {
            case STAFF -> selectedStaff = nextItem(staves, selectedStaff, direction);
            case SPELL -> selectedSpell = nextItem(spells, selectedSpell, direction);
            case ROBE -> selectedRobe = nextItem(robes, selectedRobe, direction);
            case RING -> selectedRing = nextItem(rings, selectedRing, direction);
        }
        equipSelectedGear();
    }

    private ItemDefinition nextItem(List<ItemDefinition> items, ItemDefinition selected, int direction) {
        int index = items.indexOf(selected);
        int nextIndex = Math.floorMod(index + direction, items.size());
        return items.get(nextIndex);
    }

    private ItemDefinition itemByName(List<ItemDefinition> items, String name) {
        for (ItemDefinition item : items) {
            if (item.getName().equals(name)) {
                return item;
            }
        }
        return items.get(0);
    }

    private List<ItemDefinition> unlockedItemsByType(ItemType type) {
        List<ItemDefinition> unlockedItems = new ArrayList<>();
        for (ItemDefinition item : ItemConfig.itemsByType(type)) {
            if (models.game().isGearUnlocked(item.getName())) {
                unlockedItems.add(item);
            }
        }
        return unlockedItems;
    }

    private void equipSelectedGear() {
        models.game().setGear(
                selectedStaff.getName(),
                selectedRobe.getName(),
                selectedRing.getName(),
                selectedSpell.getName()
        );
    }

    private enum Slot {
        STAFF,
        SPELL,
        ROBE,
        RING
    }

    private record ArrowHitbox(Rectangle bounds, Slot slot, int direction) {
    }
}
