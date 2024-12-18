# OMF (Optimal Momentum Finder)

## What is this?
This is a program that lets you come up with optimal minecraft macros for your jumps, given some restrictions on the jump.

## How to use?
### Macro
The main parameter for this program is the macro. Just like mpk, you can add ticks and specify inputs at each tick.
However, in this versions, inputs can not only be "ON" or "OFF", but can also be "UNKNOWN".

When set to "UNKNOWN", that input will be optimized by the bruteforce algorithm to mess around with all the different
combinations of inputs possible. Note that whenever an input is optional, it is denoted with a "?" after it, like "A?" or "JUMP?".

For example, if I wanted to try to optimize rex bwmm, I would put a couple ticks with "W? A? S? D? SPRINT", then input the macro for normal rex bwmm.
The program will then try a bunch of starting combinations until it utilizes most of the momentum.

Each tick also has a "TURN" value, just like the YAW in mpk, which can't be optimized by the bruteforce program since it would make it extremely harder to find.

Note that each time you have an "UNKNOWN" input, it will double the amount of time it will take to bruteforce.

### Restrictions
To get what you want, there are other restrictions to use. Here is all of them:

- Tier Momentum: What tier the momentum should be. For instance, flat momentum is 0, 3bc momentum is 1, etc.
- Starting angle: What angle to start with. The angle can vary during the macro since you can tell it to  turn during ticks.
- Min/Max Vector Angle Goal: If you want to reach a specific vector angle for a jump, you can specify the maximum and minimum angles allowed.
- Optimize: What speed to optimize. For instance, could be your X speed, your Z speed, or just your speed in general.
- Speed Effect: The speed level the player has. No speed is 0.
- Slowness Effect: The slowness level the player has. No slowness is 0.
- Momentum Width (X): The width of the momentum. This can be thought of the distance between when you're shifting at the left of the momentum and the right of the momentum, even if there are walls.
- Momentum Length (Z): The length of the momentum. This can be thought of the distance between when you're shifting at the front of the momentum and the back of the momentum, even if there are walls.
- Frontwall / Rightwall / Backwall / Leftwall: Simply if there are walls in any 4 directions of the momentum.

### Output / Terminal
Once you are ready to brute force, press the start bruteforce button. The results will show up in the terminal.
You will receive information about the inputs you must do, your final speed and the amount of unused momentum.