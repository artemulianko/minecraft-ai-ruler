package com.minecraftai.managermod.integration;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.minecraftai.managermod.config.EnvConfig;
import jakarta.inject.Inject;
import okhttp3.*;

import javax.annotation.Nullable;
import jakarta.inject.Singleton;
import java.io.IOException;

@Singleton
public class OpenAIClient {
    public record ChatResponse(@Nullable String responseId, String message) {}
    record ChatMessage(String role, String input) {}

    private static final String OPENAI_API_URL = "https://api.openai.com/v1/responses";
    private static final String MODEL_TYPE = "gpt-4o";
    private final String apiKey;
    private final OkHttpClient httpClient = new OkHttpClient();

    private final StringBuffer instructions = new StringBuffer();
    private String prevResponseId;

    @Inject
    public OpenAIClient(EnvConfig envConfig) {
        this.apiKey = envConfig.get("OPENAI_API_KEY");
    }


    /**
     * Sets up instructions by appending them to the existing instructions and preparing
     * a JSON object with the necessary details for an API call.
     *
     * @param instructions The instructions to be added. These are assumed to provide
     *                     specific guidance or context for subsequent API interactions.
     */
    public void setupInstructions(String instructions) {
         this.instructions.append(instructions);
         JsonObject instructionsCall = new JsonObject();

         instructionsCall.addProperty("model", MODEL_TYPE);
         instructionsCall.addProperty("input", this.instructions.toString());
    }

    /**
     * Sends predefined instructions to the OpenAI API via a chat request.
     *
     * @return The response message from the OpenAI API as a string. If the API
     **/
    public void sendInstructions() throws IOException {
        final var chatMessage = new ChatMessage("developer", this.instructions.toString());
        callChat(chatMessage);
    }

    /**
     * Sends a message to OpenAI API for the current chat.
     *
     * @param userMessage The user input to send to the model.
     * @return The assistant's response as a string.
     * @throws IOException If there are issues with the request.
     */
    public @Nullable ChatResponse chat(String userMessage) throws IOException {
        final var chatMessage = new ChatMessage("user", userMessage);

        return callChat(chatMessage);
    }

    /**
     * Sends a chat message to the OpenAI API and processes the response.
     *
     * @param chatMessage The chat message to be sent, containing the role and input.
     * @return A ChatResponse object containing the response ID and message from the OpenAI API.
     *         Returns null if the request fails or the response cannot be parsed.
     * @throws IOException If an error occurs while making the HTTP request.
     */
    private @Nullable ChatResponse callChat(ChatMessage chatMessage) throws IOException {
        JsonObject inputJson = new JsonObject();
        JsonArray inputArray = new JsonArray();
        inputJson.addProperty("role", chatMessage.role());
        inputJson.addProperty("content", chatMessage.input());
        inputArray.add(inputJson);

        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", MODEL_TYPE);
        requestBody.add("input", inputArray);

        if (prevResponseId != null) {
            requestBody.addProperty("previous_response_id", prevResponseId);
        }

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

            try {
                // Parse the response
                String responseBody = response.body() != null ? response.body().string() : "";
                JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();

                String responseMessage = jsonResponse.getAsJsonObject()
                        .getAsJsonArray("output")
                        .get(0)
                        .getAsJsonObject()
                        .getAsJsonArray("content")
                        .get(0)
                        .getAsJsonObject()
                        .get("text")
                        .getAsString();

                prevResponseId = jsonResponse.get("id").getAsString();

                return new ChatResponse(prevResponseId, responseMessage);
            } catch (Exception e) {
                System.err.println("Failed to parse response: " + response);
            }
        }

        return null;
    }
}