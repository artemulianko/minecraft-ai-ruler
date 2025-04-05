package com.minecraftai.airulermod;

import com.minecraftai.airulermod.di.DIContainer;
import com.minecraftai.airulermod.di.DaggerDIContainer;
import com.minecraftai.airulermod.service.StatsService;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(AiRulerMod.MODID)
public class AiRulerMod {
    public static final String MODID = "airulermod";
    private static DIContainer container;

    public AiRulerMod() {
        container = DaggerDIContainer.create();
        
        // Initialize stats service early to ensure timers are running
        container.getStatsService();
        
        // Register event handlers
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(container.getBlockEventsHandler());
        MinecraftForge.EVENT_BUS.register(container.getServerEventsHandler());
        MinecraftForge.EVENT_BUS.register(container.getPlayerEventsHandler());
    }

    /**
     * Retrieves the initialized dependency injection container.
     * This container provides access to various application components and services.
     *
     * @return the {@link DIContainer} instance.
     * @throws IllegalStateException if the container has not been initialized.
     */
    public static DIContainer getContainer() {
        if (container == null) {
            throw new IllegalStateException("Container not initialized");
        }
        return container;
    }
}
