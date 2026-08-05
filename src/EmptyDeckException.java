/**
 * Thrown when an operation (study, quiz, search, edit, delete, bookmark)
 * is attempted on a Deck that contains no flashcards.
 */
public class EmptyDeckException extends Exception {
    public EmptyDeckException() {
        super("The deck is empty. Please import flashcards first.");
    }

    public EmptyDeckException(String message) {
        super(message);
    }
}
