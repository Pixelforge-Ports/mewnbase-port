package org.portmaster.mewnbase;
import com.badlogic.gdx.*;
import com.badlogic.gdx.backends.lwjgl3.*;
import com.cairn4.moonbase.*;
import com.cairn4.moonbase.ui.*;
public class GameplaySmoke extends Main {
 int frame,play; boolean begun;
 public void create(){if(Boolean.getBoolean("mewnbase.resumeTest")){INSTANT_RUN=true;begun=true;}super.create();} long start;
 public static void main(String[] a){new Lwjgl3Application(new GameplaySmoke(),configuration());}
 public void render(){
  super.render();frame++;
  if(frame==600&&!begun){
   Mission m=new Mission(Mission.MissionTypes.normal,"PortTest");m.seed="portmaster";m.characterSuitColor=Player.suitColorList[0];
   m.dayCycleMode=Mission.dayCycleModes.defaultDay;m.weatherMode=Mission.weatherModes.normal;m.waterMode=Mission.waterModes.defaultWater;m.techTreeMode=Mission.techTreeModes.defaultTech;m.creatureMode=Mission.creatureModes.passive;
   setMission(m);setScreen(new LoadingScreen(this,true));begun=true;
  }
  if(begun&&getScreen() instanceof GameScreen){
   GameScreen s=(GameScreen)getScreen();play++;
   if(play==60){controls.keyDown(Input.Keys.D);start=System.nanoTime();}
   if(play==120){controls.keyUp(Input.Keys.D);if((System.nanoTime()-start)/1e9<0.9)throw new AssertionError("frame pacing");controls.keyDown(Input.Keys.TAB);controls.keyUp(Input.Keys.TAB);}
   if(play==240){s.gameLoader.saveGame(s.world,false);if(GameLoader.getSavedGames().isEmpty())throw new AssertionError("save missing");capture(System.getProperty("mewnbase.output")+"/final.png");System.out.println("GAMEPLAY_SAVE_OK");Gdx.app.exit();}
  }
  if(frame>1800)throw new AssertionError("gameplay timeout");
 }
}
