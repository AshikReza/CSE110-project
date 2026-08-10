/**
 * Defines the contract for any item that can be reviewed (studied or quizzed).
 * A Reviewable object must be able to display itself, check a user's answer,
 * and reveal the correct answer.
 *
 * Currently implemented by: Question (and its subclasses MCQQuestion,
 * TrueFalseQuestion, FillBlankQuestion).
 */
public interface Reviewable {

    /** Returns a formatted string showing the question to the user. */
    String display();

    /** Checks if the user's answer is correct. */
    boolean checkAnswer(String userAnswer);

    /** Returns the correct answer as a readable string. */
    String showAnswer();
}
