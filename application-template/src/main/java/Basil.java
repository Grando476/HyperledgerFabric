// Simplified Client Side Basil
public class Basil {
    private String qr;
    private String extraInfo;
    private String owner;
    private BasilLeg currentLeg; // Matches the chaincode structure

    // ... Constructor ...

    @Override
    public String toString() {
        String gps = (currentLeg != null) ? currentLeg.getGpsPosition() : "None";
        return "Basil [QR=" + qr + ", Info=" + extraInfo + ", Owner=" + owner + ", GPS=" + gps + "]";
    }
    
    // Inner class for the Leg (Client Side)
    public static class BasilLeg {
        private String gpsPosition;
        public String getGpsPosition() { return gpsPosition; }
    }
}