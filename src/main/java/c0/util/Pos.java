package c0.util;

public class Pos {
    public Pos(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public int row;
    public int col;

    public c0.util.Pos nextCol() {
        return new c0.util.Pos(row, col + 1);
    }

    public c0.util.Pos nextRow() {
        return new c0.util.Pos(row + 1, 0);
    }

    @Override
    public String toString() {
        return new StringBuilder().append("Pos(row: ").append(row).append(", col: ").append(col).append(")").toString();
    }
}
