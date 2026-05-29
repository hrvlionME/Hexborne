package hexborne.game.states;

import hexborne.engine.render.GameWindow;
import hexborne.engine.states.GameState;
import hexborne.engine.states.State;
import hexborne.engine.ui.Ui;
import hexborne.game.Game;
import hexborne.game.blocks.PauseMenuBlock;
import hexborne.game.items.ItemConfig;
import hexborne.game.items.ItemDefinition;
import hexborne.game.items.ItemType;
import hexborne.game.models.Models;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

public final class GameplayState extends State {
    private static final double PLAYER_SPEED = 260;
    private static final int PLAYER_DAMAGE = 18;
    private static final double PLAYER_DEXTERITY = 5.5;
    private static final double BASE_PROJECTILE_SPEED = 620;
    private static final double BASE_PROJECTILE_LIFE = 1.4;
    private static final int MAX_WAVES = 10;
    private static final double MAX_HEALTH = 100;
    private static final double MAX_MANA = 100;
    private static final double MANA_PER_LUCK = 5;
    private static final double SPEED_PER_MOVEMENT = 12;
    private static final double HEALTH_REGEN_PER_SECOND = 2.0;
    private static final double MANA_REGEN_PER_SECOND = 1.0;
    private static final double SPELL_MANA_COST = 40.0;
    private static final double MAP_SCALE = 2.0;
    private static final double MAP_EDGE_INSET = 96;
    private static final double PLAYER_COLLISION_RADIUS = 14;
    private static final int ENEMY_SHEET_SPRITE_X = 64;
    private static final int ENEMY_SHEET_FRAME_SIZE = 32;
    private static final int ENEMY_SHEET_WALK_Y = 96;
    private static final int ENEMY_SHEET_IDLE_Y = 128;
    private static final int PLAYER_DRAW_SIZE = 82;
    private static final int PLAYER_WALK_FRAMES = 6;
    private static final int PLAYER_DAMAGE_FRAMES = 6;
    private static final int PLAYER_ATTACK_FRAMES = 9;
    private static final int PROJECTILE_DRAW_SIZE = 36;
    private static final int CHEST_FRAME_SIZE = 256;
    private static final int CHEST_DRAW_SIZE = 64;
    private static final int MINIMAP_SIZE = 150;
    private static final double MINIMAP_WORLD_RADIUS = 900;
    private static final double PLAYER_WALK_FRAMES_PER_SECOND = 10.0;
    private static final double PLAYER_DAMAGE_FRAMES_PER_SECOND = 14.0;
    private static final double PLAYER_ATTACK_FRAMES_PER_SECOND = 24.0;
    private static final double PLAYER_DAMAGE_DURATION = PLAYER_DAMAGE_FRAMES / PLAYER_DAMAGE_FRAMES_PER_SECOND;
    private static final double PLAYER_ATTACK_DURATION = PLAYER_ATTACK_FRAMES / PLAYER_ATTACK_FRAMES_PER_SECOND;

    private final Random random = new Random();
    private final List<Enemy> enemies = new ArrayList<>();
    private final List<Projectile> projectiles = new ArrayList<>();
    private final List<EnemyProjectile> enemyProjectiles = new ArrayList<>();
    private final boolean[] keys = new boolean[256];

    private double playerX;
    private double playerY;
    private double playerHealth;
    private double playerMana;
    private double playerAnimationTime;
    private double playerDamageTime;
    private double playerAttackTime;
    private double playerAttackAnimationTime;
    private boolean playerMoving;
    private String playerFacing;
    private int wave;
    private double shootCooldown;
    private int mouseX;
    private int mouseY;
    private boolean shooting;
    private boolean paused;
    private PauseMenuBlock pauseMenuBlock;
    private boolean bossSpawned;
    private boolean chestDropped;
    private double chestX;
    private double chestY;
    private List<ItemDefinition> equippedItems;

    public GameplayState(Models models) {
        super(models);
    }

    @Override
    protected GameState getStateId() {
        return GameState.GAMEPLAY;
    }

    @Override
    protected void setupEvents() {
    }

    @Override
    protected void modelChanges() {
        playerX = worldWidth() / 2.0;
        playerY = worldHeight() / 2.0;
        equippedItems = equippedItemsFromModel();
        playerHealth = effectiveMaxHealth();
        playerMana = effectiveMaxMana();
        playerAnimationTime = 0;
        playerDamageTime = 0;
        playerAttackTime = 0;
        playerAttackAnimationTime = 0;
        playerMoving = false;
        playerFacing = "south";
        wave = 0;
        shootCooldown = 0;
        mouseX = GameWindow.WIDTH / 2;
        mouseY = GameWindow.HEIGHT / 2;
        shooting = false;
        paused = false;
        pauseMenuBlock = null;
        bossSpawned = false;
        chestDropped = false;
        enemies.clear();
        projectiles.clear();
        enemyProjectiles.clear();
    }

    @Override
    protected void setupBlocks() {
    }

    @Override
    protected void onBlocksComplete() {
        spawnNextWave();
    }

    @Override
    public void update(double deltaSeconds) {
        if (paused) {
            return;
        }

        updatePlayer(deltaSeconds);
        updateRegeneration(deltaSeconds);
        updateShooting(deltaSeconds);
        updateProjectiles(deltaSeconds);
        updateEnemyProjectiles(deltaSeconds);
        updateEnemies(deltaSeconds);
        handleCombat();

        if (playerHealth <= 0) {
            models.game().finishRun(Math.max(5, wave * 3), Math.max(10, wave * 8));
            Game.get().changeState(GameState.GAME_OVER);
            return;
        }

        if (enemies.isEmpty() && !chestDropped) {
            if (wave < MAX_WAVES) {
                spawnNextWave();
            } else if (!bossSpawned) {
                spawnBoss();
            }
        }

        if (chestDropped && distance(playerX, playerY, chestX, chestY) < 42) {
            models.game().unlockRandomGear(random);
            models.game().finishRun(75, 160);
            Game.get().changeState(GameState.RUN_SUCCESS);
        }
    }

    private void updatePlayer(double deltaSeconds) {
        double dx = 0;
        double dy = 0;
        if (isDown(KeyEvent.VK_W)) dy -= 1;
        if (isDown(KeyEvent.VK_S)) dy += 1;
        if (isDown(KeyEvent.VK_A)) dx -= 1;
        if (isDown(KeyEvent.VK_D)) dx += 1;

        double length = Math.sqrt(dx * dx + dy * dy);
        if (length > 0) {
            double speed = effectivePlayerSpeed();
            double moveX = dx / length * speed * deltaSeconds;
            double moveY = dy / length * speed * deltaSeconds;
            movePlayer(moveX, moveY);
            playerAnimationTime += deltaSeconds;
            playerFacing = directionFromVector(dx, dy);
            playerMoving = true;
        } else {
            double aimDx = screenToWorldX(mouseX) - playerX;
            double aimDy = screenToWorldY(mouseY) - playerY;
            if (Math.abs(aimDx) > 0.01 || Math.abs(aimDy) > 0.01) {
                playerFacing = directionFromVector(aimDx, aimDy);
            }
            playerMoving = false;
        }

        playerDamageTime = Math.max(0, playerDamageTime - deltaSeconds);
        if (playerAttackTime > 0) {
            playerAttackAnimationTime += deltaSeconds;
            playerAttackTime = Math.max(0, playerAttackTime - deltaSeconds);
        }
        playerX = clamp(playerX, MAP_EDGE_INSET, worldWidth() - MAP_EDGE_INSET);
        playerY = clamp(playerY, MAP_EDGE_INSET, worldHeight() - MAP_EDGE_INSET);
    }

