package com.minecraftai.managermod.di;

import com.minecraftai.managermod.handler.BlockEventsHandler;
import com.minecraftai.managermod.handler.ServerEventsHandler;
import dagger.Component;
import jakarta.inject.Singleton;

@Singleton
@Component(modules = {AppModule.class})
public interface DIContainer {
    BlockEventsHandler getBlockEventsHandler();
    ServerEventsHandler getServerEventsHandler();
}
