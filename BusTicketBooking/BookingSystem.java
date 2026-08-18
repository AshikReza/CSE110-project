import java.util.*;
import java.io.*;

/**
 * BookingSystem.java  <-- MAIN ENTRY POINT
 * ===========================================================
 *  This single file ties everything together and shows:
 *
 *  OOP Concepts:
 *    - Abstraction   : Vehicle (abstract class)
 *    - Inheritance   : Bus extends Vehicle
 *    - Encapsulation : private fields + getters in all classes
 *    - Polymorphism  : toString() calls overridden getDetails()
 *
 *  File Handling    : tickets saved/loaded from tickets.txt
 *
 *  Exception Handling:
 *    - Custom exception: SeatNotAvailableException
 *    - try-catch for file I/O and invalid user input
 *
 *  Collections     : ArrayList<Bus>, ArrayList<Ticket>
 * ===========================================================
 */

// CUSTOM EXCEPTION (Exception Handling)
class SeatNotAvailableException extends Exception {
    public SeatNotAvailableException(String busId) {
        super("No seats available on bus " + busId + "!");
    }
}

// MAIN APPLICATION CLASS
public class BookingSystem {

    // Constants
    private static final String TICKET_FILE = "tickets.txt";

    // Data stores
    private static List<Bus>    buses   = new ArrayList<>();
    private static List<Ticket> tickets = new ArrayList<>();
    private static int          ticketCounter = 1;

    private static Scanner sc = new Scanner(System.in);

    // =========================================================
    //  MAIN METHOD
    // =========================================================
    public static void main(String[] args) {

        initBuses();    // load sample buses
        loadTickets();  // load previously saved tickets from file

        System.out.println("===========================================");
        System.out.println("     BUS TICKET BOOKING SYSTEM  v1.0      ");
        System.out.println("     Developed for CSE 110 Project        ");
        System.out.println("===========================================");

        boolean running = true;
        while (running) {
            printMenu();
            int choice = readInt("Enter choice: ");
            switch (choice) {
                case 1: viewBuses();    break;
                case 2: bookTicket();   break;
                case 3: viewMyTickets(); break;
                case 4: cancelTicket(); break;
                case 5:
                    running = false;
                    System.out.println("\n  Thank you for using our system! Goodbye.\n");
                    break;
                default:
                    System.out.println("  Invalid option. Please try again.\n");
            }
        }
        sc.close();
    }

    // =========================================================
    //  MENU
    // =========================================================
    private static void printMenu() {
        System.out.println("-------------------------------------------");
        System.out.println("  1. View Available Buses");
        System.out.println("  2. Book a Ticket");
        System.out.println("  3. View My Tickets");
        System.out.println("  4. Cancel a Ticket");
        System.out.println("  5. Exit");
        System.out.println("-------------------------------------------");
    }

    // =========================================================
    //  FEATURE 1 - VIEW BUSES
    //  Calls bus.toString() which calls overridden getDetails()
    //  -> demonstrates POLYMORPHISM
    // =========================================================
    private static void viewBuses() {
        System.out.println("\n---- Available Buses ----------------------");
        if (buses.isEmpty()) {
            System.out.println("  No buses available.");
        } else {
            for (Bus b : buses) {
                System.out.println("  " + b); // calls getDetails() via toString()
            }
        }
        System.out.println();
    }

    // =========================================================
    //  FEATURE 2 - BOOK A TICKET
    //  Shows: custom exception, file handling, OOP usage
    // =========================================================
    private static void bookTicket() {
        viewBuses();
        System.out.print("  Enter Bus ID to book (e.g. B101): ");
        String busId = sc.nextLine().trim().toUpperCase();

        Bus selectedBus = findBus(busId);
        if (selectedBus == null) {
            System.out.println("  Bus not found.\n");
            return;
        }

        System.out.print("  Enter your name: ");
        String name = sc.nextLine().trim();
        if (name.isEmpty()) {
            System.out.println("  Name cannot be empty.\n");
            return;
        }

        try {
            // Throws custom exception if no seats left
            if (!selectedBus.bookSeat()) {
                throw new SeatNotAvailableException(busId);
            }

            // Calculate assigned seat number
            int seatNo = selectedBus.getTotalSeats()
                       - selectedBus.getAvailableSeats();

            // Create ticket object
            String ticketId = "TK-" + ticketCounter++;
            Ticket t = new Ticket(ticketId, name, busId,
                                  selectedBus.getRoute(),
                                  seatNo,
                                  selectedBus.getTicketPrice());
            tickets.add(t);
            saveTickets(); // FILE HANDLING: persist to disk

            System.out.println("\n  [SUCCESS] Booking Confirmed! Your ticket:");
            System.out.println("  ==========================================");
            System.out.println(t);
            System.out.println("  ==========================================\n");

        } catch (SeatNotAvailableException e) {
            // EXCEPTION HANDLING: custom exception caught here
            System.out.println("\n  [ERROR] " + e.getMessage() + "\n");
        }
    }

