package hexborne.engine.render;

import hexborne.engine.states.State;
import hexborne.game.Game;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.io.InputStream;

public final class GameWindow extends JFrame {
    public static int WIDTH = 1280;
    public static int HEIGHT = 720;

    private final GamePanel panel = new GamePanel();
    private boolean fullscreen;

    public GameWindow() {
        super("Hexborne");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setContentPane(panel);
        applyDisplayMode(getScreenWidth(), getScreenHeight(), true);
    }

    public void startLoop() {
        setVisible(true);
        panel.startLoop();
    }

    public void applyDisplayMode(int width, int height, boolean fullscreen) {
        WIDTH = width;
        HEIGHT = height;
        this.fullscreen = fullscreen;

        boolean wasVisible = isVisible();
        if (isDisplayable()) {
            dispose();
        }

        setUndecorated(fullscreen);
        panel.applySize(new Dimension(width, height));
        setSize(width, height);

        if (fullscreen) {
            setExtendedState(JFrame.MAXIMIZED_BOTH);
        } else {
            setExtendedState(JFrame.NORMAL);
            pack();
            setLocationRelativeTo(null);
        }

        if (wasVisible) {
            setVisible(true);
            panel.requestFocusInWindow();
        }
    }

    public boolean isFullscreen() {
        return fullscreen;
    }

    private int getScreenWidth() {
        return getScreenDevice().getDisplayMode().getWidth();
    }

    private int getScreenHeight() {
        return getScreenDevice().getDisplayMode().getHeight();
    }

    private GraphicsDevice getScreenDevice() {
        return GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
    }

    private static final class GamePanel extends JPanel {
        private long lastFrameNanos = System.nanoTime();
        private long lastMemoryLogNanos = System.nanoTime();
        private BufferedImage cursorImage;
        private int mouseX;
        private int mouseY;
        private boolean mouseInside;

        private GamePanel() {
            applySize(new Dimension(GameWindow.WIDTH, GameWindow.HEIGHT));
            setBackground(new Color(9, 13, 20));
            setFocusable(true);
            setupCustomCursor();

            addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent event) {
                    State state = Game.get().getCurrentState();
                    if (state != null) {
                        state.keyPressed(event);
                    }
                }

                @Override
                public void keyReleased(KeyEvent event) {
                    State state = Game.get().getCurrentState();
                    if (state != null) {
                        state.keyReleased(event);
                    }
                }
            });

            MouseAdapter mouse = new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent event) {
                    mouseInside = true;
                    updateMousePosition(event);
                }

                @Override
                public void mouseExited(MouseEvent event) {
                    mouseInside = false;
                }

                @Override
                public void mousePressed(MouseEvent event) {
                    updateMousePosition(event);
                    State state = Game.get().getCurrentState();
                    if (state != null) {
                        state.mousePressed(event);
                    }
                }

                @Override
                public void mouseReleased(MouseEvent event) {
                    updateMousePosition(event);
                    State state = Game.get().getCurrentState();
                    if (state != null) {
                        state.mouseReleased(event);
                    }
                }

                @Override
                public void mouseMoved(MouseEvent event) {
                    updateMousePosition(event);
                    State state = Game.get().getCurrentState();
                    if (state != null) {
                        state.mouseMoved(event);
                    }
                }

                @Override
                public void mouseDragged(MouseEvent event) {
                    mouseMoved(event);
                }
            };
            addMouseListener(mouse);
            addMouseMotionListener(mouse);
        }

        private void setupCustomCursor() {
            try (InputStream stream = GameWindow.class.getResourceAsStream("/assets/ui/cursor.png")) {
                if (stream == null) {
                    return;
                }

                BufferedImage image = ImageIO.read(stream);
                cursorImage = scaleCursorImage(image, 4.0 / 3.0);
                BufferedImage blankImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
                Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(blankImage, new Point(0, 0), "Hexborne Hidden Cursor");
                setCursor(blankCursor);
            } catch (IOException | RuntimeException exception) {
                System.out.println("Failed to load custom cursor: " + exception.getMessage());
            }
        }

        private BufferedImage scaleCursorImage(BufferedImage image, double scale) {
            int width = Math.max(1, (int) Math.round(image.getWidth() * scale));
            int height = Math.max(1, (int) Math.round(image.getHeight() * scale));
            BufferedImage scaledImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = scaledImage.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g.drawImage(image, 0, 0, scaledImage.getWidth(), scaledImage.getHeight(), null);
            g.dispose();
            return scaledImage;
        }

        private void updateMousePosition(MouseEvent event) {
            mouseX = event.getX();
            mouseY = event.getY();
        }

        private void applySize(Dimension size) {
            setPreferredSize(size);
            setMinimumSize(size);
            setSize(size);
            revalidate();
        }

        private void logMemoryUsage(long now) {
            if (now - lastMemoryLogNanos < 5_000_000_000L) {
                return;
            }

            Runtime runtime = Runtime.getRuntime();
            long usedMb = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
            long totalMb = runtime.totalMemory() / (1024 * 1024);
            long maxMb = runtime.maxMemory() / (1024 * 1024);
            System.out.println("Memory: " + usedMb + " MB used / " + totalMb + " MB allocated / " + maxMb + " MB max");
            lastMemoryLogNanos = now;
        }

        private void startLoop() {
            Timer timer = new Timer(16, ignored -> {
                long now = System.nanoTime();
                double deltaSeconds = (now - lastFrameNanos) / 1_000_000_000.0;
                lastFrameNanos = now;
                logMemoryUsage(now);

                State state = Game.get().getCurrentState();
                if (state != null) {
                    state.update(Math.min(deltaSeconds, 0.05));
                }

                repaint();
            });
            timer.start();
            requestFocusInWindow();
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            State state = Game.get().getCurrentState();
            if (state != null) {
                state.render(g);
            }

            drawCustomCursor(g);
            g.dispose();
        }

        private void drawCustomCursor(Graphics2D g) {
            if (cursorImage == null || !mouseInside) {
                return;
            }

            Graphics2D cursorGraphics = (Graphics2D) g.create();
            cursorGraphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            cursorGraphics.drawImage(cursorImage, mouseX - cursorImage.getWidth() / 2, mouseY - cursorImage.getHeight() / 2, null);
            cursorGraphics.dispose();
        }
    }
}
