package org.portmaster.mewnbase;
import com.badlogic.gdx.*;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.math.Vector2;
import com.cairn4.moonbase.ui.*;
import com.cairn4.moonbase.UpdateCheck;
import java.lang.reflect.Field;
/** Click the original game's New Game menu through the backend input processor. */
public class MenuClickCheck extends Main {
 private Input backend;private int frames,menuFrames,popupFrames,clickedAt;
 public static void main(String[] a){new Lwjgl3Application(new MenuClickCheck(),configuration());}
 @Override public void create(){backend=Gdx.input;super.create();UpdateCheck.SHOW_WHATSNEW=true;}
 @Override public void render(){
  super.render();frames++;
  if(clickedAt>0&&frames>=clickedAt+30){capture(System.getProperty("mewnbase.output")+"/new-game-menu.png");System.out.println("MENU_CLICK_OK");Gdx.app.exit();return;}
  if(getScreen() instanceof BaseScreen){
   BaseScreen screen=(BaseScreen)getScreen();
   if(active(screen) instanceof WhatsNewPopup && ++popupFrames==120){
    clickActor(screen,active(screen),"btnClose");

   }
   if(active(screen) instanceof WhatsNewPopup && popupFrames>200)throw new AssertionError("First-run popup did not close");
   if(active(screen) instanceof MainMenu && ++menuFrames==120){
    if(popupFrames>=120)System.out.println("POPUP_CLICK_OK");
    clickActor(screen,active(screen),"btnNewGame");
    if(!(active(screen) instanceof NewGameScreen))throw new AssertionError("Original New Game click did not navigate: "+active(screen));
    clickedAt=frames;
   }
  }
  if(frames>1800)throw new AssertionError("Main menu timeout");
 }
 private Menu active(BaseScreen screen){return screen.menuStack.isEmpty()?null:screen.menuStack.get(screen.menuStack.size()-1);}
 private void clickActor(BaseScreen screen,Object menu,String name){
  try{
   Field f=(menu instanceof WhatsNewPopup?Popup.class:menu.getClass()).getDeclaredField(name);f.setAccessible(true);Actor button=(Actor)f.get(menu);
   Vector2 point=button.localToStageCoordinates(new Vector2(button.getWidth()/2,button.getHeight()/2));screen.stage.stageToScreenCoordinates(point);int px=layout.cursorX((int)point.x),py=layout.cursorY((int)point.y);backend.setCursorPosition(px,py);
   Gdx.graphics=physicalGraphics;Gdx.input=backend;Gdx.gl=physicalGraphics.getGL20();Gdx.gl20=Gdx.gl;
   backend.getInputProcessor().touchDown(px,py,0,Input.Buttons.LEFT);backend.getInputProcessor().touchUp(px,py,0,Input.Buttons.LEFT);
  }catch(ReflectiveOperationException e){throw new RuntimeException(e);}
 }
}
