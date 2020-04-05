package eu.kartoffelquadrat.openglexplorer.model;

import java.io.Serializable;

/**
 * Created by Max on 12/9/16.
 */
public class Cell implements Serializable {

    // The offset required so the cell turns flat once the correct influencers were activated.
    private int currentValue = 0;
    private int offset = 0;
    private boolean offsetImmutable = false;

    public void setOffset(int offset) {
        if (offsetImmutable)
            throw new RuntimeException("Offset can only be set once.");
        this.offset = offset;
    }

    public void reset() {
        currentValue = offset;
    }

    public void influence(int amount) {
        currentValue = (currentValue + amount) % 3;
    }

    public int getValue() {
        return currentValue;
    }

    public String toString()
    {
        return currentValue+"/"+offset;
    }
}