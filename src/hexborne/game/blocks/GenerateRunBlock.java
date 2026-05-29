package hexborne.game.blocks;

import hexborne.engine.blocks.Block;

public final class GenerateRunBlock extends Block {
    public GenerateRunBlock() {
        super("Generate Run");
    }

    @Override
    public void start() {
        System.out.println("Generating arena, waves, and boss encounter...");
        end();
    }

    @Override
    public void cleanup() {
    }
}
