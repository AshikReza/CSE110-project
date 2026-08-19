// Custom exception thrown when a bus has no available seats
public class SeatNotAvailableException extends Exception {
    public SeatNotAvailableException(String busId) {
        super("No seats available on bus " + busId + "!");
    }
}
