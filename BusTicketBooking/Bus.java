public class Bus extends Vehicle {

    private String busId;
    private String route;
    private int    totalSeats;
    private int    availableSeats;
    private double ticketPrice;

    public Bus(String busId, String operatorName,
               String route, int totalSeats, double ticketPrice) {
        super("Bus", operatorName);
        this.busId          = busId;
        this.route          = route;
        this.totalSeats     = totalSeats;
        this.availableSeats = totalSeats;
        this.ticketPrice    = ticketPrice;
    }

    public String getBusId()          { return busId;          }
    public String getRoute()          { return route;          }
    public int    getTotalSeats()     { return totalSeats;     }
    public int    getAvailableSeats() { return availableSeats; }
    public double getTicketPrice()    { return ticketPrice;    }

    // Returns false if no seats are available
    public boolean bookSeat() {
        if (availableSeats > 0) {
            availableSeats--;
            return true;
        }
        return false;
    }

    public void cancelSeat() {
        if (availableSeats < totalSeats) {
            availableSeats++;
        }
    }

    @Override
    public String getDetails() {
        return String.format(
            "[%s] %-16s | Route: %-25s | Available: %2d/%2d | Price: %.0f BDT",
            busId, getOperatorName(), route,
            availableSeats, totalSeats, ticketPrice
        );
    }
}
