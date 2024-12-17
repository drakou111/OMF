package omf;

import omf.util.InputTick;
import omf.util.MinecraftMathHelper;
import omf.util.Vec3;

public class Player {
    public Vec3 position;
    public Vec3 velocity;
    public boolean jumping;
    public boolean sprinting;
    public boolean shifting;
    public boolean grounded;
    public boolean wasGrounded;
    public float facing;
    public int jumpStrength;
    public int speedStrength;
    public int slownessStrength;

    float moveForward = 0F;
    float moveStrafe = 0F;
    float jumpMovementFactor = 0.02F;

    public Player(Vec3 position, Vec3 velocity, boolean jumping, boolean sprinting, boolean shifting, boolean grounded, boolean wasGrounded, float facing, int jumpStrength, int speedStrength, int slownessStrength) {
        this.position = position;
        this.velocity = velocity;
        this.jumping = jumping;
        this.sprinting = sprinting;
        this.grounded = grounded;
        this.wasGrounded = wasGrounded;
        this.facing = facing;
        this.shifting = shifting;
        this.jumpStrength = jumpStrength;
        this.speedStrength = speedStrength;
        this.slownessStrength = slownessStrength;
    }

    public Player(Vec3 position, float facing, int jumpStrength, int speedStrength, int slownessStrength) {
        this.position = position;
        this.velocity = Vec3.ZERO;
        this.jumping = false;
        this.sprinting = false;
        this.grounded = true;
        this.wasGrounded = true;
        this.facing = facing;
        this.jumpStrength = jumpStrength;
        this.speedStrength = speedStrength;
        this.slownessStrength = slownessStrength;
    }

    public Player(Vec3 position, float facing) {
        this.position = position;
        this.velocity = new Vec3(0, 0, 0);
        this.jumping = false;
        this.sprinting = false;
        this.grounded = true;
        this.wasGrounded = true;
        this.facing = facing;
        this.jumpStrength = 0;
        this.speedStrength = 0;
        this.slownessStrength = 0;
    }

    public Player() {
        this.position = new Vec3(0, 0, 0);
        this.velocity = new Vec3(0, 0, 0);
        this.jumping = false;
        this.sprinting = false;
        this.grounded = true;
        this.wasGrounded = true;
        this.facing = 0;
        this.jumpStrength = 0;
        this.speedStrength = 0;
        this.slownessStrength = 0;
    }

    public Player copy() {
        return new Player(
            this.position.copy(),
            this.velocity.copy(),
            this.jumping,
            this.sprinting,
            this.shifting,
            this.grounded,
            this.wasGrounded,
            this.facing,
            this.jumpStrength,
            this.speedStrength,
            this.slownessStrength
        );
    }

    private void updateFlags(InputTick inputTick) {
        wasGrounded = grounded;

        moveStrafe = 0F;
        moveForward = 0F;

        if (inputTick.W) moveForward++;
        if (inputTick.S) moveForward--;
        if (inputTick.A) moveStrafe--; // switch sites (related to x-axis difference in minecraft/javafx)
        if (inputTick.D) moveStrafe++; // switch sites (related to x-axis difference in minecraft/javafx)

        jumping = inputTick.JUMP;
        shifting = inputTick.SHIFT;
        grounded = inputTick.GROUNDED;

        if (shifting) {
            moveStrafe = (float) ((double) moveStrafe * 0.3D);
            moveForward = (float) ((double) moveForward * 0.3D);
        }

        if (!sprinting && moveForward >= 0.8F && inputTick.SPRINT) {
            sprinting = true;
        }

        if (sprinting && moveForward < 0.8F) {
            sprinting = false;
        }
    }

    public void applyTick(InputTick inputTick) {

        updateFlags(inputTick);

        if (Math.abs(velocity.x) < 0.005D) velocity.x = 0.0D;
        if (Math.abs(velocity.y) < 0.005D) velocity.y = 0.0D;
        if (Math.abs(velocity.z) < 0.005D) velocity.z = 0.0D;

        if (grounded) {
            velocity.y = 0;
        }

        if (jumping && grounded) {
            velocity.y = 0.42F;
            if (sprinting) {
                float f = facing * 0.017453292F;
                velocity.x = velocity.x - MinecraftMathHelper.sin(f) * 0.2F;
                velocity.z = velocity.z + MinecraftMathHelper.cos(f) * 0.2F;
            }

            if (jumpStrength > 0)
                velocity.y += (double) ((float) (jumpStrength * 0.1F));
        }

        moveStrafe *= .98F;
        moveForward *= .98F;

        float mult = 0.91F;
        if (grounded) mult = .6F * mult;
        float acceleration = 0.16277136F / (mult * mult * mult);

        float movement;
        if (sprinting) movement = 0.130000010133F;
        else movement = 0.1F;


        double speed = (1 + 0.2F * speedStrength) * (1 + 0.15F * slownessStrength);
        speed = speed >= 0 ? speed : 0;

        float movementFactor;
        if (grounded) movementFactor = (float) speed * movement * acceleration;
        else movementFactor = jumpMovementFactor;

        moveFlying(moveStrafe, moveForward, movementFactor);
        mult = 0.91F;

        if (grounded) mult = 0.6F * mult;

        //System.out.println(velocity.z);
        position.add(velocity);

        if (!grounded) {
            velocity.y -= 0.08D;
            velocity.y *= 0.9800000190734863D;
        }

        velocity.x = velocity.x * mult;
        velocity.z = velocity.z * mult;

        jumpMovementFactor = 0.02F;

        if (sprinting) {
            jumpMovementFactor = (float) ((double) jumpMovementFactor + (double) 0.02F * 0.3D);
        }
    }

    private void moveFlying(float strafe, float forward, float friction) {
        float speed = strafe * strafe + forward * forward;
        if (speed >= 1.0E-4F) {
            speed = MinecraftMathHelper.sqrt_float(speed);

            if (speed < 1.0F) {
                speed = 1.0F;
            }

            speed = friction / speed;
            strafe = strafe * speed;
            forward = forward * speed;
            float sin = MinecraftMathHelper.sin(facing * (float) Math.PI / 180.0F);
            float cos = MinecraftMathHelper.cos(facing * (float) Math.PI / 180.0F);
            this.velocity.x = this.velocity.x + (double) (strafe * cos - forward * sin);
            this.velocity.z = this.velocity.z + (double) (forward * cos + strafe * sin);
        }
    }
}
