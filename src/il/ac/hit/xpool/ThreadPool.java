package il.ac.hit.xpool;

import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * A pool of worker threads that execute submitted Task objects according
 * to their priority. Tasks with higher priority values are picked up
 * before tasks with lower priority values. Once a worker thread begins
 * performing a task, that task runs to completion and is never preempted,
 * even if a higher-priority task is submitted in the meantime.
 *
 * <p>This implementation relies only on {@link Thread}, {@link Object}
 * and classes from {@code java.util}. It does not use any class from
 * {@code java.util.concurrent}.</p>
 */
public class ThreadPool {

    /**
     * Priority queue of pending tasks. Ordered so that the task with
     * the highest priority value is the head of the queue.
     */
    private PriorityQueue<Task> pendingTasks;

    /**
     * Lock object guarding access to {@link #pendingTasks} and
     * {@link #isShutdownRequested}. Worker threads wait on this object
     * when no tasks are available, and the submit method notifies on
     * it after enqueueing a new task.
     */
    private Object lock;

    /**
     * Worker threads owned by this pool. Kept so the pool can be
     * gracefully shut down if needed.
     */
    private Thread[] workers;

    /**
     * Flag indicating that the pool has been asked to stop. Once set,
     * worker threads exit after draining themselves out of their wait
     * loop.
     */
    private boolean isShutdownRequested;

    /**
     * Creates a new pool with the specified number of worker threads.
     * All workers are started immediately and begin waiting for tasks.
     *
     * @param numberOfThreads the number of worker threads the pool
     *                        will manage; must be a positive integer
     * @throws IllegalArgumentException if {@code numberOfThreads} is
     *                                  not positive
     */
    public ThreadPool(int numberOfThreads) {
        if (numberOfThreads <= 0) {
            throw new IllegalArgumentException(
                    "Number of threads must be positive, was: " + numberOfThreads);
        }
        
        this.setLock(new Object());
        this.setIsShutdownRequested(false);
        this.setPendingTasks(new PriorityQueue<Task>(
                numberOfThreads,
                new Comparator<Task>() {
                    @Override
                    public int compare(Task first, Task second) {
                        return Integer.compare(
                                second.getPriority(),
                                first.getPriority());
                    }
                }));
                
        this.setWorkers(new Thread[numberOfThreads]);
        
        for (int index = 0; index < numberOfThreads; index++) {
            Thread worker = new Thread(new WorkerRunnable(),
                    "xpool-worker-" + index);
            worker.setDaemon(true);
            this.getWorkers()[index] = worker;
            worker.start();
        }
    }

    public void setPendingTasks(PriorityQueue<Task> pendingTasks) {
        if (pendingTasks == null) {
            throw new IllegalArgumentException("Pending tasks queue cannot be null.");
        }
        this.pendingTasks = pendingTasks;
    }

    public PriorityQueue<Task> getPendingTasks() {
        return pendingTasks;
    }

    public void setLock(Object lock) {
        if (lock == null) {
            throw new IllegalArgumentException("Lock object cannot be null.");
        }
        this.lock = lock;
    }

    public Object getLock() {
        return lock;
    }

    public void setWorkers(Thread[] workers) {
        if (workers == null) {
            throw new IllegalArgumentException("Workers array cannot be null.");
        }
        this.workers = workers;
    }

    public Thread[] getWorkers() {
        return workers;
    }

    public void setIsShutdownRequested(boolean isShutdownRequested) {
        this.isShutdownRequested = isShutdownRequested;
    }

    public boolean isShutdownRequested() {
        return isShutdownRequested;
    }

    /**
     * Submits a task to the pool. The task will be executed by one of
     * the worker threads as soon as one becomes available and the task
     * is the highest-priority pending task at that moment.
     *
     * @param task the task to submit; must not be {@code null}
     * @throws IllegalArgumentException if {@code task} is {@code null}
     * @throws IllegalStateException    if the pool has already been
     *                                  shut down
     */
    public void submit(Task task) {
        if (task == null) {
            throw new IllegalArgumentException("Task must not be null.");
        }
        
        synchronized (this.getLock()) {
            if (this.isShutdownRequested()) {
                throw new IllegalStateException(
                        "Cannot submit a task: the pool has been shut down.");
            }
            this.getPendingTasks().offer(task);
            this.getLock().notify();
        }
    }

    /**
     * Requests that the pool stop accepting new tasks and that its
     * worker threads exit once the pending queue is empty. Tasks that
     * are already running are not interrupted and are allowed to
     * complete normally. After this method is invoked, any further
     * call to {@link #submit(Task)} will throw an
     * {@link IllegalStateException}.
     */
    public void shutdown() {
        synchronized (this.getLock()) {
            this.setIsShutdownRequested(true);
            this.getLock().notifyAll();
        }
    }

    @Override
    public String toString() {
        return "ThreadPool{" +
                "pendingTasks=" + pendingTasks.size() +
                ", isShutdownRequested=" + isShutdownRequested +
                '}';
    }

    /**
     * The Runnable executed by every worker thread. Each worker
     * repeatedly takes the highest-priority pending task and performs
     * it. The worker waits on the shared lock while the pending queue
     * is empty, and exits when the pool is shut down and the queue is
     * empty.
     */
    private final class WorkerRunnable implements Runnable {

        @Override
        public void run() {
            while (true) {
                Task next = takeNextTask();
                if (next == null) {
                    return;
                }
                
                try {
                    next.perform();
                } catch (XpoolException | RuntimeException failure) {
                    System.err.println(
                            "xpool worker caught an exception from a task: "
                                    + failure.getMessage());
                    failure.printStackTrace();
                }
            }
        }

        /**
         * Returns the highest-priority pending task, blocking the
         * calling thread until one becomes available. Returns
         * {@code null} when the pool has been shut down and no
         * pending tasks remain, signalling to the worker that it
         * should exit.
         *
         * @return the next task, or {@code null} when the worker
         *         should stop
         */
        private Task takeNextTask() {
            synchronized (ThreadPool.this.getLock()) {
                while (ThreadPool.this.getPendingTasks().isEmpty()
                        && !ThreadPool.this.isShutdownRequested()) {
                    try {
                        ThreadPool.this.getLock().wait();
                    } catch (InterruptedException interrupted) {
                        Thread.currentThread().interrupt();
                        return null;
                    }
                }
                
                if (ThreadPool.this.getPendingTasks().isEmpty()) {
                    return null;
                }
                
                return ThreadPool.this.getPendingTasks().poll();
            }
        }
    }
}
