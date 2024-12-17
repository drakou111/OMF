package omf;

public class Effects {
    public int speed;
    public int slowness;
    public int jumpBoost;

    public Effects() {
        this.speed = 0;
        this.slowness = 0;
        this.jumpBoost = 0;
    }

    public Effects(int speed, int slowness, int jumpBoost) {
        this.speed = speed;
        this.slowness = slowness;
        this.jumpBoost = jumpBoost;
    }

    public static Effects parse(String text) {
        return new Effects(0,0,0);
    }
}
