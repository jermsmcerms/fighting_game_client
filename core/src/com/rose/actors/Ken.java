package com.rose.actors;

import static com.rose.animation.SpriteAnimation.FRAME_LENGTH;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.rose.animation.AnimationState;
import com.rose.animation.SpriteAnimation;

public class Ken extends Fighter {
    static final long serialVersionUID = 44L;
    private float[][] animation_lengths;
    transient ShapeRenderer sr;
    float default_width;

    /*
        The following arrays contain information about a
        characters boxes data as they relate to an anchor point.
        they are ordered as: width, height, x_offset_right, x_offset_left, y_offset
        x offset left/right are dependant on the direction of the player.
    */
    //#region default box data
    float[][] default_box_data = {
        // the collision box offsets can't be set until the characters anchor point has been established
        { 41, 77,  0,  0,  0 },   // collision box
        { 47, 34, 25, 22,  0 },   // foot box
        { 44, 46, 22, 22, 34 },   // chest box
        { 20, 20,  4, 15, 71 },   // head box
    };
    //#endregion
    //#region close light hurtbox data
    float[][] c_light_0 = {
        { 41, 77, -20, -21,  0 },   // collision box
        { 47, 34, -25, -22,  0 },   // foot box
        { 44, 46, -22, -22, 34 },   // chest box
        { 60, 20,  -4, -56, 69},    // head box
        { 49, 15, 18, -68,  65}      // hit box
    };
    float[][] c_light_1 = {
        { 41, 77, -20, -21,  0 },   // collision box
        { 47, 34, -25, -22,  0 },   // foot box
        { 44, 46, -22, -22, 34 },   // chest box
        { 60, 20,  -4, -68, 65},    // head box
    };
    //#endregion
    //#region hadouken hurtbox data
    // stage 1:
    float[][] hadouken_boxes_0 = {
        { 41, 77, -11, -31,  0 },   // collision box
        { 73, 35, -27, -46, 0  }, // foot box
        { 55, 40, -27, -28, 35 }, // chest box
        { 25, 20,  -7, -18, 67 } // head box
    };
    // stage 2:
    float[][] hadouken_boxes_1 = {
        { 41, 77, -11, -31,  0 },   // collision box
        { 92, 30, -28, -68, 0  },
        { 44, 38, -10, -38, 29 },
        { 26, 20,   4, -34, 59 }
    };
    // stage 3:
    float[][] hadouken_boxes_2 = {
        { 41, 77, -11, -31,  0 },   // collision box
        { 92, 30, -29, -64, 0  },
        { 66, 36,  15, -82, 29 },
        { 46, 20,  13, -60, 59 }
    };
    //#endregion

    public Ken(Vector2 startPoint, Boolean facingRight) {
        super(startPoint, facingRight);
        sr = new ShapeRenderer();

        sprite_sheet = new TextureAtlas(Gdx.files.internal("characters/ken/ken_sprite_sheet.atlas"));
        TextureRegion region = sprite_sheet.findRegions("idle").get(0);
        default_width =  region.getRegionWidth();
        anchor_point = new Vector2(anim_anchor.x + default_width / 2.0f, anim_anchor.y);
        // Collision box offset from anchor
        default_box_data[0][2] = anchor_point.x - (default_box_data[0][0] / 2);
        default_box_data[0][3] = anchor_point.x - (default_box_data[0][0] / 2 - 1);
        default_box_data[0][4] = anchor_point.y;

        initializePhysicsBoxes(default_box_data, anchor_point);
        defineAnimationLengths();
        defineKeyFrames();

        anim_state = AnimationState.IDLE;
    }

    @Override
    public void initTransientValues() {
        super.initTransientValues();
        sprite_sheet = new TextureAtlas(Gdx.files.internal("characters/ken/ken_sprite_sheet.atlas"));
        defineAnimationLengths();
        defineKeyFrames();
    }

    /*
       Manual flip function which will be used as the basis for turning characters
       around when they switch sides.
    */
    public void flip() {
        facingRight = !facingRight;
        if(facingRight) {
            float temp = anim_anchor.x;
            anchor_point.x = anim_anchor.x + default_width / 2.0f;
            anim_anchor.x -= default_width;
            anim_anchor.x = temp;
            for(int i = 1; i < physics_boxes.length; i++) {
                if(physics_boxes[i].isActive()) {
                    physics_boxes[i].setX(anchor_point.x - default_box_data[i][2]);
                }
            }
        } else {
            float temp = anim_anchor.x;
            anim_anchor.x += default_width;
            anchor_point.x = anim_anchor.x - default_width / 2.0f;
            anim_anchor.x = temp;
            for(int i = 1; i < physics_boxes.length; i++) {
                if(physics_boxes[i].isActive()) {
                    physics_boxes[i].setX(anchor_point.x - default_box_data[i][3]);
                }
            }
        }
        physics_boxes[0].setX(anchor_point.x - physics_boxes[0].getWidth() / 2.0f);
    }

