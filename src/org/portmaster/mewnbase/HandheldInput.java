package org.portmaster.mewnbase;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import java.lang.reflect.Proxy;
import java.util.function.Function;

/** Translates real keyboard/mouse input from gptokeyb into the game's viewport. */
public final class HandheldInput extends InputAdapter {
    public final Input proxy;
    private final Input physical;
    private final DisplayLayout layout;
    private final Graphics gameGraphics;
    private final GL20 gameGl;
    private InputProcessor target;
    private ShapeRenderer cursor;

    public HandheldInput(Input physical, DisplayLayout layout) {
        this.physical = physical;
        this.layout = layout;
        gameGraphics = Gdx.graphics;
        gameGl = Gdx.gl20;
        target = physical.getInputProcessor();
        proxy = (Input) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{Input.class},
            (self, method, args) -> {
                switch (method.getName()) {
                    case "setInputProcessor":
                        if (args[0] != this) target = (InputProcessor) args[0];
                        physical.setInputProcessor(this);
                        return null;
                    case "getInputProcessor": return target;
                    case "getX": return layout.inputX(args == null ? physical.getX() : physical.getX((Integer) args[0]));
                    case "getY": return layout.inputY(args == null ? physical.getY() : physical.getY((Integer) args[0]));
                    case "getDeltaX": return (int) (physical.getDeltaX() * (double) layout.gameWidth / layout.width);
                    case "getDeltaY": return (int) (physical.getDeltaY() * (double) layout.gameHeight / layout.height);
                    case "setCursorPosition":
                        physical.setCursorPosition(layout.cursorX((Integer) args[0]), layout.cursorY((Integer) args[1]));
                        return null;
                    default: return Main.delegate(physical, method, args);
                }
            });
        physical.setInputProcessor(this);
    }

    // LWJGL restores physical globals before input; Scene2D needs logical coordinates.
    private boolean send(Function<InputProcessor, Boolean> event) {
        if (target == null) return false;
        Graphics previousGraphics = Gdx.graphics;
        Input previousInput = Gdx.input;
        GL20 previousGl = Gdx.gl, previousGl20 = Gdx.gl20;
        Gdx.graphics = gameGraphics;
        Gdx.gl = gameGl;
        Gdx.gl20 = gameGl;
        Gdx.input = proxy;
        try { return event.apply(target); }
        finally {
            Gdx.graphics = previousGraphics;
            Gdx.input = previousInput;
            Gdx.gl = previousGl;
            Gdx.gl20 = previousGl20;
        }
    }

    @Override public boolean keyDown(int key) { return send(p -> p.keyDown(key)); }
    @Override public boolean keyUp(int key) { return send(p -> p.keyUp(key)); }
    @Override public boolean keyTyped(char value) { return send(p -> p.keyTyped(value)); }
    @Override public boolean mouseMoved(int x, int y) {
        return send(p -> p.mouseMoved(layout.inputX(x), layout.inputY(y)));
    }
    @Override public boolean touchDown(int x, int y, int pointer, int button) {
        return send(p -> p.touchDown(layout.inputX(x), layout.inputY(y), pointer, button));
    }
    @Override public boolean touchUp(int x, int y, int pointer, int button) {
        return send(p -> p.touchUp(layout.inputX(x), layout.inputY(y), pointer, button));
    }
    @Override public boolean touchDragged(int x, int y, int pointer) {
        return send(p -> p.touchDragged(layout.inputX(x), layout.inputY(y), pointer));
    }
    @Override public boolean scrolled(float x, float y) { return send(p -> p.scrolled(x, y)); }

    public void draw() {
        if (cursor == null) cursor = new ShapeRenderer();
        float x = layout.inputX(physical.getX()), y = layout.gameHeight - layout.inputY(physical.getY());
        cursor.setProjectionMatrix(new Matrix4().setToOrtho2D(0, 0, layout.gameWidth, layout.gameHeight));
        cursor.begin(ShapeRenderer.ShapeType.Filled);
        cursor.setColor(0, 0, 0, 1);
        cursor.rect(x - 7, y - 2, 15, 5); cursor.rect(x - 2, y - 7, 5, 15);
        cursor.setColor(1, 1, 1, 1);
        cursor.rect(x - 6, y, 13, 1); cursor.rect(x, y - 6, 1, 13);
        cursor.end();
    }
    public void dispose() {
        physical.setInputProcessor(target);
        if (cursor != null) cursor.dispose();
    }
}
