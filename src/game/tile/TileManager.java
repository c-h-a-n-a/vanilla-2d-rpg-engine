package game.tile;

import game.assets.AssetManager;
import game.config.GameConfig;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;

/**
 * Loads tile images on demand (scaled once to TILE_SIZE) and draws them.
 *
 * Images are loaded the first time a tile type is needed, so dungeon tiles
 * are never required unless a dungeon map is actually used.
 */
public class TileManager {

    private final Map<TileType, BufferedImage> images = new EnumMap<>(TileType.class);

    /**
     * Loads the given tile types now. Maps call this when they are created,
     * so a missing image fails immediately with a clear message instead of
     * causing a hitch (or an invisible tile) in the middle of the game.
     */
    public void preload(Collection<TileType> types) {
        for (TileType type : types) {
            get(type);
        }
    }

    public BufferedImage get(TileType type) {
        BufferedImage image = images.get(type);

        if (image == null) {
            image = AssetManager.scaledImage(
                    type.getResourcePath(),
                    GameConfig.TILE_SIZE,
                    GameConfig.TILE_SIZE
            );
            images.put(type, image);
        }

        return image;
    }

    public void render(Graphics2D graphics, TileType type, int screenX, int screenY) {
        graphics.drawImage(get(type), screenX, screenY, null);
    }
}
