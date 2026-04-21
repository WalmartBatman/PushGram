import { useState, useRef } from "react";

const PROFILE = {
  name: "AffaanX", tag: "#7734", avatar: "AX", level: 42,
  xp: 7840, xpMax: 10000, rank: "Diamond II",
  credits: 2840, totalWorkouts: 381, streak: 7,
  pushups: 12440, squats: 9820, crunches: 7310,
};

const LB = [
  { rank:1, name:"ProxVoid",   avatar:"PV", credits:9820, level:61, color:"#FFD700" },
  { rank:2, name:"ZeroX",      avatar:"ZX", credits:7450, level:55, color:"#C0C0C0" },
  { rank:3, name:"AffaanX",    avatar:"AX", credits:2840, level:42, color:"#CD7F32", isMe:true },
  { rank:4, name:"NightBlade", avatar:"NB", credits:2210, level:38, color:"#4FC3F7" },
  { rank:5, name:"CipherK",    avatar:"CK", credits:1780, level:33, color:"#FF6B35" },
  { rank:6, name:"ShadowRift", avatar:"SR", credits:1340, level:29, color:"#A8E6CF" },
];

// Change #1: squats credit = 1.5
const EX = [
  { id:"pushups",  label:"Push-Ups", icon:"💪", credit:2,   total:12440, color:"#FF6B35", gr:"#FF6B35,#FF9A5C" },
  { id:"squats",   label:"Squats",   icon:"🏋", credit:1.5, total:9820,  color:"#4FC3F7", gr:"#4FC3F7,#81D4FA" },
  { id:"crunches", label:"Crunches", icon:"🔥", credit:1,   total:7310,  color:"#FFD700", gr:"#FFD700,#FFEC6E" },
];

const RECENT = [
  { type:"pushups",  reps:40, credits:80, time:"12m ago" },
  { type:"squats",   reps:30, credits:45, time:"1h ago"  },
  { type:"crunches", reps:20, credits:20, time:"3h ago"  },
];

const PLATFORMS = [
  { id:"yt", name:"YouTube Music", color:"#FF4444", bg:"rgba(255,68,68,.1)",  bdr:"rgba(255,68,68,.25)", ico:"▶" },
  { id:"sp", name:"Spotify",       color:"#1DB954", bg:"rgba(29,185,84,.1)",  bdr:"rgba(29,185,84,.25)",ico:"♪" },
  { id:"am", name:"Apple Music",   color:"#FA2D55", bg:"rgba(250,45,85,.1)",  bdr:"rgba(250,45,85,.25)",ico:"♫" },
];

const DEFAULT_PLAYLIST = [
  { title:"Eye of the Tiger", artist:"Survivor",   dur:"4:05" },
  { title:"Lose Yourself",    artist:"Eminem",     dur:"5:26" },
  { title:"Can't Hold Us",    artist:"Macklemore", dur:"4:18" },
  { title:"Stronger",         artist:"Kanye West", dur:"5:11" },
];

// Change #3: dumbbell SVG logo
function Logo() {
  return (
    <svg width="28" height="20" viewBox="0 0 28 20" fill="none">
      <rect x="0"  y="7"   width="5"  height="6"  rx="2"   fill="#FF6B35"/>
      <rect x="5"  y="4"   width="3"  height="12" rx="1.5" fill="#FF6B35"/>
      <rect x="8"  y="8"   width="12" height="4"  rx="1.5" fill="#FF6B35"/>
      <rect x="20" y="4"   width="3"  height="12" rx="1.5" fill="#FF6B35"/>
      <rect x="23" y="7"   width="5"  height="6"  rx="2"   fill="#FF6B35"/>
      <circle cx="14" cy="10" r="2.5" fill="#0A0A0F"/>
      <rect x="12.5" y="8.5" width="3" height="3" rx="1" fill="#FF6B35"/>
    </svg>
  );
}

function Ring({ value, max, size=84, stroke=6, color="#FF6B35", children }) {
  const r = (size-stroke)/2;
  const c = 2*Math.PI*r;
  return (
    <div style={{position:"relative",width:size,height:size,flexShrink:0}}>
      <svg width={size} height={size} style={{transform:"rotate(-90deg)"}}>
        <circle cx={size/2} cy={size/2} r={r} fill="none" stroke="rgba(255,255,255,.07)" strokeWidth={stroke}/>
        <circle cx={size/2} cy={size/2} r={r} fill="none" stroke={color} strokeWidth={stroke}
          strokeDasharray={c} strokeDashoffset={c*(1-value/max)} strokeLinecap="round"
          style={{transition:"stroke-dashoffset 1.2s ease"}}/>
      </svg>
      <div style={{position:"absolute",inset:0,display:"flex",alignItems:"center",justifyContent:"center"}}>{children}</div>
    </div>
  );
}

