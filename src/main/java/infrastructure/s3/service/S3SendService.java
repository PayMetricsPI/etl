package infrastructure.s3.service;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class S3SendService {
    private final S3Client s3Client;
    private final String bucketName;



    public S3SendService(S3Client s3Client, String bucketName) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
    }

    public void uploadFile(Path filePath) {
        try {
            if (!Files.exists(filePath)) {
                System.out.println("Arquivo não encontrado: " + filePath);
                return;
            }

            System.out.println("Enviando arquivo para o trusted! " + filePath.getFileName());

            String fileName = filePath.getFileName().toString();

            PutObjectRequest objectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)          // deve ser "paymetricstrusted"
                    .key("output/" + fileName)
                    .contentType("text/csv; charset=UTF-8")
                    .build();

            s3Client.putObject(objectRequest, filePath);
        } catch (Exception e) {
            e.printStackTrace();                // em vez de só getMessage()
        }
    }

    public void uploadAllCSV(Path folderPath) {
        try (DirectoryStream<Path> files = Files.newDirectoryStream(folderPath)) {
            for (Path filePath : files) {
                System.out.println("Encontrado no output-csv: " + filePath);
                if (filePath.toString().endsWith(".csv")) {
                    System.out.println("Vai enviar para trusted: " + filePath.getFileName());
                    uploadFile(filePath);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void uploadAllJSON(Path folderPath) {
        try (DirectoryStream<Path> files = Files.newDirectoryStream(folderPath)) {
            for (Path filePath : files) {
                if (filePath.toString().endsWith(".json")) {

                    String fileName = filePath.getFileName().toString();

                    PutObjectRequest objectRequest = PutObjectRequest.builder()
                            .bucket("paymetricsclient")
                            .key("output/" + fileName)
                            .contentType("application/json; charset=UTF-8")
                            .build();

                    s3Client.putObject(objectRequest, filePath);
                    System.out.println(" Enviando JSON para client-paymetrics " + fileName);
                }
            }
        } catch (Exception e) {
            System.out.println("Erro ao enviar JSONs: " + e.getMessage());
        }
    }

}


