package infrastructure.Jira;

import java.io.OutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class Jira {
    private final String jiraApiUrl = "https://sptech-team-dhiovpb1.atlassian.net/rest/api/3/issue";
    private final String userEmail = "leonardo.tubero@sptech.school";
    private final String apiToken = "";
    private String auth;

    public Jira() {
        String authStr = userEmail + ":" + apiToken;
        auth = Base64.getEncoder().encodeToString(authStr.getBytes(StandardCharsets.UTF_8));
    }

    private void criarTicket(String summary, String descriptionText, String priority, String customFieldId, String customFieldValue) throws Exception {
        String body = """
        {
          "fields": {
            "project": { "key": "PAYM" },
            "summary": "%s",
            "description": {
              "type": "doc",
              "version": 1,
              "content": [
                {
                  "type": "paragraph",
                  "content": [
                    {
                      "text": "%s",
                      "type": "text"
                    }
                  ]
                }
              ]
            },
            "issuetype": { "name": "Problem" },
            "priority": { "name": "%s" },
            "labels": ["categoriaA"],
            "%s": "%s",
            "customfield_10008": "2025-11-01T13:45:00.000+0000"
          }
        }
        """.formatted(summary, descriptionText, priority, customFieldId, customFieldValue);

        URL url = new URL(jiraApiUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Authorization", "Basic " + auth);
        conn.setRequestProperty("Content-Type", "application/json");


        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.getBytes(StandardCharsets.UTF_8));
        }


        int responseCode = conn.getResponseCode();
        System.out.println("Response Code: " + responseCode);

        InputStream responseStream = (responseCode >= 200 && responseCode < 300)
                ? conn.getInputStream()
                : conn.getErrorStream();

        if (responseStream != null) {
            String response = new String(responseStream.readAllBytes(), StandardCharsets.UTF_8);
            System.out.println("Resposta do Jira: " + response);
        }

        if (responseCode == 201) {
            System.out.println(" Ticket criado com sucesso!");
        } else {
            System.out.println(" Falha ao criar o ticket.");
        }

        conn.disconnect();
    }


    public void criarAlertaNormal(String summary, String description) throws Exception {
        criarTicket(summary, description, "High", "customfield_10062", "48");
    }


    public void criarAlertaCritico(String summary, String description) throws Exception {
        criarTicket(summary, description, "Highest", "customfield_10062", "48");
    }

    public static void main(String[] args) {
        try {
            Jira jira = new Jira();
            jira.criarAlertaNormal(
                    "Alerta normal: CPU acima de 70%",
                    "A CPU do servidor passou do limite padrão, mas não há impacto crítico."
            );


            jira.criarAlertaCritico(
                    "Alerta crítico: CPU acima de 90%",
                    "A CPU do servidor atingiu um nível crítico, risco de downtime!"
            );

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