function Bar({ label, value, max, color }) {
  return (
    <div style={{marginBottom:11}}>
      <div style={{display:"flex",justifyContent:"space-between",marginBottom:4}}>
        <span style={{fontSize:11,color:"rgba(255,255,255,.4)",textTransform:"uppercase",letterSpacing:".07em"}}>{label}</span>
        <span style={{fontSize:11,fontWeight:700,color}}>{value.toLocaleString()}</span>
      </div>
      <div style={{height:4,background:"rgba(255,255,255,.07)",borderRadius:4,overflow:"hidden"}}>
        <div style={{width:`${Math.round(value/max*100)}%`,height:"100%",background:color,borderRadius:4}}/>
      </div>
    </div>
  );
}

function HomeScreen({ profile }) {
  const xp = Math.round(profile.xp/profile.xpMax*100);
  return (
    <div style={{padding:"0 17px 24px"}}>
      <div style={{background:"#141418",border:"1px solid rgba(255,107,53,.22)",borderRadius:17,padding:19,marginBottom:12,position:"relative",overflow:"hidden"}}>
        <div style={{position:"absolute",top:-50,right:-50,width:160,height:160,borderRadius:"50%",background:"radial-gradient(circle,rgba(255,107,53,.1) 0%,transparent 70%)",pointerEvents:"none"}}/>
        <div style={{display:"flex",gap:16,alignItems:"center"}}>
          <Ring value={profile.xp} max={profile.xpMax}>
            <div style={{width:60,height:60,borderRadius:"50%",background:"rgba(255,107,53,.15)",border:"2px solid rgba(255,107,53,.5)",display:"flex",alignItems:"center",justifyContent:"center",fontFamily:"'Rajdhani',sans-serif",fontWeight:700,fontSize:14,color:"#FF6B35"}}>AX</div>
          </Ring>
          <div style={{flex:1}}>
            <div style={{display:"flex",alignItems:"baseline",gap:6}}>
              <span style={{fontFamily:"'Rajdhani',sans-serif",fontSize:22,fontWeight:700}}>{profile.name}</span>
              <span style={{fontSize:12,color:"rgba(255,255,255,.3)"}}>{profile.tag}</span>
            </div>
            <div style={{display:"flex",gap:6,marginTop:5}}>
              <span style={{background:"rgba(255,107,53,.18)",border:"1px solid rgba(255,107,53,.4)",borderRadius:6,padding:"2px 8px",fontSize:11,fontWeight:700,fontFamily:"'Rajdhani',sans-serif",color:"#FF6B35"}}>LV {profile.level}</span>
              <span style={{background:"rgba(79,195,247,.12)",border:"1px solid rgba(79,195,247,.3)",borderRadius:6,padding:"2px 8px",fontSize:11,fontWeight:700,fontFamily:"'Rajdhani',sans-serif",color:"#4FC3F7"}}>{profile.rank}</span>
            </div>
            <div style={{marginTop:10}}>
              <div style={{display:"flex",justifyContent:"space-between",marginBottom:4}}>
                <span style={{fontSize:10,color:"rgba(255,255,255,.3)"}}>XP PROGRESS</span>
                <span style={{fontSize:10,color:"rgba(255,255,255,.3)"}}>{profile.xp.toLocaleString()} / {profile.xpMax.toLocaleString()}</span>
              </div>
              <div style={{height:5,background:"rgba(255,255,255,.07)",borderRadius:5,overflow:"hidden"}}>
                <div style={{width:`${xp}%`,height:"100%",background:"linear-gradient(90deg,#FF6B35,#FF9A5C)",borderRadius:5}}/>
              </div>
            </div>
          </div>
        </div>
        <div style={{marginTop:16,display:"flex",justifyContent:"center",gap:8,alignItems:"center",background:"rgba(255,107,53,.08)",border:"1px solid rgba(255,107,53,.18)",borderRadius:10,padding:"9px 14px"}}>
          <span style={{fontSize:14}}>🔥</span>
          <span style={{fontWeight:700,fontSize:13,color:"#FF6B35"}}>{profile.streak} Day Streak</span>
          <span style={{fontSize:12,color:"rgba(255,255,255,.3)"}}>— Keep pushing!</span>
        </div>
      </div>
      <div style={{display:"grid",gridTemplateColumns:"1fr 1fr 1fr",gap:9,marginBottom:12}}>
        {[
          {label:"Credits",  val:profile.credits.toLocaleString(), color:"#FFD700",bg:"rgba(255,215,0,.08)",  bdr:"rgba(255,215,0,.2)"},
          {label:"Workouts", val:profile.totalWorkouts,             color:"#4FC3F7",bg:"rgba(79,195,247,.08)", bdr:"rgba(79,195,247,.2)"},
          {label:"Reps",     val:(profile.pushups+profile.squats+profile.crunches).toLocaleString(), color:"#FF6B35",bg:"rgba(255,107,53,.08)", bdr:"rgba(255,107,53,.2)"},
        ].map(s=>(
          <div key={s.label} style={{background:s.bg,border:`1px solid ${s.bdr}`,borderRadius:13,padding:"12px 8px",textAlign:"center"}}>
            <div style={{fontFamily:"'Rajdhani',sans-serif",fontSize:19,fontWeight:800,color:s.color}}>{s.val}</div>
            <div style={{fontSize:9,color:"rgba(255,255,255,.38)",textTransform:"uppercase",letterSpacing:".07em",marginTop:3}}>{s.label}</div>
          </div>
        ))}
      </div>
      <div style={{background:"#141418",border:"1px solid rgba(255,255,255,.07)",borderRadius:17,padding:"16px 14px",marginBottom:12}}>
        <div style={{fontSize:11,fontWeight:700,color:"rgba(255,255,255,.3)",textTransform:"uppercase",letterSpacing:".08em",marginBottom:12}}>Lifetime Reps</div>
        <Bar label="Push-Ups" value={profile.pushups}  max={15000} color="#FF6B35"/>
        <Bar label="Squats"   value={profile.squats}   max={15000} color="#4FC3F7"/>
        <Bar label="Crunches" value={profile.crunches} max={15000} color="#FFD700"/>
      </div>
      <div style={{fontSize:11,fontWeight:700,color:"rgba(255,255,255,.3)",textTransform:"uppercase",letterSpacing:".08em",marginBottom:9}}>Recent Activity</div>
      {RECENT.map((r,i)=>{
        const ex=EX.find(e=>e.id===r.type);
        return(
          <div key={i} style={{display:"flex",alignItems:"center",gap:10,padding:"10px 12px",borderRadius:11,marginBottom:7,background:"rgba(255,255,255,.026)",border:"1px solid rgba(255,255,255,.055)"}}>
            <div style={{width:32,height:32,borderRadius:9,background:"rgba(79,195,247,.1)",border:"1px solid rgba(79,195,247,.3)",display:"flex",alignItems:"center",justifyContent:"center",fontSize:13,flexShrink:0}}>{ex.icon}</div>
            <div style={{flex:1}}>
              <div style={{fontSize:13,fontWeight:600}}>{ex.label} <span style={{fontWeight:400,color:"rgba(255,255,255,.38)"}}>× {r.reps}</span></div>
              <div style={{fontSize:11,color:"rgba(255,255,255,.3)",marginTop:1}}>+{r.credits} credits</div>
            </div>
            <div style={{fontSize:11,color:"rgba(255,255,255,.22)"}}>{r.time}</div>
          </div>
        );
      })}
    </div>
  );
}

