/**
 * Vehicle.java
 * ===========================================================
 * ABSTRACT CLASS -> demonstrates ABSTRACTION in Java OOP.
 *
 * Any real-world vehicle must be able to describe itself.
 * We force every subclass to implement getDetails() by
 * declaring it abstract here.
 * ===========================================================
 */
public abstract class Vehicle {

    // ENCAPSULATION: fields are private, accessed via getters
    private String vehicleType;   // e.g. "Bus"
    private String operatorName;  // e.g. "Green Line"

    // Constructor
    public Vehicle(String vehicleType, String operatorName) {
        this.vehicleType  = vehicleType;
        this.operatorName = operatorName;
    }

    // Getters
    public String getVehicleType()  { return vehicleType;  }
    public String getOperatorName() { return operatorName; }

    // ABSTRACT METHOD - subclasses MUST override this
    public abstract String getDetails();

    // toString() calls getDetails() -> POLYMORPHISM at work
    @Override
    public String toString() {
        return getDetails();
    }
}
