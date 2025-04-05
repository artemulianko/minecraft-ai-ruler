package com.minecraftai.airulermod.di;

import com.minecraftai.airulermod.handler.BlockEventsHandler;
import com.minecraftai.airulermod.handler.ServerEventsHandler;
import dagger.Component;
import jakarta.inject.Singleton;

@Singleton
@Component(modules = {AppModule.class, AppModule.Bind.class})
public interface DIContainer {
    BlockEventsHandler getBlockEventsHandler();
    ServerEventsHandler getServerEventsHandler();
}
