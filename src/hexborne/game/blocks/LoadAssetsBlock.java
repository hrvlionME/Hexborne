package hexborne.game.blocks;

import hexborne.engine.blocks.Block;
import hexborne.game.Game;

public final class LoadAssetsBlock extends Block {
    public LoadAssetsBlock() {
        super("Load Assets");
    }

    @Override
    public void start() {
        Game.get().getAssets().loadGameAssets();
        end();
    }

    @Override
    public void cleanup() {
    }
}
