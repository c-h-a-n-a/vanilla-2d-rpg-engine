package game.world;

public class Camera {

    private double x;
    private double y;

    private final int screenWidth;
    private final int screenHeight;

    public Camera(
            int screenWidth,
            int screenHeight
    ) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
    }

    /**
     * Makes the camera follow the target.
     *
     * targetX and targetY are WORLD coordinates.
     */
    public void follow(
            double targetX,
            double targetY,
            int worldWidth,
            int worldHeight
    ) {

        /*
         * Place the target at the center
         * of the screen.
         */
        x = targetX - screenWidth / 2.0;
        y = targetY - screenHeight / 2.0;

        /*
         * Prevent the camera from moving
         * outside the world boundaries.
         */
        double maxX =
                Math.max(
                        0,
                        worldWidth - screenWidth
                );

        double maxY =
                Math.max(
                        0,
                        worldHeight - screenHeight
                );

        x = Math.max(
                0,
                Math.min(x, maxX)
        );

        y = Math.max(
                0,
                Math.min(y, maxY)
        );
    }

    /**
     * Converts a WORLD X position
     * to a SCREEN X position.
     */
    public int worldToScreenX(
            double worldX
    ) {

        return (int) Math.round(
                worldX - x
        );
    }

    /**
     * Converts a WORLD Y position
     * to a SCREEN Y position.
     */
    public int worldToScreenY(
            double worldY
    ) {

        return (int) Math.round(
                worldY - y
        );
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public int getScreenWidth() {
        return screenWidth;
    }

    public int getScreenHeight() {
        return screenHeight;
    }
}