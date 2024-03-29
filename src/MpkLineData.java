import java.time.LocalTime;

public class MpkLineData {

    private String lineName;
    private LocalTime departureTime;
    private LocalTime arrivalTime;
    private double distance;
    private double xCurrent;
    private double yCurrent;
    private double xNext;
    private double yNext;

    public double getxCurrent() {
        return xCurrent;
    }

    public void setxCurrent(double xCurrent) {
        this.xCurrent = xCurrent;
    }

    public double getyCurrent() {
        return yCurrent;
    }

    public void setyCurrent(double yCurrent) {
        this.yCurrent = yCurrent;
    }

    public double getxNext() {
        return xNext;
    }

    public void setxNext(double xNext) {
        this.xNext = xNext;
    }

    public double getyNext() {
        return yNext;
    }

    public void setyNext(double yNext) {
        this.yNext = yNext;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }



    public MpkLineData(String lineName, String departureTime, String arrivalTime, double xCurrent, double yCurrent, double xNext, double yNext) {
        this.lineName = lineName;
        this.departureTime = LocalTime.parse(departureTime);
        this.arrivalTime = LocalTime.parse(arrivalTime);
        this.xCurrent = xCurrent;
        this.yCurrent = yCurrent;
        this.xNext = xNext;
        this.yNext = yNext;
        this.distance = CountDistance.distance(xCurrent, yCurrent, xNext, yNext);
    }
    public String getLineName() {
        return lineName;
    }

    public LocalTime getDepartureTime() {
        return departureTime;
    }

    public LocalTime getArrivalTime() {
        return arrivalTime;
    }
    public int getTravelTime(){
        return getArrivalTime().toSecondOfDay() - getDepartureTime().toSecondOfDay();
    }

    @Override
    public String toString(){
        return "Line name: " + lineName + ", Departure time: " + departureTime.toString() + ", Arrival time: " + arrivalTime.toString();
    }
}
