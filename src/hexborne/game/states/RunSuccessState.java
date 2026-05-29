package hexborne.game.states;

import hexborne.engine.render.GameWindow;
import hexborne.engine.states.GameState;
import hexborne.engine.states.State;
import hexborne.engine.ui.Ui;
import hexborne.game.Game;
import hexborne.game.items.ItemConfig;
import hexborne.game.items.ItemDefinition;
import hexborne.game.models.Models;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

public final class RunSuccessState extends State {
    private static final int CHEST_FRAME_SIZE = 256;
    private static final double CHEST_FRAMES_PER_SECOND = 8.0;

    private double timer;

    public RunSuccessState(Models models) {
        super(models);
    }

    @Override
    protected GameState getStateId() {
        return GameState.RUN_SUCCESS;
    }

    @Override
    protected void setupEvents() {
    }

    @Override
    protected void modelChanges() {
        timer = 0;
    }

    @Override
    protected void setupBlocks() {
    }

    @Override
    protected void onBlocksComplete() {
        // Timed animation state.
    }

    @Override
    public void update(double deltaSeconds) {
        timer += deltaSeconds;
        if (timer > 4.0) {
            Game.get().changeState(GameState.GAME_OVER);
        }
    }

    @Override
    public void render(Graphics2D g) {
        drawBackground(g);
        g.setFont(Ui.font(Font.BOLD, GameWindow.HEIGHT / 15));
        g.setColor(new Color(232, 231, 202));
        Ui.drawCentered(g, "Chest Opened", GameWindow.WIDTH / 2, GameWindow.HEIGHT / 5);

        int chestSize = Math.max(160, GameWindow.HEIGHT / 4);
        drawChestAnimation(g, GameWindow.WIDTH / 2, GameWindow.HEIGHT / 2, chestSize);

        if (timer > 1.2) {
            ItemDefinition item = lastUnlockedItem();
            int iconSize = Math.max(60, GameWindow.HEIGHT / 11);
            if (item != null) {
                BufferedImage itemImage = Game.get().getAssets().image(item.getAlias());
                if (itemImage != null) {
                    Ui.drawPixelImageCenteredTrimmed(g, itemImage, GameWindow.WIDTH / 2, GameWindow.HEIGHT / 3, iconSize);
                }
            }
            g.setFont(Ui.font(Font.BOLD, GameWindow.HEIGHT / 30));
            g.setColor(new Color(232, 231, 202));
            String rewardText = item == null ? "No locked gear remaining" : "New Item: " + item.getName();
            Ui.drawCentered(g, rewardText, GameWindow.WIDTH / 2, GameWindow.HEIGHT * 7 / 10);
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

    private void drawChestAnimation(Graphics2D g, int centerX, int centerY, int size) {
        BufferedImage image = Game.get().getAssets().image("chest.opening");
        if (image == null) {
            g.setColor(new Color(184, 124, 42));
            g.fillRoundRect(centerX - size / 2, centerY - size / 3, size, size * 2 / 3, 12, 12);
            return;
        }

        int frameCount = Math.max(1, image.getWidth() / CHEST_FRAME_SIZE);
        int frame = Math.min(frameCount - 1, (int) (timer * CHEST_FRAMES_PER_SECOND));
        int sourceX = frame * CHEST_FRAME_SIZE;
        Graphics2D pixelGraphics = (Graphics2D) g.create();
        pixelGraphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        pixelGraphics.drawImage(
                image,
                centerX - size / 2,
                centerY - size / 2,
                centerX + size / 2,
                centerY + size / 2,
                sourceX,
                0,
                sourceX + CHEST_FRAME_SIZE,
                CHEST_FRAME_SIZE,
                null
        );
        pixelGraphics.dispose();
    }

    private ItemDefinition lastUnlockedItem() {
        String itemName = models.game().getLastUnlockedGear();
        if (itemName == null) {
            return null;
        }

        for (ItemDefinition item : ItemConfig.allItems()) {
            if (item.getName().equals(itemName)) {
                return item;
            }
        }

        return null;
    }

    @Override
    protected void removeEvents() {
    }

    @Override
    protected void exitState() {
    }
}
