package com.rose.renderering;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.rose.management.GameState;
import com.rose.management.NonGameState;
import com.rose.ui.MatchUI;
import com.rose.ui.TouchControlsUI;

public class Renderer {
    private final Stage stage;
    private final SpriteBatch batch;
    private final ShapeRenderer sr;
    private final OrthographicCamera camera;
    private final Texture background;

    public Renderer(Stage stage) {
        this.stage = stage;
        batch = (SpriteBatch)stage.getBatch();
        sr = new ShapeRenderer();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 420, 220);
        background = new Texture(Gdx.files.internal("sample_background.png"));
    }

    public void draw(GameState gameState, NonGameState nonGameState) {
        camera.update();
        batch.begin();
        batch.setProjectionMatrix(camera.combined);
        batch.draw(background, -190, 0, background.getWidth(), background.getHeight());
//        batch.draw(ui, 0, 0, ui.getWidth(), ui.getHeight());

        // Draw sprites
        for(int i = 0; i < gameState.getFighters().length; i++) {
            gameState.getFighters()[i].draw(Gdx.graphics.getDeltaTime(), batch, camera);
        }

        batch.end();

        // Draw hitboxes
        Gdx.gl.glEnable(GL20.GL_BLEND);
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setProjectionMatrix(camera.combined);

        for(int i = 0; i < gameState.getFighters().length; i++) {
            gameState.getFighters()[i].drawDebug(sr, camera);
        }
        sr.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        stage.act();
        stage.draw();

        // Draw UI
//        ui.updateUI(delta, ryu, ken);
//        if(ui.isMatchOver()) {
//            this.parent.changeScreen(Rose.ScreenType.MENU);
//        }
//        if(ui.getTimer() <= 0) {
//            this.parent.changeScreen(Rose.ScreenType.MENU);
//        }

    }
}
