package infrastructure.csv.service;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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
                    + ";cpu_status;cpu_status_critico"
                    + ";ram_status;ram_status_critico"
                    + ";disco_status;disco_status_critico"
                    + ";mb_enviados_status;mb_enviados_status_critico"
                    + ";mb_recebidos_status;mb_recebidos_status_critico"
                    + ";data_alerta");
            writer.newLine();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String dataAlerta = LocalDateTime.now().format(formatter);

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

                    String cpuStatus = "";
                    String cpuStatusCritico = "";
                    if (cpuValue >= cpuCriticAlert) {
                        cpuStatusCritico = "CRITICO";
                    } else if (cpuValue >= cpuNormalAlert) {
                        cpuStatus = "NORMAL";
                    } else {
                        cpuStatus = "";
                    }

                    String ramStatus = "";
                    String ramStatusCritico = "";
                    if (ramValue >= ramCriticAlert) {
                        ramStatusCritico = "CRITICO";
                    } else if (ramValue >= ramNormalAlert) {
                        ramStatus = "NORMAL";
                    } else {
                        ramStatus = "";
                    }

                    String discoStatus = "";
                    String discoStatusCritico = "";
                    if (discoValue >= discoCriticAlert) {
                        discoStatusCritico = "CRITICO";
                    } else if (discoValue >= discoNormalAlert) {
                        discoStatus = "NORMAL";
                    } else {
                        discoStatus = "";
                    }

                    String netSendStatus = "";
                    String netSendStatusCritico = "";
                    if (netSendValue >= networkSendCriticAlert) {
                        netSendStatusCritico = "CRITICO";
                    } else if (netSendValue >= networkSendNormalAlert) {
                        netSendStatus = "NORMAL";
                    } else {
                        netSendStatus = "";
                    }

                    String netRecvStatus = "";
                    String netRecvStatusCritico = "";
                    if (netRecvValue >= networkReceivedCriticAlert) {
                        netRecvStatusCritico = "CRITICO";
                    } else if (netRecvValue >= networkReceivedNormalAlert) {
                        netRecvStatus = "NORMAL";
                    } else {
                        netRecvStatus = "";
                    }

                    writer.write(String.join(";", columns)
                            + ";" + cpuStatus + ";" + cpuStatusCritico
                            + ";" + ramStatus + ";" + ramStatusCritico
                            + ";" + discoStatus + ";" + discoStatusCritico
                            + ";" + netSendStatus + ";" + netSendStatusCritico
                            + ";" + netRecvStatus + ";" + netRecvStatusCritico
                            + ";" + dataAlerta);
                    writer.newLine();

                } catch (NumberFormatException e) {
                    System.out.println(e.getMessage());
                }
            }
        }
    }

    private String getStatus(Double value, Integer normal, Integer critic) {
        if (normal == null || critic == null) return "NULO";
        if (value >= critic) return "";
        if (value >= normal) return "NORMAL";
        return "";
    }

    private String getCritico(Double value, Integer critic) {
        if (critic == null) return "NULO";
        return value >= critic ? "CRITICO" : "";
    }
}
