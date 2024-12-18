package omf;

import omf.enu.InputState;

public class MacroRuleInput {
    public InputState[] allStates;
    public float turn;

    public MacroRuleInput(InputState W, InputState A, InputState S, InputState D, InputState SPRINT, InputState SHIFT, InputState JUMP, float turn) {
        this.allStates = new InputState[]{W,A,S,D,SPRINT,SHIFT,JUMP};
        this.turn = turn;
    }

    public MacroRuleInput copy() {
        return new MacroRuleInput(allStates[0], allStates[1], allStates[2], allStates[3], allStates[4], allStates[5], allStates[6], turn);
    }

    @Override
    public String toString() {

        String result = "";
        result += allStates[0] == InputState.ON ? "W " : (allStates[0] == InputState.UNKNOWN ? "W? " : "");
        result += allStates[1] == InputState.ON ? "A " : (allStates[1] == InputState.UNKNOWN ? "A? " : "");
        result += allStates[2] == InputState.ON ? "S " : (allStates[2] == InputState.UNKNOWN ? "S? " : "");
        result += allStates[3] == InputState.ON ? "D " : (allStates[3] == InputState.UNKNOWN ? "D? " : "");
        result += allStates[4] == InputState.ON ? "SPRINT " : (allStates[4] == InputState.UNKNOWN ? "SPRINT? " : "");
        result += allStates[5] == InputState.ON ? "SHIFT " : (allStates[5] == InputState.UNKNOWN ? "SHIFT? " : "");
        result += allStates[6] == InputState.ON ? "JUMP " : (allStates[6] == InputState.UNKNOWN ? "JUMP? " : "");
        result += turn > 0 ? ("+" + turn + "°") : (turn < 0 ? (turn + "°") : "");

        if (result.isEmpty()) return "No input";
        return result;
    }
}
