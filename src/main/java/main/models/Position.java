package main.models;

public class Position {
    public int x, y;

    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Position move(int dx, int dy) {
        return new Position(x + dx, y + dy);
    }

    public boolean isValid() {
        return x >= 0 && x < 7 && y >= 0 && y < 7;
    }

    public String toString() {
        return "" + (char) ('A' + x) + (7 - y);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Position position = (Position) obj;
        return x == position.x && y == position.y;
    }

    @Override
    public int hashCode() {
        return 31 * x + y;
    }
}