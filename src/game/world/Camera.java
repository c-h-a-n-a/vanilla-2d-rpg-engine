package game.world;

public class Camera {

    private double x;
    private double y;

    private final int screenWidth;
    private final int screenHeight;

    public Camera(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
    }

    /**
     * Centres the camera on a WORLD position, without showing anything
     * outside the world. If the world is smaller than the screen, it is
     * centred instead.
     */
    public void follow(double targetX, double targetY, int worldWidth, int worldHeight) {
        x = followAxis(targetX, screenWidth, worldWidth);
        y = followAxis(targetY, screenHeight, worldHeight);
    }

    private static double followAxis(double target, int screenSize, int worldSize) {
        double max = worldSize - screenSize;

        // World smaller than the screen: centre it (offset is negative).
        if (max <= 0) {
            return max / 2.0;
        }

        return Math.max(0, Math.min(target - screenSize / 2.0, max));
    }

    public int worldToScreenX(double worldX) {
        return (int) Math.round(worldX - x);
    }

    public int worldToScreenY(double worldY) {
        return (int) Math.round(worldY - y);
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
