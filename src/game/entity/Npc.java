package game.entity;

import game.world.GameMap;

public class Npc extends Entity {

    private static final double DEFAULT_SPEED = 80.0;

    private final String name;
    private final String[] dialogueLines;
    private final String portraitPath;

    private double[][] patrolPoints = new double[0][];
    private int waypointIndex;
    private double speed = DEFAULT_SPEED;

    private boolean paused;

    public Npc(double x, double y, int width, int height, String name, String dialogue) {
        this(x, y, width, height, name, new String[]{dialogue}, null);
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

        if (dialogueLines == null || dialogueLines.length == 0) {
            throw new IllegalArgumentException("An NPC needs at least one line of dialogue.");
        }

        this.name = name;
        this.dialogueLines = dialogueLines.clone();
        this.portraitPath = portraitPath;
    }

    public String getName() {
        return name;
    }

    public int getDialogueLineCount() {
        return dialogueLines.length;
    }

    public String getDialogueLine(int index) {
        return dialogueLines[index];
    }

    public String getPortraitPath() {
        return portraitPath;
    }

    /**
     * Waypoints are WORLD coordinates for the top-left corner of the sprite.
     */
    public void setPatrolPoints(double[][] points) {
        if (points == null || points.length < 2) {
            throw new IllegalArgumentException("At least two patrol points are required.");
        }

        patrolPoints = new double[points.length][2];

        for (int i = 0; i < points.length; i++) {
            if (points[i] == null || points[i].length < 2) {
                throw new IllegalArgumentException("Each patrol point needs an x and y coordinate.");
            }

            patrolPoints[i][0] = points[i][0];
            patrolPoints[i][1] = points[i][1];
        }

        // If the first point is where the NPC starts, head for point 2.
        waypointIndex = isAtWaypoint(0) ? 1 : 0;
    }

    public void setSpeed(double speed) {
        if (speed <= 0) {
            throw new IllegalArgumentException("Speed must be greater than zero.");
        }

        this.speed = speed;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    @Override
    public void update(double deltaTime, GameMap map) {
        if (paused || patrolPoints.length < 2) {
            return;
        }

        moveTowardWaypoint(deltaTime, map);
    }

    private void moveTowardWaypoint(double deltaTime, GameMap map) {
        double dx = patrolPoints[waypointIndex][0] - getX();
        double dy = patrolPoints[waypointIndex][1] - getY();

        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance < 0.001) {
            advanceWaypoint();
            return;
        }

        // Never overshoot the waypoint.
        double step = Math.min(speed * deltaTime, distance);

        moveBy(dx / distance * step, dy / distance * step, map);

        // Only advance on arrival. A blocked NPC simply waits.
        if (isAtWaypoint(waypointIndex)) {
            advanceWaypoint();
        }
    }

    private boolean isAtWaypoint(int index) {
        return Math.abs(getX() - patrolPoints[index][0]) < 0.5
                && Math.abs(getY() - patrolPoints[index][1]) < 0.5;
    }

    private void advanceWaypoint() {
        waypointIndex = (waypointIndex + 1) % patrolPoints.length;
    }
}
