Colors = [a,b,c,d,e]

Länder = [A, B, C, D, E,F]

Borders = [
    (A,B),
    (A,C),
    (B,C),
    (B,D),
    (C,D),
    (D,E)
]


Koodierung = n ∈ colors [n,n,n,n,n,n]

lenKoodierung = 6
Pmut = 1/6


def FitnessFunc():
    1/conflicts() * 10 + colorsUsed()

# anzahl der farben und koflikte werden berücksichitg dabei werden Konflikte deutlich höher gewichtet

def conflicts():
    conflics = 0
    for b in Borders 
        if b[0] == b[1]:
            conflicts ++
    return conflicts

def colorsUsed():
    used = []
    for c in Länder:
        if c not in used:
            used.append(c)
    return len(colorsUsed)

def crossOver(parentA, parentB):
    child = []
    
    split = new Random().nextInteger(len(Koodierung-2))+1;

    for i in range len(Koodierung):
        child.append(i < split ? parentA[i] : parentB[i])
    
    return child

def mutate(Individual):
    for i in rage Individual:
        if Math.random() < Pmut:
            Individual[i] = random.pick(Colors)
    
    return Individual

