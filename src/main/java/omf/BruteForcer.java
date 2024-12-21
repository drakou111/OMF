package omf;

import omf.enu.InputState;
import omf.util.InputTick;
import omf.util.Vec3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BruteForcer {

    public static Vec3 bestFinalVector;

    public static void start(MacroRule rules) {
        if (rules.inputs.isEmpty()) return;

        BruteForcer.bestFinalVector = new Vec3(0, 0, 0);
        Player player = new Player(new Vec3(0, 0, 0), rules.startingAngle, rules.effects.jumpBoost, rules.effects.speed, rules.effects.slowness);

        List<Vec3> positions = new ArrayList<>();
        positions.add(new Vec3(0, 0, 0));

        List<InputTick> allInputs = new ArrayList<>();
        allInputs.add(new InputTick(false, false, false, false, false, false, false, true, 0.0f));

        branch(rules, 0, 999999, player,positions, allInputs);
        System.out.println("Done.");
    }

    public static List<Integer> sortVec3Indices(List<Vec3> vecList, boolean xAxis, boolean ascending) {
        // Create an index list corresponding to the input Vec2 list
        List<Vec3> copy = new ArrayList<>();
        for (Vec3 vec : vecList) {
            copy.add(vec.copy());
        }

        List<Integer> indexList = new ArrayList<>();
        for (int i = 0; i < vecList.size(); i++) {
            indexList.add(i);
        }

        // Sort the index list using a custom comparator
        Collections.sort(indexList, (i1, i2) -> {
            if (xAxis) {
                int cmp = Double.compare(copy.get(i1).x, copy.get(i2).x);
                return ascending ? cmp : -cmp;
            } else {
                int cmp = Double.compare(copy.get(i1).z, copy.get(i2).z);
                return ascending ? cmp : -cmp;
            }
        });

        return indexList;
    }

    public static int getEdgeIndex(List<Integer> sortedIndexes, List<InputTick> allInputTicks) {
        int index = 0;
        List<Integer> edgeIndexes = new ArrayList<>();

        for (int i = 0; i < sortedIndexes.size(); i++) {
            if (!(sortedIndexes.get(i) >= allInputTicks.size() - 2 || (sortedIndexes.get(i) < allInputTicks.size() - 1 && allInputTicks.get(sortedIndexes.get(i) + 1).GROUNDED))) continue;

            // If starting tick, then must start there.
            if (sortedIndexes.get(i) == 0) break;

            // if one of tick's neighbours is in edgeIndexes, then start there.
            if (edgeIndexes.contains(sortedIndexes.get(i) - 1) || (sortedIndexes.get(i) < sortedIndexes.size() - 1 && edgeIndexes.contains(sortedIndexes.get(i) + 1))) {
                index = sortedIndexes.get(i);
                break;
            }
            // Otherwise, add it to edgeIndexes.
            else {
                edgeIndexes.add(sortedIndexes.get(i));
            }
        }
        return index;
    }

    public static OutputInfo checkIfValid(MacroRule rules, List<Vec3> allPositions, List<InputTick> allInputTicks) {

        Vec3 finalVelocity = allPositions.get(allPositions.size() - 1).copy();
        finalVelocity.subtract(allPositions.get(allPositions.size() - 2));

        double angle = Math.atan2(finalVelocity.z, finalVelocity.x);

        switch (rules.optimize) {
            case MAX_SPEED -> {if (finalVelocity.lengthVector() < bestFinalVector.lengthVector()) return null;}
            case MAX_X_SPEED -> {if (finalVelocity.x < bestFinalVector.x) return null;}
            case MAX_Z_SPEED -> {if (finalVelocity.z < bestFinalVector.z) return null;}
            case MIN_SPEED -> {if (finalVelocity.lengthVector() > bestFinalVector.lengthVector()) return null;}
            case MIN_X_SPEED -> {if (finalVelocity.x > bestFinalVector.x) return null;}
            case MIN_Z_SPEED -> {if (finalVelocity.z > bestFinalVector.z) return null;}
        }

        // Within angle goal
        if (!(angle >= rules.minVectorAngleGoal && angle <= rules.maxVectorAngleGoal))
            return null;


        // Within momentum
        Vec3 max = new Vec3(0, 0, 0);
        Vec3 min = new Vec3(0, 0, 0);
        for (int i = 0; i < allPositions.size(); i++) {
            min.x = Math.min(min.x, allPositions.get(i).x);
            min.z = Math.min(min.z, allPositions.get(i).z);

            max.x = Math.max(max.x, allPositions.get(i).x);
            max.z = Math.max(max.z, allPositions.get(i).z);
        }

        List<Integer> xAscending = sortVec3Indices(allPositions, true, true);
        List<Integer> xDescending = sortVec3Indices(allPositions, true, false);
        List<Integer> zAscending = sortVec3Indices(allPositions, false, true);
        List<Integer> zDescending = sortVec3Indices(allPositions, false, false);

        int leftIndex = getEdgeIndex(xAscending, allInputTicks);
        int rightIndex = getEdgeIndex(xDescending, allInputTicks);
        int frontIndex = getEdgeIndex(zDescending, allInputTicks);
        int backIndex = getEdgeIndex(zAscending, allInputTicks);

        Vec3 maxFloor = new Vec3(allPositions.get(rightIndex).x, 0, allPositions.get(frontIndex).z);
        Vec3 minFloor = new Vec3(allPositions.get(leftIndex).x, 0, allPositions.get(backIndex).z);

        //width
        double widthMax = maxFloor.x;
        double widthMin = minFloor.x;
        if (rules.momentumRule.rightWall)
            widthMax = max.x;
        if (rules.momentumRule.leftWall)
            widthMin = min.x;

        //length
        double lengthMax = maxFloor.z;
        double lengthMin = minFloor.z;
        if (rules.momentumRule.frontWall)
            lengthMax = max.z;
        if (rules.momentumRule.backWall)
            lengthMin = min.z;

        double usedWidth = widthMax - widthMin;
        double usedLength = lengthMax - lengthMin;

        if (usedWidth > rules.momentumRule.width || usedLength > rules.momentumRule.length)
            return null;

        return new OutputInfo(finalVelocity.lengthVector(), finalVelocity.x, finalVelocity.z, usedWidth, usedLength, rules.momentumRule.width, rules.momentumRule.length);
    }

    public static void branch(MacroRule rules, int tick, int timeSinceJumped, Player currentPlayer, List<Vec3> allPositions, List<InputTick> allInputTicks) {
        if (tick >= rules.inputs.size()) {

            OutputInfo output = checkIfValid(rules, allPositions, allInputTicks);
            if (output != null) {

                System.out.println("==========");
                for (int i = 0; i < allPositions.size(); i++) {
                    System.out.println("t" + i + ": " + allInputTicks.get(i));
                }
                System.out.println("-----------");

                Vec3 finalVelocity = allPositions.get(allPositions.size() - 1).copy();
                finalVelocity.subtract(allPositions.get(allPositions.size() - 2));

                System.out.println(output);
                System.out.println("==========");
                System.out.println();

                BruteForcer.bestFinalVector = finalVelocity;
            }

            return;
        }

        MacroRuleInput currentInputs = rules.inputs.get(tick);
        int tickGap = 12 - rules.tierMomentum - 1;
        boolean grounded = timeSinceJumped >= tickGap;

        List<InputTick> allCombinations = getAllCombinations(currentInputs, grounded, 0);

        for (InputTick combination : allCombinations) {
            int newTimeSinceJumped = timeSinceJumped;
            if (combination.JUMP && combination.GROUNDED) {
                newTimeSinceJumped = 0;
            }
            else
                newTimeSinceJumped++;

            Player newPlayer = currentPlayer.copy();
            newPlayer.applyTick(combination);

            List<Vec3> newPositions = new ArrayList<>(allPositions);
            newPositions.add(newPlayer.position);

            List<InputTick> newInputTicks = new ArrayList<>(allInputTicks);
            newInputTicks.add(combination);

            branch(rules, tick + 1, newTimeSinceJumped, newPlayer, newPositions, newInputTicks);
        }

    }

    public static List<InputTick> getAllCombinations(MacroRuleInput inputs, boolean grounded, int index) {
        List<InputTick> results = new ArrayList<>();

        if (index == inputs.allStates.length) {
            results.add(new InputTick(
                inputs.allStates[0] == InputState.ON,
                inputs.allStates[1] == InputState.ON,
                inputs.allStates[2] == InputState.ON,
                inputs.allStates[3] == InputState.ON,
                inputs.allStates[4] == InputState.ON,
                inputs.allStates[5] == InputState.ON,
                inputs.allStates[6] == InputState.ON,
                grounded,
                inputs.turn
            ));
            return results;
        }

        if (inputs.allStates[index] == InputState.UNKNOWN) {
            MacroRuleInput copy1 = inputs.copy();
            copy1.allStates[index] = InputState.OFF;

            MacroRuleInput copy2 = inputs.copy();
            copy2.allStates[index] = InputState.ON;

            // Avoid repetition like WAD, WASD, AD, SPRINT without W, JUMP midair, etc.
            if (
                    !(index == 2 && copy1.allStates[0] == InputState.ON) &&
                    !(index == 3 && copy1.allStates[1] == InputState.ON) &&
                    !(index == 4 && !grounded) &&
                    !(index == 5 && copy1.allStates[0] == InputState.OFF)
            ) {
                results.addAll(getAllCombinations(copy2, grounded, index + 1));
            }

            results.addAll(getAllCombinations(copy1, grounded, index + 1));
        }
        else {
            MacroRuleInput copy1 = inputs.copy();
            results.addAll(getAllCombinations(copy1, grounded, index + 1));
        }

        return results;
    }
}
