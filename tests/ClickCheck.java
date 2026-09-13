package org.portmaster.mewnbase;
import com.badlogic.gdx.*;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
/** Reproduce backend input delivery before Main.render restores virtual graphics. */
public class ClickCheck extends Main {
 private Input backend;private Stage stage;private int clicks;
 public static void main(String[] a){new Lwjgl3Application(new ClickCheck(),configuration());}
 @Override public void create(){
  backend=Gdx.input;super.create();
  stage=new Stage(new FitViewport(layout.gameWidth,layout.gameHeight));stage.getViewport().update(layout.gameWidth,layout.gameHeight,true);
  Actor button=new Actor();button.setBounds(930,480,180,100);stage.addActor(button);
  button.addListener(new ClickListener(){public void clicked(InputEvent event,float x,float y){clicks++;}});
  button.addListener(new ClickListener(Input.Buttons.RIGHT){public void clicked(InputEvent event,float x,float y){clicks++;}});
  controls.proxy.setInputProcessor(stage);
  backendContext();backend.getInputProcessor().touchDown(layout.cursorX(1020),layout.cursorY(190),0,Input.Buttons.LEFT);backend.getInputProcessor().touchUp(layout.cursorX(1020),layout.cursorY(190),0,Input.Buttons.LEFT);
  if(clicks!=1)throw new AssertionError("Left mouse missed scaled Scene2D button");
  backendContext();backend.getInputProcessor().touchDown(layout.cursorX(1020),layout.cursorY(190),0,Input.Buttons.RIGHT);backend.getInputProcessor().touchUp(layout.cursorX(1020),layout.cursorY(190),0,Input.Buttons.RIGHT);
  if(clicks!=2)throw new AssertionError("Right mouse missed scaled Scene2D button");
  if(Gdx.graphics!=physicalGraphics||Gdx.input!=backend)throw new AssertionError("Backend context leaked");
  if(controls.proxy.getInputProcessor()!=stage)throw new AssertionError("Input processor contract");
  System.out.println("CLICK_COORDINATES_OK "+layout.screenWidth+"x"+layout.screenHeight);Gdx.app.exit();
 }
 private void backendContext(){Gdx.graphics=physicalGraphics;Gdx.input=backend;Gdx.gl=physicalGraphics.getGL20();Gdx.gl20=Gdx.gl;}
 @Override public void dispose(){if(stage!=null)stage.dispose();super.dispose();}
}
