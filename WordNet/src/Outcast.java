
import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdOut;

public class Outcast {
    private final WordNet myWordnet;
    
    // constructor takes a WordNet object
    public Outcast(WordNet wordnet) {
        myWordnet = wordnet;
    }
    
    // given an array of WordNet nouns, return an outcast
    public String outcast(String[] nouns) {

        String noun = "";
        int distance;
        int minDistance = -1;
        
        for (String ni: nouns) {
            distance = 0;
            for (String nj: nouns) {
                distance += myWordnet.distance(ni, nj);
            }
            if (distance > minDistance) {
                minDistance = distance;
                noun = ni;
            }
        }
        return noun;
    }
    
    public static void main(String[] args) {
        WordNet wordNet = new WordNet(args[0], args[1]);
        Outcast outcast = new Outcast(wordNet);
        for (int t = 2; t < args.length; t++) {
            In in = new In(args[t]);
            String[] nouns = in.readAllStrings();
            StdOut.println(args[t] + ": " + outcast.outcast(nouns));
        }
    }
}