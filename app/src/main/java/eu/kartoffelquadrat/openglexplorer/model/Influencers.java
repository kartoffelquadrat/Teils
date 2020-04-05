package eu.kartoffelquadrat.openglexplorer.model;

import java.io.Serializable;

/**
 * Created by Max on 12/9/16.
 */
public class Influencers implements Serializable {

    private MasterCell[] mastercells;

    Influencers(Board board, double influenceProbablility) {
        // Attribute random number of board cells (plus influence amount) to master cell
        mastercells = new MasterCell[6];

        for (int i = 0; i < mastercells.length; i++) {
            mastercells[i] = new MasterCell(board, influenceProbablility);
        }
    }

    /**
     * Let all influencers use their current state to influence the board the way they were initially programmed to.
     */
    public void influence(Board board) {
        // make sure that all cells part from the current offset (is zero if the offset has not been determined yet)
        board.resetAllCells();

        for (int i = 0; i < mastercells.length; i++) {
            mastercells[i].influence();
        }
    }

    public void reset(int value) {
        for (int i = 0; i < mastercells.length; i++) {
            mastercells[i].reset(value);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Master combination:\n");
        for (int i = 0; i < mastercells.length; i++) {
            if (i > 0)
                sb.append(" - ");
            sb.append(mastercells[i].getState());
        }
        return sb.toString();
    }

    public int[] getStates()
    {
        int[] states = new int[mastercells.length];
        for (int i = 0; i < states.length; i++) {
            states[i] = mastercells[i].getState();
        }
        return states;
    }

    public void advance(int influencerId) {
        mastercells[influencerId].increment();
    }
}
