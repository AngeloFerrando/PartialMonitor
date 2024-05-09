
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Monitor {
    private Set<String> alphabet;
    private HashMap<String, State> states = new HashMap<>();
    private State currentState;
    private VerdictEvents currentVerdict;
    private String ltl;
    // private HashMap<String, Set<String>> events;
    public static String rv;
    private boolean canSayYes;
    private boolean canSayNo;

    public static void main(String[] args) throws IOException {
        System.out.println(new Monitor(args[0], args[1], args[2]));
    }

    private static class State {
        private String name;
        private HashMap<String, State> transitions = new HashMap<>();
        private VerdictEvents output;
        private State(String name, VerdictEvents output) {
            this.name = name;
            this.output = output;
        }
    }
    
    public static enum Verdict { True, False, Unknown, GiveUp };

    public static class VerdictEvents {
        private Verdict verdict;
        private Set<String> events;

        public VerdictEvents(Verdict verdict, Set<String> events) {
            this.verdict = verdict;
            this.events = events;
        }

        public Verdict getVerdict() {
            return this.verdict;
        }

        public Set<String> getEvents() {
            return this.events;
        }
    }

    public String getLtl() {
        return ltl;
    }

    public VerdictEvents getCurrentVerdict() {
        return this.currentVerdict;
    }

    public Monitor(String lamaconvPath, String ltl, String ltlAlphabet) throws IOException {
        this.ltl = "LTL=" + ltl.replace("and", "AND").replace("or", "OR").replace(" ", "");

        String command = "java -jar " + lamaconvPath + "/rltlconv.jar " + this.ltl + ",ALPHABET=" + ltlAlphabet + " --formula --nbas --min --nfas --dfas --min --moore";

        try(Scanner scanner = new Scanner(Runtime.getRuntime().exec(command).getInputStream()).useDelimiter("\n")) {
            while(scanner.hasNext()) {
                String mooreString = scanner.next();
                if(mooreString.contains("ALPHABET")) {
                    String[] alphabet = mooreString.split("=")[1].trim().replace("[", "").replace("]", "").split(",");
                    this.alphabet = new HashSet<>();
                    for(int i = 0; i < alphabet.length; i++) {
                        this.alphabet.add(alphabet[i].replace("\"", "").replace(" ", ""));
                    }
                } else if(mooreString.contains("STATES")) {
                    for(String state : mooreString.split("=")[1].split(",")) {
                        state = state.trim().replace("[", "").replace("]", "");
                        String name = state.split(":")[0];
                        String verdictStr = state.split(":")[1];
                        VerdictEvents output = new VerdictEvents(verdictStr.equals("true") ? Verdict.True : (verdictStr.equals("false") ? Verdict.False : Verdict.Unknown), new HashSet<>());
                        if(output.verdict == Verdict.True) {
                            this.canSayYes = true;
                        } else if(output.verdict == Verdict.False) {
                            this.canSayNo = true;
                        }
                        this.states.put(name, new State(name, output));
                    }
                } else if(mooreString.contains("START")) {
                    this.currentState = states.get(mooreString.split("=")[1].trim());
                } else if(mooreString.contains("DELTA")) {
                    String[] args = mooreString.substring(mooreString.indexOf("(")+1, mooreString.indexOf(")")).split(",");
                    this.states.get(args[0].trim()).transitions.put(args[1].trim().replace("\"", ""), states.get(mooreString.split("=")[1].trim()));
                }
            }
        }
        this.itIsOkToGiveUp();
        this.itIsOkToBePickyOnEvents();
        this.currentVerdict = this.currentState.output;
    }

    public boolean isPureSafety() {
        return this.canSayNo && !this.canSayYes;
    }

    public boolean isPureCoSafety() {
        return !this.canSayNo && this.canSayYes;
    }

    @Override
    public String toString() {
        String res = "MOORE {\n";
        res += "\tALPHABET = [" + String.join(", ", this.alphabet) + "]\n";
        res += "\tSTATES = [";
        boolean first = true;
        for(Map.Entry<String, State> entry : this.states.entrySet()) {
            if(first) { first = false; }
            else { res += ", "; }
            res += entry.getKey() + ":" + (entry.getValue().output.verdict == Verdict.True ? "true" : (entry.getValue().output.verdict == Verdict.False ? "false" : (entry.getValue().output.verdict == Verdict.Unknown ? "?" : "x")));
            res += ":{" + String.join(", ", entry.getValue().output.events) + "}";
        }
        res += "]\n";
        res += "\tSTART = " + this.currentState.name + "\n";
        for(Map.Entry<String, State> entry1 : this.states.entrySet()) {
            for(Map.Entry<String, State> entry2 : entry1.getValue().transitions.entrySet()) {
                res += "\tDELTA(" + entry1.getKey() + ", " + entry2.getKey() + ") = " + entry2.getValue().name + "\n";
            }
        }
        res += "}";
        return res;
    }

    private void itIsOkToGiveUp() {
        for(Map.Entry<String, State> entry : this.states.entrySet()) {
            if(entry.getValue().output.verdict == Verdict.Unknown && !canReachFinalVerdictState(entry.getValue())) {
                entry.getValue().output.verdict = Verdict.GiveUp;
            }
        }
    }

    private boolean canReachFinalVerdictState(State state) {
        Set<String> visited = new HashSet<String>();
        return canReachFinalVerdictStateAux(state, visited);
    }

    private boolean canReachFinalVerdictStateAux(State state, Set<String> visited) {
        if(visited.contains(state.name)) {
            return false;
        } else {
            visited.add(state.name);
            if(state.output.verdict == Verdict.True || state.output.verdict == Verdict.False) {
                return true;
            }
            for(Map.Entry<String, State> entry : state.transitions.entrySet()) {
                if(canReachFinalVerdictStateAux(entry.getValue(), visited)) {
                    return true;
                }
            }
            return false;
        }
    }

    private void itIsOkToBePickyOnEvents() {
        List<String> states = new LinkedList<>();
        // this.events = new HashMap<>();
        for(Map.Entry<String, State> entry : this.states.entrySet()) {
            if(entry.getValue().output.verdict != Verdict.Unknown) {
                states.add(entry.getKey());
                // this.states.get(entry.getKey()).output.events = new HashSet<>();
                // this.events.put(entry.getKey(), new HashSet<>());
            }
        }
        Set<String> alreadySeenStates = new HashSet<>();
        while(!states.isEmpty()) {
            String state = states.remove(0);
            alreadySeenStates.add(state);
            HashMap<String, Set<String>> preSet = this.pre(this.states.get(state));
            for (Entry<String, Set<String>> pS : preSet.entrySet()) {
                if (!alreadySeenStates.contains(pS.getKey())) {
                    states.add(pS.getKey());
                }
                this.states.get(pS.getKey()).output.events.addAll(pS.getValue());
                // if (!events.containsKey(pS.getKey())) {
                //     this.events.put(pS.getKey(), new HashSet<>());
                // }
                // this.events.get(pS.getKey()).addAll(pS.getValue());
            }
        }
    }

    private HashMap<String, Set<String>> pre(State state) {
        HashMap<String, Set<String>> preSet = new HashMap<>();
        for (Map.Entry<String, State> s : this.states.entrySet()) {
            if (s.getKey().equals(state.name)) continue;
            for (Map.Entry<String, State> t : s.getValue().transitions.entrySet()) {
                if (t.getValue().name.equals(state.name)) {
                    if (!preSet.containsKey(s.getKey())) {
                        preSet.put(s.getKey(), new HashSet<>());
                    }
                    preSet.get(s.getKey()).add(t.getKey());
                }
            }
        }
        return preSet;
    }

    public VerdictEvents next(String event) {
        event = event.toLowerCase();
        if(currentState.transitions.containsKey(event)) {
            currentState = currentState.transitions.get(event);
            currentVerdict = currentState.output;
            return currentVerdict;
        } else if(currentState.transitions.containsKey("?")) {
            currentState = currentState.transitions.get("?");
            currentVerdict = currentState.output;
            return currentVerdict;
        }
        return currentVerdict;
    }
}
