import math, json
from PIL import Image, ImageDraw

PALETTE = {
 'k':'3B240E','w':'6B4423','h':'9C6B3E','c':'F7E6C4','t':'E4CFA9','W':'FFF8E7',
 'g':'E5A93B','G':'B07A1E','y':'F7D774','l':'5B8C3E','L':'8DBF5A','D':'3E6329',
 'r':'C8442B','R':'8E2A1C','o':'E8803A','m':'F05A2A','a':'D98E1F','b':'5B8FC9',
 'B':'345F94','s':'9FD3E8','p':'E8A0A0','e':'A8A8A8','E':'5E5E5E','n':'C79A5B','u':'7B4B8A',
}

def grid(w,h): return [['.']*w for _ in range(h)]
def to_rows(g): return [''.join(r) for r in g]
def fix(rows,w=12):
    out=[]
    for r in rows:
        r=r[:w] if len(r)>=w else r+'.'*(w-len(r))
        out.append(r)
    return out
def rec(rows,m):
    return [''.join(m.get(ch,ch) for ch in r) for r in rows]

S={}

def disc(w,h,cx,cy,rad,fill,outline='k',hi=None,sh=None):
    g=grid(w,h)
    for y in range(h):
        for x in range(w):
            d=math.hypot(x-cx,y-cy)
            if d<=rad:
                g[y][x]=outline if d>rad-1.0 else fill
                if d<=rad-1.0:
                    if hi and (x-cx)+(y-cy)< -rad*0.55: g[y][x]=hi
                    elif sh and (x-cx)+(y-cy)> rad*0.55: g[y][x]=sh
    return g

# coin
g=disc(12,12,5.5,5.5,5.6,'g','k','y','G')
for y in range(3,9):
    for x in (5,6): g[y][x]='G'
S['coin']=to_rows(g)

# mini coin stamp
mini=["..kkkk..",".kyyggk.","kyggggGk","kyggGgGk","kgggGgGk","kgggggGk",".kGGGGk.","..kkkk.."]
def stamp(g,sp,ox,oy):
    for j,row in enumerate(sp):
        for i,ch in enumerate(row):
            if ch!='.' and 0<=oy+j<len(g) and 0<=ox+i<len(g[0]): g[oy+j][ox+i]=ch
g=grid(12,12)
stamp(g,mini,0,4); stamp(g,mini,4,4); stamp(g,mini,2,0)
S['coins']=to_rows(g)

S['bag']=fix([
"....kkkk....",
".....kk.....",
"....kGGk....",
"...kyyyyk...",
"..kyyyggyk..",
".kyyygGggGk.",
".kyygGggGgk.",
".kygggGGggk.",
".kyggGgggGk.",
".kgggGGgggk.",
"..kGgggggk..",
"...kkkkkk...",
])

S['mic']=fix([
"....kkkk....",
"...kWWWWk...",
"...kWeeWk...",
"...kWWWWk...",
"...kWeeWk...",
"...kWWWWk...",
".k.kWeeWk.k.",
".kkkWWWWkkk.",
"..kkkkkkkk..",
".....kk.....",
".....kk.....",
"...kkkkkk...",
])

def plus(ch):
    g=grid(12,12)
    for y in range(2,10):
        for x in range(5,7): g[y][x]=ch
    for y in range(5,7):
        for x in range(2,10): g[y][x]=ch
    return to_rows(g)
S['plus']=plus('W'); S['plus_dark']=plus('k')

def chev(ch,down=True):
    rows=["............"]*3+[
    ".XX......XX.",
    "..XX....XX..",
    "...XX..XX...",
    "....XXXX....",
    ".....XX.....",]+["............"]*4
    rows=[r.replace('X',ch) for r in rows]
    return rows if down else rows[::-1]
S['down']=chev('W',True); S['up']=chev('W',False)
S['down_dark']=chev('k',True); S['up_dark']=chev('k',False)

# check
g=grid(12,12)
for (x,y) in [(2,6),(3,7),(4,8),(5,7),(6,6),(7,5),(8,4),(9,3)]:
    g[y][x]='W'; g[y+1][x]='W'
S['check']=to_rows(g)

# pencil (diagonal)
g=grid(12,12)
for x in range(12):
    for y in range(12):
        s=x+y
        if 1<=x<=10:
            if s==11:
                g[y][x]='y' if 3<=x<=8 else ('p' if x>=9 else 'h')
            elif s==12:
                g[y][x]='a' if 3<=x<=8 else ('R' if x>=9 else 'w')
            elif s in (10,13):
                g[y][x]='k'