    // =========================================================
    //  FEATURE 3 - VIEW MY TICKETS
    // =========================================================
    private static void viewMyTickets() {
        System.out.print("\n  Enter your name to search tickets: ");
        String name = sc.nextLine().trim();

        boolean found = false;
        for (Ticket t : tickets) {
            if (t.getPassengerName().equalsIgnoreCase(name)) {
                System.out.println("  ------------------------------------------");
                System.out.println(t);
                System.out.println("  ------------------------------------------");
                found = true;
            }
        }
        if (!found) {
            System.out.println("  No tickets found for '" + name + "'.\n");
        } else {
            System.out.println();
        }
    }

    // =========================================================
    //  FEATURE 4 - CANCEL A TICKET
    // =========================================================
    private static void cancelTicket() {
        System.out.print("\n  Enter Ticket ID to cancel (e.g. TK-1): ");
        String ticketId = sc.nextLine().trim();

        Ticket toRemove = null;
        for (Ticket t : tickets) {
            if (t.getTicketId().equalsIgnoreCase(ticketId)) {
                toRemove = t;
                break;
            }
        }

        if (toRemove == null) {
            System.out.println("  Ticket not found.\n");
            return;
        }

        // Free up the seat on the bus
        Bus b = findBus(toRemove.getBusId());
        if (b != null) b.cancelSeat();

        tickets.remove(toRemove);
        saveTickets(); // update file after cancellation

        System.out.println("  [SUCCESS] Ticket " + ticketId + " cancelled.\n");
    }

    // =========================================================
    //  FILE HANDLING - Save all tickets to tickets.txt
    // =========================================================
    private static void saveTickets() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(TICKET_FILE))) {
            bw.write("# ticketId,passengerName,busId,route,seatNumber,fare");
            bw.newLine();
            for (Ticket t : tickets) {
                bw.write(t.toFileString());
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("  [Warning] Could not save tickets: " + e.getMessage());
        }
    }

    // =========================================================
    //  FILE HANDLING - Load saved tickets from tickets.txt
    // =========================================================
    private static void loadTickets() {
        File file = new File(TICKET_FILE);
        if (!file.exists()) return; // first run - no file yet

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("#") || line.isBlank()) continue;
                try {
                    Ticket t = Ticket.fromFileString(line);
                    tickets.add(t);
                    // keep counter ahead of highest existing ticket number
                    int num = Integer.parseInt(t.getTicketId().replace("TK-", ""));
                    if (num >= ticketCounter) ticketCounter = num + 1;
                } catch (Exception e) {
                    // Skip corrupted lines gracefully (Exception Handling)
                    System.out.println("  [Warning] Skipped bad record: " + line);
                }
            }
            if (!tickets.isEmpty()) {
                System.out.println("  [Info] Loaded " + tickets.size() + " saved ticket(s).\n");
            }
        } catch (IOException e) {
            System.out.println("  [Warning] Could not load tickets: " + e.getMessage());
        }
    }

    // =========================================================
    //  HELPERS
    // =========================================================

    /** Initialize sample buses. */
    private static void initBuses() {
        buses.add(new Bus("B101", "Green Line",      "Dhaka -> Chittagong",     40, 550));
        buses.add(new Bus("B102", "Shyamoli",        "Dhaka -> Sylhet",         35, 480));
        buses.add(new Bus("B103", "Hanif Enterprise","Dhaka -> Rajshahi",       45, 420));
        buses.add(new Bus("B104", "S. Alam",         "Chittagong -> Cox's Bazar", 30, 350));
    }

    /** Find a Bus by its ID (case-insensitive). */
    private static Bus findBus(String busId) {
        for (Bus b : buses)
            if (b.getBusId().equalsIgnoreCase(busId)) return b;
        return null;
    }

    /**
     * Safely reads an integer from the console.
     * Demonstrates EXCEPTION HANDLING for bad input.
     */
    private static int readInt(String prompt) {
        System.out.print(prompt);
        try {
            return Integer.parseInt(sc.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1; // invalid -> switch hits default
        }
    }
}
