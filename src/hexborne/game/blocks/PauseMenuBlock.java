package hexborne.game.blocks;

import hexborne.engine.blocks.Block;
import hexborne.engine.render.GameWindow;
import hexborne.engine.states.GameState;
import hexborne.engine.ui.Button;
import hexborne.engine.ui.Ui;
import hexborne.game.Game;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public final class PauseMenuBlock extends Block {
    private final Runnable resume;
    private final List<Button> buttons = new ArrayList<>();

    public PauseMenuBlock(Runnable resume) {
        super("Pause Menu");
        this.resume = resume;
    }

    @Override
    public void start() {
        buttons.clear();
        buttons.add(new Button(0, 0, 1, 1, "Resume", resume));
        buttons.add(new Button(0, 0, 1, 1, "New Run", () -> Game.get().changeState(GameState.GENERATE_RUN)));
        buttons.add(new Button(0, 0, 1, 1, "Quit To Menu", () -> Game.get().changeState(GameState.GAME_MENU)));
    }

    @Override
    public void render(Graphics2D g) {
        layoutButtons();
        g.setColor(new Color(0, 0, 0, 170));
        g.fillRect(0, 0, GameWindow.WIDTH, GameWindow.HEIGHT);

        int panelWidth = Math.max(420, GameWindow.WIDTH / 3);
        int panelHeight = Math.max(360, GameWindow.HEIGHT / 2);
        int panelX = GameWindow.WIDTH / 2 - panelWidth / 2;
        int panelY = GameWindow.HEIGHT / 2 - panelHeight / 2;

        g.setColor(new Color(18, 30, 43));
        g.fillRoundRect(panelX, panelY, panelWidth, panelHeight, 24, 24);
        g.setColor(new Color(118, 201, 176));
        g.drawRoundRect(panelX, panelY, panelWidth, panelHeight, 24, 24);

        g.setFont(Ui.font(Font.BOLD, GameWindow.HEIGHT / 14));
        g.setColor(new Color(232, 231, 202));
        Ui.drawCentered(g, "Paused", GameWindow.WIDTH / 2, panelY + GameWindow.HEIGHT / 9);

        for (Button button : buttons) {
            button.render(g);
        }

        g.setFont(Ui.font(Font.PLAIN, GameWindow.HEIGHT / 42));
        g.setColor(new Color(196, 202, 188));
        Ui.drawCentered(g, "New Run keeps your selected character and gear.", GameWindow.WIDTH / 2, panelY + panelHeight - GameWindow.HEIGHT / 20);
    }

    @Override
    public void mousePressed(MouseEvent event) {
        layoutButtons();
        for (Button button : buttons) {
            if (button.click(event.getX(), event.getY())) {
                return;
            }
        }
    }

    @Override
    public void cleanup() {
        buttons.clear();
    }

    private void layoutButtons() {
        int buttonWidth = Math.max(260, GameWindow.WIDTH / 5);
        int buttonHeight = Math.max(50, GameWindow.HEIGHT / 14);
        int gap = Math.max(14, GameWindow.HEIGHT / 45);
        int x = GameWindow.WIDTH / 2 - buttonWidth / 2;
        int startY = GameWindow.HEIGHT / 2 - buttonHeight;

        for (int i = 0; i < buttons.size(); i++) {
            buttons.get(i).setBounds(x, startY + i * (buttonHeight + gap), buttonWidth, buttonHeight);
        }
    }
}
