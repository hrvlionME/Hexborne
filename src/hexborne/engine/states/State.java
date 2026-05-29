package hexborne.engine.states;

import hexborne.engine.blocks.Block;
import hexborne.game.Game;
import hexborne.game.models.Models;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.function.Consumer;

public abstract class State {
    private final Queue<Block> blocks = new ArrayDeque<>();
    private final Consumer<Void> playThroughBlocks = ignored -> playThroughBlocks();

    protected final Models models;
    protected Block currentBlock;

    protected State(Models models) {
        this.models = models;
    }

    public final void enterState() {
        System.out.println("State enter: " + getClass().getSimpleName());

        modelChanges();
        Game.get().setActiveState(getStateId());
        Game.get().getEvents().stateChanged.dispatch(getStateId());

        setupBlockLoopEvent();
        setupEvents();
        setupBlocks();

        if (blocks.isEmpty()) {
            onBlocksComplete();
        } else {
            playThroughBlocks();
        }
    }

    private void setupBlockLoopEvent() {
        Game.get().getEvents().blockComplete.remove(playThroughBlocks);
        Game.get().getEvents().blockComplete.add(playThroughBlocks);
    }

    protected final void addBlock(Block block) {
        blocks.add(block);
    }

    private void playThroughBlocks() {
        Block nextBlock = blocks.poll();

        if (nextBlock != null) {
            currentBlock = nextBlock;
            System.out.println("Block start: " + nextBlock.getName());
            nextBlock.start();
            return;
        }

        onBlocksComplete();
    }

    protected void onBlocksComplete() {
        cleanup();
        exitState();
    }

    public void update(double deltaSeconds) {
        if (currentBlock != null) {
            currentBlock.update(deltaSeconds);
        }
    }

    public void render(Graphics2D g) {
        if (currentBlock != null) {
            currentBlock.render(g);
        }
    }

    public void keyPressed(KeyEvent event) {
        if (currentBlock != null) {
            currentBlock.keyPressed(event);
        }
    }

    public void keyReleased(KeyEvent event) {
        if (currentBlock != null) {
            currentBlock.keyReleased(event);
        }
    }

    public void mousePressed(MouseEvent event) {
        if (currentBlock != null) {
            currentBlock.mousePressed(event);
        }
    }

    public void mouseReleased(MouseEvent event) {
        if (currentBlock != null) {
            currentBlock.mouseReleased(event);
        }
    }

    public void mouseMoved(MouseEvent event) {
        if (currentBlock != null) {
            currentBlock.mouseMoved(event);
        }
    }

    public void cleanup() {
        if (currentBlock != null) {
            currentBlock.cleanup();
            currentBlock = null;
        }

        blocks.clear();
        Game.get().getEvents().blockComplete.remove(playThroughBlocks);
        removeEvents();
    }

    protected abstract GameState getStateId();

    protected abstract void setupEvents();

    protected abstract void modelChanges();

    protected abstract void setupBlocks();

    protected abstract void removeEvents();

    protected abstract void exitState();
}
