package application;

import application.factory.S3ClientFactory;
import application.service.S3Service;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

public class S3Connection {
    public static void main(String[] args) {
        String accessKey = System.getenv("AWS_ACCESS_KEY");
        String secretKey = System.getenv("AWS_SECRET_KEY");
        String sessionToken = System.getenv("AWS_SESSION_TOKEN");
        String bucketName = "raw-guilherme";
        String localFolder = "./";
        Region region = Region.US_EAST_1;

        try (S3Client s3Client = S3ClientFactory.createClient(accessKey, secretKey, sessionToken, region)) {
            S3Service service = new S3Service(s3Client, bucketName);
            service.processFiles(localFolder);
            System.out.println("Processamento concluído.");
        } catch (Exception e) {
            System.err.println("Erro: " + e.getMessage());
        }
    }
}
