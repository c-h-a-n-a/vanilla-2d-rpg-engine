package game.world;

import game.tile.TileType;

public class CollisionDebugInfo {

    private final double x;
    private final double y;

    private final double width;
    private final double height;

    private final int startColumn;
    private final int endColumn;

    private final int startRow;
    private final int endRow;

    private final boolean blocked;

    private final TileType blockingTile;

    private final int blockingColumn;
    private final int blockingRow;

    public CollisionDebugInfo(
            double x,
            double y,
            double width,
            double height,
            int startColumn,
            int endColumn,
            int startRow,
            int endRow,
            boolean blocked,
            TileType blockingTile,
            int blockingColumn,
            int blockingRow
    ) {

        this.x = x;
        this.y = y;

        this.width = width;
        this.height = height;

        this.startColumn = startColumn;
        this.endColumn = endColumn;

        this.startRow = startRow;
        this.endRow = endRow;

        this.blocked = blocked;

        this.blockingTile = blockingTile;

        this.blockingColumn = blockingColumn;
        this.blockingRow = blockingRow;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public int getStartColumn() {
        return startColumn;
    }

    public int getEndColumn() {
        return endColumn;
    }

    public int getStartRow() {
        return startRow;
    }

    public int getEndRow() {
        return endRow;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public TileType getBlockingTile() {
        return blockingTile;
    }

    public int getBlockingColumn() {
        return blockingColumn;
    }

    public int getBlockingRow() {
        return blockingRow;
    }
}