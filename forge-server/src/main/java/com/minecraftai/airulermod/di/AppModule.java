package com.minecraftai.airulermod.di;

import com.google.gson.Gson;
import com.minecraftai.airulermod.config.EnvConfig;
import com.minecraftai.airulermod.integration.*;
import dagger.Module;
import dagger.Provides;
import jakarta.inject.Singleton;

@Module
public class AppModule {
    @Provides
    @Singleton
    Gson provideGson() {
        return new Gson();
    }

    @Provides
    @Singleton
    @AIImplementation(AIType.OPENAI)
    public AIClient provideOpenAIClient(EnvConfig envConfig) {
        return new OpenAIClient(envConfig);
    }

    @Provides
    @Singleton
    @AIImplementation(AIType.MOCK)
    public AIClient provideMockAIClient() {
        return new MockAIClient();
    }
}
