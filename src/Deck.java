import java.io.*;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Holds all flashcards in an ArrayList and knows how to save/load
 * itself to disk using plain Java Serialization.
 */
public class Deck implements Serializable {

    private String name;
    private ArrayList<Question> questions;

    public Deck(String name) {
        this.name = name;
        this.questions = new ArrayList<>();
    }

    public void add(Question q) { questions.add(q); }

    public void remove(int index) { questions.remove(index); }

    public ArrayList<Question> search(String keyword) {
        ArrayList<Question> results = new ArrayList<>();
        String needle = keyword.toLowerCase();
        for (Question q : questions) {
            if (q.getQuestionText().toLowerCase().contains(needle)) {
                results.add(q);
            }
        }
        return results;
    }

    public void shuffle() { Collections.shuffle(questions); }

    public ArrayList<Question> getQuestions() { return questions; }
    public String getName() { return name; }
    public int size() { return questions.size(); }

    public void saveToFile(String filePath) throws IOException {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filePath))) {
            out.writeObject(this);
        }
    }

    public static Deck loadFromFile(String filePath) throws IOException, ClassNotFoundException {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(filePath))) {
            return (Deck) in.readObject();
        }
    }
}
