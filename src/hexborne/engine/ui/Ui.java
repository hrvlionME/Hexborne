package hexborne.engine.ui;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

public final class Ui {
    private Ui() {
    }

    public static void drawCentered(Graphics2D g, String text, int centerX, int baselineY) {
        int textWidth = g.getFontMetrics().stringWidth(text);
        g.drawString(text, centerX - textWidth / 2, baselineY);
    }

    public static Font font(int style, int size) {
        return new Font("Georgia", style, Math.max(12, size));
    }

    public static void drawPixelImageBottomLeft(Graphics2D g, BufferedImage image, int leftX, int baselineY, int size) {
        Graphics2D pixelGraphics = (Graphics2D) g.create();
        pixelGraphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        pixelGraphics.drawImage(image, leftX, baselineY - size, size, size, null);
        pixelGraphics.dispose();
    }

    public static void drawPixelImageCenteredTrimmed(Graphics2D g, BufferedImage image, int centerX, int centerY, int size) {
        int minX = image.getWidth();
        int minY = image.getHeight();
        int maxX = -1;
        int maxY = -1;

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                if (((image.getRGB(x, y) >>> 24) & 0xff) == 0) {
                    continue;
                }

                minX = Math.min(minX, x);
                minY = Math.min(minY, y);
                maxX = Math.max(maxX, x);
                maxY = Math.max(maxY, y);
            }
        }

        if (maxX < minX || maxY < minY) {
            drawPixelImageBottomLeft(g, image, centerX - size / 2, centerY + size / 2, size);
            return;
        }

        int sourceWidth = maxX - minX + 1;
        int sourceHeight = maxY - minY + 1;
        double scale = Math.min(size / (double) sourceWidth, size / (double) sourceHeight);
        int drawWidth = Math.max(1, (int) Math.round(sourceWidth * scale));
        int drawHeight = Math.max(1, (int) Math.round(sourceHeight * scale));
        int drawX = centerX - drawWidth / 2;
        int drawY = centerY - drawHeight / 2;

        Graphics2D pixelGraphics = (Graphics2D) g.create();
        pixelGraphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        pixelGraphics.drawImage(image, drawX, drawY, drawX + drawWidth, drawY + drawHeight, minX, minY, maxX + 1, maxY + 1, null);
        pixelGraphics.dispose();
    }
}
