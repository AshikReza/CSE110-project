/**
 * Ticket.java
 * ===========================================================
 * Plain class for a booked ticket -- pure ENCAPSULATION.
 *
 * Each Ticket holds one passenger's booking info.
 * Objects of this class are written to / read from a file
 * (FILE HANDLING demonstrated in BookingSystem.java).
 * ===========================================================
 */
public class Ticket {

    // Private fields
    private String ticketId;      // Auto-generated e.g. "TK-1"
    private String passengerName; // Customer name
    private String busId;         // Which bus
    private String route;         // Route for quick display
    private int    seatNumber;    // Assigned seat number
    private double fare;          // Amount paid (BDT)

    // Constructor
    public Ticket(String ticketId, String passengerName,
                  String busId, String route,
                  int seatNumber, double fare) {

        this.ticketId      = ticketId;
        this.passengerName = passengerName;
        this.busId         = busId;
        this.route         = route;
        this.seatNumber    = seatNumber;
        this.fare          = fare;
    }

    // Getters
    public String getTicketId()      { return ticketId;      }
    public String getPassengerName() { return passengerName; }
    public String getBusId()         { return busId;         }
    public String getRoute()         { return route;         }
    public int    getSeatNumber()    { return seatNumber;    }
    public double getFare()          { return fare;          }

    /**
     * Converts ticket data to a comma-separated line
     * so it can be saved in tickets.txt (File Handling).
     * Format: ticketId,passengerName,busId,route,seatNumber,fare
     */
    public String toFileString() {
        return ticketId + "," + passengerName + "," + busId + ","
               + route + "," + seatNumber + "," + fare;
    }

    /**
     * Parses one line from tickets.txt back into a Ticket object.
     * Called by BookingSystem when loading saved tickets.
     */
    public static Ticket fromFileString(String line) {
        String[] p = line.split(",", 6); // max 6 parts
        return new Ticket(p[0], p[1], p[2], p[3],
                          Integer.parseInt(p[4]),
                          Double.parseDouble(p[5]));
    }

    // Nicely formatted receipt string
    @Override
    public String toString() {
        return String.format(
            "  Ticket ID    : %s%n"
          + "  Passenger    : %s%n"
          + "  Bus ID       : %s%n"
          + "  Route        : %s%n"
          + "  Seat No.     : %d%n"
          + "  Fare Paid    : %.0f BDT",
            ticketId, passengerName, busId, route, seatNumber, fare
        );
    }
}
