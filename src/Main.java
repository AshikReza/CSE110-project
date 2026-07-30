import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Console entry point. Menu-driven: generate a prompt, paste the AI's
 * answer back in, then study/quiz/search/edit/delete/bookmark/shuffle
 * the resulting flashcards. No AI API is called anywhere in this file.
 */
public class Main {

    private static Deck deck = new Deck("My Deck");
    private static final Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("=====================================");
        System.out.println("   Quizfy - Console Flashcard Maker");
        System.out.println("=====================================");

        boolean running = true;
        while (running) {
            printMenu();
            String choice = sc.nextLine().trim();
            switch (choice) {
                case "1" -> generatePrompt();
                case "2" -> importFlashcards();
                case "3" -> studyFlashcards();
                case "4" -> startQuiz();
                case "5" -> searchCards();
                case "6" -> editCard();
                case "7" -> deleteCard();
                case "8" -> bookmarkCard();
                case "9" -> { deck.shuffle(); System.out.println("Deck shuffled.\n"); }
                case "10" -> saveDeck();
                case "11" -> loadDeck();
                case "0" -> { running = false; System.out.println("Goodbye!"); }
                default -> System.out.println("Invalid choice, try again.\n");
            }
        }
    }

    private static void printMenu() {
        System.out.println("Deck: " + deck.getName() + " (" + deck.size() + " cards)");
        System.out.println("1. Generate AI Prompt");
        System.out.println("2. Import Flashcards (paste AI output)");
        System.out.println("3. Study Flashcards");
        System.out.println("4. Start Quiz");
        System.out.println("5. Search Cards");
        System.out.println("6. Edit a Card");
        System.out.println("7. Delete a Card");
        System.out.println("8. Bookmark / Unbookmark a Card");
        System.out.println("9. Shuffle Deck");
        System.out.println("10. Save Deck to File");
        System.out.println("11. Load Deck from File");
        System.out.println("0. Exit");
        System.out.print("Choose an option: ");
    }

    // ---------- Generate the prompt ----------

    private static void generatePrompt() {
        System.out.print("Study topic: ");
        String topic = sc.nextLine().trim();

        System.out.print("Number of questions: ");
        int count;
        try {
            count = Integer.parseInt(sc.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("That's not a valid number. Returning to menu.\n");
            return;
        }

        System.out.print("Difficulty (Easy/Medium/Hard): ");
        String difficulty = sc.nextLine().trim();

        System.out.print("Question types (e.g. Multiple Choice, True/False, Fill in the Blank): ");
        String types = sc.nextLine().trim();

        try {
            String prompt = PromptGenerator.generate(topic, count, difficulty, types);
            System.out.println("\n--------- COPY THE PROMPT BELOW INTO CHATGPT / GEMINI / CLAUDE ---------\n");
            System.out.println(prompt);
            System.out.println("\n--------------------------------------------------------------------\n");
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid input: " + e.getMessage() + "\n");
        }
    }

    // ---------- Paste the AI's reply back in ----------

    private static void importFlashcards() {
        System.out.println("Paste the AI's response below. Type END on its own line when done:");
        StringBuilder sb = new StringBuilder();
        try {
            String line;
            while (sc.hasNextLine() && !(line = sc.nextLine()).equalsIgnoreCase("END")) {
                sb.append(line).append("\n");
            }
        } catch (java.util.NoSuchElementException ignored) {
            // EOF reached before "END" was typed — process whatever was collected
        }

        if (sb.isEmpty()) {
            System.out.println("Nothing was pasted.\n");
            return;
        }

        try {
            ArrayList<Question> parsed = Parser.parse(sb.toString());
            for (Question q : parsed) deck.add(q);
            System.out.println("Imported " + parsed.size() + " flashcards.\n");
        } catch (InvalidPromptFormatException e) {
            System.out.println("Could not parse the AI output: " + e.getMessage() + "\n");
        }
    }

    // ---------- Study / Quiz / Search / Edit / Delete / Bookmark ----------

    private static void studyFlashcards() {
        if (deck.size() == 0) { System.out.println("Deck is empty.\n"); return; }
        for (int i = 0; i < deck.size(); i++) {
            Question q = deck.getQuestions().get(i);
            System.out.println("\nCard " + (i + 1) + "/" + deck.size() + ":");
            System.out.println(q.display());
            System.out.print("Press Enter to reveal the answer...");
            sc.nextLine();
            System.out.println("Answer: " + q.showAnswer());
            if (!q.getExplanation().isBlank()) {
                System.out.println("Why: " + q.getExplanation());
            }
        }
        System.out.println();
    }

    private static void startQuiz() {
        if (deck.size() == 0) { System.out.println("Deck is empty.\n"); return; }
        int score = 0;
        for (Question q : deck.getQuestions()) {
            System.out.println("\n" + q.display());
            System.out.print("Your answer: ");
            String userAnswer = sc.nextLine();
            try {
                if (q.checkAnswer(userAnswer)) {
                    System.out.println("Correct!");
                    score++;
                } else {
                    System.out.println("Wrong. Correct answer: " + q.showAnswer());
                }
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid answer format: " + e.getMessage());
            }
        }
        System.out.println("\nQuiz finished! Score: " + score + "/" + deck.size() + "\n");
    }

    private static void searchCards() {
        System.out.print("Search keyword: ");
        String keyword = sc.nextLine().trim();
        ArrayList<Question> results = deck.search(keyword);
        if (results.isEmpty()) {
            System.out.println("No matches found.\n");
        } else {
            for (Question q : results) System.out.println(" - " + q);
            System.out.println();
        }
    }

    private static void editCard() {
        int index = pickCardIndex();
        if (index == -1) return;
        Question q = deck.getQuestions().get(index);
        System.out.println("Current text: " + q.getQuestionText());
        System.out.print("New question text (Enter to keep unchanged): ");
        String newText = sc.nextLine();
        if (!newText.isBlank()) {
            try {
                q.setQuestionText(newText.trim());
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid edit: " + e.getMessage());
            }
        }
        System.out.println("Card updated.\n");
    }

    private static void deleteCard() {
        int index = pickCardIndex();
        if (index == -1) return;
        deck.remove(index);
        System.out.println("Card deleted.\n");
    }

    private static void bookmarkCard() {
        int index = pickCardIndex();
        if (index == -1) return;
        Question q = deck.getQuestions().get(index);
        q.setBookmarked(!q.isBookmarked());
        System.out.println(q.isBookmarked() ? "Bookmarked.\n" : "Bookmark removed.\n");
    }

    private static int pickCardIndex() {
        if (deck.size() == 0) { System.out.println("Deck is empty.\n"); return -1; }
        for (int i = 0; i < deck.size(); i++) {
            System.out.println((i + 1) + ". " + deck.getQuestions().get(i));
        }
        System.out.print("Pick a card number: ");
        try {
            int num = Integer.parseInt(sc.nextLine().trim());
            if (num < 1 || num > deck.size()) {
                System.out.println("Out of range.\n");
                return -1;
            }
            return num - 1;
        } catch (NumberFormatException e) {
            System.out.println("Not a valid number.\n");
            return -1;
        }
    }

    // ---------- File handling ----------

    private static void saveDeck() {
        System.out.print("File name to save as (e.g. mydeck.dat): ");
        String path = sc.nextLine().trim();
        try {
            deck.saveToFile(path);
            System.out.println("Deck saved to " + path + "\n");
        } catch (IOException e) {
            System.out.println("Could not save file: " + e.getMessage() + "\n");
        }
    }

    private static void loadDeck() {
        System.out.print("File name to load (e.g. mydeck.dat): ");
        String path = sc.nextLine().trim();
        try {
            deck = Deck.loadFromFile(path);
            System.out.println("Loaded deck \"" + deck.getName() + "\" with " + deck.size() + " cards.\n");
        } catch (IOException e) {
            System.out.println("Could not read file: " + e.getMessage() + "\n");
        } catch (ClassNotFoundException e) {
            System.out.println("File format not recognized.\n");
        }
    }
}
