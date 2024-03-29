import java.time.Duration;
import java.time.LocalTime;
import java.util.*;

public class MpkGraph {
    private Map<String, Map<String, Map<String, List<MpkLineData>>>> graphMap;

    public MpkGraph(){
        graphMap = new HashMap<>();
    }

    public void addEdge(String source, String destination, MpkLineData edgeData) {
        graphMap.computeIfAbsent(source, k -> new HashMap<>())
                .computeIfAbsent(destination, k -> new HashMap<>())
                .computeIfAbsent(edgeData.getLineName(), k -> new ArrayList<>())
                .add(edgeData);
    }

    public int getAverageTravelTimeBetweenStops(String source, String destination) {
        int distancesSum = 0;
        int distancesNum = 0;
        Map<String, Map<String, List<MpkLineData>>> destinationMap = graphMap.get(source);
        Map<String, List<MpkLineData>> edgeDataMap = destinationMap.get(destination);

        for (Map.Entry<String, List<MpkLineData>> innerEntry : edgeDataMap.entrySet()) {
            List<MpkLineData> mpkLineDataList = innerEntry.getValue();
            for (MpkLineData currentEdge : mpkLineDataList) {
                distancesSum += currentEdge.getTravelTime();
                distancesNum++;
            }
        }

        return distancesSum/distancesNum;
    }

    public int getAverageDistanceBetweenManyStops(List<String> stops){
        int totalTime = 0;
        for(int it=0; it < stops.size() - 1; ++it){
            try {
                totalTime += getAverageDistanceBetweenStops(stops.get(it), stops.get(it+1));
            }
            catch(NullPointerException ne){
                System.out.println("Nie udało się znaleźć czasu między: " + stops.get(it) + " a: " + stops.get(it+1));
            }
        }
        return totalTime;
    }

    private void checkIfExistsAndPutToListStringElem(ArrayList list, String elem){
        if(!list.contains(elem)){
            list.add(elem);
        }
    }

    public ArrayList<String> getAllVertices(){
        ArrayList<String> nodeList = new ArrayList<>();
        for (Map.Entry<String, Map<String, Map<String, List<MpkLineData>>>> sources : graphMap.entrySet()){
            checkIfExistsAndPutToListStringElem(nodeList, sources.getKey());
            for (Map.Entry<String, Map<String, Map<String, List<MpkLineData>>>> destinations : graphMap.entrySet()){
                checkIfExistsAndPutToListStringElem(nodeList, destinations.getKey());
            }
        }
        return nodeList;
    }

    public ArrayList<String> getNeighbors(String currentStop){
        ArrayList<String> neighbours = new ArrayList<>();
        for (Map.Entry<String, Map<String, Map<String, List<MpkLineData>>>> entry : graphMap.entrySet()) {
            if(entry.getKey().equals(currentStop)){
                Map<String, Map<String, List<MpkLineData>>> destinationMap = entry.getValue();
                for(Map.Entry<String, Map<String, List<MpkLineData>>> destEntry : destinationMap.entrySet()){
                    neighbours.add(destEntry.getKey());
                }
            }
        }
        return neighbours;
    }

    public HashMap<String, ArrayList<String>> getNeighborsForManyStops(List<String> stops) {
        HashMap<String, ArrayList<String>> neighborhood = new HashMap<>();
        for (String stop : stops) {
            if (graphMap.containsKey(stop)) {
                Map<String, Map<String, List<MpkLineData>>> connections = graphMap.get(stop);
                for (String destination : connections.keySet()) {
                    if (!stops.contains(destination)) {
                        // Dodaj przystanek docelowy do sąsiedztwa
                        if (!neighborhood.containsKey(destination)) {
                            neighborhood.put(destination, new ArrayList<>());
                        }
                        neighborhood.get(destination).add(stop);
                    }
                }
            }
        }
        return neighborhood;
    }


