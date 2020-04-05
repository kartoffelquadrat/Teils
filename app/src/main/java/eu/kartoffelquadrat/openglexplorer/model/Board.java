package eu.kartoffelquadrat.openglexplorer.model;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

/**
 * Created by Max on 12/9/16.
 */
public class Board implements BoardLecture, Serializable {

    public static int BOARD_DIMENSIONS = 4;
    private Cell[][] cells;

    public Board() {
        cells = new Cell[BOARD_DIMENSIONS][BOARD_DIMENSIONS];
        for (int x = 0; x < BOARD_DIMENSIONS; x++) {
            for (int y = 0; y < BOARD_DIMENSIONS; y++) {
                cells[x][y] = new Cell();
            }
        }
    }

    /**
     * Sets all cells to their individual standard offset. Needs to be called before all influencers add their values.
     */
    public void resetAllCells() {
        for (Cell c : getAllCells())
            c.reset();
    }

    public Collection<Cell> getAllCells() {
        Collection allCells = new LinkedList<Cell>();

        for (int x = 0; x < cells.length; x++) {
            allCells.addAll(Arrays.asList(cells[x]));
        }

        return allCells;
    }

    /**
     * Sets each cell's offset so it is exactly complementary to the currently active value, using the mod3 arithmetic.
     */
    public void adjustOffset() {
        // For all cells, compute offset and set current value to offset.
        for (Cell c : getAllCells()) {
            c.setOffset((3 - c.getValue())%3);
            c.reset();
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Values:\n");
        for (int y = BOARD_DIMENSIONS-1; y >=0 ; y--) {
            if (y < BOARD_DIMENSIONS-1)
                sb.append("---+---+---+---\n");
            for (int x = BOARD_DIMENSIONS-1; x >=0 ; x--) {
                if (x < BOARD_DIMENSIONS-1)
                    sb.append("|");
                sb.append(cells[y][x]);
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    @Override
    public int getCellValue(int x, int y)
    {
        return cells[y][x].getValue();
    }
}