g[10][1]='k'; g[11][0]='k'; g[9][2]='h'; g[10][1]='k'
g[11][1]='k'
S['pencil']=to_rows(g)

S['trash']=fix([
"....kkkk....",
".kkkkkkkkkk.",
".khhhhhhhhk.",
".kkkkkkkkkk.",
"..kwhkwhwk..",
"..kwhkwhwk..",
"..kwhkwhwk..",
"..kwhkwhwk..",
"..kwhkwhwk..",
"..kwhkwhwk..",
"...kkkkkk...",
"............",
])

S['tree']=fix([
"....kkkk....",
"..kkLLLLkk..",
".kLLllLLLLk.",
"kLLlLLllLLLk",
"kLlLLLLLllLk",
"kLLLllLLLLlk",
".klLLLLllLk.",
"..kklLLlkk..",
"....khhk....",
"....khhk....",
"....khhk....",
"...kkkkkk...",
])
S['honey']=fix([
"...kkkkkk...",
"..khhhhhhk..",
"..kkkkkkkk..",
".kaaaaaaaak.",
"kayyyaaaaaak",
"kayyaaaaaaak",
"kayaaaccaaak",
"kaaaaccWcaak",
"kaaaaaccaGak",
"kaaaaaaaaGak",
".kGGGGGGGGk.",
"..kkkkkkkk..",
])
S['flame']=fix([
".....kk.....",
".....kmk....",
"....kmmk....",
"...kmmmmk.k.",
"..kmmooomkmk",
"..kmooyoomk.",
".kmooyyyomk.",
".kmooyyyomk.",
".kmmooyommk.",
"..kmmooommk.",
"...kkmmmkk..",
".....kkk....",
])
g=grid(12,12)
for y in range(12):
    for x in range(12):
        dx=x-5.5; dy=y-5.5; d=math.hypot(dx,dy)
        if 3.3<=d<=5.4:
            ang=math.degrees(math.atan2(-dy,dx))  # 0=right, 90=up
            if 15<=ang<=70 or -165<=ang<=-110: continue
            g[y][x]='w'
for (x,y) in [(7,1),(8,1),(9,1),(10,1),(10,2),(10,3),(9,2),(10,0),(11,1)]: pass
head1=[(8,0),(9,0),(10,0),(10,1),(10,2),(9,1)]
for (x,y) in head1: g[y][x]='k'
head2=[(3,11),(2,11),(1,11),(1,10),(1,9),(2,10)]
for (x,y) in head2: g[y][x]='k'
S['repeat']=to_rows(g)
S['clock']=None
g=disc(12,12,5.5,5.5,5.6,'W','k')
for y in range(2,7): g[y][6]='k'
for x in range(6,9): g[6][x]='k'
S['clock']=to_rows(g)

