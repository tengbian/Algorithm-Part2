import java.util.HashMap;

import edu.princeton.cs.algs4.Bag;
import edu.princeton.cs.algs4.Digraph;
import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdOut;

public class WordNet {
    private final HashMap<String, Bag<Integer>> nounsToIdBag;
    private final HashMap<Integer, String> idToSynset;
    private final Bag<String> allNouns;
    private final SAP wSap;
    
    // constructor takes the name of the two input files; Time: linearithmic (nlogn)
    public WordNet(String synsets, String hypernyms) {
        
        if (synsets == null || hypernyms == null)
            throw new IllegalArgumentException("synsets or hypernyms is null");
        
        nounsToIdBag = new HashMap<>();
        idToSynset = new HashMap<>();
        allNouns = new Bag<>();
        
        int vNum = 0;
        
        // Read the file "synsets", record the info in variables 
        In inSynset = new In(synsets);
        String line = inSynset.readLine();
        while (line != null) {
            String[] lineSplit = line.split(",");
            int id = Integer.parseInt(lineSplit[0]);
            String synset = lineSplit[1];
          
            String[] bagSplit = synset.split(" ");
            for (String noun: bagSplit)
                if (!nounsToIdBag.containsKey(noun)) {
                    allNouns.add(noun);
                    Bag<Integer> newIdBag = new Bag<>();
                    newIdBag.add(id);
                    nounsToIdBag.put(noun, newIdBag);
                }
                else
                  nounsToIdBag.get(noun).add(id);
            idToSynset.put(vNum, synset);
            vNum++;
            line = inSynset.readLine();
        }
        
        /* Read the file "hypernyms", map relations between nouns to a graph */
        Digraph wGraph = new Digraph(vNum);
        In inHypernyms = new In(hypernyms);
        line = inHypernyms.readLine();
        while (line != null) {  
            String[] lineSplit = line.split(",");
            int v = Integer.parseInt(lineSplit[0]);
            for (int i = 1; i < lineSplit.length; i++) {
                int w = Integer.parseInt(lineSplit[i]);
                wGraph.addEdge(v, w);
            }
            line = inHypernyms.readLine();
        }
        
        /* Check if rooted DAG */
        if (!topological(wGraph))   throw new IllegalArgumentException("Graph is not DAG");
        wSap = new SAP(wGraph);
    }
    
    // Check if it is rooted DAG
    private boolean topological(Digraph G) {
        boolean[] marked = new boolean[G.V()];
        int root = -1;
        for (int i = 0; i < G.V(); i++) {
            marked[i] = false;
            if (G.outdegree(i) == 0)
                if (root == -1) root = i;
                else    return false;
        }
        if (root == -1) return false;
        
        return checkDAGDFS(G, root, marked);
    }
    
    // DFS function to check DAG
    private boolean checkDAGDFS(Digraph G, int root, boolean[] marked) {
        marked[root] = true;
        for (int ri: G.adj(root)) {
            if (!marked[ri])    return false;
            checkDAGDFS(G, ri, marked);
        }
        return true;
    }

    
    // Return all WordNet nouns
    public Iterable<String> nouns() {
        return allNouns;
    }
    
    // Is the word a WordNet noun?
    public boolean isNoun(String word) {
        if (word == null)
            throw new IllegalArgumentException("The word is null");
        return nounsToIdBag.containsKey(word);
    }
    
    // Distance between nounA and nounB (defines below); Time: linear
    public int distance(String nounA, String nounB) {
        if (!isNoun(nounA) || !isNoun(nounB))
            throw new IllegalArgumentException("nounA or nounB is null");
        return wSap.length(nounsToIdBag.get(nounA), nounsToIdBag.get(nounB));
    }
    
    // A synset (second field of synsets.txt) that is the common ancestor of nounA and nounB
    // in a shortest ancestral path; Time: linear
    public String sap(String nounA, String nounB) {
        if (!isNoun(nounA) || !isNoun(nounB))
            throw new IllegalArgumentException("nounA or nounB is null");
         int ansester = wSap.ancestor(nounsToIdBag.get(nounA), nounsToIdBag.get(nounB));  
        return idToSynset.get(ansester);
    }

    // do unit testing of this class
    public static void main(String[] args) {
        WordNet myWordNet = new WordNet(args[0], args[1]);
        StdOut.println(myWordNet.sap("dash", "damage"));
    }
}