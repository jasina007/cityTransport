import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalTime;
import java.util.*;


class ReadFile {
    public String convertIncorrectRow(String[] row){
        String[] time1 = row[3].split(":");
        int hour1 = Integer.parseInt(time1[0]);
        String[] time2 = row[4].split(":");
        int hour2 = Integer.parseInt(time2[0]);
        if( hour1 >= 24 ){
            int newHour1 = hour1 - 24;
            time1[0] = String.valueOf(newHour1 < 10 ? "0" + newHour1 : newHour1);
            row[3] = time1[0] + ":" + time1[1] + ":" + time1[2];
        }
        if( hour2 >= 24 ){
            int newHour2 = hour2 - 24;
            time2[0] = String.valueOf(newHour2 < 10 ? "0" + newHour2 : newHour2);
            row[4] = time2[0] + ":" + time2[1] + ":" + time2[2];
        }
        StringBuilder result = new StringBuilder();
        for(String rowElem: row) {
            result.append(rowElem).append(",");
        }
        return result.toString();
    }

    public ArrayList<String> readConnectionGraphFile() {
        ArrayList<String> allRowsList = new ArrayList<>();
        try {
            File csvFile = new File("connection_graph.csv");
            Scanner myReader = new Scanner(csvFile);
            myReader.nextLine(); //to refuse headline row
            while (myReader.hasNextLine()) {
                String csvRowTable = convertIncorrectRow(myReader.nextLine().split(","));
                allRowsList.add(csvRowTable);
            }
            myReader.close();

        } catch (FileNotFoundException e) {
            System.out.println("There is no that file.");
            e.printStackTrace();
        }
        return allRowsList;
    }
}

public class Main {

    public void checkTimeDurationBestConnection(){

    }
    
    public static void main(String[] args) {


        Algorithms algs = new Algorithms();
        algs.initializeGraph();


        for(String connection: algs.getBestConnections("PORT LOTNICZY", "Swojczyce", 't', "09:20:00")){
            System.out.println(connection);
        }

        /* zad.2
        for(String connection: algs.getConnectionViaStops("PILCZYCE", "Niedźwiedzia;Kępa Mieszczańska", 't',"19:18:00")){
            System.out.println(connection);
        }
         */

    }
}