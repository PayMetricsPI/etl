package infrastructure.csv.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ReadCSVService {

    public List<Path> getCSVFiles(Path folderPath) throws IOException {
        List<Path> csvFiles = new ArrayList<>();
        try (DirectoryStream<Path> directory = Files.newDirectoryStream(folderPath, "*.csv")) {
            for (Path file : directory) {
                csvFiles.add(file);
            }
        }
        return csvFiles;
    }

    public List<String[]> readCSV(Path filePath) throws IOException {
        List<String[]> lines = new ArrayList<>();

        try (BufferedReader bufferedReader = Files.newBufferedReader(filePath)) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] columns = line.split(";");
                lines.add(columns);
            }
        }
        return lines;
    }

    public String getColumnValue(Path csvPath, String columnName) throws IOException {
        List<String[]> lines = readCSV(csvPath);

        String[] header = lines.get(0);
        int columnIndex = -1;

        for (int i = 0; i < header.length; i++) {
            if (header[i].equalsIgnoreCase(columnName)) {
                columnIndex = i;
                break;
            }
        }

        String[] firstDataLine = lines.get(1);

        if (columnIndex < firstDataLine.length) {
            return firstDataLine[columnIndex];
        } else {
            return "";
        }
    }
}
