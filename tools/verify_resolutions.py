from pathlib import Path
import subprocess,os,struct,argparse
ROOT=Path(__file__).resolve().parents[1]
p=argparse.ArgumentParser();p.add_argument('--java',type=Path,required=True);p.add_argument('--sizes',nargs='+',default=['640x480','720x480','720x720','1024x768','1280x720']);p.add_argument('--resume',action='store_true');a=p.parse_args()
for size in a.sizes:
 w,h=size.split('x');out=ROOT/'build/resolutions'/size;out.mkdir(parents=True,exist_ok=True)
 cmd=[str(a.java.resolve()),'-Xmx512m','-Dmewnbase.hidden=true',f'-Dmewnbase.width={w}',f'-Dmewnbase.height={h}',f'-Dmewnbase.root={ROOT}/package/mewnbase',f'-Dmewnbase.data={ROOT}/package/mewnbase/data/',f'-Dmewnbase.output={out}','-cp',os.pathsep.join([str(ROOT/'build/test-classes'),str(ROOT/'package/mewnbase/runtime/mewnbase-host.jar'),str(ROOT/'package/mewnbase/game/desktop-1.0.jar')]),'org.portmaster.mewnbase.GameplaySmoke']
 if a.resume: cmd.insert(1,'-Dmewnbase.resumeTest=true')
 with (out/('resume.log' if a.resume else 'run.log')).open('w') as log: result=subprocess.run(cmd,cwd=out,stdout=log,stderr=subprocess.STDOUT,timeout=120)
 text=(out/('resume.log' if a.resume else 'run.log')).read_text();assert result.returncode==0,text[-6000:];assert 'GAMEPLAY_SAVE_OK' in text
 assert struct.unpack('>II',(out/'final.png').read_bytes()[16:24])==(int(w),int(h))
 print('RENDER_OK',size,flush=True)
