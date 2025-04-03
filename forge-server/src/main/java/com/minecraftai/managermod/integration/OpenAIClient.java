package com.minecraftai.managermod.integration;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.minecraftai.managermod.config.EnvConfig;
import okhttp3.*;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;

public class OpenAIClient {
    private static final String OPENAI_API_URL = "https://api.openai.com/v1/responses";
    private static final String MODEL_TYPE = "gpt-4o-mini";
    private final String apiKey;
    private final OkHttpClient httpClient = new OkHttpClient();

    private final StringBuffer instructions = new StringBuffer();

    private String prevResponseId;

    @Inject
    public OpenAIClient(EnvConfig envConfig) {
        this.apiKey = envConfig.get("OPENAI_API_KEY");
    }


    public void setupInstructions(List<String> messages) {
         messages.forEach(instructions::append);
    }

    /**
     * Sends a message to OpenAI API for the current chat.
     *
     * @param userMessage The user input to send to the model.
     * @return The assistant's response as a string.
     * @throws IOException If there are issues with the request.
     */
    public String sendMessage(String userMessage) throws IOException {
        // Prepare the request payload
        JsonObject requestBody = new JsonObject();

        requestBody.addProperty("model", MODEL_TYPE);
        requestBody.addProperty("instructions", instructions.toString());
        requestBody.addProperty("input", userMessage);

        if (prevResponseId != null) {
            requestBody.addProperty("previous_response_id", prevResponseId);
        }

        // Send the HTTP POST request to OpenAI
        Request request = new Request.Builder()
                .url(OPENAI_API_URL)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(requestBody.toString(), MediaType.parse("application/json")))
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                System.err.println("Unexpected code " + response);
                assert response.body() != null;
                System.err.println(response.body().string());

                return null;
            }

            String responseMessage = "";

            try {
                // Parse the response
                String responseBody = response.body() != null ? response.body().string() : "";
                JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();

                responseMessage = jsonResponse.getAsJsonObject()
                        .getAsJsonArray("output")
                        .get(0)
                        .getAsJsonObject()
                        .getAsJsonArray("content")
                        .get(0)
                        .getAsJsonObject()
                        .get("text")
                        .getAsString();

                prevResponseId = jsonResponse.get("id").getAsString();
            } catch (Exception e) {
                System.err.println("Failed to parse response: " + response);
            }

            return responseMessage;
        }
    }
}