package eu.kartoffelquadrat.openglexplorer.model;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A Mastercell is a cell of the sollution array (displayed below the actual board)
 * Each Mastercell refers to the random number of Cells it triggers + the amount it triggers for that cell
 * <p>
 * Created by Max on 12/9/16.
 */
public class MasterCell implements Serializable {

    // The current state of this master cell (A int value between 0 and 2, both included)
    private int state = (int) (Math.random() * 3);

    // Which cells are triggered by this Mastercell, and which amount it contributes.
    private Map<Cell, Integer> contributions;

    MasterCell(Board board, double influenceProbablility) {
        contributions = new LinkedHashMap<>();
        //iterates over board, and decides for each cell randomly whether it is influenced or not and if yes, how much it is influenced
        for (Cell cell : board.getAllCells()) {
            // probability to impact cell. (Gets more difficult if higher, because more pattern overlapping)
            if (Math.random() < influenceProbablility)
                contributions.put(cell, ((int) (Math.random() * 2)) + 1);
        }
    }

    public void influence() {
        for (Cell c : contributions.keySet()) {
            // influence all attached cells by previously randomly programmed amount
            c.influence(state * contributions.get(c));
        }
    }

    public void reset(int value)
    {
        state = value;
    }

    // sets state to the consecutive state. 0->1, 1->2, 2->0
    public void increment()
    {
        state = (state+1)%3;
    }

    public int getState() {
        return state;
    }
}
