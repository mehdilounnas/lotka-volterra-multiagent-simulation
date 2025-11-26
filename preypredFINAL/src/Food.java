public class Food {
    private Position position;
    private int energyValue;
    private boolean consumed;

    public Food(Position position, int energyValue) {
        this.position = position;
        this.energyValue = energyValue;
        this.consumed = false;
    }

    public Position getPosition() {
        return position;
    }

    public int getEnergyValue() {
        return energyValue;
    }

    public boolean isConsumed() {
        return consumed;
    }

    public void consume() {
        this.consumed = true;
    }

    @Override
    public String toString() {
        return String.format("Food at %s (energy=%d)", position, energyValue);
    }
}