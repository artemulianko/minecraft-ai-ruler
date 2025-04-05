package com.minecraftai.airulermod.integration;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.minecraftai.airulermod.config.EnvConfig;
import com.minecraftai.airulermod.constants.Prompts;
import com.minecraftai.airulermod.service.TokenCounter;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import okhttp3.*;

import javax.annotation.Nullable;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@Singleton
public class OpenAIClient implements AIClient {
    private static final Logger logger = LoggerFactory.getLogger(OpenAIClient.class);
    private static final String OPENAI_API_URL = "https://api.openai.com/v1/responses";
    private static final String MODEL_TYPE = "gpt-4o";

    private final String apiKey;
    private final OkHttpClient httpClient = new OkHttpClient();
    private final StringBuffer instructions = new StringBuffer();
    private final TokenCounter tokenCounter;

    private String prevResponseId;

    @Inject
    public OpenAIClient(EnvConfig envConfig, TokenCounter tokenCounter) {
        this.apiKey = envConfig.get("OPENAI_API_KEY");
        this.tokenCounter = tokenCounter;
    }

    /**
     * Sets up instructions by appending them to the existing instructions and preparing
     * a JSON object with the necessary details for an API call.
     *
     * @param instructions The instructions to be added. These are assumed to provide
     *                     specific guidance or context for subsequent API interactions.
     */
    @Override
    public void setupInstructions(String instructions) {
         this.instructions.append(instructions);
         JsonObject instructionsCall = new JsonObject();

         instructionsCall.addProperty("model", MODEL_TYPE);
         instructionsCall.addProperty("input", this.instructions.toString());
    }

    /**
     * Sends predefined instructions to the OpenAI API via a chat request.
     **/
    @Override
    public void sendInstructions() {
        final var chatMessage = new AIClient.ChatMessage("developer", this.instructions.toString());
        callChat(chatMessage);
        
        // Reset the token counter after sending instructions
        tokenCounter.resetCounter();
    }

    /**
     * Sends a message to OpenAI API for the current chat.
     *
     * @param userMessage The user input to send to the model.
     * @return The assistant's response as a string.
     */
    @Override
    public @Nullable AIClient.ChatResponse chat(String userMessage) {
        // Check if we need to refresh instructions based on token count
        if (tokenCounter.addMessage(userMessage.length())) {
            logger.info("Token threshold reached, resending instructions...");
            setupInstructions(Prompts.getInstructions());
            sendInstructions();
        }
        
        final var chatMessage = new AIClient.ChatMessage("user", userMessage);
        AIClient.ChatResponse response = callChat(chatMessage);
        
        // Track response tokens
        if (response != null) {
            tokenCounter.addMessage(response.message().length());
        }
        
        return response;
    }

    /**
     * Sends a chat message to the OpenAI API and processes the response.
     *
     * @param chatMessage The chat message to be sent, containing the role and input.
     * @return A ChatResponse object containing the response ID and message from the OpenAI API.
     *         Returns null if the request fails or the response cannot be parsed.
     */
    private @Nullable AIClient.ChatResponse callChat(AIClient.ChatMessage chatMessage) {
        JsonObject requestBody = getRequestBody(chatMessage);

        Request request = new Request.Builder()
                .url(OPENAI_API_URL)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(requestBody.toString(), MediaType.parse("application/json")))
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                logger.error("Unexpected code {}", response);
                if (response.body() != null) logger.debug(response.body().string());

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

                return new AIClient.ChatResponse(responseMessage);
            } catch (Exception e) {
                logger.error("Failed to parse response: {}", response);
            }
        } catch (IOException e) {
            logger.error("Failed to send chat request: {}", request);
        }

        return null;
    }

    private @NotNull JsonObject getRequestBody(ChatMessage chatMessage) {
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

        return requestBody;
    }
}