    public Map<String, Object> getTravelDataBetweenNeighbors(String startStop, String endStop, LocalTime startTime,
                                                             boolean avoidChanges, String line){
        int bestTravelTime = Integer.MAX_VALUE;
        String bestLineName = "";
        LocalTime bestArrivalTime = startTime;
        LocalTime bestDepartureTime = startTime;
        int smallestWaitingTime = Integer.MAX_VALUE;

        Map<String, Map<String, List<MpkLineData>>> destinationMap = graphMap.get(startStop);
        Map<String, List<MpkLineData>> edgeDataMap = destinationMap.get(endStop);

        for (Map.Entry<String, List<MpkLineData>> edgeEntry : edgeDataMap.entrySet()) {
            List<MpkLineData> edgeDataList = edgeEntry.getValue();

            for (MpkLineData edgeData : edgeDataList) {
                boolean condition;
                if(line.isEmpty())
                    avoidChanges = false;
                if(avoidChanges) {
                    condition = line.equals(edgeData.getLineName()) && edgeData.getDepartureTime().compareTo(startTime) >= 0 &&
                            Duration.between(startTime, edgeData.getDepartureTime()).toSeconds() < smallestWaitingTime;
                } else {
                    condition = edgeData.getDepartureTime().compareTo(startTime) >= 0 &&
                            Duration.between(startTime, edgeData.getDepartureTime()).toSeconds() < smallestWaitingTime;
                }
                if (condition) {
                    smallestWaitingTime = (int) Duration.between(startTime, edgeData.getDepartureTime()).toSeconds();
                    bestTravelTime = (int) Duration.between(edgeData.getDepartureTime(), edgeData.getArrivalTime()).toSeconds();
                    bestLineName = edgeData.getLineName();
                    bestArrivalTime = edgeData.getArrivalTime();
                    bestDepartureTime = edgeData.getDepartureTime();
                }

            }
        }

        if(avoidChanges && bestLineName.isEmpty())
            return getTravelDataBetweenNeighbors(startStop, endStop, startTime,  false, "");

        Map<String, Object> returnMap = new HashMap<>();
        returnMap.put("bestTravelTime", bestTravelTime);
        returnMap.put("bestLineName", bestLineName);
        returnMap.put("bestArrivalTime", bestArrivalTime);
        returnMap.put("bestDepartureTime", bestDepartureTime);
        returnMap.put("bestWaitingTime", smallestWaitingTime);
        return returnMap;

    }


    public void printGraph(String sourceStop) {
        for (Map.Entry<String, Map<String, Map<String, List<MpkLineData>>>> entry : graphMap.entrySet()) {
            String source = entry.getKey();
            if(Objects.equals(source, sourceStop)){
                Map<String, Map<String, List<MpkLineData>>> destinationMap = entry.getValue();

                for (Map.Entry<String, Map<String, List<MpkLineData>>> destEntry : destinationMap.entrySet()) {
                    String destination = destEntry.getKey();
                    Map<String, List<MpkLineData>> edgeDataMap = destEntry.getValue();

                    System.out.println("Source: " + source + ", Destination: " + destination);
                    for (Map.Entry<String, List<MpkLineData>> edgeEntry : edgeDataMap.entrySet()) {
                        List<MpkLineData> edgeDataList = edgeEntry.getValue();

                        for (MpkLineData edgeData : edgeDataList) {
                            System.out.println("    " + edgeData);
                        }
                    }
                }
            }
        }
    }

    //between 2 stops can be rarely different distances
    public Double getAverageDistanceBetweenStops(String startStop, String endStop){
        double distancesSum = 0.0;
        int distancesNum = 0;
        Map<String, Map<String, List<MpkLineData>>> destinationMap = graphMap.get(startStop);
        Map<String, List<MpkLineData>> edgeDataMap = destinationMap.get(endStop);

        for (Map.Entry<String, List<MpkLineData>> innerEntry : edgeDataMap.entrySet()) {
            List<MpkLineData> mpkLineDataList = innerEntry.getValue();
            for (MpkLineData currentEdge : mpkLineDataList) {
                distancesSum += currentEdge.getDistance();
                distancesNum++;
            }
        }

        return distancesSum/distancesNum;
    }

    public double[] getAverageStopLocation(String sourceStop) {
        double xAverage = 0.0;
        double yAverage = 0.0;
        int times = 0;

        for (Map.Entry<String, Map<String, Map<String, List<MpkLineData>>>> entry : graphMap.entrySet()) {
            String source = entry.getKey();
            Map<String, Map<String, List<MpkLineData>>> destinationMap = entry.getValue();
            for (Map.Entry<String, Map<String, List<MpkLineData>>> destEntry : destinationMap.entrySet()) {
                String destination = destEntry.getKey();
                if (sourceStop.equals(source) || destination.equals(sourceStop)){
                    Map<String, List<MpkLineData>> edgeDataMap = destEntry.getValue();
                    for (Map.Entry<String, List<MpkLineData>> edgeEntry : edgeDataMap.entrySet()) {
                        List<MpkLineData> edgeDataList = edgeEntry.getValue();

                        for (MpkLineData edgeData : edgeDataList) {
                            if(sourceStop.equals(source)){
                                times++;
                                xAverage += edgeData.getxCurrent();
                                yAverage += edgeData.getyCurrent();
                            }
                            if(sourceStop.equals(destination)){
                                times++;
                                xAverage += edgeData.getxNext();
                                yAverage += edgeData.getyNext();
                            }
                        }
                    }
                }
            }

        }
        return new double[]{xAverage, yAverage};
    }