function WorkoutScreen({ profile, setProfile }) {
  const [active, setActive]     = useState(null);
  const [counts, setCounts]     = useState({pushups:0,squats:0,crunches:0});
  const [sessC, setSessC]       = useState(0);
  const [feedback, setFeedback] = useState("");
  // Change #2: front camera default
  const [cam, setCam]           = useState("front");
  const [wscreen, setWscreen]   = useState("main");
  const [playlist, setPlaylist] = useState([...DEFAULT_PLAYLIST]);
  const [curSong, setCurSong]   = useState(null);
  const [playing, setPlaying]   = useState(false);
  const [importing, setImporting]   = useState(null);
  const [importDone, setImportDone] = useState(null);
  const [importProg, setImportProg] = useState(0);
  const fbRef = useRef(null);

  const doRep = (id, credit) => {
    setCounts(c=>({...c,[id]:c[id]+1}));
    const nc = parseFloat((sessC+credit).toFixed(1));
    setSessC(nc);
    setProfile(p=>({...p,credits:parseFloat((p.credits+credit).toFixed(1))}));
    const msgs=["Keep going! 💪","Great form!","On fire! 🔥","Crush it!","Perfect rep!","Beast mode! ⚡"];
    setFeedback(msgs[Math.floor(Math.random()*msgs.length)]);
    clearTimeout(fbRef.current);
    fbRef.current=setTimeout(()=>setFeedback(""),1500);
  };

  const startImport = (pid) => {
    if(importing) return;
    setImporting(pid); setImportProg(0); setImportDone(null);
    let p=0;
    const iv=setInterval(()=>{
      p+=12; setImportProg(Math.min(p,100));
      if(p>=100){
        clearInterval(iv);
        const extras=[
          {title:"Till I Collapse",artist:"Eminem",    dur:"4:57"},
          {title:"Power",          artist:"Kanye West",dur:"4:52"},
        ];
        setPlaylist(pl=>{
          const t=new Set(pl.map(s=>s.title));
          return [...pl,...extras.filter(s=>!t.has(s.title))];
        });
        setImporting(null); setImportDone(pid);
      }
    },180);
  };

  const total = counts.pushups+counts.squats+counts.crunches;

  if(wscreen==="import") return (
    <div style={{padding:"0 17px 24px"}}>
      <button onClick={()=>setWscreen("main")} style={{display:"flex",alignItems:"center",gap:7,background:"none",border:"none",color:"rgba(255,255,255,.5)",cursor:"pointer",fontSize:13,marginBottom:14}}>← Back to Workout</button>
      <div style={{fontSize:11,fontWeight:700,color:"rgba(255,255,255,.3)",textTransform:"uppercase",letterSpacing:".08em",marginBottom:9}}>Your Playlist · {playlist.length} songs</div>
      <div style={{background:"#141418",border:"1px solid rgba(255,255,255,.07)",borderRadius:17,padding:0,overflow:"hidden",marginBottom:16}}>
        {playlist.length===0
          ? <div style={{padding:18,textAlign:"center",fontSize:13,color:"rgba(255,255,255,.3)"}}>No songs — import below</div>
          : playlist.map((s,i)=>(
            <div key={i} style={{display:"flex",alignItems:"center",gap:11,padding:"11px 14px",borderBottom:i<playlist.length-1?"1px solid rgba(255,255,255,.05)":"none"}}>
              <div style={{width:28,height:28,borderRadius:7,background:"rgba(29,185,84,.12)",border:"1px solid rgba(29,185,84,.25)",display:"flex",alignItems:"center",justifyContent:"center",fontSize:12,color:"#1DB954",fontWeight:700,flexShrink:0}}>{i+1}</div>
              <div style={{flex:1,minWidth:0}}>
                <div style={{fontSize:13,fontWeight:600,whiteSpace:"nowrap",overflow:"hidden",textOverflow:"ellipsis"}}>{s.title}</div>
                <div style={{fontSize:11,color:"rgba(255,255,255,.35)"}}>{s.artist}</div>
              </div>
              <div style={{fontSize:11,color:"rgba(255,255,255,.3)",flexShrink:0}}>{s.dur}</div>
              <button onClick={()=>{setCurSong(s);setPlaying(true);setWscreen("main");}} style={{background:"rgba(255,107,53,.15)",border:"1px solid rgba(255,107,53,.3)",color:"#FF6B35",borderRadius:7,padding:"4px 10px",fontSize:11,fontWeight:700,cursor:"pointer"}}>▶</button>
            </div>
          ))
        }
      </div>
      <div style={{fontSize:11,fontWeight:700,color:"rgba(255,255,255,.3)",textTransform:"uppercase",letterSpacing:".08em",marginBottom:9}}>Import From</div>
      {PLATFORMS.map(plat=>{
        const isImp=importing===plat.id, isDone=importDone===plat.id;
        return(
          <div key={plat.id} style={{background:plat.bg,border:`1px solid ${plat.bdr}`,borderRadius:13,padding:"14px 16px",display:"flex",alignItems:"center",justifyContent:"space-between",marginBottom:10}}>
            <div style={{display:"flex",alignItems:"center",gap:11}}>
              <div style={{width:40,height:40,borderRadius:10,background:`${plat.color}22`,border:`1px solid ${plat.color}44`,display:"flex",alignItems:"center",justifyContent:"center",fontSize:20,color:plat.color,flexShrink:0}}>{plat.ico}</div>
              <div>
                <div style={{fontFamily:"'Rajdhani',sans-serif",fontSize:14,fontWeight:700}}>{plat.name}</div>
                <div style={{fontSize:11,color:"rgba(255,255,255,.4)",marginTop:2}}>{isDone?"Sync complete — duplicates skipped":isImp?"Scanning your library...":"Tap to sync your library"}</div>
                {isImp&&<div style={{height:3,borderRadius:3,background:"rgba(255,255,255,.07)",overflow:"hidden",marginTop:7,width:140}}><div style={{height:"100%",borderRadius:3,background:plat.color,width:`${importProg}%`,transition:"width .3s ease"}}/></div>}
              </div>
            </div>
            <button disabled={!!isImp} onClick={()=>startImport(plat.id)} style={{background:plat.color,color:"#0A0A0F",borderRadius:8,padding:"7px 16px",fontSize:12,fontWeight:700,cursor:"pointer",border:"none",fontFamily:"'Rajdhani',sans-serif",opacity:isImp?0.4:1}}>
              {isDone?"✓ DONE":isImp?"...":"IMPORT"}
            </button>
          </div>
        );
      })}
    </div>
  );

  return (
    <div style={{padding:"0 17px 24px"}}>
      <div style={{background:"#141418",border:"1px solid rgba(255,107,53,.22)",borderRadius:17,padding:19,marginBottom:12}}>
        <div style={{display:"flex",justifyContent:"space-between",alignItems:"center",marginBottom:13}}>
          <span style={{fontFamily:"'Rajdhani',sans-serif",fontSize:12,fontWeight:700,color:"rgba(255,255,255,.35)",textTransform:"uppercase",letterSpacing:".1em"}}>Live Session</span>
          <div style={{display:"flex",alignItems:"center",gap:5}}>
            <div style={{width:7,height:7,borderRadius:"50%",background:"#FF6B35",boxShadow:"0 0 6px #FF6B35"}}/>
            <span style={{fontSize:12,color:"rgba(255,255,255,.4)"}}>Active</span>
          </div>
        </div>
        <div style={{display:"grid",gridTemplateColumns:"1fr 1fr",gap:10}}>
          <div style={{background:"rgba(255,215,0,.07)",border:"1px solid rgba(255,215,0,.2)",borderRadius:12,padding:"11px 13px",textAlign:"center"}}>
            <div style={{fontFamily:"'Rajdhani',sans-serif",fontSize:27,fontWeight:800,color:"#FFD700"}}>{sessC.toLocaleString()}</div>
            <div style={{fontSize:10,color:"rgba(255,255,255,.35)",textTransform:"uppercase",letterSpacing:".07em"}}>Credits</div>
          </div>
          <div style={{background:"rgba(79,195,247,.07)",border:"1px solid rgba(79,195,247,.2)",borderRadius:12,padding:"11px 13px",textAlign:"center"}}>
            <div style={{fontFamily:"'Rajdhani',sans-serif",fontSize:27,fontWeight:800,color:"#4FC3F7"}}>{total}</div>
            <div style={{fontSize:10,color:"rgba(255,255,255,.35)",textTransform:"uppercase",letterSpacing:".07em"}}>Reps</div>
          </div>
        </div>
        {feedback&&<div style={{marginTop:11,textAlign:"center",padding:"7px 16px",background:"rgba(255,255,255,.05)",borderRadius:30,border:"1px solid rgba(255,255,255,.1)",fontSize:13,fontWeight:600,color:"#FF9A5C"}}>{feedback}</div>}
      </div>

      {EX.map(ex=>{
        const isAct=active===ex.id;
        const rgb=ex.id==="pushups"?"255,107,53":ex.id==="squats"?"79,195,247":"255,215,0";
        return(
          <div key={ex.id} style={{background:isAct?`rgba(${rgb},.08)`:"#141418",border:`1px solid ${isAct?ex.color+"44":"rgba(255,255,255,.07)"}`,borderRadius:15,padding:"14px 16px",marginBottom:10,transition:"all .22s"}}>
            <div style={{display:"flex",alignItems:"center",justifyContent:"space-between"}}>
              <div style={{display:"flex",alignItems:"center",gap:10}}>
                <div style={{width:43,height:43,borderRadius:11,background:`${ex.color}18`,border:`1px solid ${ex.color}44`,display:"flex",alignItems:"center",justifyContent:"center",fontSize:20}}>{ex.icon}</div>
                <div>
                  <div style={{fontFamily:"'Rajdhani',sans-serif",fontSize:16,fontWeight:700}}>{ex.label}</div>
                  <div style={{fontSize:11,color:"rgba(255,255,255,.35)"}}>{ex.credit} cr/rep</div>
                </div>
              </div>
              <div style={{display:"flex",alignItems:"center",gap:9}}>
                <div style={{fontFamily:"'Rajdhani',sans-serif",fontSize:25,fontWeight:800,color:ex.color,minWidth:32,textAlign:"right"}}>{counts[ex.id]}</div>
                {isAct
                  ?<button onClick={()=>setActive(null)} style={{background:"rgba(255,107,107,.15)",border:"1px solid rgba(255,107,107,.3)",color:"#FF6B6B",borderRadius:8,fontSize:11,fontWeight:700,padding:"6px 12px",cursor:"pointer",fontFamily:"'Rajdhani',sans-serif"}}>STOP</button>
                  :<button onClick={()=>setActive(ex.id)} style={{background:`${ex.color}22`,border:`1px solid ${ex.color}55`,color:ex.color,borderRadius:8,fontSize:11,fontWeight:700,padding:"6px 12px",cursor:"pointer",fontFamily:"'Rajdhani',sans-serif"}}>START</button>
                }
              </div>
            </div>
            {isAct&&(
              <>
                <div style={{borderRadius:13,overflow:"hidden",position:"relative",background:"#0d0d18",border:`1px solid ${ex.color}33`,marginTop:11,height:120,display:"flex",alignItems:"center",justifyContent:"center",flexDirection:"column",gap:6}}>
                  <div style={{position:"absolute",top:8,left:8,background:"rgba(0,0,0,.55)",border:"1px solid rgba(255,255,255,.15)",borderRadius:6,padding:"3px 8px",fontSize:10,color:"rgba(255,255,255,.7)",fontWeight:600}}>
                    {cam==="front"?"📷 Front Camera":"📷 Back Camera"}
                  </div>
                  <button onClick={()=>setCam(c=>c==="front"?"back":"front")} style={{position:"absolute",top:8,right:8,background:"rgba(0,0,0,.55)",border:"1px solid rgba(255,255,255,.18)",borderRadius:8,width:34,height:34,display:"flex",alignItems:"center",justifyContent:"center",cursor:"pointer"}}>
                    <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                      <path d="M3 8C3 5.24 5.24 3 8 3c1.38 0 2.63.56 3.54 1.46L10 6h5V1l-1.59 1.59A6.97 6.97 0 008 1C4.13 1 1 4.13 1 8h2zm10 0c0 2.76-2.24 5-5 5a4.995 4.995 0 01-3.54-1.46L6 10H1v5l1.59-1.59A6.97 6.97 0 008 15c3.87 0 7-3.13 7-7h-2z" fill="rgba(255,255,255,.75)"/>
                    </svg>
                  </button>
                  <svg width="34" height="34" viewBox="0 0 36 36" fill="none" style={{opacity:.18}}>
                    <circle cx="18" cy="14" r="7" stroke="rgba(255,255,255,.6)" strokeWidth="1.5"/>
                    <path d="M6 32c0-6.627 5.373-12 12-12s12 5.373 12 12" stroke="rgba(255,255,255,.6)" strokeWidth="1.5"/>
                  </svg>
                  <div style={{fontSize:11,color:"rgba(255,255,255,.28)",marginTop:2}}>{cam==="front"?"Front-facing camera active":"Back camera active"}</div>
                </div>
                <button onClick={()=>doRep(ex.id,ex.credit)} style={{width:"100%",padding:13,borderRadius:11,border:"none",color:"#fff",fontFamily:"'Rajdhani',sans-serif",fontSize:15,fontWeight:800,letterSpacing:".12em",cursor:"pointer",textTransform:"uppercase",marginTop:11,background:`linear-gradient(135deg,${ex.gr})`}}>
                  + 1 REP · +{ex.credit} CR
                </button>
              </>
            )}
          </div>
        );
      })}

      <div style={{fontSize:11,fontWeight:700,color:"rgba(255,255,255,.3)",textTransform:"uppercase",letterSpacing:".08em",marginBottom:9,marginTop:4}}>Music</div>
      {curSong?(
        <div style={{background:"#141418",border:"1px solid rgba(29,185,84,.2)",borderRadius:14,padding:"13px 15px",display:"flex",alignItems:"center",gap:12,cursor:"pointer"}} onClick={()=>setPlaying(p=>!p)}>
          <div style={{width:40,height:40,borderRadius:9,background:"rgba(29,185,84,.15)",border:"1px solid rgba(29,185,84,.3)",display:"flex",alignItems:"center",justifyContent:"center",fontSize:18,flexShrink:0}}>♪</div>
          <div style={{flex:1,minWidth:0}}>
            <div style={{fontSize:13,fontWeight:600,whiteSpace:"nowrap",overflow:"hidden",textOverflow:"ellipsis"}}>{curSong.title}</div>
            <div style={{fontSize:11,color:"rgba(255,255,255,.4)",marginTop:1}}>{curSong.artist}</div>
            <div style={{height:3,background:"rgba(255,255,255,.1)",borderRadius:3,marginTop:8,overflow:"hidden"}}>
              <div style={{height:"100%",width:playing?"38%":"0%",background:"#1DB954",borderRadius:3,transition:"width .5s"}}/>
            </div>
          </div>
          <div style={{width:36,height:36,borderRadius:"50%",background:playing?"rgba(29,185,84,.2)":"rgba(255,255,255,.07)",border:`1px solid ${playing?"rgba(29,185,84,.4)":"rgba(255,255,255,.15)"}`,display:"flex",alignItems:"center",justifyContent:"center",fontSize:14,flexShrink:0}}>
            {playing?"⏸":"▶"}
          </div>
        </div>
      ):(
        <div style={{background:"#141418",border:"1px solid rgba(29,185,84,.15)",borderRadius:14,padding:14,display:"flex",flexDirection:"column",alignItems:"center",gap:9}}>
          <div style={{display:"flex",alignItems:"center",gap:8}}>
            <svg width="18" height="18" viewBox="0 0 18 18" fill="none"><circle cx="9" cy="9" r="8" stroke="rgba(29,185,84,.5)" strokeWidth="1.2"/><path d="M7 5.5v7l6-3.5-6-3.5z" fill="rgba(29,185,84,.7)"/></svg>
            <span style={{fontSize:13,color:"rgba(255,255,255,.45)"}}>No music playing</span>
          </div>
          <button onClick={()=>setWscreen("import")} style={{background:"rgba(29,185,84,.15)",border:"1px solid rgba(29,185,84,.35)",color:"#1DB954",borderRadius:8,padding:"7px 20px",fontSize:12,fontWeight:700,cursor:"pointer",fontFamily:"'Rajdhani',sans-serif",letterSpacing:".05em"}}>IMPORT MUSIC</button>
        </div>
      )}

      <button onClick={()=>{setCounts({pushups:0,squats:0,crunches:0});setSessC(0);setActive(null);}} style={{width:"100%",padding:11,borderRadius:12,background:"transparent",border:"1px solid rgba(255,255,255,.09)",color:"rgba(255,255,255,.38)",fontSize:13,cursor:"pointer",marginTop:10}}>Reset Session</button>
    </div>
  );
}

