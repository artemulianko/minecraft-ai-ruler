package com.minecraftai.airulermod.di;

import com.google.gson.Gson;
import com.minecraftai.airulermod.integration.*;
import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoMap;
import dagger.multibindings.StringKey;
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
        @IntoMap
        @StringKey("OPENAI")
        public abstract AIClient provideOpenAIClient(OpenAIClient openAIClient);

        @Binds
        @IntoMap()
        @StringKey("MOCK")
        public abstract AIClient provideMockAIClient(MockAIClient mockAIClient);
    }
}
