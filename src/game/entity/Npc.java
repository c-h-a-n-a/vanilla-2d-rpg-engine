
package game.entity;

import game.world.WorldMap;

public class Npc extends Entity {

    private static final double DEFAULT_SPEED = 80.0;

    // Match the player's collision box.
    private static final int COLLISION_WIDTH = 32;
    private static final int COLLISION_HEIGHT = 20;
    private static final int COLLISION_OFFSET_X = 16;
    private static final int COLLISION_OFFSET_Y = 40;

    private final String name;
    private final String[] dialogueLines;
    private final String portraitPath;

    private Player player;

    private double[][] patrolPoints = new double[0][];
    private int waypointIndex;
    private double speed = DEFAULT_SPEED;

    private boolean paused;

    public Npc(
            double x,
            double y,
            int width,
            int height,
            String name,
            String dialogue
    ) {
        this(
                x, y, width, height,
                name,
                new String[]{dialogue},
                null
        );
    }

    public Npc(
            double x,
            double y,
            int width,
            int height,
            String name,
            String[] dialogueLines,
            String portraitPath
    ) {
        super(x, y, width, height);

        this.name = name;
        this.dialogueLines = dialogueLines.clone();
        this.portraitPath = portraitPath;
    }

    public String getName() {
        return name;
    }

    public String[] getDialogueLines() {
        return dialogueLines.clone();
    }

    public String getDialogueLine(int index) {
        return dialogueLines[index];
    }

    public String getPortraitPath() {
        return portraitPath;
    }

    /**
     * Gives this NPC a reference to the player
     * so it can avoid walking into the player.
     */
    public void setPlayer(Player player) {
        this.player = player;
    }

    /**
     * Waypoints are world coordinates for the
     * top-left corner of this NPC's sprite.
     */
    public void setPatrolPoints(double[][] points) {
        if (points == null || points.length < 2) {
            throw new IllegalArgumentException(
                    "At least two patrol points are required."
            );
        }

        patrolPoints = new double[points.length][2];

        for (int i = 0; i < points.length; i++) {
            if (points[i] == null || points[i].length < 2) {
                throw new IllegalArgumentException(
                        "Each patrol point needs an x and y coordinate."
                );
            }

            patrolPoints[i][0] = points[i][0];
            patrolPoints[i][1] = points[i][1];
        }

        // If the first point is the NPC's starting
        // position, begin by moving toward point 2.
        if (isAtWaypoint(0)) {
            waypointIndex = 1;
        } else {
            waypointIndex = 0;
        }
    }

    public void setSpeed(double speed) {
        if (speed <= 0) {
            throw new IllegalArgumentException(
                    "Speed must be greater than zero."
            );
        }

        this.speed = speed;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    @Override
    public void update(double deltaTime, WorldMap worldMap) {
        if (paused || patrolPoints.length < 2) {
            return;
        }

        moveTowardWaypoint(deltaTime, worldMap);

        // Keep the NPC within the world boundaries.

    }

    private void moveTowardWaypoint(
            double deltaTime,
            WorldMap worldMap
    ) {
        double targetX = patrolPoints[waypointIndex][0];
        double targetY = patrolPoints[waypointIndex][1];

        double dx = targetX - x;
        double dy = targetY - y;

        double distance = Math.sqrt(dx * dx + dy * dy);

        // Reached this waypoint: move to the next one.
        if (distance < 0.001) {
            advanceWaypoint();
            return;
        }

        // Normalize the direction so diagonal movement
        // is not faster than horizontal or vertical movement.
        double directionX = dx / distance;
        double directionY = dy / distance;

        double movement = Math.min(speed * deltaTime, distance);

        double movementX = directionX * movement;
        double movementY = directionY * movement;

        // Check horizontal movement first.
        double nextX = x + movementX;

        if (!isBlockedByEnvironment(nextX, y, worldMap)
                && !isBlockedByPlayer(nextX, y)) {
            x = nextX;
        }

        // Then check vertical movement using the
        // updated horizontal position.
        double nextY = y + movementY;

        if (!isBlockedByEnvironment(x, nextY, worldMap)
                && !isBlockedByPlayer(x, nextY)) {
            y = nextY;
        }

        // Only advance when the NPC actually reaches
        // the target. A blocked NPC waits instead.
        if (isAtWaypoint(waypointIndex)) {
            advanceWaypoint();
        }
    }

    private boolean isBlockedByEnvironment(
            double candidateX,
            double candidateY,
            WorldMap worldMap
    ) {
        return worldMap.isBlocked(
                candidateX + COLLISION_OFFSET_X,
                candidateY + COLLISION_OFFSET_Y,
                COLLISION_WIDTH,
                COLLISION_HEIGHT
        );
    }

    private boolean isBlockedByPlayer(
            double candidateX,
            double candidateY
    ) {
        if (player == null) {
            return false;
        }

        double npcLeft = candidateX + COLLISION_OFFSET_X;
        double npcTop = candidateY + COLLISION_OFFSET_Y;

        double playerLeft =
                player.getX() + COLLISION_OFFSET_X;
        double playerTop =
                player.getY() + COLLISION_OFFSET_Y;

        return npcLeft < playerLeft + COLLISION_WIDTH
                && npcLeft + COLLISION_WIDTH > playerLeft
                && npcTop < playerTop + COLLISION_HEIGHT
                && npcTop + COLLISION_HEIGHT > playerTop;
    }

    private boolean isAtWaypoint(int index) {
        double targetX = patrolPoints[index][0];
        double targetY = patrolPoints[index][1];

        return Math.abs(x - targetX) < 0.5
                && Math.abs(y - targetY) < 0.5;
    }

    private void advanceWaypoint() {
        waypointIndex++;

        if (waypointIndex >= patrolPoints.length) {
            waypointIndex = 0;
        }
    }
}
