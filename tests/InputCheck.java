package org.portmaster.mewnbase;
import com.badlogic.gdx.*;
import java.lang.reflect.Proxy;
public class InputCheck {
 public static void main(String[] args){
  Input physical=(Input)Proxy.newProxyInstance(Input.class.getClassLoader(),new Class<?>[]{Input.class},(p,m,a)->{if(m.getReturnType()==boolean.class)return false;if(m.getReturnType()==int.class)return 0;return null;});
  DisplayLayout layout=new DisplayLayout();layout.resize(720,480);HandheldInput c=new HandheldInput(physical,layout);StringBuilder events=new StringBuilder();
  c.proxy.setInputProcessor(new InputAdapter(){public boolean keyTyped(char x){events.append(x);return true;}public boolean touchDown(int x,int y,int p,int b){events.append("click");return true;}});
  c.keyDown(Input.Keys.F3);if(!c.proxy.isButtonPressed(0))throw new AssertionError();c.keyUp(Input.Keys.F3);if(c.proxy.isButtonPressed(0)||!events.toString().equals("click"))throw new AssertionError();
  c.keyDown(Input.Keys.F2);c.keyUp(Input.Keys.F2);c.keyDown(Input.Keys.D);if(!c.proxy.isKeyPressed(Input.Keys.D)||!c.proxy.isKeyJustPressed(Input.Keys.D))throw new AssertionError();c.endFrame();if(c.proxy.isKeyJustPressed(Input.Keys.D))throw new AssertionError();c.keyUp(Input.Keys.D);
  c.keyDown(Input.Keys.F8);c.keyDown(Input.Keys.TAB);if(!c.proxy.isKeyPressed(Input.Keys.R))throw new AssertionError();c.keyUp(Input.Keys.F8);c.keyUp(Input.Keys.TAB);if(c.proxy.isKeyPressed(Input.Keys.R))throw new AssertionError("stuck modifier");
  c.keyDown(Input.Keys.F7);c.keyUp(Input.Keys.F7);c.keyDown(Input.Keys.F3);c.keyUp(Input.Keys.F3);if(!events.toString().equals("clickA"))throw new AssertionError("text entry");c.releaseAll();System.out.println("INPUT_OK");
 }
}
