package omf;

import omf.enu.SpeedOptimize;

import java.util.ArrayList;
import java.util.List;

public class MacroRule {
    public List<MacroRuleInput> inputs;
    MomentumRule momentumRule;
    Effects effects;
    public int tierMomentum;
    public float startingAngle;
    public float minVectorAngleGoal;
    public float maxVectorAngleGoal;
    public SpeedOptimize optimize;

    public MacroRule(List<MacroRuleInput> inputs, MomentumRule momentumRule, int tierMomentum, float startingAngle, float minVectorAngleGoal, float maxVectorAngleGoal, SpeedOptimize optimize, Effects effects) {
        this.inputs = new ArrayList<>(inputs);
        this.momentumRule = momentumRule;
        this.tierMomentum = tierMomentum;
        this.startingAngle = startingAngle;

        if (maxVectorAngleGoal < minVectorAngleGoal) {
            this.minVectorAngleGoal = maxVectorAngleGoal;
            this.maxVectorAngleGoal = minVectorAngleGoal;
        }
        else {
            this.minVectorAngleGoal = minVectorAngleGoal;
            this.maxVectorAngleGoal = maxVectorAngleGoal;
        }
        this.optimize = optimize;
        this.effects = effects;
    }
}
