package org.portmaster.mewnbase;
import java.nio.file.*;
import java.io.*;
import java.security.*;
import java.util.jar.*;
/** Standalone owner-data preflight, usable on the handheld without Python. */
public final class VerifyGame {
 public static final String KNOWN="9452d5588c457995599a3fb95ef6d570fa4aa2f3d367bd0dbe2d63a303272b6c";
 public static String check(Path root)throws Exception {
  Path jar=root.resolve("game/desktop-1.0.jar");
  MessageDigest md=MessageDigest.getInstance("SHA-256");
  try(InputStream in=Files.newInputStream(jar)){byte[] b=new byte[65536];int n;while((n=in.read(b))!=-1)md.update(b,0,n);}
  StringBuilder hex=new StringBuilder();for(byte b:md.digest())hex.append(String.format("%02x",b&255));
  String version;
  try(JarFile z=new JarFile(jar.toFile())){
   for(String entry:new String[]{"libgdxarm64.so","libgdx-box2darm64.so","linux/arm64/org/lwjgl/liblwjgl.so","linux/arm64/org/lwjgl/glfw/libglfw.so","linux/arm64/org/lwjgl/openal/libopenal.so","com/cairn4/moonbase/MoonBase.class","com/cairn4/moonbase/PlatformAdapter.class","com/badlogic/gdx/backends/lwjgl3/Lwjgl3Application.class"})if(z.getEntry(entry)==null)throw new IOException("Missing game API: "+entry);
   try(DataInputStream in=new DataInputStream(z.getInputStream(z.getEntry("com/cairn4/moonbase/MoonBase.class")))){version=readVersion(in);}
  }
  if(!version.equals("1.0.1")&&!version.equals("1.0.2"))throw new IOException("Unsupported version: "+version);
  if(version.equals("1.0.1")&&!hex.toString().equals(KNOWN))throw new IOException("Unrecognized 1.0.1 JAR; use the documented original build");
  if(version.equals("1.0.2")){
   if(!Files.exists(root.resolve("allow-unverified-1.0.2.txt")))throw new IOException("1.0.2 is untested. Read README before creating allow-unverified-1.0.2.txt");
   System.out.println("WARNING: 1.0.2 compatibility is UNVERIFIED; startup may fail");
  }
  for(String f:new String[]{"items.json","creatures.json","resource_tiles.json"})if(!Files.isRegularFile(root.resolve("data").resolve(f)))throw new IOException("Missing data/"+f+"; copy the complete data folder");
  Files.write(root.resolve("game-version.txt"),version.getBytes("UTF-8"));
  System.out.println("Game version "+version+"; SHA256="+hex);return version;
 }
 // Read the VERSION ConstantValue instead of loading or executing owner classes.
 static String readVersion(DataInputStream in)throws IOException{
  if(in.readInt()!=0xcafebabe)throw new IOException("Invalid class");in.readInt();Object[] cp=new Object[in.readUnsignedShort()];
  for(int i=1;i<cp.length;i++){int tag=in.readUnsignedByte();switch(tag){case 1:cp[i]=in.readUTF();break;case 7:case 8:case 16:case 19:case 20:cp[i]=in.readUnsignedShort();break;case 3:case 4:in.readInt();break;case 5:case 6:in.readLong();i++;break;case 9:case 10:case 11:case 12:case 17:case 18:in.readInt();break;case 15:in.readByte();in.readShort();break;default:throw new IOException("Unknown class constant");}}
  in.readShort();in.readShort();in.readShort();int count=in.readUnsignedShort();while(count-->0)in.readShort();count=in.readUnsignedShort();
  while(count-->0){in.readShort();String name=(String)cp[in.readUnsignedShort()];in.readShort();int attrs=in.readUnsignedShort();while(attrs-->0){String attr=(String)cp[in.readUnsignedShort()];int size=in.readInt();if(name.equals("VERSION")&&attr.equals("ConstantValue")&&size==2)return (String)cp[(Integer)cp[in.readUnsignedShort()]];byte[] skip=new byte[size];in.readFully(skip);}}
  throw new IOException("No VERSION constant");
 }
 public static void main(String[] args)throws Exception{check(Paths.get(args[0]));}
}
