package org.portmaster.mewnbase;

import com.badlogic.gdx.*;
import com.badlogic.gdx.backends.lwjgl3.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.glutils.HdpiMode;
import com.cairn4.moonbase.*;
import java.lang.reflect.*;
import java.nio.file.*;

/** Handheld host; all game implementation and data remain in the owner's files. */
public class Main extends MoonBase {
    public final DisplayLayout layout=new DisplayLayout();
    protected Graphics physicalGraphics;
    private GL20 physicalGl,bridgeGl;
    private Graphics bridgeGraphics;
    private Application physicalApp;
    private Input physicalInput;
    public HandheldInput controls;
    private long lastFrame;
    private boolean ready;
    private int frames;

    public Main() {super(offline(PlatformAdapter.class),offline(AchievementAdapter.class),Paths.get("").toAbsolutePath().relativize(Paths.get(System.getProperty("mewnbase.data")).toAbsolutePath()).toString().replace('\\','/')+"/");}
    private static <T> T offline(Class<T> type) {
        return type.cast(Proxy.newProxyInstance(Main.class.getClassLoader(),new Class<?>[]{type},(self,method,args)->{
            if(method.getDeclaringClass()==Object.class){
                if(method.getName().equals("hashCode"))return System.identityHashCode(self);
                if(method.getName().equals("equals"))return self==args[0];
                return "PortMaster offline adapter";
            }
            if(method.getName().equals("writeErrorLog") && args!=null && args[0] instanceof Throwable)((Throwable)args[0]).printStackTrace();
            if(method.getReturnType()==boolean.class)return false;
            return null;
        }));
    }
    public static Lwjgl3ApplicationConfiguration configuration(){
        int w=Integer.getInteger("mewnbase.width",640),h=Integer.getInteger("mewnbase.height",480);
        if(w<160||h<160||w>8192||h>8192)throw new IllegalArgumentException("Invalid screen size");
        Lwjgl3ApplicationConfiguration cfg=new Lwjgl3ApplicationConfiguration();
        cfg.setTitle("MewnBase - PortMaster");cfg.setWindowedMode(w,h);cfg.setResizable(false);
        cfg.setHdpiMode(HdpiMode.Pixels);cfg.setForegroundFPS(60);cfg.setIdleFPS(30);cfg.useVsync(false);
        cfg.setOpenGLEmulation(Lwjgl3ApplicationConfiguration.GLEmulation.GL20,2,0);
        cfg.setInitialVisible(!Boolean.getBoolean("mewnbase.hidden"));
        cfg.disableAudio(Boolean.getBoolean("mewnbase.noAudio"));
        if(Boolean.getBoolean("mewnbase.fullscreen"))cfg.setFullscreenMode(Lwjgl3ApplicationConfiguration.getDisplayMode());
        return cfg;
    }
    public static void main(String[] args)throws Exception{
        VerifyGame.check(Paths.get(System.getProperty("mewnbase.root")));
        new Lwjgl3Application(new Main(),configuration());
    }
    @Override public void create(){
        physicalApp=Gdx.app;physicalGraphics=Gdx.graphics;physicalGl=Gdx.gl20;physicalInput=Gdx.input;
        layout.resize(physicalGraphics.getWidth(),physicalGraphics.getHeight());installDisplayBridge();
        controls=new HandheldInput(physicalInput,layout);Gdx.input=controls.proxy;
        SettingsData sd=SettingsLoader.getInstance().settingsData;
        sd.USE_CONTROLLER=false;sd.FULLHD=false;sd.VIRTUALJOYSTICK=false;SettingsLoader.getInstance().save();
        // Keep the original edition flag. Offline adapters handle storefront calls.
        DISCORD_ON=false;
        super.create();ready=true;resize(physicalGraphics.getWidth(),physicalGraphics.getHeight());
        System.out.println("GAME_CREATE_OK; original game version="+version()+"; data="+coreFolder);
    }
    public static String version(){try{return (String)MoonBase.class.getField("VERSION").get(null);}catch(Exception e){throw new IllegalStateException(e);}}
    @Override public void resize(int w,int h){
        if(!ready||w<160||h<160)return;restoreDisplayBridge();layout.resize(w,h);
        super.resize(layout.gameWidth,layout.gameHeight);
        System.out.println("GAME_RESIZE_OK "+w+"x"+h);
    }
    @Override public void render(){
        restoreDisplayBridge();Gdx.input=controls.proxy;
        long now=System.nanoTime();
        while(lastFrame!=0 && now-lastFrame<16666667L){java.util.concurrent.locks.LockSupport.parkNanos(16666667L-(now-lastFrame));now=System.nanoTime();}
        lastFrame=now;
        physicalGl.glBindFramebuffer(GL20.GL_FRAMEBUFFER,0);physicalGl.glDisable(GL20.GL_SCISSOR_TEST);
        physicalGl.glClearColor(0,0,0,1);physicalGl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        super.render();
        Gdx.gl20.glBindFramebuffer(GL20.GL_FRAMEBUFFER,0);Gdx.gl20.glViewport(0,0,layout.gameWidth,layout.gameHeight);
        controls.draw();frames++;
        if(Integer.getInteger("mewnbase.smokeFrames",0)>0 && frames>=Integer.getInteger("mewnbase.smokeFrames",0)){
            capture(System.getProperty("mewnbase.capture"));System.out.println("SMOKE_OK frames="+frames);Gdx.app.exit();
        }
    }
    public void capture(String path){if(path==null)return;Pixmap pm=Pixmap.createFromFrameBuffer(0,0,physicalGraphics.getBackBufferWidth(),physicalGraphics.getBackBufferHeight());try{PixmapIO.writePNG(Gdx.files.absolute(path),pm,-1,true);}finally{pm.dispose();}}
    @Override public void pause(){super.pause();}
    @Override public void resume(){lastFrame=0;super.resume();}
    @Override public void dispose(){try{if(ready){SettingsLoader.getInstance().save();super.dispose();}if(controls!=null)controls.dispose();}finally{Gdx.app=physicalApp;Gdx.graphics=physicalGraphics;Gdx.gl=physicalGl;Gdx.gl20=physicalGl;Gdx.input=physicalInput;}}
    static Object delegate(Object target,Method method,Object[] args) throws Throwable {
        try { return method.invoke(target,args); }
        catch (InvocationTargetException e) { throw e.getCause(); }
    }
    private void restoreDisplayBridge() {
        if (bridgeGraphics != null) { Gdx.graphics = bridgeGraphics; Gdx.gl = bridgeGl; Gdx.gl20 = bridgeGl; }
    }
    private void installDisplayBridge() {
        final int[] framebuffer = {0};
        GL20 gl = (GL20)Proxy.newProxyInstance(Main.class.getClassLoader(),new Class<?>[]{GL20.class},(self,method,args)-> {
            String name = method.getName();
            if (name.equals("glBindFramebuffer")) framebuffer[0] = (Integer)args[1];
            if (framebuffer[0] == 0 && (name.equals("glViewport") || name.equals("glScissor"))) {
                args = new Object[]{layout.viewportX((Integer)args[0]),layout.viewportY((Integer)args[1]),
                    layout.viewportWidth((Integer)args[2]),layout.viewportHeight((Integer)args[3])};
            }
            return delegate(physicalGl,method,args);
        });
        Gdx.gl = gl; Gdx.gl20 = gl;
        Gdx.graphics = (Graphics)Proxy.newProxyInstance(Main.class.getClassLoader(),new Class<?>[]{Graphics.class},(self,method,args)-> {
            switch (method.getName()) {
                case "getWidth": case "getBackBufferWidth": return layout.gameWidth;
                case "getHeight": case "getBackBufferHeight": return layout.gameHeight;
                case "getGL20": return gl;
                case "setWindowedMode": case "setFullscreenMode": case "supportsDisplayModeChange": return false;
                case "setVSync": return null;
                default: return delegate(physicalGraphics,method,args);
            }
        });
        bridgeGl = gl; bridgeGraphics = Gdx.graphics;
        final Application originalApp = Gdx.app;
        Gdx.app = (Application)Proxy.newProxyInstance(Main.class.getClassLoader(),new Class<?>[]{Application.class},(self,method,args)-> {
            if (method.getName().equals("getGraphics")) return Gdx.graphics;
            if (method.getName().equals("getInput")) return Gdx.input;
            return delegate(originalApp,method,args);
        });

    }
}
