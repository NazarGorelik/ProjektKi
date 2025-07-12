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
}