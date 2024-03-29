import java.time.LocalTime;
import java.util.*;

public class Algorithms {

    private MpkGraph graph;

    public Algorithms() {
        graph = new MpkGraph();
    }

    public void initializeGraph(){
        ReadFile rf = new ReadFile();
        ArrayList<String> csvRows = rf.readConnectionGraphFile();
        for(String row: csvRows){
            String[] dividedRow = row.split(",");
            graph.addEdge(dividedRow[5], dividedRow[6], new MpkLineData(dividedRow[2], dividedRow[3], dividedRow[4],
                    Float.parseFloat(dividedRow[7]), Float.parseFloat(dividedRow[8]),
                    Float.parseFloat(dividedRow[9]), Float.parseFloat(dividedRow[10])));
        }
    }

    public HashMap<String, ArrayList<String>> aStarForPath(String startStop, ArrayList<String> path, String currentTime){
        HashMap<String, ArrayList<String>> result = new HashMap<>();
        for(String stop: path){
            try {
                result.put(stop, (ArrayList<String>) aStar(startStop, stop, false, currentTime, false));
            }
            catch(NullPointerException e){
                System.out.println("Nie udało się znaleźć trasy od: " + startStop + " do: " + stop);
            }
        }
        return result;
    }

    public List<String> aStar(String start, String end, boolean avoidChanges, String arrivalTimeToStartStop, boolean printCost){
        LocalTime startTime = LocalTime.parse(arrivalTimeToStartStop);
        LocalTime currentTime = startTime;
        String currentLine = "";

        List<String> open = new ArrayList<>();
        List<String> closed = new ArrayList<>();

        Map<String, Integer> f = new HashMap<>();
        Map<String, Integer> g = new HashMap<>();
        Map<String, Integer> h = new HashMap<>();
        Map<String, String> earlierStops = new HashMap<>();

        open.add(start);
        g.put(start, 0);
        h.put(start, 0);
        f.put(start, 0);
        earlierStops.put(start, null);


        while (!open.isEmpty()) {
            String current = null;
            int minCost = Integer.MAX_VALUE;

            for (String testStop : open) {
                if (f.get(testStop) >= 0 &&  f.get(testStop) < minCost) {
                    current = testStop;
                    minCost = f.get(testStop);
                }
            }

            if (current.equals(end)) {
                if(printCost)
                    System.err.println("Wartość funkcji kosztu dla przystanku docelowego: " + f.get(end));
                List<String> path = new ArrayList<>();
                while (current != null) {
                    path.add(current);
                    current = earlierStops.get(current);
                }
                Collections.reverse(path);
                return path;
            }

            open.remove(current);
            closed.add(current);

            if(f.get(earlierStops.get(current)) != null)
                currentTime = startTime.plusSeconds(f.get(earlierStops.get(current)));

            for (String nextStop : graph.getNeighbors(current)) {
                Map<String, Object> neighbourTransportData;
                neighbourTransportData = graph.getTravelDataBetweenNeighbors(current, nextStop, currentTime,
                        avoidChanges, currentLine);

                if(avoidChanges)
                    currentLine = (String)neighbourTransportData.get("bestLineName");

                int gValueCurrentNextStop = (int) neighbourTransportData.get("bestTravelTime");
                if (!open.contains(nextStop) && !closed.contains(nextStop)) {
                    open.add(nextStop);
                    h.put(nextStop, ((LocalTime)neighbourTransportData.get("bestDepartureTime")).toSecondOfDay() - currentTime.toSecondOfDay());
                    g.put(nextStop, g.get(current) + gValueCurrentNextStop);
                    f.put(nextStop, g.get(nextStop) + h.get(nextStop));
                    earlierStops.put(nextStop, current);
                }
                else{
                    if(g.get(nextStop) > g.get(current) + gValueCurrentNextStop && g.get(current) + gValueCurrentNextStop >= 0){
                        g.put(nextStop, g.get(current) + gValueCurrentNextStop);
                        f.put(nextStop, g.get(nextStop) + h.get(nextStop));
                        if(closed.contains(nextStop)){
                            open.add(nextStop);
                            closed.remove(nextStop);
                        }
                    }
                }
            }
        }

        return null;
    }


public ArrayList<String> getBestConnections(String startStop, String endStop, char criteria, String startTime){
    long startProcessingTime = System.nanoTime();
    LocalTime currentTime = LocalTime.parse(startTime);
    ArrayList<String> bestConnections;

    switch (criteria){
        case 't':
            List<String> shortestPathT = aStar(startStop, endStop, false, startTime, true);
            bestConnections =  graph.getQuickConnections(currentTime, (ArrayList<String>) shortestPathT);
            break;
        case 'p':
            List<String> shortestPathP = aStar(startStop, endStop, true, startTime, true);
            bestConnections =  graph.getConnectionsWithoutChanges(currentTime, (ArrayList<String>) shortestPathP);
            break;
        default:
            System.out.println("Podano złe kryterium");
            bestConnections =  null;
            break;
    }
    long endProcessingTime = System.nanoTime();
    System.err.println("Czas obliczeń w sekundach: " + ((endProcessingTime - startProcessingTime)/ 1_000_000_000.0f)); //nanoseconds to seconds
    return bestConnections;
}

