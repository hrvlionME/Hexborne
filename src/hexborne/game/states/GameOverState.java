package hexborne.game.states;

import hexborne.engine.render.GameWindow;
import hexborne.engine.states.GameState;
import hexborne.engine.states.State;
import hexborne.engine.ui.Button;
import hexborne.engine.ui.Ui;
import hexborne.game.Game;
import hexborne.game.models.Models;
import hexborne.game.persistence.GameSave;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;

public final class GameOverState extends State {
    private Button continueButton;

    public GameOverState(Models models) {
        super(models);
    }

    @Override
    protected GameState getStateId() {
        return GameState.GAME_OVER;
    }

    @Override
    protected void setupEvents() {
    }

    @Override
    protected void modelChanges() {
    }

    @Override
    protected void setupBlocks() {
        System.out.println("Saving run results...");
        GameSave.save(models.game());
        continueButton = new Button(0, 0, 1, 1, "Back To Menu", () -> Game.get().changeState(GameState.GAME_MENU));
    }

    @Override
    protected void onBlocksComplete() {
        // Wait for user to return to menu.
    }

    @Override
    public void render(Graphics2D g) {
        layoutButton();
        g.setColor(new Color(11, 14, 24));
        g.fillRect(0, 0, GameWindow.WIDTH, GameWindow.HEIGHT);
        g.setFont(Ui.font(Font.BOLD, GameWindow.HEIGHT / 13));
        g.setColor(new Color(232, 231, 202));
        Ui.drawCentered(g, "Run Complete", GameWindow.WIDTH / 2, GameWindow.HEIGHT / 5);

        g.setFont(Ui.font(Font.PLAIN, GameWindow.HEIGHT / 26));
        g.setColor(new Color(118, 201, 176));
        int lineY = GameWindow.HEIGHT / 3;
        int lineGap = GameWindow.HEIGHT / 14;
        Ui.drawCentered(g, "Gold gained: " + models.game().getLastRunCurrencyGain(), GameWindow.WIDTH / 2, lineY);
        Ui.drawCentered(g, "EXP gained: " + models.game().getLastRunExperienceGain(), GameWindow.WIDTH / 2, lineY + lineGap);
        Ui.drawCentered(g, "Total gold: " + models.game().getCurrency(), GameWindow.WIDTH / 2, lineY + lineGap * 2);
        Ui.drawCentered(g, "Total EXP: " + models.game().getExperience(), GameWindow.WIDTH / 2, lineY + lineGap * 3);

        continueButton.render(g);
    }

    @Override
    public void mousePressed(MouseEvent event) {
        layoutButton();
        continueButton.click(event.getX(), event.getY());
    }

    @Override
    protected void removeEvents() {
    }

    @Override
    protected void exitState() {
    }

    private void layoutButton() {
        int width = Math.max(260, GameWindow.WIDTH / 5);
        int height = Math.max(50, GameWindow.HEIGHT / 14);
        continueButton.setBounds(GameWindow.WIDTH / 2 - width / 2, GameWindow.HEIGHT * 3 / 4, width, height);
    }
}
