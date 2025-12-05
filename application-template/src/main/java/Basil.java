public class Basil {
    private String qr;
    private String extraInfo;
    private String owner;

    public Basil(String qr, String extraInfo, String owner) {
        this.qr = qr;
        this.extraInfo = extraInfo;
        this.owner = owner;
    }

    public String getQr() { return qr; }
    public String getExtraInfo() { return extraInfo; }
    public String getOwner() { return owner; }

    @Override
    public String toString() {
        return "Basil [QR=" + qr + ", Info=" + extraInfo + ", Owner=" + owner + "]";
    }
}