    public void keepInBounds() {
        float offset = anchor_point.x - anim_anchor.x;
        int offset_lookup;
        if(facingRight) {
            offset_lookup = 2;
        } else {
            offset_lookup = 3;
        }
        if(anim_anchor.x < 0) {
            anim_anchor.x = 0;
            physics_boxes[0].setX(anchor_point.x - physics_boxes[0].getWidth() / 2.0f);
            for(int i = 1; i < physics_boxes.length; i++) {
                if(physics_boxes[i].isActive()) {
                    physics_boxes[i].setX(anchor_point.x - default_box_data[i][offset_lookup]);
                }
            }
        } else if (anim_anchor.x + default_width > 400) {
            anim_anchor.x = 400 - default_width;
            physics_boxes[0].setX(anchor_point.x - physics_boxes[0].getWidth() / 2.0f);
            for(int i = 1; i < physics_boxes.length; i++) {
                if(physics_boxes[i].isActive()) {
                    physics_boxes[i].setX(anchor_point.x - default_box_data[i][offset_lookup]);
                }
            }
        }
        anchor_point.x = anim_anchor.x + offset;
    }

    public boolean isAttacking() {
        return attacking;
    }

    public void update(float dt, int input) {
        super.update(dt, input);
        anchor_point.x += direction * dt;
        if (attacking) {
            switch (anim_state) {
                case C_LIGHT: {
                    if (anim_state_time >= FRAME_LENGTH * 4) {
                        updateHurtBoxes(anchor_point, c_light_0, true);
                    }
                    if (anim_state_time >= FRAME_LENGTH * 7) {
                        updateHurtBoxes(anchor_point, c_light_1, false);
                    }
                    if (anim_state_time >= FRAME_LENGTH * 14) {
                        resetDefaultBoxes(anchor_point, default_box_data);
                        attacking = false;
                    }
                    break;
                }
                case SPECIAL: {
                    // start up boxes.
                    if (anim_state_time <= FRAME_LENGTH * 9) {
                        updateHurtBoxes(anchor_point, hadouken_boxes_0, false);
                    }

                    if (anim_state_time > FRAME_LENGTH * 9 && anim_state_time <= FRAME_LENGTH * 11) {
                        updateHurtBoxes(anchor_point, hadouken_boxes_1, false);
                    }

                    if (anim_state_time > FRAME_LENGTH * 11 && anim_state_time <= FRAME_LENGTH * 51) {
                        updateHurtBoxes(anchor_point, hadouken_boxes_2, false);
                    }

                    if (anim_state_time >= FRAME_LENGTH * 51) {
                        resetDefaultBoxes(anchor_point, default_box_data);
                        attacking = false;
                    }
                    break;
                }
            }
        }
    }

    @Override
    public void drawDebug(ShapeRenderer sr, OrthographicCamera camera) {
        super.drawDebug(sr, camera);
        this.sr.begin(ShapeRenderer.ShapeType.Line);
        this.sr.setProjectionMatrix(camera.combined);
        this.sr.setColor(Color.YELLOW);
        this.sr.rect(anchor_point.x, anchor_point.y, 1.0f, 1.0f);
        this.sr.end();
    }

    private void defineAnimationLengths() {
        animation_lengths = new float[AnimationState.length()][];
        // idle
        animation_lengths[0] = new float[] { FRAME_LENGTH * 6, FRAME_LENGTH * 4, FRAME_LENGTH * 6,
            FRAME_LENGTH * 6, FRAME_LENGTH * 4, FRAME_LENGTH * 6 };
        // walk Forward
        animation_lengths[1] = new float[] { FRAME_LENGTH * 3, FRAME_LENGTH * 6, FRAME_LENGTH * 6,
            FRAME_LENGTH * 6, FRAME_LENGTH * 6, FRAME_LENGTH * 6 };
        // walk Backward
        animation_lengths[2] = new float[] { FRAME_LENGTH * 3, FRAME_LENGTH * 6, FRAME_LENGTH * 6,
            FRAME_LENGTH * 6, FRAME_LENGTH * 6, FRAME_LENGTH * 6 };
        // block
        animation_lengths[3] = new float[0];
        // hit
        animation_lengths[4] = new float[0];
        // knock down
        animation_lengths[5] = new float[0];
        // get up
        animation_lengths[6] = new float[0];
        // close light
        animation_lengths[7] = new float[] { FRAME_LENGTH * 3, FRAME_LENGTH * 4, FRAME_LENGTH * 7 };
        // close medium
        animation_lengths[8] = new float[0];
        // far light
        animation_lengths[9] = new float[0];
        // far medium
        animation_lengths[10] = new float[0];
        // heavy
        animation_lengths[11] = new float[0];
        // special
        animation_lengths[12] = new float[] { FRAME_LENGTH * 2, FRAME_LENGTH * 7, FRAME_LENGTH * 2,
            FRAME_LENGTH * 4, FRAME_LENGTH * 4, FRAME_LENGTH * 4, FRAME_LENGTH * 4, FRAME_LENGTH * 4,
            FRAME_LENGTH * 4, FRAME_LENGTH * 4, FRAME_LENGTH * 4, FRAME_LENGTH * 4,
            FRAME_LENGTH * 4 };
        // super
        animation_lengths[13] = new float[0];
        // forward throw
        animation_lengths[14] = new float[0];
        // backward throw
        animation_lengths[15] = new float[0];
    }

