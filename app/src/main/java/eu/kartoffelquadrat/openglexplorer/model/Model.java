package eu.kartoffelquadrat.openglexplorer.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.zip.Inflater;

/**
 * Created by Max on 12/9/16.
 */
public class Model implements Serializable {

    private Board board;

    //The master buttons below the board influencing the ordinary cells' states.
    private Influencers influencers;

    public Model(double influenceProbablility) {
        board = new Board();

        // Create influencer each influencing a random subset of the board cells, with random impact amount.
        influencers = new Influencers(board, influenceProbablility);

        // Use master combination of all influencers to compute each cell's offset (reset is implicitly done).
        influencers.influence(board);
        System.out.println(influencers);

        // Now look at each cell in board and set offset so it becomes zero when the mod 3 arithmetic is applied
        board.adjustOffset();

        // Hide master combination by setting all master cells to zero. update all cells. Game is now ready to be played.
        // Note: It is not necessary to call influence, for all master cellsa are at 0 and therefore do not influence anything
        influencers.reset(0);

        System.out.println(board);
    }

    /**
     * Creates an entirely blank model, where the influencers are completely unattached to all cells. Needed for animations on app startup.
     */
    public Model()
    {
        board = new Board();
        influencers = new Influencers(board, 0);
        influencers.reset(3); // We initialize the blank model with invisible influencer values=-1 to get some animations on game start.
    }

    public Board getBoard() {
        return board;
    }

    public Influencers getInfluencers() {
        return influencers;
    }

    public Model deepCopy() {
        //Serialization of object
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out;
        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(this);


            //De-serialization of object
            ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
            ObjectInputStream in = new ObjectInputStream(bis);
            Model modelCopy = (Model) in.readObject();

            return modelCopy;
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Failed to create deep copy of Model. " + e.getMessage() + "\n" + e.getStackTrace());
        }
    }

    public boolean isSolved() {
        for (Cell c : getBoard().getAllCells()) {
            if (c.getValue() != 0)
                return false;
        }
        return true;
    }
}
