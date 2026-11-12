package game.battle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The player's party: up to six monsters, two of them active (one per slot)
 * and the rest in reserve. Contains no rendering or input code.
 */
public class BattleParty {

    /** Maximum number of monsters in a party. */
    public static final int MAX_SIZE = 6;

    /** Number of monsters fighting at the same time. */
    public static final int ACTIVE_SLOTS = 2;

    private final BattleMonster[] active = new BattleMonster[ACTIVE_SLOTS];
    private final List<BattleMonster> reserve = new ArrayList<>();

    /**
     * Creates a party. The first two members become active, the rest reserve.
     *
     * @param members between 1 and {@link #MAX_SIZE} non-null monsters
     */
    public BattleParty(List<BattleMonster> members) {
        if (members == null || members.isEmpty() || members.size() > MAX_SIZE) {
            throw new IllegalArgumentException(
                    "A party needs between 1 and " + MAX_SIZE + " monsters.");
        }

        for (int i = 0; i < members.size(); i++) {
            BattleMonster member = members.get(i);

            if (member == null) {
                throw new IllegalArgumentException("Party members cannot be null.");
            }

            if (i < ACTIVE_SLOTS) {
                active[i] = member;
            } else {
                reserve.add(member);
            }
        }
    }

    /** Returns the monster in the given active slot, or null if the slot is empty. */
    public BattleMonster getActive(int slot) {
        return active[slot];
    }

    /** Returns true if the slot holds a monster that is still able to fight. */
    public boolean isSlotAlive(int slot) {
        return slot >= 0
                && slot < ACTIVE_SLOTS
                && active[slot] != null
                && !active[slot].isFainted();
    }

    /** Returns how many monsters are waiting in reserve. */
    public int getReserveCount() {
        return reserve.size();
    }

    /** Returns the reserve monster at the given index. */
    public BattleMonster getReserveMember(int reserveIndex) {
        return reserve.get(reserveIndex);
    }

    /** Returns true if the reserve index is valid and that monster has not fainted. */
    public boolean canSwitchTo(int reserveIndex) {
        return reserveIndex >= 0
                && reserveIndex < reserve.size()
                && !reserve.get(reserveIndex).isFainted();
    }

    /** Returns true if at least one reserve monster can still fight. */
    public boolean hasLivingReserve() {
        for (int i = 0; i < reserve.size(); i++) {
            if (canSwitchTo(i)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Swaps the monster in an active slot with a reserve monster.
     * The swapped-out monster takes the reserve position that was freed.
     */
    public void switchSlot(int slot, int reserveIndex) {
        BattleMonster outgoing = active[slot];
        active[slot] = reserve.get(reserveIndex);
        reserve.set(reserveIndex, outgoing);
    }

    /** Returns true when every monster in the party has fainted. */
    public boolean isDefeated() {
        for (BattleMonster member : getAllMembers()) {
            if (!member.isFainted()) {
                return false;
            }
        }
        return true;
    }

    /** Returns every member, active monsters first, then reserve (read-only). */
    public List<BattleMonster> getAllMembers() {
        List<BattleMonster> all = new ArrayList<>();

        for (BattleMonster monster : active) {
            if (monster != null) {
                all.add(monster);
            }
        }
        all.addAll(reserve);

        return Collections.unmodifiableList(all);
    }

    /** Restores every member to full HP. */
    public void restoreAllHp() {
        for (BattleMonster member : getAllMembers()) {
            member.restoreFullHp();
        }
    }
}