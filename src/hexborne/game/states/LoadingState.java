package hexborne.game.states;

import hexborne.engine.states.GameState;
import hexborne.engine.states.State;
import hexborne.game.Game;
import hexborne.game.blocks.LoadAssetsBlock;
import hexborne.game.blocks.PrintMessageBlock;
import hexborne.game.models.Models;
import hexborne.game.persistence.GameSave;

public final class LoadingState extends State {
    public LoadingState(Models models) {
        super(models);
    }

    @Override
    protected GameState getStateId() {
        return GameState.LOADING;
    }

    @Override
    protected void setupEvents() {
        // Register loading-specific events here later.
    }

    @Override
    protected void modelChanges() {
        models.game().setLastLoadedState(GameState.LOADING);
        GameSave.load(models.game());
    }

    @Override
    protected void setupBlocks() {
        addBlock(new LoadAssetsBlock());
        addBlock(new PrintMessageBlock("Load Save", "Loading local save data..."));
    }

    @Override
    protected void removeEvents() {
        // Remove loading-specific events here later.
    }

    @Override
    protected void exitState() {
        Game.get().changeState(GameState.GAME_MENU);
    }
}
