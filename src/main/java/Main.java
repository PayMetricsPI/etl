import domain.entity.ParameteurEntity;
import infrastructure.Jira.Jira;
import infrastructure.csv.service.ReadCSVService;
import infrastructure.csv.service.WriteCSVService;
import infrastructure.persistence.dao.ParameteurDAO;
import infrastructure.s3.service.S3ClientFactory;
import infrastructure.s3.service.S3ReceivedService;
import infrastructure.s3.service.S3SendService;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import static infrastructure.folder.FolderProcessor.cleanFolder;

public class Main {

    public static void main(String[] args) {

        Jira jira = new Jira();
        String accessKey = "";
        String secretKey = "";
        String sessionToken = "";
        String bucketNameReceived = "raw-paymetrics";
        String bucketNameSend = "trusted-paymetrics";
        String bucketNameSendClient = "client-paymetrics";


        Path inputFolder = Path.of("src/main/resources/input-csv");
        Path outputFolder = Path.of("src/main/resources/output-csv");
        Path outputFolderJson = Path.of("src/main/resources/output-json");
        Region region = Region.US_EAST_1;

        try (S3Client s3Client = S3ClientFactory.createClient(accessKey, secretKey, sessionToken, region)) {

            if (!Files.exists(inputFolder)) {
                Files.createDirectories(inputFolder);
            }

            if (!Files.exists(outputFolder)) {
                Files.createDirectories(outputFolder);
            }

            if (!Files.exists(outputFolderJson)) {
                Files.createDirectories(outputFolderJson);
            }


            S3ReceivedService receivedService = new S3ReceivedService(s3Client, bucketNameReceived);
            receivedService.processFiles(inputFolder);

            ReadCSVService readCSVService = new ReadCSVService();
            WriteCSVService writeCSVService = new WriteCSVService();
            ParameteurDAO parameteurDAO = new ParameteurDAO();
            ObjectMapper jsonMapper = new ObjectMapper();

            List<Path> csvFiles = readCSVService.getCSVFiles(inputFolder);
            csvFiles.removeIf(path -> path.getFileName().toString().toUpperCase().contains("LIDO"));

            for (Path csvFile : csvFiles) {
                System.out.println("Processando arquivo: " + csvFile.getFileName());
                List<String[]> rows = readCSVService.readCSV(csvFile);

                if (rows.isEmpty()) continue;

                String macAddress = readCSVService.getColumnValue(csvFile, "mac_address");
                List<ParameteurEntity> alertas = parameteurDAO.verifyAlerts(macAddress);

                Integer cpuNormal = null, cpuCritic = null;
                Integer ramNormal = null, ramCritic = null;
                Integer discoNormal = null, discoCritic = null;
                Integer networkSendNormal = null, networkSendCritic = null;
                Integer networkReceivedNormal = null, networkReceivedCritic = null;

                for (ParameteurEntity alert : alertas) {
                    String component = alert.getComponent().toLowerCase();

                    switch (component) {
                        case "cpu" -> {
                            cpuNormal = alert.getNormalAlert();
                            cpuCritic = alert.getCriticAlert();
                        }
                        case "ram" -> {
                            ramNormal = alert.getNormalAlert();
                            ramCritic = alert.getCriticAlert();
                        }
                        case "disco" -> {
                            discoNormal = alert.getNormalAlert();
                            discoCritic = alert.getCriticAlert();
                        }
                        case "mb enviados - rede" -> {
                            networkSendNormal = alert.getNormalAlert();
                            networkSendCritic = alert.getCriticAlert();
                        }
                        case "mb recebidos - rede" -> {
                            networkReceivedNormal = alert.getNormalAlert();
                            networkReceivedCritic = alert.getCriticAlert();
                        }
                    }
                }

                String[] header = rows.get(0);
                Map<String, Integer> columnIndex = new HashMap<>();
                for (int i = 0; i < header.length; i++) {
                    columnIndex.put(header[i].toLowerCase(), i);
                }

                for (int i = 1; i < rows.size(); i++) {
                    String[] col = rows.get(i);

                    Double cpuValue = Double.parseDouble(col[columnIndex.get("cpu")]);
                    Double ramValue = Double.parseDouble(col[columnIndex.get("ram")]);
                    Double discoValue = Double.parseDouble(col[columnIndex.get("disco")]);
                    Double netSendValue = Double.parseDouble(col[columnIndex.get("mb_enviados")]);
                    Double netRecvValue = Double.parseDouble(col[columnIndex.get("mb_recebidos")]);
                    String mac = col[columnIndex.get("mac_address")];

                    String machineName = col[columnIndex.get("codigo_maquina")];

                    verifyAlertsLines(jira, machineName, cpuValue, cpuNormal, cpuCritic, "CPU", mac);
                    verifyAlertsLines(jira, machineName, ramValue, ramNormal, ramCritic, "RAM", mac);
                    verifyAlertsLines(jira, machineName, discoValue, discoNormal, discoCritic, "Disco", mac);
                    verifyAlertsLines(jira, machineName, netSendValue, networkSendNormal, networkSendCritic, "Mb enviados", mac);
                    verifyAlertsLines(jira, machineName, netRecvValue, networkReceivedNormal, networkReceivedCritic, "Mb recebidos", mac);
                }

                Path outputFile = outputFolder.resolve(csvFile.getFileName());

                writeCSVService.writeCSV(
                        outputFile, rows, columnIndex,
                        cpuNormal, cpuCritic,
                        ramNormal, ramCritic,
                        discoNormal, discoCritic,
                        networkSendNormal, networkSendCritic,
                        networkReceivedNormal, networkReceivedCritic
                );

                File csv = outputFile.toFile();
                CsvMapper csvMapper = new CsvMapper();
                CsvSchema csvSchema = CsvSchema.emptySchema().withHeader().withColumnSeparator(';');

                MappingIterator<Map<String, String>> mappingIterator = csvMapper.readerFor(Map.class).with(csvSchema).readValues(csv);
                List<Map<String, String>> csvData = mappingIterator.readAll();

                String json = jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(csvData);

                String jsonFileName = csvFile.getFileName().toString().replace(".csv", ".json");
                Path jsonFilePath = outputFolderJson.resolve(jsonFileName);

                Files.writeString(jsonFilePath, json);

                S3SendService sendService = new S3SendService(s3Client, bucketNameSendClient);
                sendService.uploadAllJSON(outputFolderJson);

            }

            S3SendService sendService = new S3SendService(s3Client, bucketNameSend);
            sendService.uploadAllCSV(outputFolder);

            cleanFolder(inputFolder);
            cleanFolder(outputFolder);
            cleanFolder(outputFolderJson);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void verifyAlertsLines(
            Jira jira,
            String name,
            Double value,
            Integer normal,
            Integer critic,
            String componente,
            String mac
    ) throws Exception {
        if (value >= critic) {
            jira.criarAlertaCritico(
                    "Alerta crítico na máquina " + name + ": " + componente + " acima de " + critic + "%",
                    mac
            );
        } else if (value >= normal) {
            jira.criarAlertaNormal(
                    "Alerta normal na máquina " + name + ": " + componente + " acima de " + normal + "%",
                    mac
            );
        }
    }

}
