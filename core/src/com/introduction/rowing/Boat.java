package com.introduction.rowing;

import com.badlogic.gdx.graphics.Texture;

import java.awt.*;
import java.util.ArrayList;


import static com.introduction.rowing.Constants.*;

public class Boat extends Entity{
    private int speedFactor;
    private int acceleration;
    private int robustness;
    private int momentumFactor;
    private int fatigue;
    private double speedX;
    private double speedY;
    private final GameInputProcessor inputProcessor;
    private final boolean isPlayer;
    private int timeTicker = 0;
    private boolean accelerating = false;
    private boolean isAcceleratorAvailable = false;
    private int distance_traveled = 0;
    private int numberOfAvoidedObstacles = 0;
    private double fatigueLevel = 0;
    private  float fatigueRate;
    private int boatHealth;
    private int id;
    private int width;
    private int height;
    private float collisionCooldown = 0;
    private boolean isVisible = true;
    private float flashTimer = 0;
    private int invulnerabilityTime = 0;
    private boolean momentumPowerupActive = false;

    public Boat(int id, Position position, boolean isPlayer, GameInputProcessor inputProcessor, ShopBoat shopBoat) {
        super(position, 10, 10, new Texture(shopBoat.getImageName()));
        this.speedY = getNewCalculatedSpeed();
        this.fatigue = shopBoat.getFatigue();
        this.isPlayer = isPlayer;
        this.inputProcessor = inputProcessor;
        this.fatigueRate = calculateFatigueRate(fatigue);
        this.robustness = shopBoat.getRobustness();
        this.boatHealth = determineBoatHealth();
        this.id = id;
        this.speedFactor = shopBoat.getSpeedFactor();
        this.acceleration = shopBoat.getAcceleration();
        this.momentumFactor = shopBoat.getMomentumFactor();
        this.speedX = shopBoat.getManeuverability();

        this.width = (int) ((WINDOW_WIDTH / NUMBER_OF_LANES) * BOAT_WIDTH_FRACTION);
        this.height = (image.getHeight() * this.width) / image.getWidth();
        super.height = this.height;
        super.width = this.width;

    }

    public void updateKeys(float delta, int leftBoundary) {
        if(!isPlayer) return;
        // Check if boat is moving based on input
        boolean moving = inputProcessor.moving;
        int direction = inputProcessor.direction;

        // Adjust boat position based on input direction
        float newX = position.getX();
        if (moving) {
            switch (direction) {
                case 0: // Up
                    if (isAcceleratorAvailable) {
                        accelerating = true;
                        updateY(delta);
                    }
                    break;
                case 1: // Left
                    newX -= (float) (speedX * getFatigueEffect() * 2);
                    break;
                case 3: // Right
                    newX += (float) (speedX * getFatigueEffect() * 2);
                    break;
            }
        }
        // Update boat position
        int laneWidth = Constants.WINDOW_WIDTH / Constants.NUMBER_OF_LANES;
        newX = Math.max(0, Math.min(WINDOW_WIDTH - this.width, newX));
        position.setX(Math.round(newX));
    }

    public void updateY(float delta) {
        timeTicker++;

        // Increase fatigue over time
        fatigueLevel += delta * fatigueRate;

        // Update boat speed
        speedY = getNewCalculatedSpeed();

        // Boat cannot go higher than the middle of the screen
        if (position.getY() > WINDOW_HEIGHT/2) {
            position.setY(WINDOW_HEIGHT/2);
        }
        else {
            // Update boat position
            this.setDistance_traveled((int) Math.round(speedY));
            position.setY((int) Math.round( position.getY() + speedY));
        }
    }

