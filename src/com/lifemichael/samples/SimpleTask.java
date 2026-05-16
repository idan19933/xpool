package com.lifemichael.samples;

import il.ac.hit.xpool.Task;
import il.ac.hit.xpool.XpoolException;

import java.util.Objects;

/**
 * Sample Task implementation used by {@link XpoolDemo} to exercise the
 * xpool library. Each task sleeps for ten seconds and then prints its
 * priority value followed by its message.
 */
public class SimpleTask implements Task {

    public static final int SLEEP_DURATION_MILLIS = 10000;

    /**
     * The priority level of the task.
     */
    private int priority;

    /**
     * The message associated with the task.
     */
    private String message;

    /**
     * Constructs a new SimpleTask with the given priority and message.
     * 
     * @param priority the priority level
     * @param message  the message to print
     */
    public SimpleTask(int priority, String message) {
        this.setPriority(priority);
        this.setMessage(message);
    }

    /**
     * Performs the task execution.
     * This will sleep and then print out the message.
     */
    @Override
    public void perform() throws XpoolException {
        try {
            Thread.sleep(SLEEP_DURATION_MILLIS);
            System.out.println(this.getPriority() + " " + this.getMessage());
        } catch (InterruptedException e) {
            throw new XpoolException("Task execution was interrupted", e);
        }
    }

    @Override
    public void setPriority(int level) {
        this.priority = level;
    }

    @Override
    public int getPriority() {
        return this.priority;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("Message cannot be null or empty.");
        }
        this.message = message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleTask that = (SimpleTask) o;
        return priority == that.priority && Objects.equals(message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(priority, message);
    }

    @Override
    public String toString() {
        return "SimpleTask{" +
                "priority=" + priority +
                ", message='" + message + '\'' +
                '}';
    }
}
