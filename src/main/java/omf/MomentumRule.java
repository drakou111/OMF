package omf;

public class MomentumRule {
    public double width; // X
    public double length; // Z
    public boolean frontWall;
    public boolean rightWall;
    public boolean backWall;
    public boolean leftWall;

    public MomentumRule(double width, double length, boolean frontWall, boolean rightWall, boolean backWall, boolean leftWall) {
        this.width = width;
        this.length = length;
        this.frontWall = frontWall;
        this.rightWall = rightWall;
        this.backWall = backWall;
        this.leftWall = leftWall;
    }

    public MomentumRule(double width, double length) {
        this.width = width;
        this.length = length;
        this.frontWall = false;
        this.rightWall = false;
        this.backWall = false;
        this.leftWall = false;
    }

}
