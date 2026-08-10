import java.io.Serializable;

/**
 * All question types live in this one file to keep the project small.
 * Question is the abstract parent; the three classes below it are the
 * children. Java allows more than one class per file as long as only
 * one of them is "public" (that one must match the file name).
 *
 *      Question (abstract)
 *        |-- MCQQuestion
 *        |-- TrueFalseQuestion
 *        |-- FillBlankQuestion
 */
public abstract class Question implements Serializable, Reviewable {

    protected String questionText;
    protected String explanation;
    protected boolean bookmarked;

    public Question(String questionText, String explanation) throws InvalidQuestionTextException {
        if (questionText == null || questionText.isBlank()) {
            throw new InvalidQuestionTextException("Question text cannot be empty.");
        }
        this.questionText = questionText;
        this.explanation = (explanation == null) ? "" : explanation;
        this.bookmarked = false;
    }

    // Each child class fills these in its own way -> polymorphism.
    public abstract String display();
    public abstract boolean checkAnswer(String userAnswer);
    public abstract String showAnswer();

    public String getQuestionText() { return questionText; }
    public void setQuestionText(String t) throws InvalidQuestionTextException {
        if (t == null || t.isBlank()) {
            throw new InvalidQuestionTextException("Question text cannot be set to empty.");
        }
        this.questionText = t;
    }
    public String getExplanation() { return explanation; }
    public boolean isBookmarked() { return bookmarked; }
    public void setBookmarked(boolean b) { this.bookmarked = b; }

    @Override
    public String toString() {
        return (bookmarked ? "* " : "") + questionText;
    }
}

/** Multiple choice question with 4 options. */
class MCQQuestion extends Question {

    private String optionA, optionB, optionC, optionD;
    private char correctOption;

    public MCQQuestion(String questionText, String explanation,
                        String a, String b, String c, String d, char correct)
            throws InvalidQuestionTextException {
        super(questionText, explanation);
        optionA = a;
        optionB = b;
        optionC = c;
        optionD = d;
        correctOption = Character.toUpperCase(correct);
        if ("ABCD".indexOf(correctOption) == -1) {
            throw new IllegalArgumentException("MCQ answer must be A, B, C or D.");
        }
    }

    @Override
    public String display() {
        return questionText + "\n  A) " + optionA + "\n  B) " + optionB
                + "\n  C) " + optionC + "\n  D) " + optionD;
    }

    @Override
    public boolean checkAnswer(String userAnswer) {
        if (userAnswer == null || userAnswer.isBlank()) return false;
        return Character.toUpperCase(userAnswer.trim().charAt(0)) == correctOption;
    }

    @Override
    public String showAnswer() {
        String text = switch (correctOption) {
            case 'A' -> optionA;
            case 'B' -> optionB;
            case 'C' -> optionC;
            default -> optionD;
        };
        return correctOption + ") " + text;
    }
}

/** A statement the user judges true or false. */
class TrueFalseQuestion extends Question {

    private boolean correctAnswer;

    public TrueFalseQuestion(String questionText, String explanation, boolean correctAnswer)
            throws InvalidQuestionTextException {
        super(questionText, explanation);
        this.correctAnswer = correctAnswer;
    }

    @Override
    public String display() {
        return questionText + "\n  (Type True or False)";
    }

    @Override
    public boolean checkAnswer(String userAnswer) {
        if (userAnswer == null) return false;
        String clean = userAnswer.trim().toLowerCase();
        if (clean.startsWith("t")) return correctAnswer;
        if (clean.startsWith("f")) return !correctAnswer;
        throw new IllegalArgumentException("Answer must start with True or False.");
    }

    @Override
    public String showAnswer() {
        return correctAnswer ? "True" : "False";
    }
}

/** A short-answer question graded by a loose text match. */
class FillBlankQuestion extends Question {

    private String correctAnswer;

    public FillBlankQuestion(String questionText, String explanation, String correctAnswer)
            throws InvalidQuestionTextException {
        super(questionText, explanation);
        if (correctAnswer == null || correctAnswer.isBlank()) {
            throw new InvalidQuestionTextException("Fill-in-the-blank answer cannot be empty.");
        }
        this.correctAnswer = correctAnswer;
    }

    @Override
    public String display() {
        return questionText + "\n  (Fill in the blank)";
    }

    @Override
    public boolean checkAnswer(String userAnswer) {
        if (userAnswer == null) return false;
        return userAnswer.trim().equalsIgnoreCase(correctAnswer.trim());
    }

    @Override
    public String showAnswer() {
        return correctAnswer;
    }
}
