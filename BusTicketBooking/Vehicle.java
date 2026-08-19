public abstract class Vehicle {

    private String vehicleType;
    private String operatorName;

    public Vehicle(String vehicleType, String operatorName) {
        this.vehicleType  = vehicleType;
        this.operatorName = operatorName;
    }

    public String getVehicleType()  { return vehicleType;  }
    public String getOperatorName() { return operatorName; }

    public abstract String getDetails();

    @Override
    public String toString() {
        return getDetails();
    }
}
