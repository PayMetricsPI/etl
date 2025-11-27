package infrastructure.requests;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class Limite {
    private static final String TARGET_URL = "http://localhost:8080/seu-endpoint"; // URL do seu servidor
    private static final int MAX_THREADS = 100; // Número máximo de requisições simultâneas a testar
    private static final int REQUESTS_PER_THREAD = 10; // Quantidade de requisições por thread

    public static void main(String[] args) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(MAX_THREADS);
        AtomicInteger successfulRequests = new AtomicInteger();
        AtomicInteger failedRequests = new AtomicInteger();

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < MAX_THREADS; i++) {
            executor.submit(() -> {
                for (int j = 0; j < REQUESTS_PER_THREAD; j++) {
                    try {
                        URL url = new URL(TARGET_URL);
                        HttpURLConnection con = (HttpURLConnection) url.openConnection();
                        con.setRequestMethod("GET");
                        int responseCode = con.getResponseCode();
                        if (responseCode == 200) {
                            successfulRequests.incrementAndGet();
                        } else {
                            failedRequests.incrementAndGet();
                        }
                    } catch (Exception e) {
                        failedRequests.incrementAndGet();
                    }
                }
            });
        }

        executor.shutdown();
        while (!executor.isTerminated()) {
            Thread.sleep(100);
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        System.out.println("Teste finalizado em " + duration + " ms");
        System.out.println("Requisições bem sucedidas: " + successfulRequests.get());
        System.out.println("Requisições falhadas: " + failedRequests.get());
        System.out.println("Total de requisições: " + (successfulRequests.get() + failedRequests.get()));
        System.out.println("Requisições por segundo: " + ((successfulRequests.get() * 1000) / duration));
    }
}