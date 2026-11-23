import numpy as np, math
from PIL import Image, ImageDraw, ImageFilter, ImageFont
W,H=960,220; SS=3
def noise(w,h,scale,seed):
    rng=np.random.RandomState(seed); gw,gh=w//scale+3,h//scale+3
    g=(rng.rand(gh,gw)*255).astype(np.uint8)
    return np.asarray(Image.fromarray(g).resize((w,h),Image.BICUBIC)).astype(np.float32)/255
def fbm(w,h,seed,scales=(80,40,20,10,5)):
    o=np.zeros((h,w),np.float32);a=1;t=0
    for i,s in enumerate(scales): o+=a*noise(w,h,s,seed+i);t+=a;a*=.6
    return o/t
def M(fn,w=W,h=H):
    im=Image.new('L',(w*SS,h*SS),0); d=ImageDraw.Draw(im); fn(d,SS); return np.asarray(im.resize((w,h),Image.LANCZOS)).astype(np.float32)/255
def rr(box,r): return lambda d,s: d.rounded_rectangle([box[0]*s,box[1]*s,box[2]*s-1,box[3]*s-1],r*s,fill=255)
def col(c): return np.array(c,np.float32)
def grad(c1,c2,w=W,h=H):
    t=np.linspace(0,1,h)[:,None,None]; return col(c1)*(1-t)+col(c2)*t+np.zeros((h,w,3),np.float32)
def over(base,color,mask,a=1.0):
    m=(mask*a)[...,None]; return base*(1-m)+color*m
def light(mask,r=5,lx=-.7,ly=-.7):
    h=np.asarray(Image.fromarray((mask*255).astype(np.uint8)).filter(ImageFilter.GaussianBlur(r))).astype(np.float32)/255
    gy,gx=np.gradient(h); return np.clip(-(gx*lx+gy*ly)*r*1.6,-1,1)
def blur(a,r): return np.asarray(Image.fromarray((np.clip(a,0,1)*255).astype(np.uint8)).filter(ImageFilter.GaussianBlur(r))).astype(np.float32)/255
def finish(rgb,alpha): 
    out=np.dstack([np.clip(rgb,0,255),alpha*255]).astype(np.uint8); return Image.fromarray(out,'RGBA')
def rivets(img,pts,r=7):
    for (x,y) in pts:
        m=M(lambda d,s:d.ellipse([(x-r)*s,(y-r)*s,(x+r)*s,(y+r)*s],fill=255))
        L=light(m,3)
        img=over(img,col((60,44,32)),M(lambda d,s:d.ellipse([(x-r-2)*s,(y-r-2)*s,(x+r+2)*s,(y+r+2)*s],fill=255)),.9)
        base=col((196,190,176))+L[...,None]*55
        img=over(img,base,m)
    return img
def frame_card(frame_c1,frame_c2,inner_c1,inner_c2,seed,fnoise=18,rivet=True):
    outer=M(rr((0,0,W,H),28)); fr=M(rr((5,5,W-5,H-5),24)); inn_o=M(rr((5+22,5+22,W-27,H-27),14)); inn=M(rr((5+25,5+25,W-30,H-30),12))
    n=fbm(W,H,seed)
    img=np.zeros((H,W,3),np.float32)+col((59,36,14))
    fc=grad(frame_c1,frame_c2)+((n-.5)*fnoise*2)[...,None]
    fc=fc+light(fr,4)[...,None]*40
    img=over(img,fc,fr)
    img=over(img,col((45,28,12)),inn_o)
    ic=grad(inner_c1,inner_c2)+((fbm(W,H,seed+9)-.5)*14)[...,None]
    ic=ic-light(inn,3)[...,None]*18
    img=over(img,ic,inn)
    return img,outer,inn
