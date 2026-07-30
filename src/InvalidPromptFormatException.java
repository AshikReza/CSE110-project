/**
 * Thrown when text pasted back from an AI chatbot doesn't match the
 * format we asked for (missing Question / Answer / Option lines).
 */
public class InvalidPromptFormatException extends Exception {
    public InvalidPromptFormatException(String message) {
        super(message);
    }
}
