package game.assets;

import javax.imageio.ImageIO;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Single place that loads, scales and caches images.
 *
 * Every scaled image is converted ONCE to a screen-compatible format at the
 * requested size, so drawing it later is a plain fast copy (no per-frame
 * scaling).
 */
public final class AssetManager {

    /** Marks an image that failed to load, so we don't retry (or spam errors) every frame. */
    private static final BufferedImage MISSING =
            new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);

    private static final Map<String, BufferedImage> CACHE = new HashMap<>();

    private AssetManager() {
    }

    /** Loads an image at its original size. Throws a clear error if it is missing. */
    public static synchronized BufferedImage image(String resourcePath) {
        BufferedImage image = imageOrNull(resourcePath);

        if (image == null) {
            throw new IllegalStateException("Image resource not found or unreadable: " + resourcePath);
        }

        return image;
    }

    /** Loads an image at its original size, or returns null (reported once) if it fails. */
    public static synchronized BufferedImage imageOrNull(String resourcePath) {
        if (resourcePath == null || resourcePath.isBlank()) {
            return null;
        }

        BufferedImage cached = CACHE.get(resourcePath);

        if (cached == null) {
            cached = read(resourcePath);
            CACHE.put(resourcePath, cached == null ? MISSING : cached);
        }

        return cached == MISSING ? null : cached;
    }

    /** Loads an image scaled to width x height. Throws a clear error if it is missing. */
    public static synchronized BufferedImage scaledImage(String resourcePath, int width, int height) {
        BufferedImage image = scaledImageOrNull(resourcePath, width, height);

        if (image == null) {
            throw new IllegalStateException("Image resource not found or unreadable: " + resourcePath);
        }

        return image;
    }

    /** Loads an image scaled to width x height, or returns null (reported once) if it fails. */
    public static synchronized BufferedImage scaledImageOrNull(String resourcePath, int width, int height) {
        if (resourcePath == null || resourcePath.isBlank()) {
            return null;
        }

        String key = resourcePath + "@" + width + "x" + height;
        BufferedImage cached = CACHE.get(key);

        if (cached == null) {
            BufferedImage original = imageOrNull(resourcePath);
            cached = original == null ? MISSING : scale(original, width, height);
            CACHE.put(key, cached);
        }

        return cached == MISSING ? null : cached;
    }

    private static BufferedImage read(String resourcePath) {
        try (InputStream stream = AssetManager.class.getResourceAsStream(resourcePath)) {

            if (stream == null) {
                System.err.println("Image not found: " + resourcePath);
                return null;
            }

            BufferedImage image = ImageIO.read(stream);

            if (image == null) {
                System.err.println("Unsupported image format: " + resourcePath);
            }

            return image;

        } catch (IOException exception) {
            System.err.println("Failed to read image " + resourcePath + ": " + exception.getMessage());
            return null;
        }
    }

    private static BufferedImage scale(BufferedImage source, int width, int height) {
        BufferedImage target;

        if (GraphicsEnvironment.isHeadless()) {
            target = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        } else {
            GraphicsConfiguration configuration = GraphicsEnvironment
                    .getLocalGraphicsEnvironment()
                    .getDefaultScreenDevice()
                    .getDefaultConfiguration();

            target = configuration.createCompatibleImage(width, height, Transparency.TRANSLUCENT);
        }

        Graphics2D graphics = target.createGraphics();

        try {
            // Nearest neighbour keeps pixel art sharp.
            graphics.setRenderingHint(
                    RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            graphics.drawImage(source, 0, 0, width, height, null);
        } finally {
            graphics.dispose();
        }

        return target;
    }
}
