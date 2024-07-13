package feral.telegram.AI_chat_bot.yandexGPT;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@Data
public class YandexClient {

    private @Value("${yandex.cloud.token}") String token;
    private @Value("${yandex.cloud.id}") String id;


    public String gptRequest(String promt) {
        log.info("starting gpt request with promt: {}", promt);
        RestTemplate restTemplate = new RestTemplate();
        String url = "https://llm.api.cloud.yandex.net/foundationModels/v1/completion";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", String.format("Api-Key %s", getToken()));
        headers.set("x-folder-id", getId());
        log.info("token is: {}", getToken());
        log.info("id is: {}", getId());

        String requestBody = "{\"modelUri\": \"gpt://" + getId() + "/yandexgpt-lite\", " +
                "\"completionOptions\": {\"stream\": false, \"temperature\": 0.1, \"maxTokens\": \"2000\"}, " +
                "\"messages\": [ " +
                "{\"role\": \"system\", \"text\": \"ты нейросеть, которая отвечает на вопросы новичкам в adobe after effects\"}, " +
                "{\"role\": \"user\", \"text\": \"" + promt + "\"} ]}";
        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

        log.info(requestEntity.getBody());
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);

        log.info("gpt response is: {}", response);
        if (response.getStatusCode().is2xxSuccessful()) {
            String responseBody = response.getBody();
            log.info("response body: {}", responseBody);
            return parseJson(responseBody);
        } else {
            return "Ошибка при запросе, попробуйте попозже :(";
        }

    }


    private String parseJson(String json) {
        JSONObject jsonObject = new JSONObject(json);
        JSONArray alternatives = jsonObject.getJSONObject("result").getJSONArray("alternatives");
        String messageText = alternatives.getJSONObject(0).getJSONObject("message").getString("text");

        return messageText;
    }
}