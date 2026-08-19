public class Ticket {

    private String ticketId;
    private String passengerName;
    private String busId;
    private String route;
    private int    seatNumber;
    private double fare;

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

    public String getTicketId()      { return ticketId;      }
    public String getPassengerName() { return passengerName; }
    public String getBusId()         { return busId;         }
    public String getRoute()         { return route;         }
    public int    getSeatNumber()    { return seatNumber;    }
    public double getFare()          { return fare;          }

    // Converts ticket to a CSV line for saving to file
    public String toFileString() {
        return ticketId + "," + passengerName + "," + busId + ","
               + route + "," + seatNumber + "," + fare;
    }

    // Parses a CSV line from file back into a Ticket object
    public static Ticket fromFileString(String line) {
        String[] p = line.split(",", 6);
        return new Ticket(p[0], p[1], p[2], p[3],
                          Integer.parseInt(p[4]),
                          Double.parseDouble(p[5]));
    }

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
