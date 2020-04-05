package eu.kartoffelquadrat.openglexplorer;

/**
 * Created by Max on 12/20/16.
 */
public interface CountDowner {

    void init();

    void tickDown();

    boolean isOver();

    void persist();
}
