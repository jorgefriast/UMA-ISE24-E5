package com.introduction.rowing;

import com.badlogic.gdx.Input;

public class LobbyInputProcessor extends InputProcessor {
    public LobbyInputProcessor(MyRowing myRowing) {
        super(myRowing);
    }

    @Override
    public boolean keyDown(int keycode) {
        switch (keycode) {
            case Input.Keys.NUM_1:
                setGameState(GameState.PLAY_GAME);
                setGameSubState(GameSubState.RACE_LEG);
                break;
            case Input.Keys.NUM_2:
                setGameState(GameState.ENTER_SHOP);
                break;
            case Input.Keys.NUM_3:
                setGameState(GameState.PLAY_GAME);
                setGameSubState(GameSubState.TUTORIAL);
                break;
            case Input.Keys.NUM_4:
            case Input.Keys.ESCAPE:
                System.exit(0);
                break;

        }
        return false;
    }
}