    private void defineKeyFrames() {
        if(animation_lengths != null) {
            Array<TextureAtlas.AtlasRegion> key_frames = new Array<>();
            key_frames.add(sprite_sheet.findRegions("idle").get(0));
            key_frames.add(sprite_sheet.findRegions("idle").get(1));
            key_frames.add(sprite_sheet.findRegions("idle").get(2));
            key_frames.add(sprite_sheet.findRegions("idle").get(3));
            key_frames.add(sprite_sheet.findRegions("idle").get(4));
            key_frames.add(sprite_sheet.findRegions("idle").get(2));
            animation_map.put(AnimationState.IDLE, new SpriteAnimation<>(key_frames, animation_lengths[0]));

            key_frames = new Array<>();
            key_frames.add(sprite_sheet.findRegions("walk_forward").get(0));
            key_frames.add(sprite_sheet.findRegions("walk_forward").get(1));
            key_frames.add(sprite_sheet.findRegions("walk_forward").get(2));
            key_frames.add(sprite_sheet.findRegions("walk_forward").get(3));
            key_frames.add(sprite_sheet.findRegions("walk_forward").get(4));
            key_frames.add(sprite_sheet.findRegions("walk_forward").get(2));
            animation_map.put(AnimationState.FWD_DASH, new SpriteAnimation<>(key_frames, animation_lengths[1]));

            key_frames = new Array<>();
            key_frames.add(sprite_sheet.findRegions("walk_backward").get(0));
            key_frames.add(sprite_sheet.findRegions("walk_backward").get(1));
            key_frames.add(sprite_sheet.findRegions("walk_backward").get(2));
            key_frames.add(sprite_sheet.findRegions("walk_backward").get(3));
            key_frames.add(sprite_sheet.findRegions("walk_backward").get(4));
            key_frames.add(sprite_sheet.findRegions("walk_backward").get(2));
            animation_map.put(AnimationState.BWD_DASH, new SpriteAnimation<>(key_frames, animation_lengths[2]));

            key_frames = new Array<>();
            key_frames.add(sprite_sheet.findRegions("jab").get(0));
            key_frames.add(sprite_sheet.findRegions("jab").get(1));
            key_frames.add(sprite_sheet.findRegions("jab").get(0));
            animation_map.put(AnimationState.C_LIGHT, new SpriteAnimation<>(key_frames, animation_lengths[7]));

            key_frames = new Array<>();
            key_frames.add(sprite_sheet.findRegions("hadouken").get(0));
            key_frames.add(sprite_sheet.findRegions("hadouken").get(1));
            key_frames.add(sprite_sheet.findRegions("hadouken").get(2));
            key_frames.add(sprite_sheet.findRegions("hadouken").get(3));
            key_frames.add(sprite_sheet.findRegions("hadouken").get(4));
            key_frames.add(sprite_sheet.findRegions("hadouken").get(3));
            key_frames.add(sprite_sheet.findRegions("hadouken").get(4));
            key_frames.add(sprite_sheet.findRegions("hadouken").get(3));
            key_frames.add(sprite_sheet.findRegions("hadouken").get(4));
            key_frames.add(sprite_sheet.findRegions("hadouken").get(3));
            key_frames.add(sprite_sheet.findRegions("hadouken").get(4));
            key_frames.add(sprite_sheet.findRegions("hadouken").get(3));
            key_frames.add(sprite_sheet.findRegions("hadouken").get(5));
            animation_map.put(AnimationState.SPECIAL, new SpriteAnimation<>(key_frames, animation_lengths[12]));
        }
    }
}
