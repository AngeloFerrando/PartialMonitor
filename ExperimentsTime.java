import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

public class ExperimentsTime {
    public static void main(String[] args) throws NumberFormatException, IOException {
        experiment(args[0], args[1]);
    }

    private static class Time { 
        private long min; 
        private long max; 
        private IncrementalAverage avg; 
        private Time(long min, long max, IncrementalAverage avg) {
            this.min = min;
            this.max = max;
            this.avg = avg;
        }
        @Override
        public String toString(){
            return min + ", " + max + ", " + avg.total + " [seconds]";
        }
    }

    public static void experiment(String lamaconvPath, String ltlFile) throws IOException {
        Map<Integer, Time> time = new TreeMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(ltlFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                String ltl = line.split(";")[0];
                int length = 
                    countSubstring(ltl, "X") +
                    countSubstring(ltl, "U") +
                    countSubstring(ltl, "G") + 
                    countSubstring(ltl, "F") +
                    countSubstring(ltl, "R") +
                    countSubstring(ltl, "->") +
                    countSubstring(ltl, "&&") +
                    countSubstring(ltl, "||");
                System.out.println(ltl + "[" + length + "]");
                String ltlAlphabet = line.split(";")[1].replace(" ", "");
                long startTime = System.nanoTime();
                Monitor monitor = new Monitor(lamaconvPath, ltl, ltlAlphabet, true);
                long endTime = System.nanoTime();
                Time t = null;
                if(!time.containsKey(length)) {
                    t = new Time(Long.MAX_VALUE, Long.MIN_VALUE, new IncrementalAverage());
                    time.put(length, t);
                } else {
                    t = time.get(length);
                }
                long duration = (endTime - startTime) / 1000000000;
                if(duration < t.min) {
                    t.min = duration;
                }
                if(duration > t.max) {
                    t.max = duration;
                }
                t.avg.addNumber(duration);
            }
        }
        for (Map.Entry<Integer, Time> entry : time.entrySet()) {
            System.out.println(entry.getKey() + " = " + entry.getValue());
        }
    }

    public static int countSubstring(String input, String substring) {
        int count = 0;
        int lastIndex = 0;

        while ((lastIndex = input.indexOf(substring, lastIndex)) != -1) {
            count++;
            lastIndex = lastIndex+1;
            if(lastIndex >= input.length()) break;
        }

        return count;
    }

    public static class IncrementalAverage {
        private int count;
        private long total;
    
        public IncrementalAverage() {
            count = 0;
            total = 0;
        }
    
        public void addNumber(long num) {
            total += num;
            count++;
        }
    
        public double getAverage() {
            if (count == 0) {
                return 0.0;
            }
            return total / count;
        }
    }
}
