package omf.util;

import omf.enu.InputState;

public class InputTick {
    public boolean W;
    public boolean A;
    public boolean S;
    public boolean D;
    public boolean SPRINT;
    public boolean SHIFT;
    public boolean JUMP;
    public boolean GROUNDED;

    public InputTick(boolean W, boolean A, boolean S, boolean D, boolean JUMP, boolean SPRINT, boolean SHIFT, boolean GROUNDED) {
        this.W = W;
        this.A = A;
        this.S = S;
        this.D = D;
        this.JUMP = JUMP;
        this.SPRINT = SPRINT;
        this.SHIFT = SHIFT;
        this.GROUNDED = GROUNDED;
    }

    @Override
    public String toString() {

        String result = "";
        result += W ? "W " : "";
        result += A ? "A " : "";
        result += S ? "S " : "";
        result += D ? "D " : "";
        result += JUMP ? "SPRINT " : "";
        result += SPRINT ? "SHIFT " : "";
        result += SHIFT ? "JUMP" : "";

        if (result.isEmpty()) return "No input";
        return result;
    }
}
