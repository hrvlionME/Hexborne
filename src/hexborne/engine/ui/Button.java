package hexborne.engine.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;

public final class Button {
    private Rectangle bounds;
    private final String label;
    private final Runnable action;

    public Button(int x, int y, int width, int height, String label, Runnable action) {
        this.bounds = new Rectangle(x, y, width, height);
        this.label = label;
        this.action = action;
    }

    public void setBounds(int x, int y, int width, int height) {
        this.bounds = new Rectangle(x, y, width, height);
    }

    public void render(Graphics2D g) {
        g.setColor(new Color(30, 45, 62));
        g.fillRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 14, 14);
        g.setColor(new Color(118, 201, 176));
        g.drawRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 14, 14);
        g.setFont(new Font("Georgia", Font.BOLD, Math.max(16, bounds.height / 3)));
        int textWidth = g.getFontMetrics().stringWidth(label);
        int textY = bounds.y + (bounds.height + g.getFontMetrics().getAscent()) / 2 - 4;
        g.drawString(label, bounds.x + (bounds.width - textWidth) / 2, textY);
    }

    public boolean click(int x, int y) {
        if (!bounds.contains(x, y)) {
            return false;
        }

        action.run();
        return true;
    }
}
