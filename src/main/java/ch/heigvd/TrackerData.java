package ch.heigvd;

public class TrackerData {

    private String trackerId;
    private long timestamp;
    private double latitude;
    private double longitude;
    private int batteryLevel;

    public TrackerData(String trackerId, long timestamp, double latitude, double longitude, int batteryLevel) {
        this.trackerId = trackerId;
        this.timestamp = timestamp;
        this.latitude = latitude;
        this.longitude = longitude;
        this.batteryLevel = batteryLevel;
    }

    @Override
    public String toString() {
        return "TrackerData{" +
                "trackerId='" + trackerId + '\'' +
                ", timestamp=" + timestamp +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", batteryLevel=" + batteryLevel +
                '}';
    }
}
