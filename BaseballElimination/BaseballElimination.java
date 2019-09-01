import java.util.Arrays;
import java.util.HashMap;
import edu.princeton.cs.algs4.Bag;
import edu.princeton.cs.algs4.FlowEdge;
import edu.princeton.cs.algs4.FlowNetwork;
import edu.princeton.cs.algs4.FordFulkerson;
import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdOut;

public class BaseballElimination {
    
    private final int n;
    private final String[] teamArray;
    private final HashMap<String, Integer> teamMap;
    private final int[] w;
    private final int[] l;
    private final int[] r;
    private final int[][] g;
    
    private Bag<String> teamInCut;
    
    public BaseballElimination(String filename) {        
        In in = new In(filename);
        
        String[] line = in.readLine().trim().split("\\s+");
        n = Integer.parseInt(line[0]);
        
        teamArray = new String[n];
        teamMap = new HashMap<String, Integer>();
        w = new int[n];
        l = new int[n];
        r = new int[n];
        g = new int[n][n];
        
        for (int i = 0; i < n; i++) {
            line = in.readLine().trim().split("\\s+");
            teamArray[i] = line[0];
            teamMap.put(teamArray[i], i);
            w[i] = Integer.parseInt(line[1]);
            l[i] = Integer.parseInt(line[2]);
            r[i] = Integer.parseInt(line[3]);
            for (int j = 0; j < n; j++)
                g[i][j] = Integer.parseInt(line[4+j]);
        }
    }
    
    
    // Construct FlowNetwork and use Ford-Fulkerson Algorithm
    public boolean isEliminated(String team) {
        if (!teamMap.containsKey(team)) throw new IllegalArgumentException("Not valid team");
        
        teamInCut = new Bag<String>();
        int teamNum = teamMap.get(team);
        
        // Corner case: n = 1 or 2
        if (n == 1) {
            teamInCut = null;
            return false;
        }
        for (int i = 0; i < n; i++)
            if (w[teamNum]+r[teamNum] < w[i]) {
                teamInCut.add(teamArray[i]);
                return true;
            }
        if (n == 2) {
            teamInCut = null;
            return false;
        }
        
        // If n >= 3
        int V = 2 + (n - 1) + (n-1)*(n-2)/2;
        FlowNetwork G = new FlowNetwork(V);
        
        int     index = 0;
        double restGames = 0;
        
        // The 1st layer of vertices: pairs
        int[][] pairIndex = new int[n][n];
        for (int i = 0; i < n; i++)
            for (int j = i + 1; j < n; j++)
                if (i != teamNum && j != teamNum) {
                    index++;
                    pairIndex[i][j] = index;
                    FlowEdge e = new FlowEdge(0, index, g[i][j]);
                    G.addEdge(e);
                    restGames += g[i][j];
                }
        
        // The 2nd layer of vertices: teams
        int[]   teamIndex = new int[n];
        for (int i = 0; i < n; i++)
            if (i != teamNum) {
                index++;
                teamIndex[i] = index;
            }
        for (int i = 0; i < n; i++) 
            for (int j = i+1; j < n; j++)
                if (i != teamNum && j != teamNum) {
                    FlowEdge e1 = new FlowEdge(pairIndex[i][j], teamIndex[i], Integer.MAX_VALUE);
                    FlowEdge e2 = new FlowEdge(pairIndex[i][j], teamIndex[j], Integer.MAX_VALUE);
                    G.addEdge(e1);
                    G.addEdge(e2);
                }
        
        // The sink
        index++;
        for (int i = 0; i < n; i++)
            if (i != teamNum) {
                FlowEdge e = new FlowEdge(teamIndex[i], index, w[teamNum]+r[teamNum]-w[i]);
                G.addEdge(e);
            }
        
        // Do Ford-Fulkerson Algorithm
        FordFulkerson f = new FordFulkerson(G, 0, index);
        if (Math.abs(f.value() - restGames) < 1e-5) {
            teamInCut = null;
            return false;
        }
        for (int i = 0; i < n; i++)
            if (i != teamNum) 
                if (f.inCut(teamIndex[i]))
                    teamInCut.add(teamArray[i]);
        return true;
    }
    
    public int numberOfTeams() {
        return n;
    }
    
    public Iterable<String> teams() {
        return Arrays.asList(teamArray);
    }
    
    public int wins(String team) {
        if (!teamMap.containsKey(team)) throw new IllegalArgumentException("Not valid team");
        return w[teamMap.get(team)];
    }
    
    public int losses(String team) {
        if (!teamMap.containsKey(team)) throw new IllegalArgumentException("Not valid team");
        return l[teamMap.get(team)];
    }
    
    public int remaining(String team) {
        if (!teamMap.containsKey(team)) throw new IllegalArgumentException("Not valid team");
        return r[teamMap.get(team)];
    }
    
    public int against(String team1, String team2) {
        if (!teamMap.containsKey(team1) || !teamMap.containsKey(team2)) 
            throw new IllegalArgumentException("Not valid team");
        return g[teamMap.get(team1)][teamMap.get(team2)];
    }
    
    private Bag<String> copyTeamInCut(Bag<String> teamInCut) {
        if (teamInCut == null) return null;
        Bag<String> result = new Bag<String>();
        for (String term: teamInCut)
            result.add(term);
        return result;
    }
    
    public Iterable<String> certificateOfElimination(String team) {
        if (!teamMap.containsKey(team)) throw new IllegalArgumentException("Not valid team");
        isEliminated(team);
        return copyTeamInCut(teamInCut);
    }
    
    public static void main(String[] args) {
        BaseballElimination division = new BaseballElimination(args[0]);
        for (String team : division.teams()) {
            if (division.isEliminated(team)) {
                StdOut.print(team + " is eliminated by the subset R = { ");
                for (String t : division.certificateOfElimination(team)) {
                    StdOut.print(t + " ");
                }
                StdOut.println("}");
            }
            else {
                StdOut.println(team + " is not eliminated");
            }
        }
    }
}
















