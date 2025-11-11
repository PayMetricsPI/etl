package infrastructure.csv.service;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class WriteCSVService {

    public void writeCSV (
            Path outputFile,
            List<String[]> lines,
            Map<String, Integer> columnIndex,
            Integer cpuNormalAlert, Integer cpuCriticAlert,
            Integer ramNormalAlert, Integer ramCriticAlert,
            Integer discoNormalAlert, Integer discoCriticAlert,
            Integer networkSendNormalAlert, Integer networkSendCriticAlert,
            Integer networkReceivedNormalAlert, Integer networkReceivedCriticAlert
    ) throws IOException {

        String[] header = lines.get(0);

        try (BufferedWriter writer = Files.newBufferedWriter(outputFile, StandardCharsets.UTF_8)) {

            writer.write(String.join(";", header)
                    + ";cpu_status;ram_status;disco_status;mb_enviados_status;mb_recebidos_status");
            writer.newLine();

            for (int i = 1; i < lines.size(); i++) {
                String[] columns = lines.get(i);

                if (columns.length < header.length) {
                    continue;
                }

                try {
                    Double cpuValue = Double.parseDouble(columns[columnIndex.get("cpu")]);
                    Double ramValue = Double.parseDouble(columns[columnIndex.get("ram")]);
                    Double discoValue = Double.parseDouble(columns[columnIndex.get("disco")]);
                    Double netSendValue = Double.parseDouble(columns[columnIndex.get("mb_enviados")]);
                    Double netRecvValue = Double.parseDouble(columns[columnIndex.get("mb_recebidos")]);

                    String cpuStatus = getStatus(cpuValue, cpuNormalAlert, cpuCriticAlert);
                    String ramStatus = getStatus(ramValue, ramNormalAlert, ramCriticAlert);
                    String discoStatus = getStatus(discoValue, discoNormalAlert, discoCriticAlert);
                    String netSendStatus = getStatus(netSendValue, networkSendNormalAlert, networkSendCriticAlert);
                    String netRecvStatus = getStatus(netRecvValue, networkReceivedNormalAlert, networkReceivedCriticAlert);

                    writer.write(String.join(";", columns)
                            + ";" + cpuStatus
                            + ";" + ramStatus
                            + ";" + discoStatus
                            + ";" + netSendStatus
                            + ";" + netRecvStatus);
                    writer.newLine();

                } catch (NumberFormatException e) {
                    System.out.println(e.getMessage());
                }
            }
        }
    }

    private String getStatus(Double value, Integer normal, Integer critic) {
        if (normal == null || critic == null) {
            return "NULO";
        }
        if (value >= critic) {
            return "CRITICO";
        }
        if (value >= normal) {
            return "NORMAL";
        }
        return "OK";
    }
}