def honey_drips(img,outer,seed=3):
    rng=np.random.RandomState(seed); xs=[];x=40
    while x<W-30: xs.append(x); x+=rng.randint(70,130)
    drips=[(x+rng.randint(-10,10),rng.randint(12,16),rng.randint(28,78)) for x in xs]
    def fn(d,s):
        d.rectangle([0,0,W*s,15*s],fill=255)
        for (x,w,L) in drips:
            d.rounded_rectangle([(x-w/2)*s,0,(x+w/2)*s,L*s],w/2*s,fill=255)
            d.ellipse([(x-w*.68)*s,(L-w*.5)*s,(x+w*.68)*s,(L+w*.9)*s],fill=255)
    m=M(fn); mb=blur(m,6); m=np.clip((mb-.42)*7,0,1)*outer
    sh=blur(np.roll(np.roll(m,5,0),3,1),5)*.35*(1-m)
    img=img*(1-sh[...,None]*outer[...,None])
    L=light(m,6)
    base=grad((250,190,50),(226,140,18))
    base=base+L[...,None]*70
    spec=np.clip(L-.55,0,1)*2.2
    base=base+spec[...,None]*np.array([90,80,40])
    edge=np.clip(blur(m,2)-m,0,1)
    img=over(img,col((150,88,10)),np.clip(blur(m,1.6)-m*.0,0,1)*(1-m)*.0)
    img=over(img,base,m)
    img=over(img,col((140,82,10)),np.clip(m-blur(m,2.2),0,1),.7)
    return img
def card_medium():
    img,outer,inn=frame_card((246,196,84),(196,132,20),(252,232,168),(246,208,120),11)
    # hex honeycomb
    def hexes(d,s):
        r=26; 
        for row in range(-1,10):
            for c in range(-1,22):
                cx=c*r*1.5+30; cy=row*r*1.732+(r*.866 if c%2 else 0)+10
                pts=[(cx+r*math.cos(math.radians(60*k)),cy+r*math.sin(math.radians(60*k))) for k in range(6)]
                d.line([(x*s,y*s) for x,y in pts+[pts[0]]],fill=255,width=2*s)
    hm=M(hexes); img=over(img,col((222,160,50)),hm*inn,.22)
    img=rivets(img,[(18,18),(W-18,18),(18,H-18),(W-18,H-18)],6)
    img=honey_drips(img,outer)
    return finish(img,outer)
def leaf(d,s,x,y,ang,l,w,fill):
    ca,sa=math.cos(ang),math.sin(ang); pts=[]
    for t in np.linspace(0,1,14):
        off=math.sin(math.pi*t)*w/2; pts.append((x+ca*l*t-sa*off,y+sa*l*t+ca*off))
    for t in np.linspace(1,0,14):
        off=-math.sin(math.pi*t)*w/2; pts.append((x+ca*l*t-sa*off,y+sa*l*t+ca*off))
    d.polygon([(a*s,b*s) for a,b in pts],fill=fill)
def card_easy():
    img,outer,inn=frame_card((74,122,52),(44,78,32),(240,233,196),(228,222,180),21,22)
    rng=np.random.RandomState(5)
    leaves=[]
    for x in range(10,W,34): leaves.append((x,8+rng.randint(-2,4),rng.uniform(-.5,.5)+math.pi/2*0+0.0,rng.uniform(28,42)))
    greens=[(92,148,62),(120,176,80),(70,118,48),(146,196,96)]
    for pass_,(ymin,flip) in enumerate([(3,1),(H-3,-1)]):
        for x in range(8,W,30):
            for k in range(2):
                a=rng.uniform(.2,1.0)*flip*(1 if k==0 else -1)+ (math.pi/2 if flip==1 else -math.pi/2)
                ll=rng.uniform(26,44); ww=rng.uniform(12,18)
                m=M(lambda d,s:leaf(d,s,x+rng.randint(-6,6),ymin,a,ll,ww,255))
                c=greens[rng.randint(0,4)]; Ls=light(m,3)
                img=over(img,col((36,66,28)),blur(m,1.2)*0+m,.0)
                img=over(img,col(c)+Ls[...,None]*45,m*outer)
    for (x0,y0,sx,sy) in [(0,0,1,1),(W,0,-1,1),(0,H,1,-1),(W,H,-1,-1)]:
        for k in range(7):
            a=math.atan2(sy*rng.uniform(.3,1.2),sx*rng.uniform(.3,1.2))
            m=M(lambda d,s:leaf(d,s,x0+sx*rng.uniform(0,34),y0+sy*rng.uniform(0,30),a,rng.uniform(34,54),rng.uniform(16,22),255))
            img=over(img,col(greens[rng.randint(0,4)])+light(m,3)[...,None]*45,m*outer)
    img=rivets(img,[(16,16),(W-16,16),(16,H-16),(W-16,H-16)],5) if False else img
    return finish(img,outer)
