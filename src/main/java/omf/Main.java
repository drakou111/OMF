package omf;

import omf.util.InputTick;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        List<InputTick> inputs = new ArrayList<>();
        inputs.add(new InputTick(true, false, false, false, false, false, false, true));
        inputs.add(new InputTick(false, true, false, false, false, false, false, true));
        inputs.add(new InputTick(false, true, false, false, false, false, false, true));
        inputs.add(new InputTick(false, false, true, false, false, false, false, true));
        inputs.add(new InputTick(false, false, true, false, false, false, false, true));
        inputs.add(new InputTick(false, false, true, false, false, false, false, true));
        inputs.add(new InputTick(false, false, false, true, false, false, false, true));
        inputs.add(new InputTick(false, false, false, true, false, false, false, true));
        inputs.add(new InputTick(false, false, false, true, false, false, false, true));
        inputs.add(new InputTick(false, false, false, true, false, false, false, true));
        inputs.add(new InputTick(false, false, false, false, false, false, false, true));
        inputs.add(new InputTick(false, false, false, false, false, false, false, true));
        inputs.add(new InputTick(false, false, false, false, false, false, false, true));
        inputs.add(new InputTick(false, false, false, false, false, false, false, true));
        inputs.add(new InputTick(false, false, false, false, false, false, false, true));
        inputs.add(new InputTick(false, false, false, false, false, false, false, true));
        inputs.add(new InputTick(false, false, false, false, false, false, false, true));
        inputs.add(new InputTick(false, false, false, false, false, false, false, true));
        inputs.add(new InputTick(false, false, false, false, false, false, false, true));
        inputs.add(new InputTick(false, false, false, false, false, false, false, true));
        inputs.add(new InputTick(false, false, false, false, false, false, false, true));
        inputs.add(new InputTick(false, false, false, false, false, false, false, true));

        Player player = new Player();
        for (int i = 0; i < inputs.size(); i++) {
            player.applyTick(inputs.get(i));
            System.out.println(player.position);
        }
    }
}