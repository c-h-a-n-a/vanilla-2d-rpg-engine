package game.state;

import game.config.GameConfig;
import game.entity.Entity;
import game.entity.Monster;
import game.entity.MonsterType;
import game.entity.Npc;
import game.entity.Player;
import game.input.KeyboardInput;
import game.tile.TileManager;
import game.world.Camera;
import game.world.GameMap;
import game.world.Maps;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * All game logic: owns the map, the entities, the camera and the dialogue.
 * It never draws anything (see the render package).
 */
public class Game {

    private final KeyboardInput input;
    private final TileManager tileManager = new TileManager();
    private final Camera camera =
            new Camera(GameConfig.SCREEN_WIDTH, GameConfig.SCREEN_HEIGHT);
    private final DialogueManager dialogue = new DialogueManager();

    private final Player player;
    private final List<Npc> npcs = new ArrayList<>();
    private final List<Monster> monsters = new ArrayList<>();

    /** Every entity (player included). Also the list entities collide against. */
    private final List<Entity> entities = new ArrayList<>();
    private final List<Entity> entitiesView = Collections.unmodifiableList(entities);

    private GameMap currentMap;
    private boolean debugEnabled;

    public Game(KeyboardInput input) {
        this.input = input;

        currentMap = Maps.overworld(tileManager);

        // Positions are WORLD coordinates (top-left corner of the sprite).
        player = new Player(400, 400, 64, 64);

        spawnEntities();
        registerEntities();

        updateCamera();
    }

    private void spawnEntities() {
        Npc elder = new Npc(
                600, 400, 64, 64,
                "Village Elder",
                new String[]{
                        "Welcome, traveler! I am the elder of this village.",
                        "The forest ahead is dangerous. Many monsters live there.",
                        "Be careful, and may your journey be blessed!"
                },
                "/sprites/npc/village_elder_portrait.png"
        );

        // Patrols between two world positions, stopping short of the monster.
        elder.setPatrolPoints(new double[][]{
                {600, 400},
                {720, 400}
        });
        elder.setSpeed(80.0);

        npcs.add(elder);

        monsters.add(new Monster(MonsterType.GENERIC, 800, 400));
    }

    private void registerEntities() {
        entities.add(player);
        entities.addAll(npcs);
        entities.addAll(monsters);

        // Every entity collides with every other one (it skips itself).
        for (Entity entity : entities) {
            entity.setObstacles(entitiesView);
        }
    }

    // ------------------------------------------------------------------
    // Update
    // ------------------------------------------------------------------

    public void update(double deltaTime) {
        if (input.consumeJustPressed(KeyEvent.VK_F3)) {
            debugEnabled = !debugEnabled;
        }

        handleInteraction();

        boolean talking = dialogue.isActive();

        updatePlayer(deltaTime, talking);

        for (Npc npc : npcs) {
            npc.setPaused(talking);
            npc.update(deltaTime, currentMap);
        }

        if (!talking) {
            for (Monster monster : monsters) {
                monster.update(deltaTime, currentMap);
            }
        }

        for (Entity entity : entities) {
            entity.keepInside(currentMap);
        }

        updateCamera();
    }

    private void updatePlayer(double deltaTime, boolean talking) {
        if (talking) {
            player.setMoveIntent(0, 0);
        } else {
            player.setMoveIntent(
                    axis(KeyEvent.VK_A, KeyEvent.VK_LEFT, KeyEvent.VK_D, KeyEvent.VK_RIGHT),
                    axis(KeyEvent.VK_W, KeyEvent.VK_UP, KeyEvent.VK_S, KeyEvent.VK_DOWN)
            );
        }

        player.update(deltaTime, currentMap);
    }

    /** Converts a pair of opposing keys into -1, 0 or 1. */
    private int axis(int negativeKey, int negativeAlt, int positiveKey, int positiveAlt) {
        int value = 0;

        if (input.isAnyPressed(negativeKey, negativeAlt)) {
            value--;
        }

        if (input.isAnyPressed(positiveKey, positiveAlt)) {
            value++;
        }

        return value;
    }

    private void handleInteraction() {
        if (!input.consumeJustPressed(KeyEvent.VK_E)) {
            return;
        }

        if (dialogue.isActive()) {
            dialogue.advance();
            return;
        }

        Npc nearby = findNearbyNpc();

        if (nearby != null) {
            dialogue.start(nearby);
        }
    }

    /** Nearest NPC within range that the player is facing, or null. */
    private Npc findNearbyNpc() {
        double playerCenterX = player.getX() + player.getWidth() / 2.0;
        double playerCenterY = player.getY() + player.getHeight() / 2.0;

        double maxDistanceSquared =
                GameConfig.INTERACTION_RANGE * GameConfig.INTERACTION_RANGE;

        Npc nearestNpc = null;
        double nearestDistanceSquared = Double.MAX_VALUE;

        for (Npc npc : npcs) {
            double dx = npc.getX() + npc.getWidth() / 2.0 - playerCenterX;
            double dy = npc.getY() + npc.getHeight() / 2.0 - playerCenterY;

            double distanceSquared = dx * dx + dy * dy;

            if (distanceSquared > maxDistanceSquared) {
                continue;
            }

            boolean inFront = switch (player.getFacingDirection()) {
                case UP -> dy < 0 && Math.abs(dy) >= Math.abs(dx);
                case DOWN -> dy > 0 && Math.abs(dy) >= Math.abs(dx);
                case LEFT -> dx < 0 && Math.abs(dx) >= Math.abs(dy);
                case RIGHT -> dx > 0 && Math.abs(dx) >= Math.abs(dy);
            };

            if (inFront && distanceSquared < nearestDistanceSquared) {
                nearestNpc = npc;
                nearestDistanceSquared = distanceSquared;
            }
        }

        return nearestNpc;
    }

    private void updateCamera() {
        camera.follow(
                player.getX() + player.getWidth() / 2.0,
                player.getY() + player.getHeight() / 2.0,
                currentMap.getWorldWidth(),
                currentMap.getWorldHeight()
        );
    }

    // ------------------------------------------------------------------
    // Maps
    // ------------------------------------------------------------------

    /**
     * Switches to another map and puts the player at the given WORLD position.
     * Example: game.changeMap(Maps.dungeon(tileManager), 128, 128)
     */
    public void changeMap(GameMap newMap, double playerX, double playerY) {
        dialogue.end();
        currentMap = newMap;
        player.teleportTo(playerX, playerY);
        updateCamera();
    }

    public TileManager getTileManager() {
        return tileManager;
    }

    // ------------------------------------------------------------------
    // Read-only access for the renderer
    // ------------------------------------------------------------------

    public GameMap getCurrentMap() {
        return currentMap;
    }

    public Camera getCamera() {
        return camera;
    }

    public Player getPlayer() {
        return player;
    }

    public List<Entity> getEntities() {
        return entitiesView;
    }

    public DialogueManager getDialogue() {
        return dialogue;
    }

    public boolean isDebugEnabled() {
        return debugEnabled;
    }
}
