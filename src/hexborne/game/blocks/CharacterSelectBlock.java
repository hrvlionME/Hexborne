package hexborne.game.blocks;

import hexborne.engine.blocks.Block;
import hexborne.engine.render.GameWindow;
import hexborne.engine.ui.Button;
import hexborne.engine.ui.Ui;
import hexborne.game.Game;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

public final class CharacterSelectBlock extends Block {
    private Button confirm;

    public CharacterSelectBlock() {
        super("Choose Character");
    }

    @Override
    public void start() {
        models.game().setSelectedCharacter("Mage");
        confirm = new Button(0, 0, 1, 1, "Choose Mage", this::end);
    }

    @Override
    public void render(Graphics2D g) {
        layoutButton();
        drawBackground(g);
        g.setFont(Ui.font(Font.BOLD, GameWindow.HEIGHT / 14));
        g.setColor(new Color(232, 231, 202));
        Ui.drawCentered(g, "Choose Character", GameWindow.WIDTH / 2, GameWindow.HEIGHT / 6);

        int bodySize = Math.max(120, Math.min(GameWindow.WIDTH, GameWindow.HEIGHT) / 5);
        int cx = GameWindow.WIDTH / 2;
        int cy = GameWindow.HEIGHT / 2 - GameWindow.HEIGHT / 20;
        BufferedImage character = Game.get().getAssets().image("player.walk.south.0");
        if (character != null) {
            Ui.drawPixelImageCenteredTrimmed(g, character, cx, cy, bodySize);
        } else {
            g.setColor(new Color(55, 91, 118));
            g.fillOval(cx - bodySize / 2, cy - bodySize / 2, bodySize, bodySize);
            g.setColor(new Color(201, 220, 255));
            g.fillOval(cx - bodySize / 5, cy - bodySize / 4, bodySize * 2 / 5, bodySize * 2 / 5);
            g.setColor(new Color(180, 95, 255));
            g.fillRect(cx - bodySize / 8, cy + bodySize / 5, bodySize / 4, bodySize * 3 / 5);
        }

        g.setFont(Ui.font(Font.BOLD, GameWindow.HEIGHT / 25));
        g.setColor(new Color(118, 201, 176));
        Ui.drawCentered(g, "Mage", GameWindow.WIDTH / 2, GameWindow.HEIGHT * 13 / 20);
        confirm.render(g);
    }

    @Override
    public void mousePressed(MouseEvent event) {
        layoutButton();
        confirm.click(event.getX(), event.getY());
    }

    @Override
    public void cleanup() {
    }

    private void drawBackground(Graphics2D g) {
        BufferedImage background = Game.get().getAssets().image("ui.sub-menu");
        if (background != null) {
            g.drawImage(background, 0, 0, GameWindow.WIDTH, GameWindow.HEIGHT, null);
            return;
        }

        g.setColor(new Color(11, 14, 24));
        g.fillRect(0, 0, GameWindow.WIDTH, GameWindow.HEIGHT);
    }

    private void layoutButton() {
        int width = Math.max(260, GameWindow.WIDTH / 5);
        int height = Math.max(50, GameWindow.HEIGHT / 14);
        confirm.setBounds(GameWindow.WIDTH / 2 - width / 2, GameWindow.HEIGHT * 3 / 4, width, height);
    }
}
