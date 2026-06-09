package appbot.mixins;

final class PoolManaExtraction {

    private PoolManaExtraction() {
    }

    static int simulate(int amount, int currentMana) {
        return Math.min(amount, currentMana);
    }

    static int modulate(int amount, int oldMana, int newMana) {
        var old = oldMana;
        var requested = Math.min(amount, old);
        return Math.max(requested, old - newMana);
    }
}
