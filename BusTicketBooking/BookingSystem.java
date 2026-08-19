import java.util.*;
import java.io.*;

// Custom exception thrown when a bus has no available seats
class SeatNotAvailableException extends Exception {
    public SeatNotAvailableException(String busId) {
        super("No seats available on bus " + busId + "!");
    }
}

public class BookingSystem {

    private static final String TICKET_FILE = "tickets.txt";

    private static List<Bus>    buses   = new ArrayList<>();
    private static List<Ticket> tickets = new ArrayList<>();
    private static int          ticketCounter = 1;

    private static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        initBuses();
        loadTickets();

        System.out.println("===========================================");
        System.out.println("     BUS TICKET BOOKING SYSTEM  v1.0      ");
        System.out.println("     Developed for CSE 110 Project        ");
        System.out.println("===========================================");

        boolean running = true;
        while (running) {
            printMenu();
            int choice = readInt("Enter choice: ");
            switch (choice) {
                case 1: viewBuses();     break;
                case 2: bookTicket();    break;
                case 3: viewMyTickets(); break;
                case 4: cancelTicket();  break;
                case 5:
                    running = false;
                    System.out.println("\n  Thank you! Goodbye.\n");
                    break;
                default:
                    System.out.println("  Invalid option. Try again.\n");
            }
        }
        sc.close();
    }

    private static void printMenu() {
        System.out.println("-------------------------------------------");
        System.out.println("  1. View Available Buses");
        System.out.println("  2. Book a Ticket");
        System.out.println("  3. View My Tickets");
        System.out.println("  4. Cancel a Ticket");
        System.out.println("  5. Exit");
        System.out.println("-------------------------------------------");
    }

    private static void viewBuses() {
        System.out.println("\n---- Available Buses ----------------------");
        if (buses.isEmpty()) {
            System.out.println("  No buses available.");
        } else {
            for (Bus b : buses) {
                System.out.println("  " + b);
            }
        }
        System.out.println();
    }

    private static void bookTicket() {
        viewBuses();
        System.out.print("  Enter Bus ID (e.g. B101): ");
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
            if (!selectedBus.bookSeat()) {
                throw new SeatNotAvailableException(busId);
            }

            int seatNo   = selectedBus.getTotalSeats() - selectedBus.getAvailableSeats();
            String ticketId = "TK-" + ticketCounter++;
            Ticket t = new Ticket(ticketId, name, busId,
                                  selectedBus.getRoute(),
                                  seatNo,
                                  selectedBus.getTicketPrice());
            tickets.add(t);
            saveTickets();

            System.out.println("\n  [SUCCESS] Booking Confirmed!");
            System.out.println("  ------------------------------------------");
            System.out.println(t);
            System.out.println("  ------------------------------------------\n");

        } catch (SeatNotAvailableException e) {
            System.out.println("\n  [ERROR] " + e.getMessage() + "\n");
        }
    }

    private static void viewMyTickets() {
        System.out.print("\n  Enter your name: ");
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

        Bus b = findBus(toRemove.getBusId());
        if (b != null) b.cancelSeat();

        tickets.remove(toRemove);
        saveTickets();

        System.out.println("  [SUCCESS] Ticket " + ticketId + " cancelled.\n");
    }

    // Saves all current tickets to tickets.txt
    private static void saveTickets() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(TICKET_FILE))) {
            bw.write("# ticketId,passengerName,busId,route,seatNumber,fare");
            bw.newLine();
            for (Ticket t : tickets) {
                bw.write(t.toFileString());
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("  [Warning] Could not save: " + e.getMessage());
        }
    }

    // Loads previously saved tickets from tickets.txt on startup
    private static void loadTickets() {
        File file = new File(TICKET_FILE);
        if (!file.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("#") || line.isBlank()) continue;
                try {
                    Ticket t = Ticket.fromFileString(line);
                    tickets.add(t);
                    int num = Integer.parseInt(t.getTicketId().replace("TK-", ""));
                    if (num >= ticketCounter) ticketCounter = num + 1;
                } catch (Exception e) {
                    System.out.println("  [Warning] Skipped bad record: " + line);
                }
            }
            if (!tickets.isEmpty()) {
                System.out.println("  [Info] Loaded " + tickets.size() + " saved ticket(s).\n");
            }
        } catch (IOException e) {
            System.out.println("  [Warning] Could not load: " + e.getMessage());
        }
    }

    private static void initBuses() {
        buses.add(new Bus("B101", "Green Line",       "Dhaka -> Chittagong",      40, 550));
        buses.add(new Bus("B102", "Shyamoli",         "Dhaka -> Sylhet",          35, 480));
        buses.add(new Bus("B103", "Hanif Enterprise", "Dhaka -> Rajshahi",        45, 420));
        buses.add(new Bus("B104", "S. Alam",          "Chittagong -> Cox's Bazar", 30, 350));
    }

    private static Bus findBus(String busId) {
        for (Bus b : buses)
            if (b.getBusId().equalsIgnoreCase(busId)) return b;
        return null;
    }

    // Returns -1 if input is not a valid number
    private static int readInt(String prompt) {
        System.out.print(prompt);
        try {
            return Integer.parseInt(sc.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
