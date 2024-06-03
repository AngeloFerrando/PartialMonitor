import sys
import string
import spot

def check(f):
    global ok
    if f._is(spot.op_And) or f._is(spot.op_Implies) or f._is(spot.op_Not) or f._is(spot.op_Xor) or f._is(spot.op_W) or f._is(spot.op_M) or f._is(spot.op_R) or f._is(spot.op_ff) or f._is(spot.op_tt) or f._is(spot.op_Equiv):
        ok = False
    return False

def main(args):
    global ok
    n = int(args[1])
    # min_size_ltl = int(args[2])
    # max_size_ltl = int(args[3])
    ap = list(string.ascii_lowercase)
    with open('props.txt', 'w') as file:
        count = 0
        for ltl in spot.randltl(list(ap)):
            if count >= n: break
            ok = True
            ltl.traverse(check)
            if not ok: continue
            file.write(str(ltl).replace('|', '||').replace('&', '&&') + '; ' + str(spot.atomic_prop_collect(ltl)).replace('"', '').replace('{', '[').replace('}', ']') + '\n')
            count += 1

if __name__ == '__main__':
    main(sys.argv)