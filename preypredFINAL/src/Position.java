import java.io.Serializable;

public class Position implements Serializable {
    private double x;
    private double y;

    public Position(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() { return x; }
    public double getY() { return y; }

    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }

    public double distance(Position other) {
        double dx = this.x - other.x;
        double dy = this.y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    public Position moveTo(double directionX, double directionY, double speed) {
        double magnitude = Math.sqrt(directionX * directionX + directionY * directionY);
        if (magnitude > 0) {
            directionX /= magnitude;
            directionY /= magnitude;
        }
        return new Position(x + directionX * speed, y + directionY * speed);
    }

    public Position randomMove(double speed, int envWidth, int envHeight) {
        double angle = Math.random() * 2 * Math.PI;
        double newX = x + Math.cos(angle) * speed;
        double newY = y + Math.sin(angle) * speed;

        newX = Math.max(0, Math.min(envWidth, newX));
        newY = Math.max(0, Math.min(envHeight, newY));

        return new Position(newX, newY);
    }

    @Override
    public String toString() {
        return String.format("(%.2f, %.2f)", x, y);
    }
}