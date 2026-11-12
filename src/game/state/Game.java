
package game.state;

import game.battle.BattleFactory;
import game.battle.BattleMonster;
import game.battle.BattleState;
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
    private final Camera camera = new Camera(GameConfig.SCREEN_WIDTH, GameConfig.SCREEN_HEIGHT);
    private final DialogueManager dialogue = new DialogueManager();

    private final Player player;
    private final List<Npc> npcs = new ArrayList<>();
    private final List<Monster> monsters = new ArrayList<>();

    /** Every entity (player included). Also the list entities collide against. */
    private final List<Entity> entities = new ArrayList<>();
    private final List<Entity> entitiesView = Collections.unmodifiableList(entities);

    private GameMap currentMap;
    private boolean debugEnabled;

    private GameMode gameMode = GameMode.EXPLORATION;
    private Monster encounteredMonster;
    private static final double ENCOUNTER_DISTANCE = 56.0;
    private static final double ENCOUNTER_RESET_DISTANCE = 120.0;

    // Prevents the same monster from immediately starting another battle.
    private Monster encounterLockoutMonster;

    private final BattleMonster playerBattleMonster = BattleFactory.createPlayerMonster();
    private BattleState battleState;
    private int selectedMoveIndex;

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
                new String[] {
                        "Welcome, traveler! I am the elder of this village.",
                        "The forest ahead is dangerous. Many monsters live there.",
                        "Be careful, and may your journey be blessed!"
                },
                "/sprites/npc/village_elder_portrait.png");

        // Patrols between two world positions, stopping short of the monster.
        elder.setPatrolPoints(new double[][] {
                { 600, 400 },
                { 720, 400 }
        });
        elder.setSpeed(80.0);

        npcs.add(elder);

        Monster wolf = new Monster(MonsterType.WOLF, 800, 400);
        monsters.add(wolf);
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

        if (gameMode == GameMode.BATTLE) {
            updateBattle();
            return;
        }

        if (input.consumeJustPressed(KeyEvent.VK_F3)) {
            debugEnabled = !debugEnabled;
        }

        updateEncounterLockout();
        handleInteraction();

        // Interaction may have started a battle.
        if (gameMode == GameMode.BATTLE) {
            return;
        }

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

            checkAutomaticEncounter();
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
                    axis(KeyEvent.VK_W, KeyEvent.VK_UP, KeyEvent.VK_S, KeyEvent.VK_DOWN));
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

        // Give NPC interaction priority.
        Npc nearbyNpc = findNearbyNpc();

        if (nearbyNpc != null) {
            dialogue.start(nearbyNpc);
            return;
        }

        // If no NPC is nearby, check for a monster.
        // Manual interaction can target a monster even during encounter lockout.
        Monster nearbyMonster = findNearbyMonster(true);

        if (nearbyMonster != null) {
            startBattle(nearbyMonster);
        }
    }

    /** Nearest NPC within range that the player is facing, or null. */
    private Npc findNearbyNpc() {
        double playerCenterX = player.getX() + player.getWidth() / 2.0;
        double playerCenterY = player.getY() + player.getHeight() / 2.0;

        double maxDistanceSquared = GameConfig.INTERACTION_RANGE * GameConfig.INTERACTION_RANGE;

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

    /**
     * Finds the nearest monster within interaction range and in front of the
     * player.
     *
     * @param ignoreLockout true allows manual interaction with the locked-out
     *                      monster;
     *                      false excludes it from interaction.
     */
    private Monster findNearbyMonster(boolean ignoreLockout) {
        double playerCenterX = player.getX() + player.getWidth() / 2.0;
        double playerCenterY = player.getY() + player.getHeight() / 2.0;

        double maxDistanceSquared = GameConfig.INTERACTION_RANGE * GameConfig.INTERACTION_RANGE;

        Monster nearestMonster = null;
        double nearestDistanceSquared = Double.MAX_VALUE;

        for (Monster monster : monsters) {
            if (!ignoreLockout && monster == encounterLockoutMonster) {
                continue;
            }

            double dx = monster.getX() + monster.getWidth() / 2.0
                    - playerCenterX;

            double dy = monster.getY() + monster.getHeight() / 2.0
                    - playerCenterY;

            double distanceSquared = dx * dx + dy * dy;

            if (distanceSquared > maxDistanceSquared) {
                continue;
            }

            if (isInFront(dx, dy) && distanceSquared < nearestDistanceSquared) {
                nearestMonster = monster;
                nearestDistanceSquared = distanceSquared;
            }
        }

        return nearestMonster;
    }

    /**
     * Checks whether a target is in the direction the player is facing.
     */
    private boolean isInFront(double dx, double dy) {
        return switch (player.getFacingDirection()) {
            case UP -> dy < 0 && Math.abs(dy) >= Math.abs(dx);
            case DOWN -> dy > 0 && Math.abs(dy) >= Math.abs(dx);
            case LEFT -> dx < 0 && Math.abs(dx) >= Math.abs(dy);
            case RIGHT -> dx > 0 && Math.abs(dx) >= Math.abs(dy);
        };
    }

    private void checkAutomaticEncounter() {
        double playerCenterX = player.getX() + player.getWidth() / 2.0;
        double playerCenterY = player.getY() + player.getHeight() / 2.0;

        double encounterDistanceSquared = ENCOUNTER_DISTANCE * ENCOUNTER_DISTANCE;

        for (Monster monster : monsters) {
            if (monster == encounterLockoutMonster) {
                continue;
            }

            double monsterCenterX = monster.getX() + monster.getWidth() / 2.0;

            double monsterCenterY = monster.getY() + monster.getHeight() / 2.0;

            double dx = monsterCenterX - playerCenterX;
            double dy = monsterCenterY - playerCenterY;

            double distanceSquared = dx * dx + dy * dy;

            if (distanceSquared <= encounterDistanceSquared) {
                startBattle(monster);
                return;
            }
        }
    }

    // -----------------------------------
    // battle related
    // ---------------------------------
    /**
     * Handles input while a battle is running: move selection, confirming a move,
     * and leaving once the battle is over.
     */
    private void updateBattle() {
        // Temporary escape hatch for testing; the real Run command comes later.
        if (input.consumeJustPressed(KeyEvent.VK_ESCAPE)) {
            endBattle(false);
            return;
        }

        // Non-short-circuit '|' so every confirm key is consumed on the same frame.
        boolean confirm = input.consumeJustPressed(KeyEvent.VK_E)
                | input.consumeJustPressed(KeyEvent.VK_ENTER);

        // Battle finished: wait for confirmation, then return to the world.
        if (battleState.getPhase() != BattleState.Phase.PLAYER_TURN) {
            if (confirm) {
                endBattle(battleState.getPhase() == BattleState.Phase.VICTORY);
            }
            return;
        }

        // 2x2 move grid: horizontal keys switch column, vertical keys switch row.
        boolean horizontal = input.consumeJustPressed(KeyEvent.VK_LEFT)
                | input.consumeJustPressed(KeyEvent.VK_A)
                | input.consumeJustPressed(KeyEvent.VK_RIGHT)
                | input.consumeJustPressed(KeyEvent.VK_D);

        boolean vertical = input.consumeJustPressed(KeyEvent.VK_UP)
                | input.consumeJustPressed(KeyEvent.VK_W)
                | input.consumeJustPressed(KeyEvent.VK_DOWN)
                | input.consumeJustPressed(KeyEvent.VK_S);

        if (horizontal) {
            selectedMoveIndex = (selectedMoveIndex / 2) * 2 + (selectedMoveIndex + 1) % 2;
        }
        if (vertical) {
            selectedMoveIndex = (selectedMoveIndex + 2) % BattleMonster.MOVE_COUNT;
        }

        if (confirm) {
            battleState.executePlayerMove(selectedMoveIndex);
        }
    }

    /**
     * Switches to battle mode and creates the battle for the given overworld
     * monster.
     */
    private void startBattle(Monster monster) {
        if (gameMode == GameMode.BATTLE) {
            return;
        }

        dialogue.end();

        encounteredMonster = monster;
        encounterLockoutMonster = monster;

        battleState = new BattleState(
                playerBattleMonster,
                BattleFactory.createEnemy(monster.getType()));
        selectedMoveIndex = 0;

        gameMode = GameMode.BATTLE;

        player.setMoveIntent(0, 0);
        input.clearJustPressed(); // drop stale key presses from exploration
    }

    /**
     * Returns to exploration. On victory the defeated monster is removed from the
     * world. The player's monster is healed afterwards (prototype behaviour).
     *
     * @param won true if the player won and the enemy should disappear
     */
    private void endBattle(boolean won) {
        if (won && encounteredMonster != null) {
            monsters.remove(encounteredMonster);
            entities.remove(encounteredMonster);

            if (encounterLockoutMonster == encounteredMonster) {
                encounterLockoutMonster = null;
            }
        }

        playerBattleMonster.restoreFullHp();

        gameMode = GameMode.EXPLORATION;
        encounteredMonster = null;
        battleState = null;

        player.setMoveIntent(0, 0);
        input.clearJustPressed();

        updateCamera();
    }

    /**
     * Returns the active battle, or null when not in battle (read-only for the
     * renderer).
     */
    public BattleState getBattleState() {
        return battleState;
    }

    /** Returns the move slot currently highlighted in the battle menu. */
    public int getSelectedMoveIndex() {
        return selectedMoveIndex;
    }

    private void updateEncounterLockout() {
        if (encounterLockoutMonster == null) {
            return;
        }

        double playerCenterX = player.getX() + player.getWidth() / 2.0;
        double playerCenterY = player.getY() + player.getHeight() / 2.0;

        double monsterCenterX = encounterLockoutMonster.getX()
                + encounterLockoutMonster.getWidth() / 2.0;

        double monsterCenterY = encounterLockoutMonster.getY()
                + encounterLockoutMonster.getHeight() / 2.0;

        double dx = monsterCenterX - playerCenterX;
        double dy = monsterCenterY - playerCenterY;

        double distanceSquared = dx * dx + dy * dy;

        if (distanceSquared > ENCOUNTER_RESET_DISTANCE * ENCOUNTER_RESET_DISTANCE) {
            encounterLockoutMonster = null;
        }
    }

    private void updateCamera() {
        camera.follow(
                player.getX() + player.getWidth() / 2.0,
                player.getY() + player.getHeight() / 2.0,
                currentMap.getWorldWidth(),
                currentMap.getWorldHeight());
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

    public GameMode getGameMode() {
        return gameMode;
    }

    public Monster getEncounteredMonster() {
        return encounteredMonster;
    }
}