    public int getHeuristic(String start, String end) {
        double[] xLocation = graph.getAverageStopLocation(start);
        double[] yLocation = graph.getAverageStopLocation(end);
        return (int)CountDistance.distance(xLocation[0], xLocation[1], yLocation[0], yLocation[1]) * 10;
    }


    public List<String> generateRandomSolution(String startStop, List<String> stopsToVisit, String currentTime, boolean avoidChanges) {
        List<String> randomSolution = new ArrayList<>();
        randomSolution.add(startStop);
        stopsToVisit.add(0, startStop);
        for(int i=0; i < stopsToVisit.size() - 1; i++) {
            randomSolution.remove(stopsToVisit.get(i));
            randomSolution.addAll(aStar(stopsToVisit.get(i), stopsToVisit.get(i+1), avoidChanges, currentTime, false));
        }
        return randomSolution;
    }


    public List<String> knoxAlgorithm(String startStop, String route, String startTime, boolean avoidChanges) {
        int k = 0;
        final int STEP_LIMIT = 8;
        final int OP_LIMIT = 4;
        LocalTime currentTime = LocalTime.parse(startTime);
        ArrayList<String> stopsToVisit = new ArrayList<>(Arrays.asList(route.split(";")));
        stopsToVisit.add(startStop); //to remember that we must return to startStop
        List<String> s = generateRandomSolution(startStop, stopsToVisit, startTime, avoidChanges);
        List<String> sStar = new ArrayList<>(s);
        Set<List<String>> tabuList = new HashSet<>(); //set because it doesn't have duplicates

        while (k < STEP_LIMIT) {
            int i = 0;
            while (i < OP_LIMIT) {
                HashMap<String, ArrayList<String>> neighborhood = aStarForPath(startStop, (ArrayList<String>) s, currentTime.toString());
                List<String> bestNeighbor = null;
                double bestNeighborDistance = Double.POSITIVE_INFINITY;

                for (List<String> neighbor : neighborhood.values()) {
                    if (!tabuList.contains(neighbor)) {
                        double neighborDistance = graph.getAverageDistanceBetweenManyStops(neighbor);
                        if (neighborDistance < bestNeighborDistance) {
                            bestNeighbor = neighbor;
                            bestNeighborDistance = neighborDistance;
                        }
                    }
                }
                tabuList.add(bestNeighbor);

                if (bestNeighborDistance < graph.getAverageDistanceBetweenManyStops(s)) {
                    s = bestNeighbor;
                }
                i++;
            }
            k++;

            int sTotalDistance = graph.getAverageDistanceBetweenManyStops(s);
            if (sTotalDistance > 0 && sTotalDistance < graph.getAverageDistanceBetweenManyStops(sStar)) {
                sStar = new ArrayList<>(s);
            }
        }
        return sStar;
    }

    public ArrayList<String> getConnectionViaStops(String startStop, String route, char criteria, String startTime){
        long startProcessingTime = System.nanoTime();
        ArrayList<String> bestConnections;

        switch (criteria){
            case 't':
                List<String> shortestPathT = knoxAlgorithm(startStop, route, startTime, false);
                bestConnections =  graph.getQuickConnections(LocalTime.parse(startTime), (ArrayList<String>) shortestPathT);
                break;
            case 'p':
                List<String> shortestPathP = knoxAlgorithm(startStop, route, startTime, true);
                bestConnections =  graph.getConnectionsWithoutChanges(LocalTime.parse(startTime), (ArrayList<String>) shortestPathP);
                break;
            default:
                System.out.println("Podano złe kryterium");
                bestConnections =  null;
                break;
        }
        long endProcessingTime = System.nanoTime();
        System.err.println("Czas obliczeń w sekundach: " + ((endProcessingTime - startProcessingTime)/ 1_000_000_000.0f)); //nanoseconds to seconds
        return bestConnections;
    }
}