    /**
     * Avoid obstacles on the way for the boat controlled by the computer
     */
    public void avoidObstacles(ArrayList<Obstacle> obstacles, int leftBoundary) {
        Obstacle nearestObstacle = null;
        double nearestDistance = Double.MAX_VALUE;
        for (Obstacle obstacle : obstacles) {
            double distance = Math.sqrt(Math.pow(obstacle.getPosition().getX() - position.getX(), 2) + Math.pow(obstacle.getPosition().getY() - position.getY(), 2));
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearestObstacle = obstacle;
            }
        }
        if (nearestObstacle != null) {
            // Calculate desired position to steer away from the obstacle
            int desiredX = position.getX();
            int laneWidth = Constants.WINDOW_WIDTH / Constants.NUMBER_OF_LANES;
            int obstacleWidth = nearestObstacle.getWidth();
            int buffer = 20;
            int safeDistance = this.width / 2 + obstacleWidth / 2 + buffer;

            if (nearestObstacle.getPosition().getX() < position.getX()) {
                // Steer to the right if there's enough space; otherwise, steer to the left
                desiredX = Math.max(leftBoundary, nearestObstacle.getPosition().getX() + obstacleWidth / 2 + safeDistance);
            } else {
                // Steer to the left if there's enough space; otherwise, steer to the right
                desiredX = Math.min(leftBoundary + laneWidth - this.width, nearestObstacle.getPosition().getX() - obstacleWidth / 2 - safeDistance);
            }

            int newX = position.getX() + Math.round((desiredX - position.getX()) * 0.1f);

            // ensure the boat stays within the lane
            int positionInLane = Math.max(leftBoundary, Math.min(leftBoundary + laneWidth - this.width/2, newX));
            position.setX(positionInLane);
        }
    }

    /**
     * Calculate the current speed of the boat (Speed algorithm)
     * @return the current speed of the boat between -2.5 and 2.5
     */
    public double getNewCalculatedSpeed() {
        double accelerationWeight = 0;
        double baseSpeed = 0;
        double momentumEffect = 0;

        if (accelerating) {
            accelerationWeight = 0.5;
        }

        if (timeTicker % 10 == 0) {
            baseSpeed = 0.5 * speedFactor;
        }

        // Momentum effect
        if (timeTicker % (7 - momentumFactor) == 0) {
            momentumEffect = getCurrentMomentum();
        }

        return (baseSpeed + momentumEffect + accelerationWeight * this.acceleration) * getFatigueEffect();
    }

    public double getCurrentMomentum() {
        if (this.momentumPowerupActive) {
            return 3;
        } else if (numberOfAvoidedObstacles >= 15)
            return 2.5;
        else if (numberOfAvoidedObstacles >= 10)
            return 2;
        else if (numberOfAvoidedObstacles >= 8)
            return 1.5;
        else if (numberOfAvoidedObstacles >= 5)
            return 1;
        else if (numberOfAvoidedObstacles >= 3)
            return 0.5;
        else
            return 0;
    }

    private float calculateFatigueRate(int fatigue) {
        // The fatigue rate is inversely proportional to the fatigue value
        return 1.0f / fatigue;
    }

    public double getFatigueEffect() {
        return 1 - (fatigueLevel / 60);
    }

    private int determineBoatHealth() {
        return (50 + robustness * 25);
    }

    public float getCollisionCooldown() {
        return collisionCooldown;
    }

    public void setCollisionCooldown(float collisionCooldown) {
        this.collisionCooldown = collisionCooldown;
        if (collisionCooldown > 0) {
            flashTimer = 0;
        }
    }

    public void updateCooldown(float deltaTime) {
        if (collisionCooldown > 0) {
            collisionCooldown -= deltaTime;
            flashTimer += deltaTime;
            if (flashTimer >= 0.1f) {
                isVisible = !isVisible;
                flashTimer = 0;
            }
        } else {
            isVisible = true;
        }
    }
    public boolean canCollide() {
        return collisionCooldown <= 0;
    }
    public boolean isVisible() {
        return isVisible;
    }

    /**
     * Change the speed of the boat vertically
     * @param speed the new speed of the boat
     */
    public void setSpeedY(int speed) {
        this.speedY = speed;
    }

    /**
     * Change the speed of the boat horizontally
     * @param speedX the new speed of the boat horizontally
     */
    public void setSpeedX(int speedX) {
        this.speedX = speedX;
    }

    /**
     * Get the speed of the boat horizontally
     * @return the speed of the boat horizontally
     */
    public double getSpeedX() {
        return speedX;
    }

    /**
     * Get the speed of the boat vertically
     * @return the speed of the boat vertically
     */
    public double getSpeedY() {
        return speedY;
    }

    /**
     * Get the distance traveled by the boat
     * @return the distance traveled by the boat
     */
    public int getDistance_traveled() {
        return distance_traveled;
    }

    /**
     * Set the distance traveled by the boat
     * @param distance_traveled the distance traveled by the boat
     */
    public void setDistance_traveled(int distance_traveled) {
        this.distance_traveled += distance_traveled;
    }

    /**
     * Get the position of the boat
     * @return the position of the boat
     */
    public Position getPosition() {
        return position;
    }

    /**
     * Get the speedFactor of the boat
     * @return the speedFactor of the boat
     */
    public int getSpeedFactor() {
        return speedFactor;
    }

    /**
     * Get the acceleration of the boat
     * @return the acceleration of the boat
     */
    public int getAcceleration() {
        return acceleration;
    }

    /**
     * Get the robustness of the boat
     * @return the robustness of the boat
     */
    public int getRobustness() {
        return robustness;
    }

    /**
     * Get the momentum of the boat
     * @return the momentum of the boat
     */
    public int getMomentumFactor() {
        return momentumFactor;
    }

    /**
     * Get the fatigue of the boat
     * @return the fatigue of the boat
     */
    public int getFatigue() {
        return fatigue;
    }

    public int getBoatHealth() {
        return boatHealth;
    }

    public boolean getIsPlayer() {
        return isPlayer;
    }

    public boolean getAccelerating() {
        return accelerating;
    }

    public void setAccelerating(boolean accelerating) {
        this.accelerating = accelerating;
    }

    public void setIsAcceleratorAvailable(boolean b) {
        isAcceleratorAvailable = b;
    }

    public void increaseNumberOfAvoidedObstacles() {
        numberOfAvoidedObstacles++;
    }

    public int getNumberOfAvoidedObstacles() {
        return numberOfAvoidedObstacles;
    }

    public void resetNumberOfAvoidedObstacles() {
        numberOfAvoidedObstacles = 0;
    }

    public void damageBoat(int damage) {
        boatHealth -= damage;
    }

    public void setBoatHealth(int value) {
        boatHealth = value;
    }

    public int getId() {
        return this.id;
    }


    @Override
    public Rectangle getBounds() {
        return new Rectangle(position.getX(), position.getY() - this.height, this.width, this.height);
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public int getHeight() {
        return this.height;
    }

    @Override
    public String toString() {
        return "BOAT " + this.id;
    }

    public boolean isInvulnerable() {
        return this.invulnerabilityTime > 0;
    }

    public void setInvulnerabilityTime(int seconds) {
        this.invulnerabilityTime = seconds;
    }

    public int getInvulnerabilityTime() {
        return invulnerabilityTime;
    }

    public void decreaseInvulnerabilityTime() {
        invulnerabilityTime--;
        if (invulnerabilityTime < 0) {
            this.invulnerabilityTime = 0;
        }
    }

    public void activateMomemtumPowerup() {
        this.momentumPowerupActive = true;
    }

    public void deactivateMomemtumPowerup() {
        this.momentumPowerupActive = false;
    }
}
