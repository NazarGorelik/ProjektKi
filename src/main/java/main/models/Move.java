package main.models;

public class Move {
    public Position from;
    public Position to;
    // Hohe des zu ziehenden Turms
    public int step;

    public Move(Position from, Position to, int step) {
        this.from = from;
        this.to = to;
        this.step = step;
    }

    public String toString() {
        return from + "-" + to + "-" + step;
    }
}
