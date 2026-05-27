package com.lifemichael.samples;

import il.ac.hit.xpool.Task;
import il.ac.hit.xpool.ThreadsPool;

/**
 * A demonstration class for the ThreadsPool and Task implementations.
 * It creates a pool, submits tasks, and waits for them to complete.
 */
public class XpoolDemo {

    public static final int THREAD_COUNT = 4;
    public static final int SHORT_SLEEP_MILLIS = 1000;
    public static final int LONG_SLEEP_MILLIS = 40000;

    /**
     * The main entry point for the demo application.
     * 
     * @param args command line arguments
     */
    public static void main(String[] args) {

        ThreadsPool pool = new ThreadsPool(THREAD_COUNT);

        Task helloTask = new SimpleTask(2, "Hello");
        Task morningTask = new SimpleTask(7, "Good Morning");
        Task afternoonTask = new SimpleTask(2, "Good Afternoon");
        Task eveningTask = new SimpleTask(12, "Good Evening");

        // Submit initial tasks
        pool.submit(helloTask);
        pool.submit(morningTask);
        pool.submit(afternoonTask);
        pool.submit(eveningTask);

        try {
            Thread.sleep(SHORT_SLEEP_MILLIS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        Task nightTask = new SimpleTask(12, "Good Night");
        Task dayTask = new SimpleTask(4, "Good Day");
        Task everyoneTask = new SimpleTask(1, "Hello Everyone");
        Task luckTask = new SimpleTask(8, "Good Luck");
        Task bonjournoTask = new SimpleTask(2, "Bonjourno");
        Task bonjourTask = new SimpleTask(8, "Bonjour");

        // Submit subsequent tasks
        pool.submit(nightTask);
        pool.submit(dayTask);
        pool.submit(everyoneTask);
        pool.submit(luckTask);
        pool.submit(bonjournoTask);
        pool.submit(bonjourTask);

        // Wait long enough for all tasks to finish so the demo prints
        // its output before the JVM exits.
        try {
            Thread.sleep(LONG_SLEEP_MILLIS);
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }

        /*
        We can expect the following:
        1. after the first four tasks and their output, we can expect the output
        of the next four tasks (nightTask, bonjourTask, luckTask, and dayTask) - not necessary in this order!
        2. after getting the output of tasks nightTask, bonjourTask, luckTask, and dayTask, we can expect to get
        the output of tasks everyoneTask and bonjournoTask - not necessary in this order!
         */
    }
}