S['house']=fix([
".....kk.....",
"....kRRk....",
"...kRRRRk...",
"..kRRRrRRk..",
".kRRRRRRRRk.",
"kRRRRRRRRRRk",
".kkkkkkkkkk.",
"..kWWWWWWk..",
"..kWWkkWWk..",
"..kWWkhWWk..",
"..kWWkkWWk..",
"..kkkkkkkk..",
])
S['scroll']=fix([
"............",
".kkkkkkkkkk.",
".kwwwwwwwwk.",
".kkkkkkkkkk.",
"..kccccccck.",
"..kckkkkkck.",
"..kccccccck.",
"..kckkkkkck.",
"..kccccccck.",
".kkkkkkkkkk.",
".kwwwwwwwwk.",
".kkkkkkkkkk.",
])
S['basket']=fix([
".....kk.....",
"....k..k....",
"...k....k...",
"..kkkkkkkk..",
".kaaYaaYaak.",
".kyaayaayak.",
".kaaYaaYaak.",
"..kyaayaak..",
"..kaaYaayk..",
"...kkkkkk...",
"............",
"............",
])
S['basket']=[r.replace('Y','y') for r in S['basket']]
S['calendar']=fix([
"............",
".kkkkkkkkkk.",
".kRRRRRRRRk.",
".kRkRRRRkRk.",
".kkkkkkkkkk.",
".kWWWWWWWWk.",
".kWkWkWkWWk.",
".kWWWWWWWWk.",
".kWkWkWkWWk.",
".kWWWWWWWWk.",
".kkkkkkkkkk.",
"............",
])
S['dumbbell']=fix([
"............",
"............",
".kk......kk.",
"kEEk....kEEk",
"kEEkkkkkkEEk",
"kEEkhhhhkEEk",
"kEEkkkkkkEEk",
"kEEk....kEEk",
".kk......kk.",
"............",
"............",
"............",
])
# market sprites
S['apple']=fix([
"......k.....",
".....kDLk...",
"..kkkkkDLk..",
".krrrkrrrrk.",
"krrWrrrrrrrk",
"krWrrrrrrrRk",
"krrrrrrrrrRk",
"krrrrrrrrRRk",
".krrrrrrrRk.",
".krrrrrrRRk.",
"..kkrrRRkk..",
"....kkkk....",
])
S['meat']=fix([
"..kkkkk.....",
".koooooRk...",
"koooWooooRk.",
"kooooooooRk.",
"kooooooooRk.",
".kooooooRRk.",
"..kkoRRRkk..",
"....kkRkWk..",
".....kkWWWk.",
"......kWWWWk",
".......kWWk.",
"........kk..",
])
S['milk']=fix([
"....kkkk....",
"....kbbk....",
"...kkWWkk...",
"..kWWWWWWk..",
"..kWbbbbWk..",
"..kWbWWbWk..",
"..kWbbbbWk..",
"..kWWWWWWk..",
"..kWWWWWWk..",
"..kWWWWWWk..",
"..kssssssk..",
"...kkkkkk...",
])
S['bread']=fix([
"............",
"...kkkkkk...",
"..kayayaak..",
".kayayayaak.",
"kayyayayayak",
"kaaaaaaaaaak",
"kaaaaaaaaaak",
"kayyyyyyyyak",
"kayyyyyyyyak",
".kaaaaaaaak.",
"..kkkkkkkk..",
"............",
])
S['jar']=fix([
"...kkkkkk...",
"...khhhhk...",
"..kkkkkkkk..",
"..kssssssk..",
".kssWssssRk.",
".ksrrrrrrRk.",
".kcrrWrrrRk.",
".kcrrrrrrRk.",
".ksrrrrrrRk.",
".kssssssssk.",
"..kkkkkkkk..",
"............",
])
S['sponge']=fix([
"............",
"............",
"..kkkkkkkk..",
".kLLLLLLLLk.",
".kllllllllk.",
".kkkkkkkkkk.",
".kyyyyyyyyk.",
".kyyaayyayk.",
".kyyyyyyyyk.",
".kyayyyyayk.",
".kkkkkkkkkk.",
"............",
])
S['phone']=fix([
"...kkkkkk...",
"..kEEEEEEk..",
"..kEssssEk..",
"..kEsBBsEk..",
"..kEssssEk..",
"..kEssssEk..",
"..kEssssEk..",
"..kEssssEk..",
"..kEEEEEEk..",
"..kEEggEEk..",
"..kEEEEEEk..",
"...kkkkkk...",
])
S['crate']=fix([
"............",
".kkkkkkkkkk.",
".khhhhhhhhk.",
".kwkhhhhkwk.",
".khwkhhkwhk.",
".khhwkkwhhk.",
".khhhwwhhhk.",
".khhwkkwhhk.",
".khwkhhkwhk.",
".kwkhhhhkwk.",
".khhhhhhhhk.",
".kkkkkkkkkk.",
])
# sky
g=disc(12,12,5.5,5.5,3.2,'y','k')
for (x,y) in [(5,0),(6,0),(5,1),(6,1),(5,10),(6,10),(5,11),(6,11),(0,5),(0,6),(1,5),(1,6),(10,5),(10,6),(11,5),(11,6),(2,2),(9,2),(2,9),(9,9)]:
    g[y][x]='g'
S['sun']=to_rows(g)
g=grid(12,12)
for y in range(12):
    for x in range(12):
        d=math.hypot(x-5.5,y-8.5)
        if y<=8 and d<=4.2: g[y][x]='o' if d<=3.2 else 'k'
