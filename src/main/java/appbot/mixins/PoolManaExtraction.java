package appbot.mixins;

final class PoolManaExtraction {

    private PoolManaExtraction() {
    }

    static int simulate(int amount, int currentMana) {
        return Math.min(amount, currentMana);
    }

    static int modulate(int amount, int oldMana, int newMana) {
        var requested = Math.min(amount, oldMana);
        return Math.max(requested, oldMana - newMana);
    }
}
