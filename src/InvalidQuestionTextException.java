/**
 * Thrown when a question's text is null, blank, or otherwise invalid
 * (e.g. when constructing a Question or when the user tries to edit
 * a card with an empty replacement text).
 */
public class InvalidQuestionTextException extends Exception {
    public InvalidQuestionTextException(String message) {
        super(message);
    }
}
