"""REFERENCE ONLY - balance simulation used to tune the v4 rules (not production code).
Defaults below are the v4 values. Bots are crude: 'rand' leans green/polluting at random,
'exploit' pollutes maximally and converts at the end, 'green' only goes green.
Known simplifications: no junctions/City Centre route, random board order, simplified Climate Cards.
Run: python3 balance_sim.py
"""

import random, statistics as st

C = dict(M0=50, BASE=3, POL_METER=2, GREEN_METER=1, ROUNDS=12, GREEN_INC=2,
         POL_INC=7, POL_GP=1, POL_COST=(4, 7), GREEN_COST=(8, 12), GREEN_GP=2, CONVERT=5,
         N_POL=10, N_GREEN=12, N_NATURE=4, N_RES=4, N_GLIDER=3, N_CARD=7, DISTRICTS=6,
         COFUND=3, LOBBY=2, RES_COST=(3, 5, 7), RES_CUT=(0.25, 0.5, 0.75),
         LOAN=10, LOAN_REPAY=15, CHECK={2: 26, 7: 11}, CHECK_PEN=2, FAIL_POL_GP=5)

CHARS = {'Industrialist': (40, 8), 'Tech': (36, 10), 'Planner': (38, 12),
         'Farmer': (36, 13), 'Student': (34, 16), 'Activist': (32, 18)}

