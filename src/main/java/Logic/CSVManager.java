package Logic;

import Logic.Net.Network;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.HeaderColumnNameMappingStrategy;
import sun.nio.ch.Net;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by igor on 03.08.16.
 */
public class CSVManager {
    private String path;
    private char delimiter;

    public CSVManager(String path, char delimeter) {
        this.path = path;
        this.delimiter = delimeter;
    }

    public List<Network> readData() throws Exception {
        List<Network> list = new ArrayList<>();
        BufferedReader fileReader = new BufferedReader(new FileReader(path));
        String line;
        while((line = fileReader.readLine()) != null){
            String[] tokens = line.split(String.valueOf(delimiter));
            if(tokens.length < 5)
                throw new Exception("Count obj in the line of the file < 5. {mth. readData()}");
            list.add(new Network(tokens[0], tokens[1], tokens[2], tokens[3], tokens[4],
                    tokens.length < 6 ? "" : tokens[5], tokens.length < 7 ? "" : tokens[6],
                    tokens.length < 8 ? null : tokens[7]));
        }
        fileReader.close();
        return list;
    }

    public void writeData(List<Network> list) throws IOException {
        FileWriter fileWriter = new FileWriter(path);

        for (Network aList : list) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(aList.getIp().getIp());        stringBuilder.append(delimiter);
            stringBuilder.append(aList.getMaskString());        stringBuilder.append(delimiter);
            stringBuilder.append(aList.getSize());              stringBuilder.append(delimiter);
            stringBuilder.append(aList.getStatusString());      stringBuilder.append(delimiter);
            stringBuilder.append(aList.getPriorityString());    stringBuilder.append(delimiter);
            stringBuilder.append(aList.getClient());            stringBuilder.append(delimiter);
            stringBuilder.append(aList.getTypeOfConnection());  stringBuilder.append(delimiter);
            stringBuilder.append(aList.getDate() == null
                    ? "" : aList.getDateString());              stringBuilder.append("\n");
            fileWriter.append(stringBuilder);
        }
        fileWriter.close();
    }
}