function LeaderboardScreen() {
  const medals=["🥇","🥈","🥉"];
  const pod=[LB[1],LB[0],LB[2]];
  const ht=[100,120,88],sz=[48,56,44];
  return (
    <div style={{padding:"0 17px 24px"}}>
      <div style={{display:"flex",justifyContent:"center",alignItems:"flex-end",gap:10,marginBottom:20,padding:"0 6px"}}>
        {pod.map((p,vi)=>(
          <div key={p.rank} style={{flex:1,display:"flex",flexDirection:"column",alignItems:"center",gap:6}}>
            <div style={{fontSize:vi===1?"21px":"15px"}}>{medals[vi===1?0:vi===0?1:2]}</div>
            <div style={{width:sz[vi],height:sz[vi],borderRadius:"50%",background:`${p.color}22`,border:`2px solid ${p.color}`,display:"flex",alignItems:"center",justifyContent:"center",fontFamily:"'Rajdhani',sans-serif",fontWeight:700,fontSize:vi===1?15:12,color:p.color,...(p.isMe&&{boxShadow:`0 0 13px ${p.color}44`})}}>{p.avatar}</div>
            <div style={{fontSize:vi===1?13:11,fontWeight:700,color:p.isMe?"#FF6B35":"#fff",fontFamily:"'Rajdhani',sans-serif"}}>{p.name}</div>
            <div style={{width:"100%",background:vi===1?"rgba(255,215,0,.1)":"rgba(255,255,255,.04)",border:`1px solid ${p.color}44`,borderRadius:"10px 10px 0 0",height:ht[vi],display:"flex",alignItems:"flex-end",justifyContent:"center",paddingBottom:10}}>
              <div style={{textAlign:"center"}}>
                <div style={{fontFamily:"'Rajdhani',sans-serif",fontSize:vi===1?17:13,fontWeight:800,color:p.color}}>{p.credits.toLocaleString()}</div>
                <div style={{fontSize:9,color:"rgba(255,255,255,.3)",textTransform:"uppercase"}}>creds</div>
              </div>
            </div>
          </div>
        ))}
      </div>
      <div style={{background:"#141418",border:"1px solid rgba(255,255,255,.07)",borderRadius:17,padding:0,overflow:"hidden"}}>
        {LB.map((p,i)=>(
          <div key={p.rank} style={{display:"flex",alignItems:"center",gap:10,padding:"12px 14px",borderBottom:i<LB.length-1?"1px solid rgba(255,255,255,.05)":"none",background:p.isMe?"rgba(255,107,53,.06)":"transparent"}}>
            <div style={{width:22,textAlign:"center",fontSize:13,fontWeight:700,color:p.rank<=3?["#FFD700","#C0C0C0","#CD7F32"][p.rank-1]:"rgba(255,255,255,.25)",fontFamily:"'Rajdhani',sans-serif"}}>#{p.rank}</div>
            <div style={{width:35,height:35,borderRadius:"50%",background:`${p.color}18`,border:`1.5px solid ${p.color}55`,display:"flex",alignItems:"center",justifyContent:"center",fontFamily:"'Rajdhani',sans-serif",fontWeight:700,fontSize:11,color:p.color,flexShrink:0}}>{p.avatar}</div>
            <div style={{flex:1}}>
              <div style={{fontFamily:"'Rajdhani',sans-serif",fontSize:14,fontWeight:600,color:p.isMe?"#FF6B35":"#fff"}}>
                {p.name}{p.isMe&&<span style={{fontSize:9,background:"rgba(255,107,53,.2)",color:"#FF6B35",borderRadius:4,padding:"1px 5px",marginLeft:5}}>YOU</span>}
              </div>
              <div style={{fontSize:11,color:"rgba(255,255,255,.3)"}}>Lv.{p.level}</div>
            </div>
            <div style={{textAlign:"right"}}>
              <div style={{fontFamily:"'Rajdhani',sans-serif",fontSize:15,fontWeight:800,color:p.rank<=3?["#FFD700","#C0C0C0","#CD7F32"][p.rank-1]:"#fff"}}>{p.credits.toLocaleString()}</div>
              <div style={{fontSize:10,color:"rgba(255,255,255,.28)"}}>credits</div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

export default function App() {
  const [tab, setTab]         = useState("home");
  const [profile, setProfile] = useState({...PROFILE});
  const tabs = [
    {id:"home",label:"Home",icon:"⊞"},
    {id:"workout",label:"Workout",icon:"◈"},
    {id:"leaderboard",label:"Ranks",icon:"◆"},
  ];
  return (
    <div style={{fontFamily:"'Inter',sans-serif",background:"#0A0A0F",minHeight:"100vh",color:"#fff",maxWidth:430,margin:"0 auto",position:"relative"}}>
      <style>{`
        @import url('https://fonts.googleapis.com/css2?family=Rajdhani:wght@500;700&family=Inter:wght@300;400;600&display=swap');
        *{box-sizing:border-box;margin:0;padding:0}
        ::-webkit-scrollbar{width:3px}::-webkit-scrollbar-thumb{background:rgba(255,255,255,.1);border-radius:4px}
      `}</style>
      <div style={{position:"fixed",inset:0,backgroundImage:"linear-gradient(rgba(255,107,53,.028) 1px,transparent 1px),linear-gradient(90deg,rgba(255,107,53,.028) 1px,transparent 1px)",backgroundSize:"44px 44px",zIndex:0,pointerEvents:"none"}}/>
      <div style={{position:"sticky",top:0,zIndex:20,background:"rgba(10,10,15,.95)",backdropFilter:"blur(14px)",borderBottom:"1px solid rgba(255,255,255,.07)",padding:"13px 17px",display:"flex",alignItems:"center",justifyContent:"space-between"}}>
        <div style={{display:"flex",alignItems:"center",gap:9}}>
          <Logo/>
          <span style={{fontFamily:"'Rajdhani',sans-serif",fontSize:20,fontWeight:700,letterSpacing:".14em"}}>PUSH<span style={{color:"#FF6B35"}}>GRAM</span></span>
        </div>
        <div style={{display:"flex",alignItems:"center",gap:9}}>
          <div style={{background:"rgba(255,215,0,.1)",border:"1px solid rgba(255,215,0,.25)",borderRadius:20,padding:"4px 11px",fontFamily:"'Rajdhani',sans-serif",fontSize:12,fontWeight:700,color:"#FFD700"}}>🎬 {profile.credits.toLocaleString()}</div>
          <div style={{width:31,height:31,borderRadius:"50%",background:"rgba(255,107,53,.15)",border:"1.5px solid rgba(255,107,53,.4)",display:"flex",alignItems:"center",justifyContent:"center",fontFamily:"'Rajdhani',sans-serif",fontWeight:700,fontSize:11,color:"#FF6B35"}}>AX</div>
        </div>
      </div>
      <div style={{position:"relative",zIndex:1,paddingBottom:80}}>
        {tab==="home"        && <HomeScreen profile={profile}/>}
        {tab==="workout"     && <WorkoutScreen profile={profile} setProfile={setProfile}/>}
        {tab==="leaderboard" && <LeaderboardScreen/>}
      </div>
      <div style={{position:"fixed",bottom:0,left:"50%",transform:"translateX(-50%)",width:"100%",maxWidth:430,background:"rgba(10,10,15,.97)",backdropFilter:"blur(16px)",borderTop:"1px solid rgba(255,255,255,.07)",padding:"8px 0 14px",zIndex:30,display:"flex"}}>
        {tabs.map(t=>(
          <button key={t.id} onClick={()=>setTab(t.id)} style={{flex:1,display:"flex",flexDirection:"column",alignItems:"center",gap:3,background:"none",border:"none",cursor:"pointer",padding:"4px 0"}}>
            <div style={{width:37,height:37,borderRadius:11,display:"flex",alignItems:"center",justifyContent:"center",fontSize:15,background:tab===t.id?"rgba(255,107,53,.18)":"transparent",border:tab===t.id?"1px solid rgba(255,107,53,.4)":"1px solid transparent",color:tab===t.id?"#FF6B35":"rgba(255,255,255,.3)",transition:"all .2s"}}>{t.icon}</div>
            <span style={{fontSize:9,fontWeight:600,letterSpacing:".06em",textTransform:"uppercase",color:tab===t.id?"#FF6B35":"rgba(255,255,255,.28)"}}>{t.label}</span>
          </button>
        ))}
      </div>
    </div>
  );
}
