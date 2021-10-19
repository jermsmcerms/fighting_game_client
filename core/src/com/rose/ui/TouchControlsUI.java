package com.rose.ui;

import static com.rose.constants.Constants.*;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.rose.input.TouchInput;

public class TouchControlsUI {
    private int row_height;
    private int col_width;
    private Skin skin;
    private TouchInput touchInput;
    private Button l_button;
    private Button r_button;
    private Button a_button;
    private Button b_button;
    private Button c_button;
    private Button d_button;
    private Button t_button;
    private Button s_button;
    private int input_combo;

    public TouchControlsUI() {
    }

    public void draw() {

    }

    public int getInput() {
        return input_combo;
    }

    public void showUI(Stage stage) {
        Gdx.input.setInputProcessor(stage);
        skin = new Skin(Gdx.files.internal("skin/glassy-ui.json"));

        l_button = new Button(skin, "small");
        l_button.setSize(30, 30);
        l_button.setPosition(25,25);
        l_button.setColor(0.0f, 0.0f, 0.0f, 0.4f);
        l_button.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y,
                                     int pointer, int button) {
                input_combo += LEFT;
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y,
                                int pointer, int button) {
                input_combo -= LEFT;
            }
        });
        stage.addActor(l_button);

        r_button = new Button(skin, "small");
        r_button.setSize(30, 30);
        r_button.setPosition(90,25);
        r_button.setColor(0.0f, 0.0f, 0.0f, 0.4f);
        r_button.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y,
                                     int pointer, int button) {
                input_combo += RIGHT;
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y,
                                int pointer, int button) {
                input_combo -= RIGHT;
            }
        });
        stage.addActor(r_button);

        a_button = new Button(skin, "small");
        a_button.setSize(30, 30);
        a_button.setPosition(275, 60);
        a_button.setColor(1.0f, 0.0f, 0.0f, 0.4f);
        a_button.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y,
                                     int pointer, int button) {
                input_combo += A_BTN;
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y,
                                int pointer, int button) {
                input_combo -= A_BTN;
            }
        });
        stage.addActor(a_button);

        b_button = new Button(skin, "small");
        b_button.setSize(30, 30);
        b_button.setPosition(320,105);
        b_button.setColor(0.0f, 1.0f, 0.0f, 0.4f);
        b_button.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y,
                                     int pointer, int button) {
                input_combo += B_BTN;
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y,
                                int pointer, int button) {
                input_combo -= B_BTN;
            }
        });
        stage.addActor(b_button);

        c_button = new Button(skin, "small");
        c_button.setSize(30, 30);
        c_button.setPosition(365,60);
        c_button.setColor(0.0f, 0.0f, 1.0f, 0.4f);
        c_button.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y,
                                     int pointer, int button) {
                input_combo += C_BTN;
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y,
                                int pointer, int button) {
                input_combo -= C_BTN;
            }
        });
        stage.addActor(c_button);

        d_button = new Button(skin, "small");
        d_button.setSize(30, 30);
        d_button.setPosition(320,15);
        d_button.setColor(1.0f, 1.0f, 0.0f, 0.4f);
        d_button.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y,
                                     int pointer, int button) {
                input_combo += D_BTN;
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y,
                                int pointer, int button) {
                input_combo -= D_BTN;
            }
        });
        stage.addActor(d_button);

        t_button = new Button(skin, "small");
        t_button.setSize(30, 30);
        t_button.setPosition(320,60);
        t_button.setColor(1.0f, 1.0f, 1.0f, 0.4f);
        t_button.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y,
                                     int pointer, int button) {
                input_combo += T_BTN;
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y,
                                int pointer, int button) {
                input_combo -= T_BTN;
            }
        });
        stage.addActor(t_button);

//        s_button = new Button(skin, "small");
//        s_button.setSize(30, 30);
//        s_button.setPosition(90,25);
//        s_button.setColor(0.0f, 0.0f, 0.0f, 0.4f);
//        s_button.addListener(new InputListener() {
//            @Override
//            public boolean touchDown(InputEvent event, float x, float y,
//                                     int pointer, int button) {
//                input_combo += S_BTN
//                return true;
//            }
//        @Override
//        public void touchUp(InputEvent event, float x, float y,
//        int pointer, int button) {
//            input_combo -= S_BTN;
//        }
//        });
//        stage.addActor(s_button);
    }
}
