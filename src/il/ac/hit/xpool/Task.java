package il.ac.hit.xpool;

/**
 * Represents a unit of work that can be submitted to a ThreadPool.
 * Each Task carries an integer priority level: the higher the value,
 * the more important the task. Priority values are not restricted to
 * any specific range.
 */
public interface Task {

    /**
     * Performs the work associated with this task. Invoked by a worker
     * thread of the ThreadPool that picks up this task.
     *
     * @throws XpoolException if an error occurs during task execution
     */
    public abstract void perform() throws XpoolException;

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
