package omf.util;

public class InputTick {
    public boolean W;
    public boolean A;
    public boolean S;
    public boolean D;
    public boolean SPRINT;
    public boolean SHIFT;
    public boolean JUMP;
    public boolean GROUNDED;
    public float TURN;

    public InputTick(boolean W, boolean A, boolean S, boolean D,boolean SPRINT, boolean SHIFT, boolean JUMP, boolean GROUNDED, float TURN) {
        this.W = W;
        this.A = A;
        this.S = S;
        this.D = D;
        this.SPRINT = SPRINT;
        this.SHIFT = SHIFT;
        this.JUMP = JUMP;
        this.GROUNDED = GROUNDED;
        this.TURN = TURN;
    }

    @Override
    public String toString() {

        String result = "";
        result += W ? "W " : "";
        result += A ? "A " : "";
        result += S ? "S " : "";
        result += D ? "D " : "";
        result += SPRINT ? "SPRINT " : "";
        result += SHIFT ? "SHIFT" : "";
        result += JUMP ? "JUMP " : "";
        result += TURN > 0 ? ("+" + TURN + "°") : (TURN < 0 ? (TURN + "°") : "");
        result += GROUNDED ? "(on ground)" : "";

        if (result.isEmpty()) return "No input";
        return result;
    }
}
