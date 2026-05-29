package hexborne.game.states;

import hexborne.engine.render.GameWindow;
import hexborne.engine.states.GameState;
import hexborne.engine.states.State;
import hexborne.engine.ui.Button;
import hexborne.game.Game;
import hexborne.game.models.Models;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public final class GameMenuState extends State {
    private final List<Button> buttons = new ArrayList<>();

    public GameMenuState(Models models) {
        super(models);
    }

    @Override
    protected GameState getStateId() {
        return GameState.GAME_MENU;
    }

    @Override
    protected void setupEvents() {
    }

    @Override
    protected void modelChanges() {
        models.game().setLastLoadedState(GameState.GAME_MENU);
    }

    @Override
    protected void setupBlocks() {
        buttons.clear();
        buttons.add(new Button(0, 0, 1, 1, "Start Run", () -> Game.get().changeState(GameState.GEAR_SELECT)));
        buttons.add(new Button(0, 0, 1, 1, "Settings", () -> Game.get().changeState(GameState.SETTINGS)));
        buttons.add(new Button(0, 0, 1, 1, "Collection", () -> Game.get().changeState(GameState.COLLECTION)));
        buttons.add(new Button(0, 0, 1, 1, "Hexes", () -> {
        }));
        buttons.add(new Button(0, 0, 1, 1, "Exit", () -> Game.get().exitGame()));
    }

    @Override
    protected void onBlocksComplete() {
        // Menu is persistent and waits for button input.
    }

    @Override
    public void render(Graphics2D g) {
        layoutButtons();
        renderBackground(g);

        for (Button button : buttons) {
            button.render(g);
        }

        g.drawString("Gold: " + models.game().getCurrency() + "   EXP: " + models.game().getExperience(), GameWindow.WIDTH / 40, GameWindow.HEIGHT / 18);
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
    protected void removeEvents() {
    }

    @Override
    protected void exitState() {
    }

    private void layoutButtons() {
        int buttonWidth = Math.max(260, GameWindow.WIDTH / 5);
        int buttonHeight = Math.max(50, GameWindow.HEIGHT / 14);
        int gap = Math.max(12, GameWindow.HEIGHT / 55);
        int x = GameWindow.WIDTH / 2 - buttonWidth / 2;
        int startY = GameWindow.HEIGHT / 2 - (buttonHeight * buttons.size() + gap * (buttons.size() - 1)) / 2 + GameWindow.HEIGHT / 4;

        for (int i = 0; i < buttons.size(); i++) {
            buttons.get(i).setBounds(x, startY + i * (buttonHeight + gap), buttonWidth, buttonHeight);
        }
    }

    private void renderBackground(Graphics2D g) {
        BufferedImage background = Game.get().getAssets().image("ui.main-menu");
        if (background == null) {
            g.setColor(new Color(8, 12, 20));
            g.fillRect(0, 0, GameWindow.WIDTH, GameWindow.HEIGHT);
            return;
        }

        double scale = Math.max(
                GameWindow.WIDTH / (double) background.getWidth(),
                GameWindow.HEIGHT / (double) background.getHeight()
        );
        int width = (int) Math.ceil(background.getWidth() * scale);
        int height = (int) Math.ceil(background.getHeight() * scale);
        int x = (GameWindow.WIDTH - width) / 2;
        int y = (GameWindow.HEIGHT - height) / 2;
        g.drawImage(background, x, y, width, height, null);
    }
}