    private void movePlayer(double moveX, double moveY) {
        double nextX = clamp(playerX + moveX, MAP_EDGE_INSET, worldWidth() - MAP_EDGE_INSET);
        double nextY = clamp(playerY + moveY, MAP_EDGE_INSET, worldHeight() - MAP_EDGE_INSET);

        if (isWalkablePlayerPosition(nextX, nextY)) {
            playerX = nextX;
            playerY = nextY;
            return;
        }

        if (isWalkablePlayerPosition(nextX, playerY)) {
            playerX = nextX;
        }
        if (isWalkablePlayerPosition(playerX, nextY)) {
            playerY = nextY;
        }
    }

    private void updateShooting(double deltaSeconds) {
        shootCooldown -= deltaSeconds;
        if (!shooting || shootCooldown > 0) {
            return;
        }

        double dx = screenToWorldX(mouseX) - playerX;
        double dy = screenToWorldY(mouseY) - playerY;
        double length = Math.max(1, Math.sqrt(dx * dx + dy * dy));
        playerFacing = directionFromVector(dx, dy);
        fireStaffWeapon(dx / length, dy / length);
        startAttackAnimation();
        shootCooldown = 1.0 / (effectiveDexterity() * currentStaffWeapon().fireRateMultiplier);
    }

    private void startAttackAnimation() {
        if (playerAttackTime <= 0) {
            playerAttackAnimationTime = 0;
        }
        playerAttackTime = PLAYER_ATTACK_DURATION;
    }

    private void fireStaffWeapon(double directionX, double directionY) {
        StaffWeapon weapon = currentStaffWeapon();
        double perpendicularX = -directionY;
        double perpendicularY = directionX;
        double startOffset = -(weapon.projectileCount - 1) * weapon.parallelSpacing / 2.0;

        for (int i = 0; i < weapon.projectileCount; i++) {
            double angle = weapon.projectileCount == 1
                    ? (random.nextDouble() - 0.5) * weapon.spreadAngle
                    : -weapon.spreadAngle / 2.0 + weapon.spreadAngle * i / (weapon.projectileCount - 1);
            double shotDirectionX = directionX;
            double shotDirectionY = directionY;
            if (Math.abs(angle) > 0.0001) {
                double cos = Math.cos(angle);
                double sin = Math.sin(angle);
                shotDirectionX = directionX * cos - directionY * sin;
                shotDirectionY = directionX * sin + directionY * cos;
            }

            double parallelOffset = startOffset + i * weapon.parallelSpacing;
            double shotX = playerX + perpendicularX * parallelOffset;
            double shotY = playerY + perpendicularY * parallelOffset;
            double phase = weapon.helix ? i * Math.PI : 0;
            projectiles.add(new Projectile(
                    shotX,
                    shotY,
                    shotDirectionX * weapon.speed,
                    shotDirectionY * weapon.speed,
                    weapon.damage + totalDamageBonus(),
                    5,
                    new Color(119, 203, 255),
                    currentStaffProjectileAlias(),
                    weapon.life,
                    weapon.pierce,
                    weapon.boomerang,
                    weapon.helix,
                    phase
            ));
        }
    }

    private void updateRegeneration(double deltaSeconds) {
        playerHealth = clamp(playerHealth + HEALTH_REGEN_PER_SECOND * deltaSeconds, 0, effectiveMaxHealth());
        playerMana = clamp(playerMana + MANA_REGEN_PER_SECOND * deltaSeconds, 0, effectiveMaxMana());
    }

    private void castSpell() {
        if (playerMana < SPELL_MANA_COST) {
            return;
        }

        double dx = screenToWorldX(mouseX) - playerX;
        double dy = screenToWorldY(mouseY) - playerY;
        double length = Math.max(1, Math.sqrt(dx * dx + dy * dy));
        projectiles.add(new Projectile(playerX, playerY, dx / length * 760, dy / length * 760, 55 + totalDamageBonus(), 12, new Color(142, 91, 255), null, BASE_PROJECTILE_LIFE, 0, false, false, 0));
        playerFacing = directionFromVector(dx, dy);
        startAttackAnimation();
        playerMana -= SPELL_MANA_COST;
    }

    private void updateProjectiles(double deltaSeconds) {
        Iterator<Projectile> iterator = projectiles.iterator();
        while (iterator.hasNext()) {
            Projectile projectile = iterator.next();
            projectile.age += deltaSeconds;
            if (projectile.boomerang && !projectile.returning && projectile.age >= projectile.maxLife * 0.5) {
                projectile.returning = true;
            }

            if (projectile.returning) {
                double returnDx = playerX - projectile.x;
                double returnDy = playerY - projectile.y;
                double returnLength = Math.max(1, Math.sqrt(returnDx * returnDx + returnDy * returnDy));
                double projectileSpeed = Math.max(1, Math.sqrt(projectile.vx * projectile.vx + projectile.vy * projectile.vy));
                projectile.vx = returnDx / returnLength * projectileSpeed;
                projectile.vy = returnDy / returnLength * projectileSpeed;
            }

            double previousHelixOffset = projectile.helixOffset;
            if (projectile.helix) {
                projectile.helixOffset = Math.sin(projectile.age * 16.0 + projectile.helixPhase) * 18.0;
            }
            double helixDelta = projectile.helixOffset - previousHelixOffset;
            double speed = Math.max(1, Math.sqrt(projectile.vx * projectile.vx + projectile.vy * projectile.vy));
            double perpendicularX = -projectile.vy / speed;
            double perpendicularY = projectile.vx / speed;

            projectile.x += projectile.vx * deltaSeconds + perpendicularX * helixDelta;
            projectile.y += projectile.vy * deltaSeconds + perpendicularY * helixDelta;
            projectile.life -= deltaSeconds;

            if (projectile.life <= 0 || projectile.x < -20 || projectile.x > worldWidth() + 20 || projectile.y < -20 || projectile.y > worldHeight() + 20) {
                iterator.remove();
            }
        }
    }

