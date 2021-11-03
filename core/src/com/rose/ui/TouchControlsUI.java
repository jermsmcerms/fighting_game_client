package com.rose.ui;

import static com.rose.constants.Constants.*;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.rose.actors.ButtonActor;
import com.rose.input.TouchInput;

import java.util.ArrayList;

public class TouchControlsUI extends InputAdapter {
    private final TextureAtlas textureAtlas;
    private ButtonActor pause_button;
    private ButtonActor l_button;
    private ButtonActor r_button;
    private ButtonActor a_button;
    private ButtonActor b_button;
    private ButtonActor c_button;
    private ButtonActor d_button;
    private ButtonActor t_button;
    private ButtonActor s_button;
    private int input_combo;
    private boolean show_buttons = true;
    private ArrayList<ButtonActor> buttons;

    public TouchControlsUI(Stage stage) {
        buttons = new ArrayList<>(9);

        Gdx.input.setInputProcessor(this);
        textureAtlas = new TextureAtlas(Gdx.files.internal("ui_elements.atlas"));

        Gdx.input.setInputProcessor(stage);
        Button.ButtonStyle style = new Button.ButtonStyle();
        style.up = new TextureRegionDrawable(new Texture(Gdx.files.internal("pause_button.png")));
        pause_button = new ButtonActor(style);
        pause_button.setSize(50, 50);
        pause_button.setPosition(stage.getWidth() / 2.0f - pause_button.getWidth() / 2.0f, stage.getHeight() - pause_button.getHeight());
        pause_button.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                l_button.drawable = !l_button.drawable;
                r_button.drawable = !r_button.drawable;
                a_button.drawable = !a_button.drawable;
                b_button.drawable = !b_button.drawable;
                c_button.drawable = !c_button.drawable;
                d_button.drawable = !d_button.drawable;
                t_button.drawable = !t_button.drawable;
                return super.touchDown(event, x, y, pointer, button);
            }


        });
        buttons.add(pause_button);

        ButtonActor pause_button = new ButtonActor(new Button.ButtonStyle());
        pause_button.setSize(30, 30);
        pause_button.setPosition(stage.getWidth() / 2.0f, stage.getHeight() - 30);
        pause_button.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y,
                                     int pointer, int button) {
                show_buttons = !show_buttons;
                return true;
            }
        });

        style = new Button.ButtonStyle();
        style.up = new TextureRegionDrawable(textureAtlas.findRegion("l_btn_up"));
        style.down = new TextureRegionDrawable(textureAtlas.findRegion("l_btn_dwn"));
        l_button = new ButtonActor(style);
        l_button.setSize(41,43);
        l_button.setPosition(21, 9);
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
        buttons.add(l_button);

        style = new Button.ButtonStyle();
        style.up = new TextureRegionDrawable(textureAtlas.findRegion("r_btn_up"));
        style.down = new TextureRegionDrawable(textureAtlas.findRegion("r_btn_dwn"));
        r_button = new ButtonActor(style);
        r_button.setSize(41,43);
        r_button.setPosition(102, 9);
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
        buttons.add(r_button);

        style = new Button.ButtonStyle();
        style.up = new TextureRegionDrawable(textureAtlas.findRegion("a_btn_up"));
        style.down = new TextureRegionDrawable(textureAtlas.findRegion("a_btn_dwn"));
        a_button = new ButtonActor(style);
        a_button.setSize(41,43);
        a_button.setPosition(282, 9);
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
        buttons.add(a_button);

        style = new Button.ButtonStyle();
        style.up = new TextureRegionDrawable(textureAtlas.findRegion("b_btn_up"));
        style.down = new TextureRegionDrawable(textureAtlas.findRegion("b_btn_dwn"));
        b_button = new ButtonActor(style);
        b_button.setSize(41,43);
        b_button.setPosition(282, 81);
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
        buttons.add(b_button);

        style = new Button.ButtonStyle();
        style.up = new TextureRegionDrawable(textureAtlas.findRegion("c_btn_up"));
        style.down = new TextureRegionDrawable(textureAtlas.findRegion("c_btn_dwn"));
        c_button = new ButtonActor(style);
        c_button.setSize(41,43);
        c_button.setPosition(372, 81);
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
        buttons.add(c_button);

        style = new Button.ButtonStyle();
        style.up = new TextureRegionDrawable(textureAtlas.findRegion("d_btn_up"));
        style.down = new TextureRegionDrawable(textureAtlas.findRegion("d_btn_dwn"));
        d_button = new ButtonActor(style);
        d_button.setSize(41,43);
        d_button.setPosition(372, 9);
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
        buttons.add(d_button);

        style = new Button.ButtonStyle();
        style.up = new TextureRegionDrawable(textureAtlas.findRegion("t_btn_up"));
        style.down = new TextureRegionDrawable(textureAtlas.findRegion("t_btn_dwn"));
        t_button = new ButtonActor(style);
        t_button.setSize(41,43);
        t_button.setPosition(327, 48);
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
        buttons.add(t_button);

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

        style = new Button.ButtonStyle();
        style.up = new TextureRegionDrawable(new Texture(Gdx.files.internal("pause_button.png")));
        pause_button = new ButtonActor(style);
        pause_button.setSize(50, 50);
        pause_button.setPosition(stage.getWidth() / 2.0f - pause_button.getWidth() / 2.0f, stage.getHeight() - pause_button.getHeight());
        pause_button.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                for(int i = 0; i < buttons.size()-1; i++) {
                    buttons.get(i).drawable = !buttons.get(i).drawable;
                }
                return super.touchDown(event, x, y, pointer, button);
            }


        });
        buttons.add(pause_button);
    }

    public int getInput() {
        return input_combo;
    }

    public ArrayList<ButtonActor> getButtons() {
        return buttons;
    }
}
