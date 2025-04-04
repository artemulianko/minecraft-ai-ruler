package com.minecraftai.airulermod.di;

import com.google.gson.Gson;
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
}
