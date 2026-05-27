package il.ac.hit.xpool;

/**
 * Custom checked exception for the xpool project.
 * Thrown when an error occurs during task execution or pool operations.
 */
public class XpoolException extends RuntimeException {

    /**
     * Constructs a new XpoolException with the specified detail message.
     *
     * @param message the detail message
     */
    public XpoolException(String message) {
        super(message);
    }

    /**
     * Constructs a new XpoolException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause   the cause of the exception
     */
    public XpoolException(String message, Throwable cause) {
        super(message, cause);
    }
}
