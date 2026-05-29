package hexborne.game;

import hexborne.engine.events.CustomEvent;
import hexborne.engine.render.GameWindow;
import hexborne.engine.states.GameState;
import hexborne.engine.states.State;
import hexborne.game.assets.AssetManager;
import hexborne.game.models.Models;
import hexborne.game.states.CollectionState;
import hexborne.game.states.GameMenuState;
import hexborne.game.states.GameOverState;
import hexborne.game.states.GameplayState;
import hexborne.game.states.GearSelectState;
import hexborne.game.states.GenerateRunState;
import hexborne.game.states.LoadingState;
import hexborne.game.states.RunSuccessState;
import hexborne.game.states.SettingsState;

import java.util.EnumMap;
import java.util.Map;

public final class Game {
    private static final Game INSTANCE = new Game();

    private final Models models = new Models();
    private final AssetManager assets = new AssetManager();
    private final GameEvents events = new GameEvents();
    private final Map<GameState, State> states = new EnumMap<>(GameState.class);

    private GameState activeState = GameState.LOADING;
    private GameWindow window;
    private boolean running;

    private Game() {
        states.put(GameState.LOADING, new LoadingState(models));
        states.put(GameState.GAME_MENU, new GameMenuState(models));
        states.put(GameState.SETTINGS, new SettingsState(models));
        states.put(GameState.COLLECTION, new CollectionState(models));
        states.put(GameState.GEAR_SELECT, new GearSelectState(models));
        states.put(GameState.GENERATE_RUN, new GenerateRunState(models));
        states.put(GameState.GAMEPLAY, new GameplayState(models));
        states.put(GameState.RUN_SUCCESS, new RunSuccessState(models));
        states.put(GameState.GAME_OVER, new GameOverState(models));
    }

    public static Game get() {
        return INSTANCE;
    }

    public void startGame() {
        if (running) {
            return;
        }

        running = true;
        System.out.println("Starting Hexborne state machine...");
        window = new GameWindow();
        window.startLoop();
        changeState(GameState.LOADING);
    }

    public void stopGame() {
        if (!running) {
            return;
        }

        State currentState = states.get(activeState);
        if (currentState != null) {
            currentState.cleanup();
        }

        running = false;
        System.out.println("Stopped Hexborne state machine.");
    }

    public void exitGame() {
        stopGame();
        if (window != null) {
            window.dispose();
        }
        System.exit(0);
    }

    public void changeState(GameState nextState) {
        State currentState = states.get(activeState);
        if (running && currentState != null && activeState != nextState) {
            currentState.cleanup();
        }

        State state = states.get(nextState);
        if (state == null) {
            throw new IllegalArgumentException("Missing state: " + nextState);
        }

        state.enterState();
    }

    public Models getModels() {
        return models;
    }

    public AssetManager getAssets() {
        return assets;
    }

    public State getCurrentState() {
        return states.get(activeState);
    }

    public GameWindow getWindow() {
        return window;
    }

    public GameEvents getEvents() {
        return events;
    }

    public GameState getActiveState() {
        return activeState;
    }

    public void setActiveState(GameState activeState) {
        this.activeState = activeState;
    }

    public static final class GameEvents {
        public final CustomEvent<Void> blockComplete = new CustomEvent<>("block-complete");
        public final CustomEvent<GameState> stateChanged = new CustomEvent<>("state-changed");
    }
}