    public HashMap<String, Integer> getLongestLines(ArrayList<String> path, int startIndex){
        HashMap<String, Integer> longestLines = new HashMap<>();
        HashMap<String, Integer> linesWithDrivenStops = new HashMap<>();
        int maxValue = Integer.MIN_VALUE;

        for(int i=startIndex; i< path.size() - 1 ; i++){
            Map<String, Map<String, List<MpkLineData>>> destinationMap = graphMap.get(path.get(i));
            Map<String, List<MpkLineData>> edgeDataMap = destinationMap.get(path.get(i+1));

            for (Map.Entry<String, List<MpkLineData>> edgeEntry : edgeDataMap.entrySet()) {
                List<MpkLineData> edgeDataList = edgeEntry.getValue();

                for (MpkLineData edgeData : edgeDataList) {
                        linesWithDrivenStops.put(edgeData.getLineName(), i+1);
                }
            }
        }
        for (int value : linesWithDrivenStops.values()) {
            if (value > maxValue) {
                maxValue = value;
            }
        }

        for (Map.Entry<String, Integer> entry : linesWithDrivenStops.entrySet()) {
            if (entry.getValue() == maxValue) {
                longestLines.put(entry.getKey(), entry.getValue());
            }
        }

        return longestLines;
    }


    public ArrayList<String> getConnectionsWithoutChanges(LocalTime currentTime, ArrayList<String> path) {
        int currentStop = 0;
        int bestLineStopsToGo = 0;
        ArrayList<String> bestConnections = new ArrayList<>();
        while (currentStop < path.size() - 1) {
            String bestLine = "";
            int smallestWaitingTime = Integer.MAX_VALUE;
            HashMap<String, Integer> bestLines = getLongestLines(path, currentStop);
            for (String line : bestLines.keySet()) {
                int currentWaitingTime = (int) getTravelDataBetweenNeighbors(path.get(currentStop), path.get(currentStop + 1),
                        currentTime,  true, line).get("bestWaitingTime");

                if (currentWaitingTime < smallestWaitingTime) {
                    smallestWaitingTime = currentWaitingTime;
                    bestLine = line;
                    bestLineStopsToGo = bestLines.get(line);
                }
            }
            String currentLine = bestLine;
            for (int i = currentStop; i < bestLineStopsToGo; i++) {
                Map<String, Object> neighbourTransportData = getTravelDataBetweenNeighbors(path.get(i), path.get(i + 1),
                        currentTime,  true, currentLine);
                bestConnections.add("Odjazd z przystanku: " + path.get(i) + " Linią: " + neighbourTransportData.get("bestLineName")
                                        + "| O godzinie: " + neighbourTransportData.get("bestDepartureTime")
                                        + " dojazd do następnego przystanku: " + neighbourTransportData.get("bestArrivalTime"));

                currentTime = (LocalTime) neighbourTransportData.get("bestArrivalTime");
            }
            currentStop = bestLineStopsToGo;
        }
        bestConnections.add("Dojazd do przystanku końcowego: " + path.get(path.size() - 1) + " o godzinie: " + currentTime);
        return bestConnections;
    }

    public ArrayList<String> getQuickConnections(LocalTime currentTime, ArrayList<String> path){
        ArrayList<String> bestConnections = new ArrayList<>();
        for(int i=0; i<path.size() -1; i++){
            Map<String, Object> neighbourTransportData = getTravelDataBetweenNeighbors(path.get(i), path.get(i+1),
                    currentTime,  false, "");

            bestConnections.add("Odjazd z przystanku: " + path.get(i) + " Linią: " +  neighbourTransportData.get("bestLineName")
                    + "| O godzinie: " + neighbourTransportData.get("bestDepartureTime")
                    + " dojazd do następnego przystanku: " + neighbourTransportData.get("bestArrivalTime"));

            currentTime = (LocalTime)neighbourTransportData.get("bestArrivalTime");
        }
        bestConnections.add("Dojazd na przystanek końcowy: " + path.get(path.size() - 1) + " o godzinie: " + currentTime);
        return bestConnections;
    }

}
