package com.minecraftai.airulermod.di;

import com.google.gson.Gson;
import com.minecraftai.airulermod.integration.*;
import dagger.Binds;
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

    @Module
    public abstract static class Bind {
        @Binds
        @Singleton
        @AIImplementation(AIType.OPENAI)
        public abstract AIClient provideOpenAIClient(OpenAIClient openAIClient);

        @Binds
        @Singleton
        @AIImplementation(AIType.MOCK)
        public abstract AIClient provideMockAIClient(MockAIClient mockAIClient);
    }
}