for x in range(12): g[9][x]='k'; g[10][x]='a'
for (x,y) in [(5,0),(6,0),(1,3),(10,3),(0,6),(11,6)]: g[y][x]='g'
S['sunset']=to_rows(g)
g=grid(12,12)
for y in range(12):
    for x in range(12):
        d1=math.hypot(x-5.5,y-5.5); d2=math.hypot(x-8.0,y-4.0)
        if d1<=5.4 and d2>4.6:
            g[y][x]='y' if d1<=4.4 else 'k'
for (x,y) in [(9,8),(10,2)]: g[y][x]='W'
S['moon']=to_rows(g)

S['pin']=["..kkkk..",".kRrrrk.",".kRrWrk.",".kRrrrk.","..kkkk..","...ee...","...ke...","....k..."]
S['cat']=[
"................",
"..k.k...........",
".kokok..kkkkkk..",
".koooookkooooook",
".kowoowoooaaoook",
".kooooooooooaook",
".kopokoooooooook",
"..kkkoooaaooook.",
"....kooooooooook",
"....kkkkkkkkkkk.",
]
S['sprout']=["..L....L..",".LlL..LlL.",".LllLLllL.","..LlllLl..","....ll....","....lk....","...hhhh...","..hhhhhh.."]
S['cloud']=["......WWWW......","....WWWWWWWW....","..WWWWWWWWWWWW..",".WWWWWWWWWWWWWW.","WWWWWWWWWWWWWWWW",".tttttttttttttt."]


def chevside(ch,left=True):
    rows=["............","............","......XX....",".....XX.....","....XX......","...XX.......","...XX.......","....XX......",".....XX.....","......XX....","............","............"]
    rows=[r.replace('X',ch) for r in rows]
    if not left: rows=[r[::-1] for r in rows]
    return rows
S['left']=chevside('W',True); S['right']=chevside('W',False)
S['left_dark']=chevside('k',True); S['right_dark']=chevside('k',False)


# chart (bars) and gear
g=grid(12,12)
for x in range(1,11): g[10][x]='k'
for y in range(1,11): g[y][1]='k'
def bar(x0,h,c,hi):
    for y in range(10-h,10):
        for x in range(x0,x0+2): g[y][x]=c
    for x in range(x0,x0+2): g[10-h][x]=hi
bar(3,3,'l','L'); bar(6,5,'g','y'); bar(9,7,'r','o')
S['chart']=to_rows(g)
g=grid(12,12)
for y in range(12):
    for x in range(12):
        dx=x-5.5; dy=y-5.5; d=math.hypot(dx,dy)
        if d<=4.0: g[y][x]='E' if d>3.0 else 'e'
        if d<=1.8: g[y][x]='.'
for (x,y) in [(5,0),(6,0),(5,1),(6,1),(5,10),(6,10),(5,11),(6,11),(0,5),(0,6),(1,5),(1,6),(10,5),(10,6),(11,5),(11,6),(2,2),(3,2),(2,3),(9,2),(8,2),(9,3),(2,9),(3,9),(2,8),(9,9),(8,9),(9,8)]:
    g[y][x]='E'
S['gear']=to_rows(g)

# fix sizes: 12 wide for non-special
for k,v in list(S.items()):
    if k in ('pin','cat','sprout','cloud'): continue
    S[k]=fix(v,12)
S['cat']=fix(S['cat'],16)

# validate
bad=0
for k,v in S.items():
    w=len(v[0])
    for r in v:
        if len(r)!=w: print('BAD',k,r); bad+=1
        for ch in r:
            if ch!='.' and ch not in PALETTE: print('BADCH',k,ch); bad+=1
print('sprites',len(S),'bad',bad)

if __name__=='__main__':
    names=list(S)
    scale=8; cols=8
    cw=20*scale; ch_=16*scale
    rows=(len(names)+cols-1)//cols
    img=Image.new('RGB',(cols*cw,rows*ch_),(238,217,183))
    d=ImageDraw.Draw(img)
    for i,n in enumerate(names):
        ox=(i%cols)*cw+4; oy=(i//cols)*ch_+4
        if n in ('plus','down','up','check','mic'): d.rectangle([ox-2,oy-2,ox+12*scale+2,oy+12*scale+2],fill=(107,68,35))
        for j,row in enumerate(S[n]):
            for x,c in enumerate(row):
                if c!='.': d.rectangle([ox+x*scale,oy+j*scale,ox+(x+1)*scale-1,oy+(j+1)*scale-1],fill='#'+PALETTE[c])
    img.save('/tmp/sheet.png')
    print(names)
