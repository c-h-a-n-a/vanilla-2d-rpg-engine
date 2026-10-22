package game.entity;

/**
 * The part of an entity that collides, relative to the top-left corner of
 * its sprite. Using only the "feet" lets the head overlap trees and walls,
 * which is what gives top-down games their depth.
 */
public record CollisionBox(int offsetX, int offsetY, int width, int height) {

    /** Lower body of a 64x64 sprite. Used by every entity unless overridden. */
    public static final CollisionBox FEET = new CollisionBox(16, 40, 32, 20);
}