    private void updateEnemies(double deltaSeconds) {
        for (Enemy enemy : enemies) {
            double dx = playerX - enemy.x;
            double dy = playerY - enemy.y;
            double length = Math.max(1, Math.sqrt(dx * dx + dy * dy));
            enemy.x += dx / length * enemy.speed * deltaSeconds;
            enemy.y += dy / length * enemy.speed * deltaSeconds;
            enemy.moving = length > 2;
            enemy.animationTime += deltaSeconds;
            enemy.hitCooldown -= deltaSeconds;
            enemy.shootCooldown -= deltaSeconds;

            if (enemy.kind.shoots && enemy.shootCooldown <= 0 && length < enemy.kind.shootRange) {
                addEnemyProjectile(enemy.x, enemy.y, dx / length, dy / length, enemy.kind.projectileSpeed, enemy.kind.projectileDamage, enemy.kind.projectileRadius);
                enemy.shootCooldown = enemy.kind.shootCooldown + random.nextDouble() * 0.35;
            }

            if (enemy.boss) {
                updateBossAttacks(enemy, deltaSeconds, dx / length, dy / length);
            }
        }
    }

    private void updateBossAttacks(Enemy boss, double deltaSeconds, double directionX, double directionY) {
        boss.bossRadialCooldown -= deltaSeconds;
        boss.bossVolleyCooldown -= deltaSeconds;
        boss.bossSpiralCooldown -= deltaSeconds;

        if (boss.bossRadialCooldown <= 0) {
            fireBossRadialBurst(boss);
            boss.bossRadialCooldown = 4.2;
        }

        if (boss.bossVolleyCooldown <= 0) {
            fireBossAimedVolley(boss, directionX, directionY);
            boss.bossVolleyCooldown = 2.8;
        }

        if (boss.bossSpiralCooldown <= 0) {
            fireBossSpiralShot(boss);
            boss.bossSpiralCooldown = boss.bossSpiralShotsRemaining > 0 ? 0.09 : 3.5;
        }
    }

    private void fireBossRadialBurst(Enemy boss) {
        int shots = 18;
        double offset = random.nextDouble() * Math.PI * 2.0;
        for (int i = 0; i < shots; i++) {
            double angle = offset + Math.PI * 2.0 * i / shots;
            addEnemyProjectile(boss.x, boss.y, Math.cos(angle), Math.sin(angle), 235, 7, 7);
        }
    }

