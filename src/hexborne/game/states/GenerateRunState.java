package hexborne.game.states;

import hexborne.engine.states.GameState;
import hexborne.engine.states.State;
import hexborne.game.Game;
import hexborne.game.blocks.GenerateRunBlock;
import hexborne.game.models.Models;

public final class GenerateRunState extends State {
    public GenerateRunState(Models models) {
        super(models);
    }

    @Override
    protected GameState getStateId() {
        return GameState.GENERATE_RUN;
    }

    @Override
    protected void setupEvents() {
    }

    @Override
    protected void modelChanges() {
        models.game().setLastLoadedState(GameState.GENERATE_RUN);
    }

    @Override
    protected void setupBlocks() {
        addBlock(new GenerateRunBlock());
    }

    @Override
    protected void removeEvents() {
    }

    @Override
    protected void exitState() {
        Game.get().changeState(GameState.GAMEPLAY);
    }
}
