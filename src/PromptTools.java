import java.util.ArrayList;
import java.util.HashMap;

/**
 * Everything related to talking to (well, copy-pasting with) an AI lives
 * in this one file: building the prompt text, and reading the AI's reply
 * back into Question objects. No network calls anywhere here.
 */

/** Builds the text the user copies into ChatGPT / Gemini / Claude. */
class PromptGenerator {

    public static String generate(String topic, int count, String difficulty, String types) {
        if (topic == null || topic.isBlank()) {
            throw new IllegalArgumentException("Topic cannot be empty.");
        }
        if (count <= 0) {
            throw new IllegalArgumentException("Number of questions must be greater than zero.");
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Generate ").append(count).append(" flashcards about ").append(topic).append(".\n\n");
        sb.append("Difficulty: ").append(difficulty).append("\n");
        sb.append("Question Types: ").append(types).append("\n\n");
        sb.append("Return the output ONLY in this exact format, one block per question,\n");
        sb.append("with each field on its own line, separated by a line containing only ---\n\n");
        sb.append("Question: ...\n");
        sb.append("Type: (MCQ | TRUE_FALSE | FILL_BLANK)\n");
        sb.append("Option A: ...   (MCQ only)\n");
        sb.append("Option B: ...   (MCQ only)\n");
        sb.append("Option C: ...   (MCQ only)\n");
        sb.append("Option D: ...   (MCQ only)\n");
        sb.append("Answer: ...    (letter for MCQ, True/False, or the exact word/phrase)\n");
        sb.append("Explanation: ...\n---\n\n");
        sb.append("Repeat until all ").append(count).append(" questions are complete.");
        return sb.toString();
    }
}

/**
 * Reads the pasted AI text back into Question objects.
 * Works line by line - no regular expressions - so it is easy to follow:
 *   1. Split the whole paste into blocks wherever a "---" line appears.
 *   2. Inside each block, look at every line. If it starts with a known
 *      label like "Question:" or "Answer:", remember that value.
 *   3. Once a block is fully read, build the right Question subclass.
 */
class Parser {

    private static final String[] LABELS = {
            "Question", "Type", "Option A", "Option B", "Option C", "Option D", "Answer", "Explanation"
    };

    public static ArrayList<Question> parse(String rawText) throws InvalidPromptFormatException {
        if (rawText == null || rawText.isBlank()) {
            throw new InvalidPromptFormatException("Pasted text is empty.");
        }

        ArrayList<Question> result = new ArrayList<>();
        ArrayList<String> currentBlockLines = new ArrayList<>();

        for (String line : rawText.split("\n")) {
            if (line.trim().matches("^-{3,}$")) {
                addBlockIfNotEmpty(currentBlockLines, result);
                currentBlockLines = new ArrayList<>();
            } else {
                currentBlockLines.add(line);
            }
        }
        addBlockIfNotEmpty(currentBlockLines, result);

        if (result.isEmpty()) {
            throw new InvalidPromptFormatException("No question blocks were found in the pasted text.");
        }
        return result;
    }

    private static void addBlockIfNotEmpty(ArrayList<String> lines, ArrayList<Question> result)
            throws InvalidPromptFormatException {
        if (lines.stream().allMatch(String::isBlank)) return;
        result.add(buildQuestionFromLines(lines));
    }

    /** Turns the raw lines of one block into a label -> value map, then builds a Question. */
    private static Question buildQuestionFromLines(ArrayList<String> lines) throws InvalidPromptFormatException {
        HashMap<String, String> fields = new HashMap<>();

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;

            String matchedLabel = findLabel(trimmed);
            if (matchedLabel != null) {
                String value = trimmed.substring(matchedLabel.length() + 1).trim();
                fields.put(matchedLabel.toUpperCase(), value);
            }
        }

        String questionText = fields.get("QUESTION");
        String type = fields.getOrDefault("TYPE", "").toUpperCase();
        String answer = fields.get("ANSWER");
        String explanation = fields.getOrDefault("EXPLANATION", "");

        if (questionText == null || questionText.isBlank()) {
            throw new InvalidPromptFormatException("A block is missing 'Question:'.");
        }
        if (answer == null || answer.isBlank()) {
            throw new InvalidPromptFormatException("Question \"" + questionText + "\" is missing 'Answer:'.");
        }

        try {
            if (type.contains("MCQ") || fields.containsKey("OPTION A")) {
                String a = require(fields, "OPTION A");
                String b = require(fields, "OPTION B");
                String c = require(fields, "OPTION C");
                String d = require(fields, "OPTION D");
                char letter = answer.trim().toUpperCase().charAt(0);
                return new MCQQuestion(questionText, explanation, a, b, c, d, letter);
            } else if (type.contains("TRUE") || type.contains("FALSE")) {
                boolean value = answer.trim().toLowerCase().startsWith("t");
                return new TrueFalseQuestion(questionText, explanation, value);
            } else {
                return new FillBlankQuestion(questionText, explanation, answer.trim());
            }
        } catch (IllegalArgumentException iae) {
            throw new InvalidPromptFormatException(
                    "Could not build question \"" + questionText + "\": " + iae.getMessage());
        } catch (InvalidQuestionTextException iqte) {
            throw new InvalidPromptFormatException(
                    "Invalid question text in block \"" + questionText + "\": " + iqte.getMessage());
        }
    }

    /** Checks if a line starts with one of our known labels (e.g. "Question:") and returns that label. */
    private static String findLabel(String line) {
        for (String label : LABELS) {
            if (line.toLowerCase().startsWith(label.toLowerCase() + ":")) {
                return label;
            }
        }
        return null;
    }

    private static String require(HashMap<String, String> fields, String key) throws InvalidPromptFormatException {
        String v = fields.get(key);
        if (v == null || v.isBlank()) {
            throw new InvalidPromptFormatException("MCQ block is missing '" + key + ":'.");
        }
        return v;
    }
}
