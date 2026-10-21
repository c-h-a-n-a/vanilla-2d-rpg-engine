package game.entity;

import java.util.List;

/**
 * Entity-versus-entity collision, shared by the player, NPCs and monsters.
 */
final class EntityCollision {

    private EntityCollision() {
    }

    /**
     * True if moving {@code self} to (nextX, nextY) would make it overlap
     * another entity.
     * <p>
     * If the two already overlap (for example an NPC walked into the player),
     * movement that reduces the overlap is allowed so nobody gets stuck.
     */
    static boolean isBlockedByEntity(
            Entity self,
            double nextX,
            double nextY,
            List<? extends Entity> others
    ) {
        CollisionBox box = self.getCollisionBox();

        double candidateX = nextX + box.offsetX();
        double candidateY = nextY + box.offsetY();
        double currentX = self.getX() + box.offsetX();
        double currentY = self.getY() + box.offsetY();

        for (Entity other : others) {

            if (other == self) {
                continue;
            }

            CollisionBox otherBox = other.getCollisionBox();

            double otherX = other.getX() + otherBox.offsetX();
            double otherY = other.getY() + otherBox.offsetY();

            double candidateOverlap = overlapArea(
                    candidateX, candidateY, box.width(), box.height(),
                    otherX, otherY, otherBox.width(), otherBox.height());

            if (candidateOverlap <= 0) {
                continue;
            }

            double currentOverlap = overlapArea(
                    currentX, currentY, box.width(), box.height(),
                    otherX, otherY, otherBox.width(), otherBox.height());

            if (currentOverlap > 0 && candidateOverlap < currentOverlap) {
                continue;
            }

            return true;
        }

        return false;
    }

    private static double overlapArea(
            double ax, double ay, double aw, double ah,
            double bx, double by, double bw, double bh
    ) {
        double overlapX = Math.min(ax + aw, bx + bw) - Math.max(ax, bx);
        double overlapY = Math.min(ay + ah, by + bh) - Math.max(ay, by);

        if (overlapX <= 0 || overlapY <= 0) {
            return 0;
        }

        return overlapX * overlapY;
    }
}
