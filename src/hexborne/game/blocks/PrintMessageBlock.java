package hexborne.game.blocks;

import hexborne.engine.blocks.Block;

public final class PrintMessageBlock extends Block {
    private final String message;

    public PrintMessageBlock(String name, String message) {
        super(name);
        this.message = message;
    }

    @Override
    public void start() {
        System.out.println(message);
        end();
    }

    @Override
    public void cleanup() {
        // Nothing to clean up yet. Timers, input listeners, and spawned entities will go here later.
    }
}
