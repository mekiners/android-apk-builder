import random, math
from PIL import Image

def C(h): h=h.lstrip('#'); return tuple(int(h[i:i+2],16) for i in (0,2,4))+(255,)
def clamp(v): return max(0,min(255,int(v)))
def shade(c,d): return (clamp(c[0]+d),clamp(c[1]+d),clamp(c[2]+d),c[3] if len(c)>3 else 255)
def mix(a,b,t): return tuple(clamp(a[i]*(1-t)+b[i]*t) for i in range(3))+(255,)

class T:
    def __init__(s,w,h,base=None):
        s.w=w;s.h=h
        s.im=Image.new('RGBA',(w,h),base if base else (0,0,0,0)); s.px=s.im.load()
    def p(s,x,y,c): s.px[x%s.w,y%s.h]=c           # wraps (tileable)
    def q(s,x,y,c):                                # no wrap
        if 0<=x<s.w and 0<=y<s.h: s.px[x,y]=c
    def g(s,x,y): return s.px[x%s.w,y%s.h]
    def rect(s,x,y,w,h,c):
        for j in range(h):
            for i in range(w): s.p(x+i,y+j,c)
    def save(s,name): s.im.save(f'/home/claude/gen/tex/{name}.png')

def blotch(t,rng,cell,amp):
    gw=t.w//cell; gh=t.h//cell
    vals=[[rng.uniform(-amp,amp) for _ in range(gw)] for _ in range(gh)]
    for y in range(t.h):
        for x in range(t.w):
            c=t.px[x,y]
            if c[3]: t.px[x,y]=shade(c,vals[y//cell][x//cell])

def streaks(t,rng,n,lmin,lmax,fn):
    for _ in range(n):
        x=rng.randrange(t.w); y=rng.randrange(t.h); l=rng.randint(lmin,lmax); c=fn()
        for i in range(l): t.p(x+i,y,c)

def specks(t,rng,n,fn):
    for _ in range(n): t.p(rng.randrange(t.w),rng.randrange(t.h),fn())

def parchment(name,base,seed):
    rng=random.Random(seed); t=T(32,32,base)
    blotch(t,rng,8,4)
    streaks(t,rng,16,3,6,lambda: shade(base,-11))
    specks(t,rng,14,lambda: shade(base,-22))
    specks(t,rng,10,lambda: shade(base,9))
    t.save(name)

parchment('tex_parchment',C('F3E2BC'),1)
parchment('tex_parchment_dark',C('E1CBA0'),2)

# beige (easy quest): paler with faint green fibres
rng=random.Random(3); t=T(32,32,C('EDE5BF')); blotch(t,rng,8,4)
streaks(t,rng,10,3,6,lambda: shade(C('EDE5BF'),-10))
specks(t,rng,12,lambda: (176,196,128,255)); specks(t,rng,8,lambda: shade(C('EDE5BF'),-20)); t.save('tex_beige')

def wood(name,k,seed):
    rng=random.Random(seed); t=T(32,32)
    for plank in range(2):
        y0=plank*16
        base=[C('80522C'),C('76492A')][plank]; base=tuple(clamp(v*k) for v in base[:3])+(255,)
        for y in range(16):
            for x in range(32): t.p(x,y0+y,base)
        for y in range(16):
            if y==0:
                for x in range(32): t.p(x,y0,tuple(clamp(v*0.5) for v in base[:3])+(255,))
            elif y==1:
                for x in range(32): t.p(x,y0+1,shade(base,22))
            elif y==15:
                for x in range(32): t.p(x,y0+15,shade(base,-22))
        for _ in range(7):
            x=rng.randrange(32); y=y0+rng.randint(3,13); l=rng.randint(6,15)
            c=shade(base,-16) if rng.random()<.7 else shade(base,12)
            for i in range(l): t.p(x+i,y,c)
        kx=rng.randrange(6,26); ky=y0+rng.randint(6,10)
        t.rect(kx,ky,3,2,shade(base,-26)); t.p(kx+1,ky,shade(base,-40))
        for nx in (4,27):
            t.rect(nx,y0+8,2,2,tuple(clamp(v*0.45) for v in base[:3])+(255,)); t.p(nx,y0+8,shade(base,40))
    t.save(name)
wood('tex_wood',1.0,4); wood('tex_wood_dark',0.74,5)

rng=random.Random(6); t=T(32,32,C('C9A063'))
specks(t,rng,70,lambda: C('A87E45')); specks(t,rng,55,lambda: C('DDB97C')); specks(t,rng,10,lambda: C('78562D'))
for _ in range(14):
    x=rng.randrange(32);y=rng.randrange(32); t.p(x,y,C('A87E45')); t.p(x+1,y,C('A87E45'))
t.save('tex_cork')

# grass
rng=random.Random(7); t=T(48,48,C('5C9A3C'))
for y in range(48):
    for x in range(48):
        r=rng.random()
        if r<.28: t.p(x,y,C('508C34'))
        elif r<.44: t.p(x,y,C('6AAE48'))
for _ in range(26):
    x=rng.randrange(48);y=rng.randrange(48)
    t.p(x,y,C('3F7A2A')); t.p(x,y-1,C('3F7A2A')); t.p(x-1,y-1,C('7CC058')); t.p(x+1,y-2,C('7CC058')); t.p(x-2,y-2,C('6AAE48'))
for _ in range(4):
    x=rng.randrange(2,46);y=rng.randrange(2,46)
    col=rng.choice([C('FFF3C4'),C('F2D25A'),C('F0A0B0'),C('FFFFFF')])
    t.p(x,y,C('F2C23A') if col!=C('F2C23A') else C('FFF3C4'))
    for dx,dy in ((1,0),(-1,0),(0,1),(0,-1)): t.p(x+dx,y+dy,col)
for _ in range(5):
    x=rng.randrange(48);y=rng.randrange(48); t.p(x,y,C('8D8A7C')); t.p(x+1,y,C('A9A698'))
for _ in range(4):
    x=rng.randrange(48);y=rng.randrange(48); t.rect(x,y,3,2,C('7A5A34')); t.p(x+1,y,C('93703F'))
t.save('tex_grass')

# tilled soil
rng=random.Random(8); t=T(48,48)
rowc=[C('4A2E16'),C('5E3A1C'),C('6B4423'),C('6B4423'),C('73492A'),C('73492A'),C('7E5530'),C('7E5530'),C('8C6038'),C('8C6038'),C('6B4423'),C('55361A')]
for y in range(48):
    for x in range(48):
        c=rowc[y%12]
        if rng.random()<.18: c=shade(c,-10)
        elif rng.random()<.14: c=shade(c,10)
        t.p(x,y,c)
for _ in range(10):
    x=rng.randrange(48);y=rng.randrange(48); t.rect(x,y,2,1,C('3E2511'))
for sx,sy in ((8,8),(30,20),(14,32),(40,44),(24,2)):
    t.p(sx,sy,C('3E6329')); t.p(sx,sy-1,C('5B8C3E')); t.p(sx-1,sy-2,C('8DBF5A')); t.p(sx+1,sy-2,C('8DBF5A')); t.p(sx,sy-2,C('5B8C3E'))
for _ in range(5):
    x=rng.randrange(48);y=rng.randrange(48); t.p(x,y,C('9A9486')); t.p(x+1,y,C('B8B2A2'))
t.save('tex_soil')

# cobblestone
rng=random.Random(9); t=T(64,64,C('34312B'))
heights=[21,22,21]; y=0
stones=[C('6E685D'),C('666056'),C('766F63'),C('5F5A50')]
for ri,rh in enumerate(heights):
    ws=[];rem=64
    while rem>0:
        w=rng.randint(14,24)
        if rem-w<12: w=rem
        ws.append(w); rem-=w
    x=(ri*9)%64
    for w in ws:
        base=rng.choice(stones)
        for j in range(rh):
            for i in range(w):
                px_,py_=x+i,y+j
                edge_l=i==0; edge_t=j==0
                if edge_l or edge_t: t.p(px_,py_,C('34312B')); continue
                c=base
                if i==1 or j==1: c=shade(base,22)
                elif i==w-1 or j==rh-1: c=shade(base,-26)
                elif i>=w-2 or j>=rh-2: c=shade(base,-12)
                if rng.random()<.16: c=shade(c,-9)
                elif rng.random()<.12: c=shade(c,8)
                if (i<2 and j<2) or (i>=w-1 and j>=rh-1 and False): c=C('34312B')
                t.p(px_,py_,c)
        if rng.random()<.65:
            for _ in range(rng.randint(5,12)):
                mx=x+1+rng.randint(0,max(1,w-4)); my=y+1+rng.randint(0,3)
                t.p(mx,my,rng.choice([C('567C38'),C('6A9444'),C('44662C')]))
        x+=w
    y+=rh
t.save('tex_stone')

rng=random.Random(10); t=T(24,24,C('6A665C'))
specks(t,rng,60,lambda: C('7C786E')); specks(t,rng,40,lambda: C('55524A')); specks(t,rng,10,lambda: C('8E8A80'))
t.save('tex_slate')

# lava rock (voronoi)
rng=random.Random(11); W=48; t=T(W,W)
seeds=[(rng.uniform(0,W),rng.uniform(0,W)) for _ in range(8)]
plates=[C('DA8450'),C('D67B49'),C('DF8E58'),C('D2733F'),D_:=C('D98150'),C('E29A64'),C('CF6F3C'),C('DC8854')]
def td(a,b):
    dx=abs(a[0]-b[0]); dy=abs(a[1]-b[1]); dx=min(dx,W-dx); dy=min(dy,W-dy); return math.hypot(dx,dy)
for y in range(W):
    for x in range(W):
        ds=sorted((td((x,y),s_),i) for i,s_ in enumerate(seeds))
        d1,i1=ds[0]; d2,_=ds[1]; gap=d2-d1
        c=plates[i1]
        if gap<1.3:
            c=C('9A4020') if gap>0.5 else C('7A2E16')
            if rng.random()<.025: c=C('F3A23C')
        elif gap<2.2: c=shade(plates[i1],-14)
        elif d1<5: c=shade(plates[i1],6)
        if rng.random()<.04: c=shade(c,-8)
        t.p(x,y,c)
t.save('tex_lava')

rng=random.Random(12); t=T(24,24,C('413030'))
specks(t,rng,50,lambda: C('5A3F38')); specks(t,rng,24,lambda: C('2E2222')); specks(t,rng,7,lambda: C('F0782A')); specks(t,rng,6,lambda: C('B8481E'))
t.save('tex_rock')

rng=random.Random(13); t=T(32,32,C('F8DE8C')); blotch(t,rng,8,4)
streaks(t,rng,12,5,10,lambda: C('FDEEB2')); streaks(t,rng,10,4,8,lambda: C('EBC66E'))
for _ in range(3):
    x=rng.randrange(32);y=rng.randrange(32); t.p(x,y,C('FFF6CF')); t.p(x+1,y,C('F2D27A')); t.p(x,y+1,C('F2D27A'))
t.save('tex_honey')

rng=random.Random(14); t=T(24,24,C('D99820'))
specks(t,rng,40,lambda: C('F2BA46')); specks(t,rng,34,lambda: C('B87610')); specks(t,rng,8,lambda: C('FFE08A'))
t.save('tex_honey_frame')

rng=random.Random(15); t=T(24,24,C('3E6329'))
specks(t,rng,30,lambda: C('2F4F20'))
for _ in range(7):
    x=rng.randrange(24);y=rng.randrange(24)
    for dx,dy,c in ((0,0,C('5B8C3E')),(1,0,C('5B8C3E')),(0,1,C('8DBF5A')),(1,1,C('5B8C3E')),(2,1,C('3E6329'))): t.p(x+dx,y+dy,c)
t.save('tex_leaf_frame')

# wallpaper
rng=random.Random(16); t=T(32,32,C('468A86'))
for x in range(32):
    for y in range(32):
        if (x//4)%2==0: t.p(x,y,C('529A95'))
        if rng.random()<.05: t.p(x,y,shade(t.g(x,y),-8))
for cx,cy in ((2,8),(10,24),(18,8),(26,24)):
    for dx,dy in ((0,0),(1,0),(-1,0),(0,1),(0,-1)): t.p(cx+dx,cy+dy,C('E8D6A0'))
    t.p(cx,cy,C('F6E9BD'))
t.save('tex_wall')

# bolt 5x5
b=T(5,5)
rows=[".ddd.","dlLld","dLlsd","dlssd",".ddd."]
cm={'d':C('3A302A'),'l':C('C8C6BE'),'L':C('F0EEE6'),'s':C('77746E')}
for j,r in enumerate(rows):
    for i,ch in enumerate(r):
        if ch!='.': b.px[i,j]=cm[ch]
b.save('deco_bolt')

# honey drips 64x14
d=T(64,14)
hi=C('FFE27A'); mid=C('F4B834'); lo=C('D48C14'); ol=C('8A540A')
for x in range(64):
    d.q(x,0,hi); d.q(x,1,mid); d.q(x,2,mid); d.q(x,3,lo)
drips=[(6,6),(17,3),(29,8),(38,4),(50,7),(58,3)]
for cx,L in drips:
    for j in range(4,4+L):
        d.q(cx-1,j,hi if j%3 else mid); d.q(cx,j,mid); d.q(cx+1,j,lo)
    e=4+L
    d.q(cx-1,e,mid); d.q(cx,e,mid); d.q(cx+1,e,lo); d.q(cx,e+1,lo)
    d.q(cx-2,e,ol) if False else None
d.save('deco_drips')

# moss corner
rng=random.Random(17); m=T(16,12)
for y in range(12):
    for x in range(16):
        if x/16+y/12 < 0.9 + rng.uniform(-.12,.12):
            m.px[x,y]=rng.choice([C('3E6329'),C('4F7A33'),C('5B8C3E'),C('6FA24A')])
for _ in range(8):
    x=rng.randrange(14);y=rng.randrange(10)
    if m.px[x,y][3]: m.px[x,y]=C('9ACB62')
m.save('deco_moss_tl')
Image.open('/home/claude/gen/tex/deco_moss_tl.png').rotate(180).save('/home/claude/gen/tex/deco_moss_br.png')

def leaf(t,x,y,dirx,c1,c2):
    for dx,dy,c in ((0,0,c1),(dirx,0,c1),(2*dirx,0,c2),(dirx,-1,c2),(dirx,1,c1)): t.q(x+dx,y+dy,c)
def vine(name,w,h,seed,leafgap=7,amp=2.5):
    rng=random.Random(seed); t=T(w,h)
    for y in range(h):
        x=int(w/2+round(amp*math.sin(y/6.5+seed)))
        t.q(x,y,C('2F5522')); t.q(x+1,y,C('3E6329'))
        if y%leafgap==3:
            side=1 if (y//leafgap)%2==0 else -1
            leaf(t,x+(2 if side>0 else -1),y,side,C('4F7A33'),C('8DBF5A'))
        if y%leafgap==0 and y>0:
            side=-1 if (y//leafgap)%2==0 else 1
            t.q(x+side*2,y,C('5B8C3E')); t.q(x+side*3,y,C('8DBF5A'))
    ex=int(w/2+round(amp*math.sin((h-1)/6.5+seed)))
    t.q(ex,h-1,C('F2D25A')); t.q(ex+1,h-1,C('FFF3C4')); t.q(ex,h-2,C('F2D25A'))
    t.save(name)
vine('deco_vine_a',14,58,1); vine('deco_vine_b',12,40,3)
vine('deco_vine_side_l',16,100,2,leafgap=8,amp=3)
Image.open('/home/claude/gen/tex/deco_vine_side_l.png').transpose(Image.FLIP_LEFT_RIGHT).save('/home/claude/gen/tex/deco_vine_side_r.png')

# preview sheet
import glob,os
names=sorted(glob.glob('/home/claude/gen/tex/*.png'))
S=4
sheet=Image.new('RGB',(1400,900),(40,40,40)); x=y=10; rowh=0
for n in names:
    im=Image.open(n).convert('RGBA')
    big=im.resize((im.width*S,im.height*S),Image.NEAREST)
    tile=n.split('/')[-1].startswith('tex_')
    if tile:
        # show 2x2 tiling to check seams
        big2=Image.new('RGBA',(big.width*2,big.height*2))
        for a in range(2):
            for b2 in range(2): big2.paste(big,(a*big.width,b2*big.height))
        big=big2
    if x+big.width>1390: x=10; y+=rowh+10; rowh=0
    sheet.paste(big,(x,y),big); x+=big.width+10; rowh=max(rowh,big.height)
sheet.save('/tmp/texsheet.png'); print(sheet.size, y+rowh)
