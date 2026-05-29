package hexborne.game.states;

import hexborne.engine.states.GameState;
import hexborne.engine.states.State;
import hexborne.game.Game;
import hexborne.game.blocks.CharacterSelectBlock;
import hexborne.game.blocks.GearSelectBlock;
import hexborne.game.models.Models;

public final class GearSelectState extends State {
    public GearSelectState(Models models) {
        super(models);
    }

    @Override
    protected GameState getStateId() {
        return GameState.GEAR_SELECT;
    }

    @Override
    protected void setupEvents() {
    }

    @Override
    protected void modelChanges() {
        models.game().setLastLoadedState(GameState.GEAR_SELECT);
    }

    @Override
    protected void setupBlocks() {
        addBlock(new CharacterSelectBlock());
        addBlock(new GearSelectBlock());
    }

    @Override
    protected void removeEvents() {
    }

    @Override
    protected void exitState() {
        Game.get().changeState(GameState.GENERATE_RUN);
    }
}
