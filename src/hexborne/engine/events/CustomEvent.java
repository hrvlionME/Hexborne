package hexborne.engine.events;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class CustomEvent<T> {
    private final String eventName;
    private final boolean multipleCallersAllowed;
    private final List<Consumer<T>> listeners = new ArrayList<>();

    public CustomEvent(String eventName) {
        this(eventName, false);
    }

    public CustomEvent(String eventName, boolean multipleCallersAllowed) {
        this.eventName = eventName;
        this.multipleCallersAllowed = multipleCallersAllowed;
    }

    public void add(Consumer<T> listener) {
        if (!multipleCallersAllowed && listeners.contains(listener)) {
            System.out.println("Duplicate listener ignored for event: " + eventName);
            return;
        }

        listeners.add(listener);
    }

    public void remove(Consumer<T> listener) {
        listeners.remove(listener);
    }

    public void removeAll() {
        listeners.clear();
    }

    public void dispatch(T value) {
        List<Consumer<T>> snapshot = new ArrayList<>(listeners);
        for (Consumer<T> listener : snapshot) {
            listener.accept(value);
        }
    }

    public void dispatch() {
        dispatch(null);
    }

    public boolean hasListeners() {
        return !listeners.isEmpty();
    }
}
