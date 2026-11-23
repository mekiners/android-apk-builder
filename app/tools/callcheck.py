import re,glob,collections
files=glob.glob('**/*.kt',recursive=True)
src={f:open(f).read() for f in files}
def strip(s):
    s=re.sub(r'//[^\n]*','',s); s=re.sub(r'/\*.*?\*/','',s,flags=re.S)
    return re.sub(r'"(\\.|[^"\\])*"','""',s)
clean={f:strip(s) for f,s in src.items()}
def split_top(s,sep=','):
    out=[];d=0;cur=''
    for ch in s:
        if ch in '([{<': d+= (1 if ch!='<' else 0)
        if ch in ')]}': d-=1
        if ch==sep and d==0: out.append(cur); cur=''
        else: cur+=ch
    if cur.strip(): out.append(cur)
    return out
def match_paren(s,i):
    d=0
    for j in range(i,len(s)):
        if s[j]=='(': d+=1
        elif s[j]==')':
            d-=1
            if d==0: return j
    return -1
defs=collections.defaultdict(list)
for f,s in clean.items():
    for m in re.finditer(r'\bfun\s+(?:<[^>]*>\s*)?(?:[\w.]+\.)?(\w+)\s*\(',s):
        name=m.group(1); i=m.end()-1; j=match_paren(s,i)
        params=split_top(s[i+1:j])
        plist=[]
        for p in params:
            p=p.strip()
            if not p: continue
            pm=re.match(r'(?:vararg\s+)?(\w+)\s*:',p)
            if pm: plist.append((pm.group(1),'=' in p.split(':',1)[1] and True or False, p))
        defs[name].append((f,plist))
bad=0
for f,s in clean.items():
    for name,dl in defs.items():
        if len(dl)!=1: continue
        plist=dl[0][1]
        pnames=[p[0] for p in plist]
        for m in re.finditer(r'(?<![\w.])'+name+r'\s*\(',s):
            # skip the declaration itself
            pre=s[max(0,m.start()-12):m.start()]
            if re.search(r'fun\s+$',pre) or re.search(r'fun\s+[\w.<>]*\.$',pre): continue
            i=m.end()-1; j=match_paren(s,i)
            if j<0: continue
            args=split_top(s[i+1:j])
            named=[];pos=0
            for a in args:
                a=a.strip()
                if not a: continue
                nm=re.match(r'(\w+)\s*=(?!=)',a)
                if nm: named.append(nm.group(1))
                else: pos+=1
            trailing=1 if re.match(r'\s*\{',s[j+1:j+4] if j+1<len(s) else '') or re.match(r'\s*\{',s[j+1:j+3]) else 0
            for n in named:
                if n not in pnames:
                    print(f'{f}: call {name}(...) unknown named arg {n!r}'); bad+=1
            provided=pos+len(named)+trailing
            required=[p[0] for p in plist if not p[1]]
            # count required satisfied: positional fill first params
            filled=set(pnames[:pos])|set(named)
            if trailing and plist: filled.add(pnames[-1])
            missing=[r for r in required if r not in filled]
            if missing and not name[0].islower() == False:
                pass
            if missing:
                print(f'{f}: call {name}(...) missing {missing}'); bad+=1
            if pos+trailing>len(pnames):
                print(f'{f}: call {name}(...) too many positional args ({pos}+{trailing} > {len(pnames)})'); bad+=1
print('callcheck problems:',bad)
