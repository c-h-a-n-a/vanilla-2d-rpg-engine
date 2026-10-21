package game;

import game.entity.Entity;
import game.entity.Monster;
import game.entity.Npc;
import game.entity.Player;
import game.tile.TileManager;
import game.world.DungeonMap;
import game.world.WorldMap;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

public class Game {

    private final Player player;
    private final KeyboardInput input;
    private final WorldMap worldMap;
    //private final DungeonMap dungeonMap;


    private final List<Npc> npcs = new ArrayList<>();
    private final List<Monster> monsters = new ArrayList<>();
    private final List<Entity> solidEntities = new ArrayList<>();
    private Npc activeNpc;
    private int dialogueIndex = 0;


    public Game(
            KeyboardInput input
    ) {

        this.input = input;

        /*
         * Create the world.
         */
        TileManager tileManager =
                new TileManager();

        worldMap =
                new WorldMap(
                        tileManager
                );
//        dungeonMap = new DungeonMap(
//                tileManager
//        );

        /*
         * Player position is a WORLD position.
         *
         * x = 300
         * y = 200
         */
        player = new Player(
                400,
                400,
                64,
                64,
                input
        );


        /*
         * Create a stationary NPC.
         * Coordinates are WORLD coordinates.
         */

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

        // Patrol between two world positions.
        elder.setPatrolPoints(new double[][]{
                {600, 400},
                {800, 400}
        });

        elder.setSpeed(80.0);
        elder.setPlayer(player);

        npcs.add(elder);


        /*
         * Create a stationary monster.
         */
        monsters.add(
                new Monster(
                        800,
                        400,
                        64,
                        64
                )
        );

        solidEntities.addAll(npcs);
        solidEntities.addAll(monsters);

        player.setSolidEntities(solidEntities);

    }

    public void update(
            double deltaTime
    ) {

        /*
         * Update player movement.
         */
        player.update(
                deltaTime,
                worldMap
        );

        /*
         * Keep the player inside
         * the WORLD boundaries.
         */
        player.keepInside(
                worldMap.getWorldWidth(),
                worldMap.getWorldHeight()
        );
//        player.keepInside(
//                dungeonMap.getWorldWidth(),
//                dungeonMap.getWorldHeight()
//        );



        for (Npc npc : npcs) {
            npc.setPaused(activeNpc != null);
            npc.update(deltaTime, worldMap);
        }

        for (Monster monster : monsters) {
            monster.update(deltaTime, worldMap);
        }

        if (input.consumeJustPressed(KeyEvent.VK_E)) {

            if (activeNpc != null) {

                // Advance to the next message.
                if (dialogueIndex < activeNpc.getDialogueLines().length - 1) {
                    dialogueIndex++;
                } else {
                    // Close the conversation after the final message.
                    activeNpc = null;
                    dialogueIndex = 0;
                }

            } else {

                // Try to start a conversation.
                activeNpc = findNearbyNpc();
                dialogueIndex = 0;
            }
        }

    }


    private Npc findNearbyNpc() {
        Player player = getPlayer();

        double playerCenterX =
                player.getX() + player.getWidth() / 2.0;

        double playerCenterY =
                player.getY() + player.getHeight() / 2.0;

        Npc nearestNpc = null;
        double nearestDistanceSquared = Double.MAX_VALUE;

        // Only interact with NPCs in front of the player.
        for (Npc npc : npcs) {
            double npcCenterX =
                    npc.getX() + npc.getWidth() / 2.0;

            double npcCenterY =
                    npc.getY() + npc.getHeight() / 2.0;

            double dx = npcCenterX - playerCenterX;
            double dy = npcCenterY - playerCenterY;

            double distanceSquared = dx * dx + dy * dy;

            // Interaction range: 100 world pixels.
            if (distanceSquared > 100.0 * 100.0) {
                continue;
            }

            // Determine whether the NPC is in front.
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

    public Npc getActiveNpc() {
        return activeNpc;
    }


    public Player getPlayer() {
        return player;
    }

    public KeyboardInput getInput() {
        return input;
    }

    public WorldMap getWorldMap() {
        return worldMap;
    }

//    public DungeonMap getDungeonMap() {
//        return dungeonMap;
//    }


    public List<Npc> getNpcs() {
        return npcs;
    }

    public List<Monster> getMonsters() {
        return monsters;
    }

    public String getActiveDialogueLine() {
        if (activeNpc == null) {
            return null;
        }

        return activeNpc.getDialogueLine(dialogueIndex);
    }

}