def play(c, names, strats, rng, g=None):
    sq = []
    for _ in range(c['N_POL']): sq.append(dict(t='pol', cost=rng.randint(*c['POL_COST'])))
    for i in range(c['N_GREEN']): sq.append(dict(t='green', nat=i < c['N_NATURE'], cost=rng.randint(*c['GREEN_COST'])))
    for t in ['res'] * c['N_RES'] + ['glider'] * c['N_GLIDER'] + ['card'] * c['N_CARD']: sq.append(dict(t=t))
    rng.shuffle(sq); n = len(sq)
    for i, s in enumerate(sq): s.update(owner=None, d=i * c['DISTRICTS'] // n, closed=0, lob=0)
    P = [dict(n=nm, s=st_, g=gg, m=CHARS[nm][0], gp=CHARS[nm][1], pos=rng.randrange(n), cof=None,
              bank=0, debt=0, res=0, polrounds=0) for nm, st_, gg in zip(names, strats, g)]
    meter = c['M0']; flood = None; drought = False; stats = dict(bank=0, dis_cost=0)
    own = lambda p, t: [s for s in sq if s['owner'] is p and s['t'] == t]
    def pay(p, a):
        a = round(a * (1 - (c['RES_CUT'][p['res'] - 1] if p['res'] else 0)))
        p['m'] -= a; stats['dis_cost'] += a
        if p['m'] < 0 and not p['bank']: p['m'] = 0; p['bank'] = 1; stats['bank'] += 1
    for r in range(c['ROUNDS']):
        for p in P * c.get('TPR', 1):
            if p['bank']:
                if rng.randint(1, 6) == 6 or p['bank'] >= 2:
                    p['bank'] = 0; p['m'] += c['LOAN']; p['debt'] += c['LOAN_REPAY']
                else: p['bank'] += 1; continue
            snap = (p['m'], p['gp'], id(p['cof']), p['res'], meter); stats['turns'] = stats.get('turns',0)+1
            p['pos'] = (p['pos'] + rng.randint(1, 6) + rng.randint(1, 6)) % n
            s = sq[p['pos']]; S = p['s']; buf = 3
            if s['t'] == 'glider':
                if p['n'] == 'Student': p['gp'] += 1
                else: p['m'] -= 1
                p['pos'] = rng.choice([i for i, x in enumerate(sq) if x['t'] == 'glider']); s = sq[p['pos']]
            if s['t'] == 'card':
                k = rng.random()
                if k < 0.4: p['gp'] += 2 if rng.random() < 0.7 else 0          # quiz
                elif k < 0.7:                                                   # choice card
                    if S == 'exploit' or (S == 'rand' and rng.random() > p['g']):
                        p['m'] += 6; meter += 2
                    else: p['gp'] += 1; p['m'] -= 2
                else: p['m'] += rng.choice([-3, -2, 2, 3])
            elif s['t'] in ('pol', 'green') and s['owner'] is None:
                cost = s['cost'] - (3 if p['n'] == 'Tech' and s['t'] == 'green' else 0)
                if S == 'exploit': want = True
                elif S == 'green': want = s['t'] == 'green'
                else: want = rng.random() < ((1 - p['g']) + 0.15 if s['t'] == 'pol' else p['g'] + 0.2)
                if want and p['m'] >= cost + buf: p['m'] -= cost; s['owner'] = p
            elif s['t'] == 'green' and s['owner'] not in (None, p):
                if p['cof'] is None and p['m'] >= c['COFUND'] + buf and (S != 'rand' or rng.random() < 0.6):
                    p['m'] -= c['COFUND']; p['cof'] = s
            elif s['t'] == 'pol' and s['owner'] not in (None, p):
                if (S == 'green' or (S == 'rand' and p['g'] > 0.6 and rng.random() < 0.4)):
                    if p['n'] == 'Activist': s['lob'] = 2
                    elif p['m'] >= c['LOBBY'] + buf: p['m'] -= c['LOBBY']; s['lob'] = 1
            elif s['t'] == 'res' and p['res'] < 3:
                price = c['RES_COST'][p['res']] - (2 if p['n'] == 'Planner' else 0)
                if p['m'] >= price + buf + 4 and (S in ('planner_res',) or rng.random() < 0.35):
                    p['m'] -= price; p['res'] += 1
            # one conversion per turn
            mine = own(p, 'pol')
            if mine:
                late = r >= c['ROUNDS'] - 2
                go = (S == 'exploit' and late) or (S in ('rand', 'planner_res') and rng.random() < p['g'])
                cc = c['CONVERT']
                if go and p['m'] >= cc + buf:
                    x = mine[0]; p['m'] -= cc; x['t'] = 'green'; x['nat'] = False
                    if p['n'] == 'Industrialist': x['ind'] = 1
            if (p['m'], p['gp'], id(p['cof']), p['res'], meter) != snap or s['lob']: stats['act'] = stats.get('act',0)+1
        # end of round
        for s in sq:
            o = s['owner']
            if not o or s['closed'] or (flood is not None and s['d'] == flood): continue
            if s['t'] == 'pol' and not s['lob']: o['m'] += c['POL_INC'] if not o['bank'] else 0
            if s['t'] == 'green':
                gp = 0 if (s['nat'] and drought) else c['GREEN_GP'] + (1 if o['n'] == 'Farmer' and s['nat'] else 0)
                o['gp'] += gp
                if not o['bank']: o['m'] += c.get('GREEN_INC',0)
                if s.get('ind') and not o['bank']: o['m'] += 2
        for p in P:
            if p['cof'] and not p['cof']['closed']: p['gp'] += 1; p['cof']['owner']['gp'] += 1
            np_ = len(own(p, 'pol')); p['gp'] -= c['POL_GP'] * np_; p['polrounds'] += np_   # carbon tax in GP
            if p['n'] == 'Planner': pass
        flood = None; drought = False
        for s in sq: s['closed'] = max(0, s['closed'] - 1)
        pol = sum(1 for s in sq if s['owner'] and s['t'] == 'pol' and s['lob'] != 2)
        grn = sum(1 for s in sq if s['owner'] and s['t'] == 'green')
        meter += c['BASE'] + c['POL_METER'] * pol - c['GREEN_METER'] * grn
        for s in sq: s['lob'] = 0
        if r in c['CHECK'] and meter > c['CHECK'][r]:                 # interim targets 2030, 2040
            for p in P: p['gp'] -= c['CHECK_PEN'] * len(own(p, 'pol'))
        band = 1 if meter <= 10 else 2 if meter <= 25 else 3 if meter <= 40 else 4
        if rng.randint(1, 6) <= band:
            k = rng.choice(['flood', 'storm', 'heat', 'drought'])
            if k == 'flood':
                ds = list({s['d'] for s in sq if s['owner']})
                if ds:
                    flood = rng.choice(ds)
                    for s in sq:
                        if s['owner'] and s['d'] == flood: pay(s['owner'], band)
            elif k == 'storm':
                ow = [s for s in sq if s['owner']]
                if ow: s = rng.choice(ow); s['closed'] = 1; pay(s['owner'], 2 * band)
            elif k == 'heat':
                for p in P: pay(p, band)
            else: drought = True
    ok = meter <= 0
    for p in P:
        money = p['m'] - p['debt']
        if ok: p['score'] = p['gp'] + money / 5
        else: p['score'] = p['gp'] - c['FAIL_POL_GP'] * len(own(p, 'pol'))     # money worthless in a failed city
        p['tie'] = len(own(p, 'green'))
    nsq = sum(1 for s in sq if s['owner']) / len(P)
    return ok, meter, P, nsq, stats

def trial(c, N=3000, smart=None, smart_char=None, gm=0.6, seed=1):
    rng = random.Random(seed); succ = 0; wins = 0; fair = []; sqs = []; bank = []; cw = {k: [0, 0] for k in CHARS}; dc = []; acts = []
    for _ in range(N):
        k = rng.randint(4, 6); names = rng.sample(list(CHARS), k)
        if smart_char and smart_char not in names: names[0] = smart_char
        strats = ['rand'] * k; g = [min(.95, max(.05, rng.gauss(gm, .2))) for _ in names]
        si = None
        if smart: si = names.index(smart_char) if smart_char else 0; strats[si] = smart
        ok, meter, P, nsq, stt = play(c, names, strats, rng, g)
        succ += ok; sqs.append(nsq); bank.append(stt['bank']); dc.append(stt['dis_cost'] / k); acts.append(stt.get('act',0)/max(1,stt.get('turns',1)))
        best = max(P, key=lambda p: (p['score'], p['tie']))
        for p in P: cw[p['n']][1] += 1
        cw[best['n']][0] += 1
        if si is not None: wins += best is P[si]; fair.append(1 / k)
    out = f"success {succ/N:.0%}, squares/player {st.mean(sqs):.1f}, bankrupt/game {st.mean(bank):.2f}, disaster £M/player {st.mean(dc):.1f}, turns with an action {st.mean(acts):.0%}"
    if smart: out += f" | {smart}{'/'+smart_char if smart_char else ''} wins {wins/N:.0%} (fair {st.mean(fair):.0%})"
    return out, {k: v[0] / max(1, v[1]) for k, v in cw.items()}

if __name__ == '__main__':
    for gm in (0.35, 0.6, 0.8):
        o, cw = trial(C, gm=gm); print('rand gm', gm, o);
    print('chars', {k: f'{v:.0%}' for k, v in trial(C)[1].items()})
    for sm in ('exploit', 'green'):
        print(trial(C, smart=sm)[0])
    print(trial(C, smart='planner_res', smart_char='Planner')[0])
    print(trial(C, smart='exploit', smart_char='Industrialist')[0])
