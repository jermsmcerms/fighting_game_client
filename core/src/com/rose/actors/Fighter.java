package com.rose.actors;

import static com.rose.constants.Constants.*;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.rose.animation.AnimationState;
import com.rose.animation.SpriteAnimation;
import com.rose.physics.Boxes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Fighter implements Serializable {
    public static final long serialVersionUID = 43L;
    protected ArrayList<Boxes> ground_boxes;
    protected boolean facingRight;
    protected boolean showBoxes;
    protected Vector2 anim_anchor;
    protected Vector2 anchor_point;
    transient protected TextureAtlas sprite_sheet;
    protected  AnimationState anim_state;
    transient protected Map<AnimationState, SpriteAnimation<TextureAtlas.AtlasRegion>> animation_map;
    transient SpriteAnimation<TextureAtlas.AtlasRegion> animation;
    protected float anim_state_time;
    protected float direction;
    protected boolean attacking;
    protected Boxes[] physics_boxes;

    private int health;
    private boolean damage_applied;
    private int hitstun;
    private int combo_counter;
    private boolean attacked;

    public Fighter(Vector2 anim_anchor, boolean facingRight) {
        this.anim_anchor = anim_anchor;
        this.facingRight = facingRight;

        /*
            These boxes must apply to ALL characters when standing on the ground, in an idle state.
            physics_box[0] - Collision box
            physics_box[1-3] - Std hurt boxes
            physics_box[4] - Hurt box
         */
        physics_boxes = new Boxes[5];
        physics_boxes[0] = new Boxes(Boxes.Type.Collision, true);
        for(int i = 1; i < 4; i++) {
            physics_boxes[i] = new Boxes(Boxes.Type.Hurt, true);
        }
        physics_boxes[4] = new Boxes(Boxes.Type.Hit, false);

        showBoxes = true;

        health = 100;

        animation_map = new HashMap<>(AnimationState.length());
    }

    protected void initializePhysicsBoxes(float[][] box_sizes, Vector2 anchor_point) {
        if(physics_boxes != null) {
            if(facingRight) {
                physics_boxes[0].initialize(box_sizes[0][2], box_sizes[0][4], box_sizes[0][0], box_sizes[0][1]);
                for(int i = 1; i < physics_boxes.length - 1; i++) {
                    physics_boxes[i].initialize(anchor_point.x - box_sizes[i][2],
                                                anchor_point.y + box_sizes[i][4],
                                                box_sizes[i][0],
                                                box_sizes[i][1]);
                }
            } else {
                physics_boxes[0].initialize(box_sizes[0][3], box_sizes[0][4], box_sizes[0][0], box_sizes[0][1]);
                for(int i = 1; i < physics_boxes.length - 1; i++) {
                    physics_boxes[i].initialize(anchor_point.x - box_sizes[i][3],
                                                anchor_point.y + box_sizes[i][4],
                                                box_sizes[i][0],
                                                box_sizes[i][1]);
                }
            }
        }
    }

    public boolean isAttacking() {
        return attacking;
    }

    public void draw(float dt, SpriteBatch batch, OrthographicCamera camera) {
        animation = animation_map.get(anim_state);
        if(anim_state_time > (animation.getAnimationDuration())) {
            anim_state_time -= animation.getAnimationDuration();
            if(anim_state_time < 0f) {
                anim_state_time = 0f;
            }
            if( anim_state == AnimationState.C_LIGHT ||
                anim_state == AnimationState.SPECIAL) {
                anim_state = AnimationState.IDLE;
                attacking = false;
            }
        }

        Optional<TextureAtlas.AtlasRegion> currentFrame = animation.getFrame(anim_state_time);
        currentFrame.ifPresent(current_anim -> {
            float width = facingRight ? currentFrame.get().getRegionWidth() : -currentFrame.get().getRegionWidth();
            // TODO: There appears to some extra offset I'm not accounting for when drawing left facing sprites
            float x = facingRight ? anim_anchor.x : anim_anchor.x + currentFrame.get().getRegionWidth();
            batch.draw(current_anim, x, anim_anchor.y - 4, width, currentFrame.get().getRegionHeight());
        });
    }

    public void drawDebug(ShapeRenderer sr, OrthographicCamera camera) {
        if(physics_boxes != null) {
            sr.setColor(0,0,1,0.5F);
            sr.rect(physics_boxes[0].getX(), physics_boxes[0].getY(), physics_boxes[0].getWidth(), physics_boxes[0].getHeight());

            sr.setColor(0,1,0,0.4F);
            for (int i = 1; i < physics_boxes.length - 1; i++) {
                if (physics_boxes[i] != null) {
                    sr.rect(physics_boxes[i].getX(), physics_boxes[i].getY(), physics_boxes[i].getWidth(), physics_boxes[i].getHeight());
                }
            }

            sr.setColor(Color.RED);
            sr.rect(physics_boxes[physics_boxes.length-1].getX(), physics_boxes[physics_boxes.length-1].getY(), physics_boxes[physics_boxes.length-1].getWidth(), physics_boxes[physics_boxes.length-1].getHeight());

            sr.setColor(Color.PINK);
            sr.rect(anim_anchor.x, anim_anchor.y, 1.0f, 1.0f);
        }
    }

    public void checkForHit(Fighter otherFighter, float delta) {
        if(physics_boxes[4].isActive()) {
            for(Boxes box : otherFighter.getBoxes()) {
                if(box.getType() == Boxes.Type.Hurt) {
                    if(physics_boxes[4].overlaps(box) && !damage_applied) {
                        otherFighter.applyHit(5, 17);
                        damage_applied = true;
                        applyPushBack(anim_state, delta);
                        return;
                    }
                }
            }
        }
    }

    private void applyHit(int damage, int hitstun) {
        attacked = true;
        health -= damage; // TODO: change this to be damage * some decreasing value for every combo hit
        if(this.hitstun > 0) {
            combo_counter++;
        }

        this.hitstun = hitstun;
    }

    private void applyPushBack(AnimationState anim_state, float delta) {
        float direction;
        if(anim_state == AnimationState.C_LIGHT) {
            if(facingRight) {
                direction = -700 * delta;
            } else {
                direction = 700 * delta;
            }

            anchor_point.x += direction;
            anim_anchor.x += direction;
            for(Boxes box : physics_boxes) {
                if(box.isActive()) {
                    box.update(direction);
                }
            }
        }
    }

    public Boxes[] getBoxes() {
        return physics_boxes;
    }

    public void update(float dt, int input) {
        anim_state_time += dt;
        anim_anchor.x += direction * dt;

        if(!attacked) {
            direction = 0.0f; // Allows continuous movement only if movement buttons are held.
            if (!attacking) {
                if (damage_applied) {
                    damage_applied = false;
                }

                if (input >= A_BTN) {
                    anim_state_time = 0.0f;
                }
                switch (input) {
                    case NEUTRAL: {
                        anim_state = AnimationState.IDLE;
                        break;
                    }
                    case LEFT: {
                        direction = -200f;
                        if (facingRight) {
                            anim_state = AnimationState.BWD_DASH;
                        } else {
                            anim_state = AnimationState.FWD_DASH;
                        }
                        break;
                    }
                    case RIGHT: {
                        direction = 200f;
                        if (facingRight) {
                            anim_state = AnimationState.FWD_DASH;
                        } else {
                            anim_state = AnimationState.BWD_DASH;
                        }
                        break;
                    }
                    case A_BTN: {
                        attacking = true;
                        anim_state = AnimationState.C_LIGHT;
                        break;
                    }
                    case D_BTN: {
                        attacking = true;
                        anim_state = AnimationState.SPECIAL;
                        break;
                    }
                    default:
                        break;
                }
                for (Boxes physics_box : physics_boxes) {
                    if (physics_box.isActive()) {
                        physics_box.update(dt, direction);
                    }
                }
            }
        } else {
            hitstun--;
            if(hitstun < 0) {
                hitstun = 0;
                attacked = false;
                if(combo_counter != 0) {
                    combo_counter = 0;
                }
            }
        }
    }

    public float getHealth() {
        return health;
    }

    protected void updateHurtBoxes(Vector2 anchor_point, float[][] box_data, boolean activateHitBox) {
        physics_boxes[physics_boxes.length-1].setActive(activateHitBox);
        if(!physics_boxes[physics_boxes.length-1].isActive() && physics_boxes[physics_boxes.length-1].getWidth() > 0) {
            physics_boxes[physics_boxes.length-1].resize(0,0,0,0);
        }

        for(int i = 0; i < physics_boxes.length; i++) {
            if(physics_boxes[i].isActive()) {
                if(facingRight) {
                    physics_boxes[i].resize(
                        anchor_point.x + box_data[i][2],
                        anchor_point.y + box_data[i][4],
                        box_data[i][0],
                        box_data[i][1]);
                } else {
                    physics_boxes[i].resize(
                        anchor_point.x + box_data[i][3],
                        anchor_point.y + box_data[i][4],
                        box_data[i][0],
                        box_data[i][1]);
                }
            }
        }
    }

    protected void resetDefaultBoxes(Vector2 anchor_point, float[][] box_data) {
        box_data[0][2] = anchor_point.x - (box_data[0][0] / 2);
        box_data[0][3] = anchor_point.x - (box_data[0][0] / 2 - 1);
        box_data[0][4] = anchor_point.y;
        initializePhysicsBoxes(box_data, anchor_point);
    }

    private boolean testOverlap(Rectangle jab_hitbox, ArrayList<Boxes> ground_boxes) {
        for(int i = 1; i < ground_boxes.size(); i++) {
            Boxes test_box = ground_boxes.get(i);
            if( jab_hitbox.x < test_box.getX() + test_box.getWidth() &&
                jab_hitbox.x + jab_hitbox.getWidth() > test_box.getX() &&
                jab_hitbox.y < test_box.getY() + test_box.getHeight() &&
                jab_hitbox.y + jab_hitbox.getHeight() > test_box.getY()) {
                return true;
            }
        }
        return false;
    }

    public void faceOpponent(Fighter fighter) {
        Boxes center = ground_boxes.get(3);
        Boxes other_center = fighter.ground_boxes.get(3);
        facingRight =
            center.getX() + center.getWidth() / 2f <
                other_center.getX() + other_center.getWidth() / 2f;
    }

    public Vector2 getAnchorPoint() {
        return anchor_point;
    }

    public AnimationState getAnimationState() {
        return anim_state;
    }

    public float getAnimStateTime() {
        return anim_state_time;
    }

    public void keepInBounds() {
    }

    public int getComboCounter() {
        return combo_counter;
    }
}
