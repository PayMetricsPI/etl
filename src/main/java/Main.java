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

            S3ReceivedService receivedService = new S3ReceivedService(s3Client, bucketNameReceived);
            receivedService.processFiles(inputFolder);

            ReadCSVService readCSVService = new ReadCSVService();
            WriteCSVService writeCSVService = new WriteCSVService();
            ParameteurDAO parameteurDAO = new ParameteurDAO();
            ObjectMapper jsonMapper = new ObjectMapper();

            List<Path> csvFiles = readCSVService.getCSVFiles(inputFolder);

            for (Path csvFile : csvFiles) {
                System.out.println("Processando arquivo: " + csvFile.getFileName());
                List<String[]> rows = readCSVService.readCSV(csvFile);

                if (rows.isEmpty()) {
                    continue;
                }

                String macAddress = readCSVService.getColumnValue(csvFile, "mac_address");
                List<ParameteurEntity> alertas = parameteurDAO.verifyAlerts(macAddress);

                Integer cpuNormal = 0, cpuCritic = 0;
                Integer ramNormal = 0, ramCritic = 0;
                Integer discoNormal = 0, discoCritic = 0;
                Integer networkSendNormal = 0, networkSendCritic = 0;
                Integer networkReceivedNormal = 0, networkReceivedCritic = 0;

                for (int i = 0; i < alertas.size(); i++) {
                    ParameteurEntity alert = alertas.get(i);
                    String component = alert.getComponent().toLowerCase();
                    String name = alert.getName();

                    if (component.equals("cpu")) {
                        cpuNormal = alert.getNormalAlert();
                        cpuCritic = alert.getCriticAlert();
                        jira.criarAlertaNormal(
                                "Alerta normal na máquina "+name+": CPU acima de "+cpuNormal+"%",
                                "A CPU do servidor passou do limite padrão");
                        jira.criarAlertaCritico(
                                "Alerta crítico na máquina "+name+": CPU acima de "+cpuCritic+"%",
                                "A CPU do servidor atingiu um nível crítico"
                        );
                    }
                    if (component.equals("ram")) {
                        ramNormal = alert.getNormalAlert();
                        ramCritic = alert.getCriticAlert();
                        jira.criarAlertaNormal(
                                "Alerta normal na máquina "+name+": Ram acima de "+ramNormal+"%",
                                "A Ram do servidor passou do limite padrão");
                        jira.criarAlertaCritico(
                                "Alerta crítico na máquina "+name+": Ram acima de "+ramCritic+"%",
                                "A Ram do servidor atingiu um nível crítico"
                        );
                    }
                    if (component.equals("disco")) {
                        discoNormal = alert.getNormalAlert();
                        discoCritic = alert.getCriticAlert();
                        jira.criarAlertaNormal(
                                "Alerta normal na máquina "+name+": Disco acima de "+discoNormal+"%",
                                "O Disco do servidor passou do limite padrão");
                        jira.criarAlertaCritico(
                                "Alerta crítico na máquina "+name+": Disco acima de "+discoCritic+"%",
                                "O Disco do servidor atingiu um nível crítico"
                        );
                    }
                    if (component.equals("mb enviados - rede")) {
                        networkSendNormal = alert.getNormalAlert();
                        networkSendCritic = alert.getCriticAlert();
                        jira.criarAlertaNormal(
                                "Alerta na máquina "+name+": Mb enviados acima de "+networkSendNormal+"%",
                                "Os Mb enviados do servidor passaram do limite padrão");
                        jira.criarAlertaCritico(
                                "Alerta crítico na máquina "+name+": Mb recebidos acima de "+networkSendCritic+"%",
                                "os Mb recebidos do servidor atingiram um nível crítico, risco!"
                        );
                    }
                    if (component.equals("mb recebidos - rede")) {
                        networkReceivedNormal = alert.getNormalAlert();
                        networkReceivedCritic = alert.getCriticAlert();
                        jira.criarAlertaNormal(
                                "Alerta normal na máquina "+name+": Mb recebidos acima de "+networkReceivedNormal+"%",
                                "Os Mb recebidos do servidor passaram do limite padrão");
                        jira.criarAlertaCritico(
                                "Alerta crítico na máquina "+name+": Mb recebidos acima de "+networkReceivedCritic+"%",
                                "os Mb recebidos do servidor atingiram um nível crítico, risco!"
                        );
                    }
                }

                String[] header = rows.get(0);
                Map<String, Integer> columnIndex = new HashMap<>();
                for (int i = 0; i < header.length; i++) {
                    columnIndex.put(header[i].toLowerCase(), i);
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

                File csv = csvFile.toFile();
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

          //cleanFolder(inputFolder);
          //cleanFolder(outputFolder);
            // cleanFolder(outputFolderJson);

        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}
