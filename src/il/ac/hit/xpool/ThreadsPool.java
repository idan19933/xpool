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
public class ThreadsPool {

    /**
     * Priority queue of pending tasks. Ordered so that the task with
     * the highest priority value is the head of the queue.
     */
    private final PriorityQueue<Task> pendingTasks;

    /**
     * Lock object guarding access to {@link #pendingTasks} and
     * {@link #shutdownRequested}. Worker threads wait on this object
     * when no tasks are available, and the submit method notifies on
     * it after enqueueing a new task.
     */
    private final Object lock;

    /**
     * Worker threads owned by this pool. Kept so the pool can be
     * gracefully shut down if needed.
     */
    private final Thread[] workers;

    /**
     * Flag indicating that the pool has been asked to stop. Once set,
     * worker threads exit after draining themselves out of their wait
     * loop.
     */
    private boolean shutdownRequested;

    /**
     * Creates a new pool with the specified number of worker threads.
     * All workers are started immediately and begin waiting for tasks.
     *
     * @param numberOfThreads the number of worker threads the pool
     *                        will manage; must be a positive integer
     * @throws IllegalArgumentException if {@code numberOfThreads} is
     *                                  not positive
     */
    public ThreadsPool(int numberOfThreads) {
        if (numberOfThreads <= 0) {
            throw new IllegalArgumentException(
                    "Number of threads must be positive, was: " + numberOfThreads);
        }
        this.lock = new Object();
        this.shutdownRequested = false;
        this.pendingTasks = new PriorityQueue<Task>(
                numberOfThreads,
                new Comparator<Task>() {
                    @Override
                    public int compare(Task first, Task second) {
                        // Higher priority value comes first, therefore
                        // we invert the natural ordering of integers.
                        return Integer.compare(
                                second.getPriority(),
                                first.getPriority());
                    }
                });
        this.workers = new Thread[numberOfThreads];
        for (int index = 0; index < numberOfThreads; index++) {
            Thread worker = new Thread(new WorkerRunnable(),
                    "xpool-worker-" + index);
            worker.setDaemon(true);
            this.workers[index] = worker;
            worker.start();
        }
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
        synchronized (this.lock) {
            if (this.shutdownRequested) {
                throw new IllegalStateException(
                        "Cannot submit a task: the pool has been shut down.");
            }
            this.pendingTasks.offer(task);
            // Wake one waiting worker so it can pick up the new task.
            this.lock.notify();
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
        synchronized (this.lock) {
            this.shutdownRequested = true;
            // Wake every worker so they can observe the flag and exit
            // if there is nothing left to do.
            this.lock.notifyAll();
        }
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
                    // Pool shut down and no tasks remain.
                    return;
                }
                try {
                    next.perform();
                } catch (RuntimeException runtimeFailure) {
                    // A misbehaving task must not kill the worker.
                    // Report and continue with the next task.
                    System.err.println(
                            "xpool worker caught a RuntimeException from a task: "
                                    + runtimeFailure);
                    runtimeFailure.printStackTrace();
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
            synchronized (ThreadsPool.this.lock) {
                while (ThreadsPool.this.pendingTasks.isEmpty()
                        && !ThreadsPool.this.shutdownRequested) {
                    try {
                        ThreadsPool.this.lock.wait();
                    } catch (InterruptedException interrupted) {
                        // Preserve the interrupt status and exit.
                        Thread.currentThread().interrupt();
                        return null;
                    }
                }
                if (ThreadsPool.this.pendingTasks.isEmpty()) {
                    // We were woken because of shutdown.
                    return null;
                }
                return ThreadsPool.this.pendingTasks.poll();
            }
        }
    }
}