def card_hard():
    img,outer,inn=frame_card((76,56,50),(44,32,30),(222,136,84),(206,112,62),31,26)
    # lava plates + cracks in interior
    rng=np.random.RandomState(7); pts=np.stack([rng.rand(16)*W,rng.rand(16)*H],1)
    yy,xx=np.mgrid[0:H,0:W].astype(np.float32)
    d=np.sqrt((xx[...,None]-pts[:,0])**2+(yy[...,None]-pts[:,1])**2); d.sort(axis=2)
    gap=d[...,1]-d[...,0]
    plate=fbm(W,H,41)[...,None]
    crack=np.clip(1-gap/5.0,0,1)
    base=img.copy()
    lava=base+((plate-.5)*36)
    lava=lava*(1-crack[...,None]*.38)+crack[...,None]*np.array([20,-30,-30])
    glow=np.clip(1-gap/2.0,0,1)**2*(noise(W,H,18,3)>.62)
    lava=lava+glow[...,None]*np.array([60,60,10])
    img=over(img,lava,inn)
    # frame cracks glow
    fcrack=np.clip(1-np.abs(fbm(W,H,51,(40,20,10))-.5)/.012,0,1)*(outer-inn)
    img=over(img,col((255,150,50)),np.clip(fcrack,0,1),.9)
    img=rivets(img,[(18,18),(W-18,18),(18,H-18),(W-18,H-18)],6)
    return finish(img,outer)
def panel():
    outer=M(rr((0,0,W,H),30))
    n=fbm(W,H,61,(120,60,30,12,6))
    streak=noise(W,H,3,70)
    wood=grad((140,94,52),(106,70,38))+((n-.5)*34+(streak-.5)*18)[...,None]
    fr=M(rr((4,4,W-4,H-4),27)); img=np.zeros((H,W,3),np.float32)+col((59,36,14))
    img=over(img,wood+light(fr,4)[...,None]*38,fr)
    # parchment interior with torn edge
    rng=np.random.RandomState(8); pts=[]
    def edge(d,s):
        m=28; P=[]
        for x in np.linspace(m,W-m,90): P.append((x,m+rng.uniform(-2.5,2.5)))
        for y in np.linspace(m,H-m,26): P.append((W-m+rng.uniform(-2.5,2.5),y))
        for x in np.linspace(W-m,m,90): P.append((x,H-m+rng.uniform(-2.5,2.5)))
        for y in np.linspace(H-m,m,26): P.append((m+rng.uniform(-2.5,2.5),y))
        d.polygon([(a*s,b*s) for a,b in P],fill=255)
    inn=M(edge); par=grad((246,232,198),(232,212,168))+((fbm(W,H,71)-.5)*26)[...,None]
    img=over(img,col((70,46,22)),blur(np.roll(np.roll(inn,4,0),3,1),3),.5)
    img=over(img,par-light(inn,3)[...,None]*14,inn)
    img=rivets(img,[(16,16),(W-16,16),(16,H-16),(W-16,H-16)],6)
    return finish(img,outer)
if __name__=='__main__':
    names={'card_medium':card_medium,'card_easy':card_easy,'card_hard':card_hard,'panel_parchment':panel}
    ims={k:f() for k,f in names.items()}
    for k,v in ims.items(): v.save(f'/tmp/art/{k}.png')
    bg=Image.new('RGBA',(1000,1000),(86,146,58,255)); bgn=(fbm(1000,1000,99,(60,30,12,6))-.5)*30
    arr=np.asarray(bg).astype(np.float32); arr[...,:3]+=bgn[...,None]; bg=Image.fromarray(np.clip(arr,0,255).astype(np.uint8),'RGBA')
    f=ImageFont.truetype('/tmp/fonts/Jersey15-Regular.ttf',44); f2=ImageFont.truetype('/tmp/fonts/Jersey15-Regular.ttf',34)
    y=20
    for k,label in (('panel_parchment','Good afternoon  Thu, Nov 19'),('card_easy','Easy quest   1'),('card_medium','Medium quest   5'),('card_hard','Hard quest   10')):
        im=ims[k].resize((960,220)); bg.alpha_composite(im,(20,y))
        ImageDraw.Draw(bg).text((90,y+62),label,font=f,fill=(44,30,17,255)); y+=240
    bg.convert('RGB').save('/tmp/art/preview.png'); print('done')
