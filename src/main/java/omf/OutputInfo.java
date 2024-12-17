package omf;

public class OutputInfo {
    public double speed;
    public double xSpeed;
    public double zSpeed;
    public double usedX;
    public double usedZ;
    public double allowedX;
    public double allowedZ;

    public OutputInfo(double speed, double xSpeed, double zSpeed, double usedX, double usedZ, double allowedX, double allowedZ) {
        this.speed = speed;
        this.xSpeed = xSpeed;
        this.zSpeed = zSpeed;
        this.usedX = usedX;
        this.usedZ = usedZ;
        this.allowedX = allowedX;
        this.allowedZ = allowedZ;
    }

    @Override
    public String toString() {
        return  "Speed: " + speed + "\n" +
                "X Speed: " + xSpeed + "\n" +
                "Z Speed: " + zSpeed + "\n" +
                "X Unused momentum: " + (allowedX - usedX) + " (" + usedX + "bm)\n" +
                "Z Unused momentum: " + (allowedZ - usedZ) + " (" + usedZ + "bm)";
    }
}
