package hexborne.engine.blocks;

import hexborne.game.Game;
import hexborne.game.models.Models;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

public abstract class Block {
    private final String name;
    protected final Models models;

    protected Block(String name) {
        this.name = name;
        this.models = Game.get().getModels();
    }

    public String getName() {
        return name;
    }

    public abstract void start();

    public void update(double deltaSeconds) {
    }

    public void render(Graphics2D g) {
    }

    public void keyPressed(KeyEvent event) {
    }

    public void keyReleased(KeyEvent event) {
    }

    public void mousePressed(MouseEvent event) {
    }

    public void mouseReleased(MouseEvent event) {
    }

    public void mouseMoved(MouseEvent event) {
    }

    protected final void end() {
        Game.get().getEvents().blockComplete.dispatch();
    }

    public abstract void cleanup();
}
