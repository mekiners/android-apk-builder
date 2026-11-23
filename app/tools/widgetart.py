import numpy as np, math
from PIL import Image, ImageDraw, ImageFilter
from art import noise, fbm, col, over, light, blur

SS=3
def Mw(fn,w,h):
    im=Image.new('L',(w*SS,h*SS),0); d=ImageDraw.Draw(im); fn(d,SS); return np.asarray(im.resize((w,h),Image.LANCZOS)).astype(np.float32)/255
def rrm(w,h,box,r): return Mw(lambda d,s: d.rounded_rectangle([box[0]*s,box[1]*s,box[2]*s-1,box[3]*s-1],r*s,fill=255),w,h)
def gradw(c1,c2,w,h):
    t=np.linspace(0,1,h)[:,None,None]; return col(c1)*(1-t)+col(c2)*t+np.zeros((h,w,3),np.float32)
def rivet(img,w,h,x,y,r):
    m=Mw(lambda d,s:d.ellipse([(x-r)*s,(y-r)*s,(x+r)*s,(y+r)*s],fill=255),w,h)
    o=Mw(lambda d,s:d.ellipse([(x-r-1.5)*s,(y-r-1.5)*s,(x+r+1.5)*s,(y+r+1.5)*s],fill=255),w,h)
    img=over(img,col((60,44,32)),o,.9)
    return over(img,col((196,190,176))+light(m,2)[...,None]*55,m)
def fin(rgb,alpha): return Image.fromarray(np.dstack([np.clip(rgb,0,255),alpha*255]).astype(np.uint8),'RGBA')
def nine(img,l,r,t,b):
    w,h=img.size; out=Image.new('RGBA',(w+2,h+2),(0,0,0,0)); out.paste(img,(1,1)); px=out.load()
    for x in range(1+l,1+r): px[x,0]=(0,0,0,255)
    for y in range(1+t,1+b): px[0,y]=(0,0,0,255)
    return out

def panel(size=180,fr=22,seed=3):
    w=h=size
    outer=rrm(w,h,(0,0,w,h),22)
    wood=gradw((140,94,52),(106,70,38),w,h)+((fbm(w,h,seed,(60,30,15,6))-.5)*34+(noise(w,h,3,seed+9)-.5)*16)[...,None]
    frm=rrm(w,h,(2,2,w-2,h-2),20)
    img=np.zeros((h,w,3),np.float32)+col((59,36,14))
    img=over(img,wood+light(frm,3)[...,None]*36,frm)
    inn=rrm(w,h,(fr-3,fr-3,w-fr+3,h-fr+3),8)
    img=over(img,col((45,28,12)),inn)
    inner=rrm(w,h,(fr,fr,w-fr,h-fr),6)
    par=gradw((246,232,198),(236,218,176),w,h)+((fbm(w,h,seed+5)-.5)*14)[...,None]
    img=over(img,par-light(inner,2)[...,None]*14,inner)
    for (x,y) in ((12,12),(w-12,12),(12,h-12),(w-12,h-12)): img=rivet(img,w,h,x,y,4)
    return fin(img,outer)

def button(c1,c2,w=96,h=64,seed=1):
    outer=rrm(w,h,(0,0,w,h),14)
    img=np.zeros((h,w,3),np.float32)+col((59,36,14))
    body=rrm(w,h,(3,3,w-3,h-3),12)
    g=gradw(c1,c2,w,h)+((fbm(w,h,seed,(30,12,6))-.5)*16)[...,None]
    img=over(img,g+light(body,3)[...,None]*40,body)
    hi=rrm(w,h,(6,6,w-6,h//2),9)
    img=over(img,col((255,255,255)),hi,.10)
    return fin(img,outer)

R='/home/claude/proj/android-apk-builder-2.0-cozy-stable/app/src/main/res/drawable-nodpi/'
nine(panel(),30,150,30,150).save(R+'widget_panel.9.png')
nine(button((120,170,76),(70,118,48)),20,76,20,44).save(R+'btn_in.9.png')
nine(button((222,128,92),(168,78,54)),20,76,20,44).save(R+'btn_out.9.png')
# preview
prev=Image.new('RGBA',(700,320),(86,146,58,255))
p=panel().resize((300,300),Image.NEAREST); prev.alpha_composite(p,(10,10))
prev.alpha_composite(button((120,170,76),(70,118,48)).resize((192,128)),(350,30)); prev.alpha_composite(button((222,128,92),(168,78,54)).resize((192,128)),(350,170))
prev.convert('RGB').save('/tmp/widget_assets.png'); print('ok')
