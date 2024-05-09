import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class Experiments {
    public static void main(String[] args) throws NumberFormatException, IOException {
        experiment(args[0], args[1], Integer.valueOf(args[2]), Integer.valueOf(args[3]));
    }

    public static void experiment(String lamaconvPath, String ltlFile, int traceLength, int nRuns) throws IOException {
        long totalAmount = 0;
        long totalAmountSafety = 0;
        long totalAmountCoSafety = 0;
        long partialAmount = 0;
        long partialAmountSafety = 0;
        long partialAmountCoSafety = 0;
        Random random = new Random();
        try (BufferedReader br = new BufferedReader(new FileReader(ltlFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                String ltl = line.split(";")[0];
                System.out.println(ltl);
                String ltlAlphabet = line.split(";")[1].replace(" ", "");
                for (int k = 0; k < nRuns; k++) {
                    Monitor monitor = new Monitor(lamaconvPath, ltl, ltlAlphabet);
                    // System.out.print(monitor);
                    Set<String> alphabet = monitor.getCurrentVerdict().getEvents();
                    if (k == 0 && monitor.isPureSafety()) {
                        System.out.println("pure SAFETY");
                    }
                    else if (k == 0 && monitor.isPureCoSafety()) {
                        System.out.println("pure CO-SAFETY");
                    }
                    else {
                        break;
                    }
                    for (int i = 0; i < traceLength; i++) {
                        Monitor.VerdictEvents currentVerdict = monitor.getCurrentVerdict();
                        
                        if (currentVerdict.getVerdict() != Monitor.Verdict.Unknown) break;
                        
                        Set<String> eventsOfInterest = currentVerdict.getEvents();
                        
                        int randomSize = random.nextInt(alphabet.size()) + 1;
                        for (String event : alphabet) {
                            if (randomSize == 0) break;
                            totalAmount += event.length() * 2;
                            if (monitor.isPureSafety()) {
                                totalAmountSafety += event.length() * 2;
                            }
                            if (monitor.isPureCoSafety()) {
                                totalAmountCoSafety += event.length() * 2;
                            }
                            if (eventsOfInterest.contains(event)) {
                                monitor.next(event);
                                partialAmount += event.length() * 2;
                                if (monitor.isPureSafety()) {
                                    partialAmountSafety += event.length() * 2;
                                }
                                if (monitor.isPureCoSafety()) {
                                    partialAmountCoSafety += event.length() * 2;
                                }
                            }
                            randomSize--;
                        }
                    }
                }
            }
        }
        System.out.println("Total amount: " + (totalAmount));
        System.out.println("Total amount [Safety]: " + (totalAmountSafety));
        System.out.println("Total amount [Co-Safety]: " + (totalAmountCoSafety));
        System.out.println("Partial amount: " + (partialAmount));
        System.out.println("Partial amount [Safety]: " + (partialAmountSafety));
        System.out.println("Partial amount [Co-Safety]: " + (partialAmountCoSafety));
    }
}
