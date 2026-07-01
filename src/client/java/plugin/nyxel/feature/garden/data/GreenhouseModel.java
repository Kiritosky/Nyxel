package plugin.nyxel.feature.garden.data;

/**
 * The greenhouse plot grid the planner solves against: a {@code cols × rows} grid
 * with a growth surface per cell. The exact in-game greenhouse dimensions aren't
 * exposed by the API, so this is a configurable model (defaults to a square grid;
 * can be sized from the number of unlocked garden plots).
 */
public final class GreenhouseModel {

    public final int cols;
    public final int rows;
    private final String[][] surface;

    public GreenhouseModel(int cols, int rows) {
        this.cols = cols;
        this.rows = rows;
        this.surface = new String[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                surface[r][c] = "Farmland";
            }
        }
    }

    public String surfaceAt(int col, int row) {
        return surface[row][col];
    }

    public void setSurface(int col, int row, String s) {
        surface[row][col] = s;
    }

    public int cellCount() {
        return cols * rows;
    }

    public static GreenhouseModel defaultGrid() {
        return new GreenhouseModel(7, 7);
    }

    /** Square grid loosely sized from unlocked plot count (odd dimension). */
    public static GreenhouseModel forUnlockedPlots(int plots) {
        int side = Math.max(5, (int) Math.ceil(Math.sqrt(Math.max(1, plots))) * 2 + 1);
        return new GreenhouseModel(side, side);
    }
}
