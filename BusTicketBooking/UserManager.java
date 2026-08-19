import java.io.*;
import java.util.*;

/**
 * UserManager — handles user registration and login.
 * Credentials are persisted to users.txt as: username,passwordHash
 * Default admin account: admin / admin123
 */
public class UserManager {

    private static final String USERS_FILE = "users.txt";

    // username -> passwordHash
    private Map<String, Integer> users = new HashMap<>();

    public UserManager() {
        ensureAdminExists();
        loadUsers();
    }

    /** Returns true if registration succeeded, false if username already taken. */
    public boolean register(String username, String password) {
        if (username == null || username.isBlank()) return false;
        if (password == null || password.length() < 4) return false;
        String key = username.trim().toLowerCase();
        if (users.containsKey(key)) return false;
        users.put(key, password.hashCode());
        saveUsers();
        return true;
    }

    /**
     * Returns the stored username (original case) if credentials match, else null.
     * Case-insensitive username comparison.
     */
    public String login(String username, String password) {
        if (username == null || password == null) return null;
        String key = username.trim().toLowerCase();
        if (!users.containsKey(key)) return null;
        if (users.get(key) == password.hashCode()) {
            // Return the display name stored; for simplicity use the key
            return key;
        }
        return null;
    }

    /** Returns true if the logged-in user is admin. */
    public static boolean isAdmin(String username) {
        return username != null && username.equalsIgnoreCase("admin");
    }

    // ---------------------------------------------------------------
    //  Private helpers
    // ---------------------------------------------------------------

    private void ensureAdminExists() {
        File f = new File(USERS_FILE);
        if (!f.exists()) {
            // Create file with default admin
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(f))) {
                bw.write("admin," + "admin123".hashCode());
                bw.newLine();
            } catch (IOException ignored) {}
        }
    }

    private void loadUsers() {
        File f = new File(USERS_FILE);
        if (!f.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                String[] parts = line.split(",", 2);
                if (parts.length == 2) {
                    try {
                        users.put(parts[0].toLowerCase(), Integer.parseInt(parts[1]));
                    } catch (NumberFormatException ignored) {}
                }
            }
        } catch (IOException ignored) {}
    }

    private void saveUsers() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(USERS_FILE))) {
            bw.write("# username,passwordHash");
            bw.newLine();
            for (Map.Entry<String, Integer> e : users.entrySet()) {
                bw.write(e.getKey() + "," + e.getValue());
                bw.newLine();
            }
        } catch (IOException ignored) {}
    }
}
