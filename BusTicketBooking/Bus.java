/**
 * Bus.java
 * ===========================================================
 * CONCRETE CLASS -> demonstrates INHERITANCE and POLYMORPHISM.
 *
 * Bus extends Vehicle (INHERITANCE).
 * It overrides getDetails() (POLYMORPHISM / method overriding).
 * All fields are private (ENCAPSULATION).
 * ===========================================================
 */
public class Bus extends Vehicle {

    // Private fields (ENCAPSULATION)
    private String busId;           // Unique ID e.g. "B101"
    private String route;           // e.g. "Dhaka -> Chittagong"
    private int    totalSeats;      // Total seat capacity
    private int    availableSeats;  // Seats still free
    private double ticketPrice;     // Price per seat (BDT)

    // Constructor
    public Bus(String busId, String operatorName,
               String route, int totalSeats, double ticketPrice) {

        super("Bus", operatorName);   // call parent constructor
        this.busId          = busId;
        this.route          = route;
        this.totalSeats     = totalSeats;
        this.availableSeats = totalSeats; // all seats free at start
        this.ticketPrice    = ticketPrice;
    }

    // Getters
    public String getBusId()          { return busId;          }
    public String getRoute()          { return route;          }
    public int    getTotalSeats()     { return totalSeats;     }
    public int    getAvailableSeats() { return availableSeats; }
    public double getTicketPrice()    { return ticketPrice;    }

    // Business Logic

    /** Reserve one seat. Returns false if no seats available. */
    public boolean bookSeat() {
        if (availableSeats > 0) {
            availableSeats--;
            return true;
        }
        return false;
    }

    /** Cancel one seat reservation. */
    public void cancelSeat() {
        if (availableSeats < totalSeats) {
            availableSeats++;
        }
    }

    // OVERRIDING abstract method (POLYMORPHISM)
    @Override
    public String getDetails() {
        return String.format(
            "[%s] %-16s | Route: %-25s | Available: %2d/%2d | Price: %.0f BDT",
            busId, getOperatorName(), route,
            availableSeats, totalSeats, ticketPrice
        );
    }
}
