package com.introduction.rowing;


import com.badlogic.gdx.graphics.Texture;

public interface Powerup {
    void use();
    Texture getTexture();
    String getDescription();
    String getName();
    int getPrice();
}
