import java.util.HashMap;

import edu.princeton.cs.algs4.Bag;
import edu.princeton.cs.algs4.Digraph;
import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.Queue;
import edu.princeton.cs.algs4.StdOut;

public class SAP {
// all methods Space & Time: O( E + V )

    private final Digraph sapGraph;
    
    // for simplification in length and ancestor calculation
    private class Result {
        int sLength = -1;
        int sAncestor = -1;
    }

    // constructor takes a digraph (not necessarily a DAG)
    public SAP(final Digraph wordnet) {
        if (wordnet == null)
            throw new IllegalArgumentException("wordnet is null");
        this.sapGraph = graphCopy(wordnet);
    }
    
    // defensive copy
    private Digraph graphCopy(Digraph G) {
        Digraph newG = new Digraph(G.V());
        for (int v = 0; v < G.V(); v++)
            for (int vc: G.adj(v))
                newG.addEdge(v, vc);
        return newG;
    }
    
    /* ********************** For length ********************* */
    
    // length of shortest ancestral path between v and w; return -1 if no such path
    public int length(int v, int w) {
        Bag<Integer> vBag = new Bag<>();
        Bag<Integer> wBag = new Bag<>();
        vBag.add(v);
        wBag.add(w);
        return length(vBag, wBag);
    }
    
    // length of shortest ancestral path between any vertex in v and any vertex in w; return -1 if no such path
    public int length(Iterable<Integer> v, Iterable<Integer> w) {
        checkVW(v, w);
        Result myResult = new Result();
        runSAP(v, w, myResult);
        return myResult.sLength;
    }
    
//    // illegal input of length
//    public <Item1, Item2> int length(Item1 v, Item2 w) {
//        throw new IllegalArgumentException("Other cases for v and w");
//    }
//    
    /* ********************** For ancestor ********************* */
    
    // a common ancestor of v and w that participates in a shortest ancestral path; return -1 if no such path
    public int ancestor(int  v, int w) {
        Bag<Integer> vBag = new Bag<>();
        Bag<Integer> wBag = new Bag<>();
        vBag.add(v);
        wBag.add(w);
        return ancestor(vBag, wBag);
    }
    
    // a common ancestor that participates in shortes t ancestral path; return -1 if no such path
    public int ancestor(Iterable<Integer> v, Iterable<Integer> w) {
        checkVW(v, w);
        Result myResult = new Result();
        runSAP(v, w, myResult);
        return myResult.sAncestor;
    }
    
//    // illegal input of ancestor
//    public <Item1, Item2> int ancestor(Item1 v, Item2 w) {
//        throw new IllegalArgumentException("Other cases for v and w");
//    }
    
    /* ********** Helper Private Functions ********** */

    private void checkVW(Iterable<Integer> v, Iterable<Integer> w) {
        if (v == null || w == null)
            throw new IllegalArgumentException("v or w is null");
        for (Integer vi: v)
            if (vi == null || vi < 0  || vi >= sapGraph.V())  throw new IllegalArgumentException("v or w is null or exceeds legal range");
        for (Integer wi: w)
            if (wi == null || wi < 0  || wi >= sapGraph.V())  throw new IllegalArgumentException("v or w is null or exceeds legal range");
    }
    
    private void runSAP(Iterable<Integer> v, Iterable<Integer> w, Result myResult) {
        Queue<Integer> vQueue = new Queue<>();
        HashMap<Integer, Integer> vHash = new HashMap<>();
        vBFS(v, vQueue, vHash); 
        
        // do BFS for w, return distance when find the same ancestor
        Queue<Integer> wQueue = new Queue<>();
        HashMap<Integer, Integer> wHash = new HashMap<>();
        wBFS(w, wQueue, wHash, vHash, myResult);
    }

    
    // BFS for v to find the distance to different ancestors
    private void vBFS(Iterable<Integer> v, Queue<Integer> vQueue, HashMap<Integer, Integer> vHash) {
        for (int vi: v) {
            vQueue.enqueue(vi);
            vHash.put(vi, 0);
        }
        while (!vQueue.isEmpty()) {
            int vertex = vQueue.dequeue();
            for (int vertexChild: sapGraph.adj(vertex)) {
                if (!vHash.containsKey(vertexChild)) {
                    vQueue.enqueue(vertexChild);
                    vHash.put(vertexChild, vHash.get(vertex) + 1);
                }
            }            
        }  
    }
    
    // DFS for w to find the distance from ancestors, pick the one with least distance from w
    private void wBFS(Iterable<Integer> w, Queue<Integer> wQueue, HashMap<Integer, Integer> wHash, HashMap<Integer, Integer> vHash, Result myResult) {
        myResult.sLength = sapGraph.V() + 1;
        
        for (int wi: w) {
            wQueue.enqueue(wi);
            wHash.put(wi, 0);
        }
        while (!wQueue.isEmpty()) {
            int vertex = wQueue.dequeue();
            if (vHash.containsKey(vertex)) {
                if (vHash.get(vertex) + wHash.get(vertex) < myResult.sLength) {
                    myResult.sLength = vHash.get(vertex) + wHash.get(vertex);
                    myResult.sAncestor = vertex;
                }
            }
            for (int vertexChild: sapGraph.adj(vertex)) {
                if (!wHash.containsKey(vertexChild)) {
                    wQueue.enqueue(vertexChild);
                    wHash.put(vertexChild, wHash.get(vertex) + 1);
                }
            }            
        }
        if (myResult.sLength > sapGraph.V())
            myResult.sLength = -1;
    }
    
    
    // do unit testing of this class
    public static void main(String[] args) {
        In in = new In("wordnet/digraph2.txt");
        Digraph G = new Digraph(in);
        SAP sap = new SAP(G);
        StdOut.printf("length = %d, ancestor = %d\n", sap.length(1, 5), sap.ancestor(1, 5));
//        while (!StdIn.isEmpty()) {
//            int v = StdIn.readInt();
//            int w = StdIn.readInt();
//            int length = sap.length(v, w);
//            int ancestor = sap.ancestor(v, w);
//            StdOut.printf("length = %d, ancestor = %d\n", length, ancestor);
//        }
    } 
    
    
}



























