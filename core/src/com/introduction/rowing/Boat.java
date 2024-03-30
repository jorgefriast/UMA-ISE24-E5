package com.introduction.rowing;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;

public class Boat extends Entity{
    private int speedX;
    private int speedY;
    private final int lane;
    private final MyInputProcessor inputProcessor;

    public Boat(int speedX, int speedY, int lane, Position position, Texture image) {
        super(position, image.getWidth(), image.getHeight(), image);
        this.speedX = speedX;
        this.speedY = speedY;
        this.lane = lane;
        this.inputProcessor = new MyInputProcessor();
        Gdx.input.setInputProcessor(inputProcessor);
    }

    public void update(float delta) {
        // Check if boat is moving based on input
        boolean moving = inputProcessor.moving;
        int direction = inputProcessor.direction;

        // Adjust boat position based on input direction
        float newX = position.getX();
        if (moving) {
            switch (direction) {
                case 1: // Left
                    newX -= (speedX * 2);
                    break;
                case 3: // Right
                    newX +=  (speedX * 2);
                    break;
            }
        }
        // Update boat position
        position.setX(Math.round(Math.max(0, Math.min(1920 - image.getWidth(), newX))));
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
     * Get the lane of the boat
     * @return the lane of the boat
     */
    public int getLane() {
        return lane;
    }

    /**
     * Get the position of the boat
     * @return the position of the boat
     */
    public Position getPosition() {
        return position;
    }

}
