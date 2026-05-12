package il.ac.hit.xpool;

/**
 * Represents a unit of work that can be submitted to a ThreadsPool.
 * Each Task carries an integer priority level: the higher the value,
 * the more important the task. Priority values are not restricted to
 * any specific range.
 */
public interface Task {

    /**
     * Performs the work associated with this task. Invoked by a worker
     * thread of the ThreadsPool that picks up this task.
     */
    public abstract void perform();

    /**
     * Sets the priority level of this task. Higher values indicate
     * higher importance.
     *
     * @param level the priority level
     */
    public abstract void setPriority(int level);

    /**
     * Returns the priority level of this task.
     *
     * @return the priority level
     */
    public abstract int getPriority();
}