    private void fireBossAimedVolley(Enemy boss, double directionX, double directionY) {
        int shots = 7;
        double spread = Math.toRadians(56);
        for (int i = 0; i < shots; i++) {
            double angle = -spread / 2.0 + spread * i / (shots - 1);
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);
            addEnemyProjectile(
                    boss.x,
                    boss.y,
                    directionX * cos - directionY * sin,
                    directionX * sin + directionY * cos,
                    350,
                    8,
                    6
            );
        }
    }

    private void fireBossSpiralShot(Enemy boss) {
        if (boss.bossSpiralShotsRemaining <= 0) {
            boss.bossSpiralShotsRemaining = 30;
        }

        double angle = boss.bossSpiralAngle;
        addEnemyProjectile(boss.x, boss.y, Math.cos(angle), Math.sin(angle), 285, 6, 5);
        addEnemyProjectile(boss.x, boss.y, Math.cos(angle + Math.PI), Math.sin(angle + Math.PI), 285, 6, 5);
        boss.bossSpiralAngle += Math.toRadians(24);
        boss.bossSpiralShotsRemaining--;
    }

    private void addEnemyProjectile(double x, double y, double directionX, double directionY, double speed, int damage, int radius) {
        enemyProjectiles.add(new EnemyProjectile(
                x,
                y,
                directionX * speed,
                directionY * speed,
                damage,
                radius
        ));
    }

    private void updateEnemyProjectiles(double deltaSeconds) {
        Iterator<EnemyProjectile> iterator = enemyProjectiles.iterator();
        while (iterator.hasNext()) {
            EnemyProjectile projectile = iterator.next();
            projectile.x += projectile.vx * deltaSeconds;
            projectile.y += projectile.vy * deltaSeconds;
            projectile.life -= deltaSeconds;

            if (projectile.life <= 0 || projectile.x < -20 || projectile.x > worldWidth() + 20 || projectile.y < -20 || projectile.y > worldHeight() + 20) {
                iterator.remove();
            }
        }
    }

    private void handleCombat() {
        Iterator<Projectile> projectileIterator = projectiles.iterator();
        while (projectileIterator.hasNext()) {
            Projectile projectile = projectileIterator.next();
            boolean projectileHit = false;

            Iterator<Enemy> enemyIterator = enemies.iterator();
            while (enemyIterator.hasNext()) {
                Enemy enemy = enemyIterator.next();
                if (projectile.hitEnemies.contains(enemy)) {
                    continue;
                }
                if (distance(projectile.x, projectile.y, enemy.x, enemy.y) < enemy.radius + projectile.radius) {
                    enemy.health -= projectile.damage;
                    projectileHit = true;
                    projectile.hitEnemies.add(enemy);
                    if (enemy.health <= 0) {
                        if (enemy.boss) {
                            chestDropped = true;
                            chestX = enemy.x;
                            chestY = enemy.y;
                        }
                        enemyIterator.remove();
                    }
                    if (projectile.remainingPierces <= 0) {
                        break;
                    }
                    projectile.remainingPierces--;
                    projectileHit = false;
                }
            }

            if (projectileHit) {
                projectileIterator.remove();
            }
        }

        for (Enemy enemy : enemies) {
            if (distance(playerX, playerY, enemy.x, enemy.y) < enemy.radius + 16 && enemy.hitCooldown <= 0) {
                damagePlayer(enemy.boss ? 18 : 7);
                playerDamageTime = PLAYER_DAMAGE_DURATION;
                enemy.hitCooldown = 0.7;
            }
        }

        Iterator<EnemyProjectile> enemyProjectileIterator = enemyProjectiles.iterator();
        while (enemyProjectileIterator.hasNext()) {
            EnemyProjectile projectile = enemyProjectileIterator.next();
            if (distance(playerX, playerY, projectile.x, projectile.y) < projectile.radius + 16) {
                damagePlayer(projectile.damage);
                playerDamageTime = PLAYER_DAMAGE_DURATION;
                enemyProjectileIterator.remove();
            }
        }
    }

    private void spawnNextWave() {
        wave++;
        int count = 4 + wave * 2;
        for (int i = 0; i < count; i++) {
            EnemyKind kind = EnemyKind.regularForWave(wave, random);
            enemies.add(new Enemy(randomEdgeX(), randomEdgeY(), 28 + wave * 4, kind.speedForWave(wave), kind.radius, false, kind));
        }
    }

    private void spawnBoss() {
        bossSpawned = true;
        enemies.add(new Enemy(worldWidth() / 2.0, 90, 2000, EnemyKind.WIZARD.speedForWave(wave) * 0.82, 42, true, EnemyKind.WIZARD));
    }

    private double randomEdgeX() {
        if (random.nextBoolean()) {
            return random.nextBoolean() ? MAP_EDGE_INSET - 30 : worldWidth() - MAP_EDGE_INSET + 30;
        }
        return MAP_EDGE_INSET + random.nextDouble() * Math.max(1, worldWidth() - MAP_EDGE_INSET * 2);
    }

    private double randomEdgeY() {
        if (random.nextBoolean()) {
            return random.nextBoolean() ? MAP_EDGE_INSET - 30 : worldHeight() - MAP_EDGE_INSET + 30;
        }
        return MAP_EDGE_INSET + random.nextDouble() * Math.max(1, worldHeight() - MAP_EDGE_INSET * 2);
    }

    @Override
    public void render(Graphics2D g) {
        double cameraX = cameraX();
        double cameraY = cameraY();
        renderMap(g, cameraX, cameraY);

        if (chestDropped) {
            int screenChestX = worldToScreenX(chestX, cameraX);
            int screenChestY = worldToScreenY(chestY, cameraY);
            drawChestFrame(g, 0, screenChestX, screenChestY, CHEST_DRAW_SIZE);
        }

        for (Projectile projectile : projectiles) {
            int screenX = worldToScreenX(projectile.x, cameraX);
            int screenY = worldToScreenY(projectile.y, cameraY);
            drawProjectile(g, projectile, screenX, screenY);
        }

        for (EnemyProjectile projectile : enemyProjectiles) {
            int screenX = worldToScreenX(projectile.x, cameraX);
            int screenY = worldToScreenY(projectile.y, cameraY);
            drawEnemyProjectile(g, projectile, screenX, screenY);
        }

        for (Enemy enemy : enemies) {
            int screenX = worldToScreenX(enemy.x, cameraX);
            int screenY = worldToScreenY(enemy.y, cameraY);
            drawEnemy(g, enemy, screenX, screenY);
        }

        int playerScreenX = worldToScreenX(playerX, cameraX);
        int playerScreenY = worldToScreenY(playerY, cameraY);
        drawPlayer(g, playerScreenX, playerScreenY);

        drawStatusBars(g);
        drawGearHud(g);
        drawMinimap(g);

        g.setFont(new Font("Georgia", Font.BOLD, 20));
        g.setColor(new Color(232, 231, 202));
        String encounter = bossSpawned ? "Boss" : "Wave " + wave + "/" + MAX_WAVES;
        g.drawString(encounter + "   Enemies: " + enemies.size(), 28, 148);
        g.setFont(new Font("Georgia", Font.PLAIN, 16));
        g.drawString("WASD move. Hold left click to shoot. Space casts spell. ESC pauses.", 28, 176);

        if (paused && pauseMenuBlock != null) {
            pauseMenuBlock.render(g);
        }
    }

    @Override
    public void keyPressed(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.VK_ESCAPE) {
            togglePause();
            return;
        }

        if (paused) {
            return;
        }

        if (event.getKeyCode() == KeyEvent.VK_SPACE) {
            castSpell();
            return;
        }

        if (event.getKeyCode() >= 0 && event.getKeyCode() < keys.length) {
            keys[event.getKeyCode()] = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent event) {
        if (paused) {
            return;
        }

        if (event.getKeyCode() >= 0 && event.getKeyCode() < keys.length) {
            keys[event.getKeyCode()] = false;
        }
    }

    @Override
    public void mouseMoved(MouseEvent event) {
        if (paused) {
            return;
        }

        mouseX = event.getX();
        mouseY = event.getY();
    }

    @Override
    public void mousePressed(MouseEvent event) {
        if (paused) {
            if (pauseMenuBlock != null) {
                pauseMenuBlock.mousePressed(event);
            }
            return;
        }

        mouseX = event.getX();
        mouseY = event.getY();
        if (event.getButton() == MouseEvent.BUTTON1) {
            shooting = true;
        }
    }

    @Override
    public void mouseReleased(MouseEvent event) {
        if (paused) {
            return;
        }

        mouseX = event.getX();
        mouseY = event.getY();
        if (event.getButton() == MouseEvent.BUTTON1) {
            shooting = false;
        }
    }

    @Override
    protected void removeEvents() {
    }

    @Override
    protected void exitState() {
    }

    @Override
    public void cleanup() {
        if (pauseMenuBlock != null) {
            pauseMenuBlock.cleanup();
            pauseMenuBlock = null;
        }
        super.cleanup();
    }

    private void togglePause() {
        if (paused) {
            resumeFromPause();
            return;
        }

        paused = true;
        shooting = false;
        clearMovementInput();
        pauseMenuBlock = new PauseMenuBlock(this::resumeFromPause);
        pauseMenuBlock.start();
    }

    private void resumeFromPause() {
        paused = false;
        shooting = false;
        clearMovementInput();
        if (pauseMenuBlock != null) {
            pauseMenuBlock.cleanup();
            pauseMenuBlock = null;
        }
    }

    private void clearMovementInput() {
        for (int i = 0; i < keys.length; i++) {
            keys[i] = false;
        }
    }

    private boolean isDown(int keyCode) {
        return keyCode >= 0 && keyCode < keys.length && keys[keyCode];
    }

    private double distance(double ax, double ay, double bx, double by) {
        double dx = ax - bx;
        double dy = ay - by;
        return Math.sqrt(dx * dx + dy * dy);
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private boolean isWalkablePlayerPosition(double x, double y) {
        return isWalkableWorldPixel(x, y)
                && isWalkableWorldPixel(x - PLAYER_COLLISION_RADIUS, y)
                && isWalkableWorldPixel(x + PLAYER_COLLISION_RADIUS, y)
                && isWalkableWorldPixel(x, y - PLAYER_COLLISION_RADIUS)
                && isWalkableWorldPixel(x, y + PLAYER_COLLISION_RADIUS)
                && isWalkableWorldPixel(x - PLAYER_COLLISION_RADIUS, y - PLAYER_COLLISION_RADIUS)
                && isWalkableWorldPixel(x + PLAYER_COLLISION_RADIUS, y - PLAYER_COLLISION_RADIUS)
                && isWalkableWorldPixel(x - PLAYER_COLLISION_RADIUS, y + PLAYER_COLLISION_RADIUS)
                && isWalkableWorldPixel(x + PLAYER_COLLISION_RADIUS, y + PLAYER_COLLISION_RADIUS);
    }

    private boolean isWalkableWorldPixel(double worldX, double worldY) {
        BufferedImage collision = Game.get().getAssets().image("map.collision");
        if (collision == null) {
            return true;
        }

        int collisionX = (int) Math.floor(worldX / worldWidth() * collision.getWidth());
        int collisionY = (int) Math.floor(worldY / worldHeight() * collision.getHeight());
        if (collisionX < 0 || collisionX >= collision.getWidth() || collisionY < 0 || collisionY >= collision.getHeight()) {
            return false;
        }

        int rgb = collision.getRGB(collisionX, collisionY);
        int red = (rgb >> 16) & 0xff;
        int green = (rgb >> 8) & 0xff;
        int blue = rgb & 0xff;
        return red + green + blue >= 384;
    }

    private double worldWidth() {
        BufferedImage map = Game.get().getAssets().image("map.arena");
        if (map == null) {
            return GameWindow.WIDTH;
        }
        return Math.max(GameWindow.WIDTH, map.getWidth() * MAP_SCALE);
    }

    private double worldHeight() {
        BufferedImage map = Game.get().getAssets().image("map.arena");
        if (map == null) {
            return GameWindow.HEIGHT;
        }
        return Math.max(GameWindow.HEIGHT, map.getHeight() * MAP_SCALE);
    }

    private double cameraX() {
        return clamp(playerX - GameWindow.WIDTH / 2.0, 0, Math.max(0, worldWidth() - GameWindow.WIDTH));
    }

    private double cameraY() {
        return clamp(playerY - GameWindow.HEIGHT / 2.0, 0, Math.max(0, worldHeight() - GameWindow.HEIGHT));
    }

    private double screenToWorldX(int screenX) {
        return cameraX() + screenX;
    }

    private double screenToWorldY(int screenY) {
        return cameraY() + screenY;
    }

    private int worldToScreenX(double worldX, double cameraX) {
        return (int) Math.round(worldX - cameraX);
    }

    private int worldToScreenY(double worldY, double cameraY) {
        return (int) Math.round(worldY - cameraY);
    }

    private void renderMap(Graphics2D g, double cameraX, double cameraY) {
        BufferedImage map = Game.get().getAssets().image("map.arena");
        if (map == null) {
            renderFallbackGrid(g);
            return;
        }

        Graphics2D pixelGraphics = (Graphics2D) g.create();
        pixelGraphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        int sourceX1 = (int) Math.floor(cameraX / MAP_SCALE);
        int sourceY1 = (int) Math.floor(cameraY / MAP_SCALE);
        int sourceX2 = (int) Math.ceil((cameraX + GameWindow.WIDTH) / MAP_SCALE);
        int sourceY2 = (int) Math.ceil((cameraY + GameWindow.HEIGHT) / MAP_SCALE);
        sourceX1 = Math.max(0, Math.min(map.getWidth(), sourceX1));
        sourceY1 = Math.max(0, Math.min(map.getHeight(), sourceY1));
        sourceX2 = Math.max(sourceX1, Math.min(map.getWidth(), sourceX2));
        sourceY2 = Math.max(sourceY1, Math.min(map.getHeight(), sourceY2));
        pixelGraphics.drawImage(map, 0, 0, GameWindow.WIDTH, GameWindow.HEIGHT, sourceX1, sourceY1, sourceX2, sourceY2, null);
        pixelGraphics.dispose();
    }

    private void renderFallbackGrid(Graphics2D g) {
        g.setColor(new Color(12, 20, 25));
        g.fillRect(0, 0, GameWindow.WIDTH, GameWindow.HEIGHT);
        g.setColor(new Color(20, 35, 39));
        for (int x = 0; x < GameWindow.WIDTH; x += 64) {
            g.drawLine(x, 0, x, GameWindow.HEIGHT);
        }
        for (int y = 0; y < GameWindow.HEIGHT; y += 64) {
            g.drawLine(0, y, GameWindow.WIDTH, y);
        }
    }

    private void drawStatusBars(Graphics2D g) {
        int x = 28;
        int y = 28;
        int width = Math.max(276, GameWindow.WIDTH / 5);
        int height = Math.max(24, GameWindow.HEIGHT / 34);
        double maxHealth = effectiveMaxHealth();
        double maxMana = effectiveMaxMana();
        drawImageBar(g, "ui.health-bar", x, y, width, height, playerHealth / maxHealth, new Color(174, 54, 61), "HEALTH " + (int) Math.ceil(playerHealth) + "/" + (int) maxHealth);
        drawImageBar(g, "ui.mana-bar", x, y + height + 8, width, height, playerMana / maxMana, new Color(65, 126, 224), "MANA " + (int) Math.floor(playerMana) + "/" + (int) maxMana);
    }

    private void drawImageBar(Graphics2D g, String assetAlias, int x, int y, int width, int height, double value, Color fallbackFill, String label) {
        BufferedImage image = Game.get().getAssets().image(assetAlias);
        double clampedValue = clamp(value, 0, 1);
        int filledWidth = (int) Math.round(width * clampedValue);

        g.setColor(new Color(9, 12, 18, 210));
        g.fillRoundRect(x, y, width, height, 8, 8);

        if (image != null && filledWidth > 0) {
            Graphics2D pixelGraphics = (Graphics2D) g.create();
            pixelGraphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            int sourceWidth = Math.max(1, (int) Math.round(image.getWidth() * clampedValue));
            pixelGraphics.drawImage(image, x, y, x + filledWidth, y + height, 0, 0, sourceWidth, image.getHeight(), null);
            pixelGraphics.dispose();
        } else if (filledWidth > 0) {
            g.setColor(fallbackFill);
            g.fillRoundRect(x, y, filledWidth, height, 8, 8);
        }

        g.setColor(new Color(232, 231, 202));
        g.setFont(Ui.font(Font.BOLD, Math.max(12, height - 4)));
        g.drawString(label, x + 10, y + height - 5);
    }

    private void drawEnemy(Graphics2D g, Enemy enemy, int screenX, int screenY) {
        BufferedImage sheet = Game.get().getAssets().image(enemy.kind.assetAlias);
        if (sheet == null) {
            g.setColor(enemy.boss ? new Color(159, 55, 57) : new Color(64, 151, 91));
            g.fillOval(screenX - enemy.radius, screenY - enemy.radius, enemy.radius * 2, enemy.radius * 2);
            drawEnemyHealthBar(g, enemy, screenX, screenY + enemy.radius + 8);
            return;
        }

        int frameCount = enemy.moving ? enemy.kind.walkFrames : enemy.kind.idleFrames;
        int frame = (int) (enemy.animationTime * enemy.kind.framesPerSecond) % frameCount;
        int sourceX = ENEMY_SHEET_SPRITE_X + frame * ENEMY_SHEET_FRAME_SIZE;
        int sourceY = enemy.moving ? ENEMY_SHEET_WALK_Y : ENEMY_SHEET_IDLE_Y;
        int drawSize = enemy.boss ? (int) (enemy.radius * 7.5) : enemy.radius * 10;
        int drawX = screenX - drawSize / 2;
        int drawY = screenY - drawSize / 2;

        Graphics2D pixelGraphics = (Graphics2D) g.create();
        pixelGraphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        pixelGraphics.drawImage(
                sheet,
                drawX,
                drawY,
                drawX + drawSize,
                drawY + drawSize,
                sourceX,
                sourceY,
                sourceX + ENEMY_SHEET_FRAME_SIZE,
                sourceY + ENEMY_SHEET_FRAME_SIZE,
                null
        );
        pixelGraphics.dispose();
        drawEnemyHealthBar(g, enemy, screenX, screenY + drawSize / 2 - Math.max(10, drawSize / 8));
    }

    private void drawEnemyHealthBar(Graphics2D g, Enemy enemy, int centerX, int y) {
        int width = enemy.boss ? 86 : 34;
        int height = enemy.boss ? 7 : 4;
        int x = centerX - width / 2;
        double healthPercent = clamp(enemy.health / (double) enemy.maxHealth, 0, 1);
        int fillWidth = (int) Math.round(width * healthPercent);

        g.setColor(new Color(18, 14, 18, 210));
        g.fillRect(x, y, width, height);
        g.setColor(enemy.boss ? new Color(230, 66, 86) : new Color(205, 48, 58));
        g.fillRect(x, y, fillWidth, height);
        g.setColor(new Color(55, 28, 32, 210));
        g.drawRect(x, y, width, height);
    }

    private void drawProjectile(Graphics2D g, Projectile projectile, int screenX, int screenY) {
        BufferedImage image = projectile.assetAlias == null ? null : Game.get().getAssets().image(projectile.assetAlias);
        if (image == null) {
            g.setColor(projectile.color);
            g.fillOval(screenX - projectile.radius, screenY - projectile.radius, projectile.radius * 2, projectile.radius * 2);
            return;
        }

        Graphics2D projectileGraphics = (Graphics2D) g.create();
        projectileGraphics.rotate(Math.atan2(projectile.vy, projectile.vx), screenX, screenY);
        projectileGraphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        projectileGraphics.drawImage(
                image,
                screenX - PROJECTILE_DRAW_SIZE / 2,
                screenY - PROJECTILE_DRAW_SIZE / 2,
                PROJECTILE_DRAW_SIZE,
                PROJECTILE_DRAW_SIZE,
                null
        );
        projectileGraphics.dispose();
    }

    private void drawChestFrame(Graphics2D g, int frame, int centerX, int centerY, int size) {
        BufferedImage image = Game.get().getAssets().image("chest.opening");
        if (image != null) {
            int frameCount = Math.max(1, image.getWidth() / CHEST_FRAME_SIZE);
            int sourceX = Math.min(frame, frameCount - 1) * CHEST_FRAME_SIZE;
            Graphics2D pixelGraphics = (Graphics2D) g.create();
            pixelGraphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            pixelGraphics.drawImage(
                    image,
                    centerX - size / 2,
                    centerY - size / 2,
                    centerX + size / 2,
                    centerY + size / 2,
                    sourceX,
                    0,
                    sourceX + CHEST_FRAME_SIZE,
                    CHEST_FRAME_SIZE,
                    null
            );
            pixelGraphics.dispose();
            return;
        }

        g.setColor(new Color(184, 124, 42));
        g.fillRoundRect(centerX - 18, centerY - 14, 36, 28, 6, 6);
        g.setColor(new Color(255, 220, 99));
        g.fillRect(centerX - 4, centerY - 14, 8, 28);
    }

    private void drawEnemyProjectile(Graphics2D g, EnemyProjectile projectile, int screenX, int screenY) {
        g.setColor(new Color(209, 74, 93));
        g.fillOval(screenX - projectile.radius, screenY - projectile.radius, projectile.radius * 2, projectile.radius * 2);
        g.setColor(new Color(255, 180, 130));
        g.drawOval(screenX - projectile.radius, screenY - projectile.radius, projectile.radius * 2, projectile.radius * 2);
    }

    private void drawPlayer(Graphics2D g, int screenX, int screenY) {
        BufferedImage image = Game.get().getAssets().image(playerImageAlias());
        if (image == null) {
            g.setColor(new Color(202, 219, 255));
            g.fillOval(screenX - 16, screenY - 16, 32, 32);
            g.setColor(new Color(117, 78, 180));
            g.fillRect(screenX - 5, screenY - 28, 10, 28);
            return;
        }

        Ui.drawPixelImageCenteredTrimmed(g, image, screenX, screenY - 8, PLAYER_DRAW_SIZE);
    }

    private String playerImageAlias() {
        String cardinalFacing = cardinalDirection(playerFacing);
        if (playerDamageTime > 0) {
            double elapsed = PLAYER_DAMAGE_DURATION - playerDamageTime;
            int frame = Math.min(PLAYER_DAMAGE_FRAMES - 1, (int) (elapsed * PLAYER_DAMAGE_FRAMES_PER_SECOND));
            return "player.damage." + cardinalFacing + "." + frame;
        }

        if (playerAttackTime > 0) {
            int frame = (int) (playerAttackAnimationTime * PLAYER_ATTACK_FRAMES_PER_SECOND) % PLAYER_ATTACK_FRAMES;
            return "player.attack." + cardinalFacing + "." + frame;
        }

        if (playerMoving) {
            int frame = (int) (playerAnimationTime * PLAYER_WALK_FRAMES_PER_SECOND) % PLAYER_WALK_FRAMES;
            return "player.walk." + cardinalFacing + "." + frame;
        }

        return "player.idle." + playerFacing;
    }

    private String directionFromVector(double dx, double dy) {
        return Math.abs(dx) > Math.abs(dy) ? (dx > 0 ? "east" : "west") : (dy > 0 ? "south" : "north");
    }

    private String cardinalDirection(String direction) {
        if (direction.contains("east")) {
            return "east";
        }
        if (direction.contains("west")) {
            return "west";
        }
        return direction.equals("north") ? "north" : "south";
    }

    private void drawGearHud(Graphics2D g) {
        List<ItemDefinition> items = equippedItems != null ? equippedItems : equippedItemsFromModel();
        int iconSize = Math.max(42, GameWindow.HEIGHT / 16);
        int slotWidth = Math.max(132, GameWindow.WIDTH / 11);
        int gap = Math.max(10, GameWindow.WIDTH / 120);
        int height = iconSize + Math.max(44, GameWindow.HEIGHT / 24);
        int width = slotWidth * items.size() + gap * (items.size() - 1);
        int x = GameWindow.WIDTH - width - 28;
        int y = 28;

        for (int i = 0; i < items.size(); i++) {
            drawGearSlot(g, items.get(i), x + i * (slotWidth + gap), y, slotWidth, height, iconSize);
        }
    }

    private void drawGearSlot(Graphics2D g, ItemDefinition item, int x, int y, int width, int height, int iconSize) {
        g.setColor(new Color(14, 20, 30, 215));
        g.fillRoundRect(x, y, width, height, 12, 12);
        g.setColor(new Color(82, 120, 125));
        g.drawRoundRect(x, y, width, height, 12, 12);

        int iconX = x + width / 2 - iconSize / 2;
        int iconBaseline = y + iconSize + 10;
        BufferedImage image = Game.get().getAssets().image(item.getAlias());
        if (image != null) {
            Ui.drawPixelImageCenteredTrimmed(g, image, iconX + iconSize / 2, iconBaseline - iconSize / 2, iconSize);
        } else {
            g.setColor(new Color(80, 95, 110));
            g.fillRect(iconX, iconBaseline - iconSize, iconSize, iconSize);
        }

        g.setColor(new Color(232, 231, 202));
        g.setFont(Ui.font(Font.PLAIN, Math.max(12, GameWindow.HEIGHT / 55)));
        Ui.drawCentered(g, item.getName(), x + width / 2, y + height - 14);
    }

    private void drawMinimap(Graphics2D g) {
        int size = Math.min(MINIMAP_SIZE, Math.max(112, GameWindow.HEIGHT / 5));
        int radius = size / 2;
        int x = GameWindow.WIDTH - size - 28;
        int y = GameWindow.HEIGHT - size - 28;
        int centerX = x + radius;
        int centerY = y + radius;

        Graphics2D radarGraphics = (Graphics2D) g.create();
        radarGraphics.setColor(new Color(8, 13, 19, 210));
        radarGraphics.fillRoundRect(x, y, size, size, 8, 8);
        radarGraphics.setColor(new Color(83, 134, 137, 190));
        radarGraphics.drawRoundRect(x, y, size, size, 8, 8);

        for (Enemy enemy : enemies) {
            double dx = enemy.x - playerX;
            double dy = enemy.y - playerY;
            int markerX = centerX + (int) Math.round(clamp(dx / MINIMAP_WORLD_RADIUS, -1, 1) * (radius - 10));
            int markerY = centerY + (int) Math.round(clamp(dy / MINIMAP_WORLD_RADIUS, -1, 1) * (radius - 10));
            radarGraphics.setColor(enemy.boss ? new Color(244, 184, 75) : new Color(220, 78, 87));
            int markerSize = enemy.boss ? 8 : 5;
            radarGraphics.fillOval(markerX - markerSize / 2, markerY - markerSize / 2, markerSize, markerSize);
        }

        radarGraphics.setColor(new Color(107, 218, 255));
        radarGraphics.fillOval(centerX - 4, centerY - 4, 8, 8);
        radarGraphics.setColor(new Color(232, 231, 202));
        radarGraphics.drawOval(centerX - 5, centerY - 5, 10, 10);
        radarGraphics.dispose();
    }

    private List<ItemDefinition> equippedItemsFromModel() {
        return List.of(
                itemByName(ItemType.STAFF, models.game().getStaff()),
                itemByName(ItemType.SPELL, models.game().getSpell()),
                itemByName(ItemType.ROBE, models.game().getRobe()),
                itemByName(ItemType.RING, models.game().getRing())
        );
    }

    private ItemDefinition itemByName(ItemType type, String name) {
        List<ItemDefinition> items = ItemConfig.itemsByType(type);
        for (ItemDefinition item : items) {
            if (item.getName().equals(name)) {
                return item;
            }
        }
        return items.get(0);
    }

    private double effectiveMaxHealth() {
        return MAX_HEALTH + totalMaxHealthBonus();
    }

    private double effectiveMaxMana() {
        return MAX_MANA + totalLuckBonus() * MANA_PER_LUCK;
    }

    private double effectivePlayerSpeed() {
        return PLAYER_SPEED + totalMovementSpeedBonus() * SPEED_PER_MOVEMENT;
    }

    private double effectiveDexterity() {
        return PLAYER_DEXTERITY + totalAttackSpeedBonus() * 0.35;
    }

    private void damagePlayer(double damage) {
        playerHealth -= Math.max(1, damage - totalDefenseBonus() * 0.45);
    }

    private int totalDamageBonus() {
        int total = 0;
        for (ItemDefinition item : equippedItems()) {
            total += item.getDamage();
        }
        return total;
    }

    private int totalAttackSpeedBonus() {
        int total = 0;
        for (ItemDefinition item : equippedItems()) {
            total += item.getAttackSpeed();
        }
        return total;
    }

    private int totalDefenseBonus() {
        int total = 0;
        for (ItemDefinition item : equippedItems()) {
            total += item.getDefense();
        }
        return total;
    }

    private int totalMaxHealthBonus() {
        int total = 0;
        for (ItemDefinition item : equippedItems()) {
            total += item.getMaxHealth();
        }
        return total;
    }

    private int totalMovementSpeedBonus() {
        int total = 0;
        for (ItemDefinition item : equippedItems()) {
            total += item.getMovementSpeed();
        }
        return total;
    }

    private int totalLuckBonus() {
        int total = 0;
        for (ItemDefinition item : equippedItems()) {
            total += item.getLuck();
        }
        return total;
    }

    private List<ItemDefinition> equippedItems() {
        return equippedItems != null ? equippedItems : equippedItemsFromModel();
    }

    private String currentStaffProjectileAlias() {
        ItemDefinition staff = equippedItems != null ? equippedItems.get(0) : itemByName(ItemType.STAFF, models.game().getStaff());
        return staff.getAlias().replace("gear_staves_", "gear_projectiles_");
    }

    private StaffWeapon currentStaffWeapon() {
        ItemDefinition staff = equippedItems != null ? equippedItems.get(0) : itemByName(ItemType.STAFF, models.game().getStaff());
        return StaffWeapon.forStaff(staff.getName());
    }

    private enum StaffWeapon {
        APPRENTICE("Apprentice Staff", 1, 0.70, 0.70, 0.65, 1.00, 0, 0, 0, false, false),
        ARCANE("Arcane Staff", 2, 1.22, 0.72, 0.70, 1.00, 0, 0, 18, false, false),
        WANDERER("Wanderer's Staff", 1, 0.86, 0.82, 1.12, 0.90, 0, 0, 0, false, false),
        ASHFIRE("Ashfire Rod", 6, 0.95, 0.32, 0.62, 0.82, 0, Math.PI, 0, false, false),
        VERDANT("Verdant Staff", 1, 1.00, 1.00, 1.00, 1.00, 0, 0, 0, false, false),
        DAWNSPIRE("Dawnspire Wand", 1, 1.00, 0.72, 0.90, 1.00, 2, 0, 0, true, false),
        FROSTWOVEN("Frostwoven Staff", 1, 1.00, 1.25, 1.28, 0.95, 0, 0, 0, false, false),
        GRAVE_KEEPER("Grave Keeper", 2, 1.00, 1.00, 1.00, 1.00, 0, 0, 18, false, false),
        NIGHTVEIL("Nightveil Staff", 1, 1.00, 1.00, 1.32, 0.92, 0, Math.toRadians(12), 0, false, false),
        PHOENIX("Phoenix Soul", 1, 0.88, 1.28, 1.38, 0.70, 2, 0, 0, false, false),
        STORMCALLER("Stormcaller", 2, 1.00, 1.00, 1.00, 1.00, 0, 0, 0, false, true),
        THORNBINDER("Thornbinder", 1, 0.64, 0.72, 1.75, 0.78, 2, 0, 0, false, false),
        VOIDCALLER("Voidcaller", 1, 0.58, 0.34, 3.80, 0.72, 3, 0, 0, false, false),
        SUNFORGED("Sunforged Staff", 1, 1.00, 1.00, 1.00, 1.00, 0, 0, 0, false, true),
        EMBER("Ember Branch", 1, 1.00, 0.78, 1.00, 1.00, 0, 0, 0, false, false),
        BLOODREAPER("Bloodreaper Staff", 1, 1.00, 1.00, 0.88, 1.18, 0, 0, 0, false, false);

        private final String staffName;
        private final int projectileCount;
        private final double speed;
        private final double life;
        private final int damage;
        private final double fireRateMultiplier;
        private final int pierce;
        private final double spreadAngle;
        private final double parallelSpacing;
        private final boolean boomerang;
        private final boolean helix;

        StaffWeapon(
                String staffName,
                int projectileCount,
                double speedMultiplier,
                double lifeMultiplier,
                double damageMultiplier,
                double fireRateMultiplier,
                int pierce,
                double spreadAngle,
                double parallelSpacing,
                boolean boomerang,
                boolean helix
        ) {
            this.staffName = staffName;
            this.projectileCount = projectileCount;
            this.speed = BASE_PROJECTILE_SPEED * speedMultiplier;
            this.life = BASE_PROJECTILE_LIFE * lifeMultiplier;
            this.damage = Math.max(1, (int) Math.round(PLAYER_DAMAGE * damageMultiplier));
            this.fireRateMultiplier = fireRateMultiplier;
            this.pierce = pierce;
            this.spreadAngle = spreadAngle;
            this.parallelSpacing = parallelSpacing;
            this.boomerang = boomerang;
            this.helix = helix;
        }

        private static StaffWeapon forStaff(String staffName) {
            for (StaffWeapon weapon : values()) {
                if (weapon.staffName.equals(staffName)) {
                    return weapon;
                }
            }
            return VERDANT;
        }
    }

    private static final class Enemy {
        private double x;
        private double y;
        private int health;
        private final int maxHealth;
        private final double speed;
        private final int radius;
        private final boolean boss;
        private final EnemyKind kind;
        private double hitCooldown;
        private double shootCooldown;
        private double bossRadialCooldown;
        private double bossVolleyCooldown;
        private double bossSpiralCooldown;
        private double bossSpiralAngle;
        private int bossSpiralShotsRemaining;
        private double animationTime;
        private boolean moving;

        private Enemy(double x, double y, int health, double speed, int radius, boolean boss, EnemyKind kind) {
            this.x = x;
            this.y = y;
            this.health = health;
            this.maxHealth = health;
            this.speed = speed;
            this.radius = radius;
            this.boss = boss;
            this.kind = kind;
            this.shootCooldown = kind.shootCooldown;
            this.bossRadialCooldown = 1.4;
            this.bossVolleyCooldown = 0.8;
            this.bossSpiralCooldown = 2.2;
            this.bossSpiralAngle = 0;
        }
    }

    private enum EnemyKind {
        BLUE_SLIME("enemy.blue-slime", 14, 4, 5, 7, 116, false, 0, 0, 0, 0, 0),
        GREEN_SLIME("enemy.green-slime", 14, 4, 5, 7, 96, false, 0, 0, 0, 0, 0),
        GHOST("enemy.ghost", 15, 4, 4, 7, 108, true, 1.7, 560, 260, 6, 7),
        SKELETON("enemy.skeleton", 16, 4, 4, 7, 104, true, 1.9, 620, 250, 6, 8),
        SKELETON_SWORDSMAN("enemy.skeleton-swordsman", 17, 4, 4, 7, 118, true, 2.1, 520, 240, 6, 8),
        WIZARD("enemy.wizard", 18, 4, 4, 7, 100, true, 1.35, 760, 310, 8, 10);

        private final String assetAlias;
        private final int radius;
        private final int walkFrames;
        private final int idleFrames;
        private final int framesPerSecond;
        private final double baseSpeed;
        private final boolean shoots;
        private final double shootCooldown;
        private final double shootRange;
        private final double projectileSpeed;
        private final int projectileRadius;
        private final int projectileDamage;

        EnemyKind(
                String assetAlias,
                int radius,
                int walkFrames,
                int idleFrames,
                int framesPerSecond,
                double baseSpeed,
                boolean shoots,
                double shootCooldown,
                double shootRange,
                double projectileSpeed,
                int projectileRadius,
                int projectileDamage
        ) {
            this.assetAlias = assetAlias;
            this.radius = radius;
            this.walkFrames = walkFrames;
            this.idleFrames = idleFrames;
            this.framesPerSecond = framesPerSecond;
            this.baseSpeed = baseSpeed;
            this.shoots = shoots;
            this.shootCooldown = shootCooldown;
            this.shootRange = shootRange;
            this.projectileSpeed = projectileSpeed;
            this.projectileRadius = projectileRadius;
            this.projectileDamage = projectileDamage;
        }

        private static EnemyKind regularForWave(int wave, Random random) {
            EnemyKind[] early = {BLUE_SLIME, GREEN_SLIME, GHOST};
            EnemyKind[] late = {BLUE_SLIME, GREEN_SLIME, GHOST, SKELETON, SKELETON_SWORDSMAN};
            EnemyKind[] options = wave < 4 ? early : late;
            return options[random.nextInt(options.length)];
        }

        private double speedForWave(int wave) {
            return baseSpeed + wave * 7;
        }
    }

    private static final class EnemyProjectile {
        private double x;
        private double y;
        private final double vx;
        private final double vy;
        private final int damage;
        private final int radius;
        private double life = 2.8;

        private EnemyProjectile(double x, double y, double vx, double vy, int damage, int radius) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.damage = damage;
            this.radius = radius;
        }
    }

    private static final class Projectile {
        private double x;
        private double y;
        private double vx;
        private double vy;
        private final int damage;
        private final int radius;
        private final Color color;
        private final String assetAlias;
        private final double maxLife;
        private final boolean boomerang;
        private final boolean helix;
        private final double helixPhase;
        private final Set<Enemy> hitEnemies = new HashSet<>();
        private int remainingPierces;
        private double life;
        private double age;
        private double helixOffset;
        private boolean returning;

        private Projectile(
                double x,
                double y,
                double vx,
                double vy,
                int damage,
                int radius,
                Color color,
                String assetAlias,
                double life,
                int pierce,
                boolean boomerang,
                boolean helix,
                double helixPhase
        ) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.damage = damage;
            this.radius = radius;
            this.color = color;
            this.assetAlias = assetAlias;
            this.maxLife = life;
            this.life = life;
            this.remainingPierces = pierce;
            this.boomerang = boomerang;
            this.helix = helix;
            this.helixPhase = helixPhase;
        }
    }
}
