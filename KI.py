# State: (Orks, Elven, Pferd) die daten reiche für eine Seite des Flusses
# Start:
# S = (3,3,1)
#End:
#E = (0,0,0)
#Aktionen:
#Pferd nimmt leute rüber
#(0,1,1) -> ( 0,-1,-1)
#(1,1,1) -> (-1,-1,-1)
#(0,2,1) -> ( 0,-2,-1)
#(2,0,1) -> (-2, 0,-1)
#(1,0,1) -> (-1, 0,-1)
#
#Pferd nimmt leute zurück
#(0,0,0) -> ( 0,+1,+1)
#(0,0,0) -> (+1,+1,+1)
#(0,0,0) -> ( 0,+2,+1)
#(0,0,0) -> (+2, 0,+1)
#(0,0,0) -> (+1, 0,+1)
#
#
#Ziel test:
#(o,e,p) -> o == 0 && e == 0
#
#Validität:
#(o,e,p) -> o <= e && 3-o <= 3-e 
#
#(3,3,1)

#Num Orcs
no = 3
#Num Elves
ne = 3
def isValid(s):
    if s[0] < 0 or s[1] < 0 or s[2] < 0:
        # cannot be negative
        return False
    if s[2] > 1 or s[0] > no or s[1] > ne:
        #nich 2 Pferde
        return False
    # Valid if orks auf der Seite <= elbs oder elbs == 0
    if (s[0] <= s[1] or s[1] == 0) and (no-s[0] <= ne-s[1] or ne-s[1] == 0):
        return True
    else:
        return False

def toStr(s):
    return "("+str(s[0])+","+str(s[1])+","+str(s[2])+")"

def orcs():
    actions = []
    actions.append(lambda s: (s[0],  s[1]-1, s[2]-1))
    actions.append(lambda s: (s[0]-1,s[1]-1, s[2]-1))
    actions.append(lambda s: (s[0],  s[1]-2, s[2]-1))
    actions.append(lambda s: (s[0]-2,  s[1], s[2]-1))
    actions.append(lambda s: (s[0]-1,  s[1], s[2]-1))

    actions.append(lambda s: (s[0],  s[1]+1, s[2]+1))
    actions.append(lambda s: (s[0]+1,s[1]+1, s[2]+1))
    actions.append(lambda s: (s[0],  s[1]+2, s[2]+1))
    actions.append(lambda s: (s[0]+2,  s[1], s[2]+1))
    actions.append(lambda s: (s[0]+1,  s[1], s[2]+1))

    start = (no,ne,1)
    end = (0,0,0)

    totalStates = [start]
    currentStates = []
    for i in range(20):
        for state in totalStates:
            for action in actions:
                newState = action(state)
                if not isValid(newState):
                    continue
                if newState not in totalStates:
                    print(toStr(state) + " " + toStr(newState))
                    # visualized by https://csacademy.com/app/graph_editor/
                    currentStates.append(newState)
            for created in currentStates:
                if created not in totalStates:
                    totalStates.append(created)
            currentState = []
        if end in totalStates:
            break
        #print(len(totalStates))
    #print(totalStates)
orcs()


