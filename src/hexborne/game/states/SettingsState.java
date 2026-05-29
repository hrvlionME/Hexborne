package hexborne.game.states;

import hexborne.engine.render.GameWindow;
import hexborne.engine.states.GameState;
import hexborne.engine.states.State;
import hexborne.engine.ui.Button;
import hexborne.engine.ui.Ui;
import hexborne.game.Game;
import hexborne.game.models.Models;

import java.awt.Color;
import java.awt.DisplayMode;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public final class SettingsState extends State {
    private static final int[][] RESOLUTIONS = {
            {1280, 720},
            {1600, 900},
            {1920, 1080},
            {2560, 1440}
    };

    private final List<Button> buttons = new ArrayList<>();
    private boolean fullscreenEnabled = true;
    private int resolutionIndex = 2;
    private String note = "Choose display settings, then apply.";

    public SettingsState(Models models) {
        super(models);
    }

    @Override
    protected GameState getStateId() {
        return GameState.SETTINGS;
    }

    @Override
    protected void setupEvents() {
    }

    @Override
    protected void modelChanges() {
        models.game().setLastLoadedState(GameState.SETTINGS);
        syncFromWindow();
    }

    @Override
    protected void setupBlocks() {
        buttons.clear();
        buttons.add(new Button(0, 0, 1, 1, "<", this::previousResolution));
        buttons.add(new Button(0, 0, 1, 1, ">", this::nextResolution));
        buttons.add(new Button(0, 0, 1, 1, fullscreenLabel(), this::toggleFullscreen));
        buttons.add(new Button(0, 0, 1, 1, "Apply", this::applySettings));
        buttons.add(new Button(0, 0, 1, 1, "Back", () -> Game.get().changeState(GameState.GAME_MENU)));
    }

    private void syncFromWindow() {
        if (Game.get().getWindow() != null) {
            fullscreenEnabled = Game.get().getWindow().isFullscreen();
        }

        int bestIndex = 0;
        int bestDistance = Integer.MAX_VALUE;
        for (int i = 0; i < RESOLUTIONS.length; i++) {
            int distance = Math.abs(GameWindow.WIDTH - RESOLUTIONS[i][0]) + Math.abs(GameWindow.HEIGHT - RESOLUTIONS[i][1]);
            if (distance < bestDistance) {
                bestDistance = distance;
                bestIndex = i;
            }
        }
        resolutionIndex = bestIndex;
    }

    private void previousResolution() {
        resolutionIndex = (resolutionIndex - 1 + RESOLUTIONS.length) % RESOLUTIONS.length;
        note = "Resolution selected: " + resolutionLabel() + ".";
    }

    private void nextResolution() {
        resolutionIndex = (resolutionIndex + 1) % RESOLUTIONS.length;
        note = "Resolution selected: " + resolutionLabel() + ".";
    }

    private void toggleFullscreen() {
        fullscreenEnabled = !fullscreenEnabled;
        note = fullscreenEnabled ? "Fullscreen enabled." : "Windowed mode enabled.";
        setupBlocks();
    }

    private void applySettings() {
        int width = RESOLUTIONS[resolutionIndex][0];
        int height = RESOLUTIONS[resolutionIndex][1];

        if (fullscreenEnabled) {
            DisplayMode displayMode = GraphicsEnvironment
                    .getLocalGraphicsEnvironment()
                    .getDefaultScreenDevice()
                    .getDisplayMode();
            width = displayMode.getWidth();
            height = displayMode.getHeight();
        }

        Game.get().getWindow().applyDisplayMode(width, height, fullscreenEnabled);
        note = (fullscreenEnabled ? "Applied fullscreen " : "Applied windowed ") + resolutionLabel() + ".";
        setupBlocks();
    }

    private String fullscreenLabel() {
        return fullscreenEnabled ? "Fullscreen: ON" : "Fullscreen: OFF";
    }

    private String resolutionLabel() {
        return RESOLUTIONS[resolutionIndex][0] + " x " + RESOLUTIONS[resolutionIndex][1];
    }

    @Override
    protected void onBlocksComplete() {
        // Settings waits for user input.
    }

    @Override
    public void render(Graphics2D g) {
        layoutButtons();
        drawBackground(g);

        int panelWidth = Math.max(520, GameWindow.WIDTH / 3);
        int panelHeight = Math.max(460, GameWindow.HEIGHT * 2 / 3);
        int panelX = GameWindow.WIDTH / 2 - panelWidth / 2;
        int panelY = GameWindow.HEIGHT / 6;
        g.setColor(new Color(21, 36, 48));
        g.fillRoundRect(panelX, panelY, panelWidth, panelHeight, 26, 26);

        g.setFont(Ui.font(Font.BOLD, GameWindow.HEIGHT / 14));
        g.setColor(new Color(232, 231, 202));
        Ui.drawCentered(g, "Settings", GameWindow.WIDTH / 2, panelY + GameWindow.HEIGHT / 10);

        g.setFont(Ui.font(Font.BOLD, GameWindow.HEIGHT / 30));
        g.setColor(new Color(157, 188, 177));
        Ui.drawCentered(g, "Resolution", GameWindow.WIDTH / 2, panelY + panelHeight / 3);

        g.setFont(Ui.font(Font.BOLD, GameWindow.HEIGHT / 24));
        g.setColor(new Color(232, 231, 202));
        Ui.drawCentered(g, resolutionLabel(), GameWindow.WIDTH / 2, panelY + panelHeight / 3 + GameWindow.HEIGHT / 15);

        for (Button button : buttons) {
            button.render(g);
        }

        g.setFont(Ui.font(Font.PLAIN, GameWindow.HEIGHT / 40));
        g.setColor(new Color(196, 202, 188));
        Ui.drawCentered(g, note, GameWindow.WIDTH / 2, panelY + panelHeight - GameWindow.HEIGHT / 20);
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
        int panelWidth = Math.max(520, GameWindow.WIDTH / 3);
        int panelHeight = Math.max(460, GameWindow.HEIGHT * 2 / 3);
        int panelX = GameWindow.WIDTH / 2 - panelWidth / 2;
        int panelY = GameWindow.HEIGHT / 6;

        int chevronSize = Math.max(52, GameWindow.HEIGHT / 14);
        int centerY = panelY + panelHeight / 3 + GameWindow.HEIGHT / 25;
        buttons.get(0).setBounds(panelX + panelWidth / 8, centerY, chevronSize, chevronSize);
        buttons.get(1).setBounds(panelX + panelWidth - panelWidth / 8 - chevronSize, centerY, chevronSize, chevronSize);

        int buttonWidth = Math.max(260, panelWidth / 2);
        int buttonHeight = Math.max(48, GameWindow.HEIGHT / 15);
        int x = GameWindow.WIDTH / 2 - buttonWidth / 2;
        buttons.get(2).setBounds(x, panelY + panelHeight / 2, buttonWidth, buttonHeight);
        buttons.get(3).setBounds(x, panelY + panelHeight / 2 + buttonHeight + GameWindow.HEIGHT / 45, buttonWidth, buttonHeight);
        buttons.get(4).setBounds(x, panelY + panelHeight / 2 + (buttonHeight + GameWindow.HEIGHT / 45) * 2, buttonWidth, buttonHeight);
    }
}
