package com.minecraftai.airulermod;

import com.minecraftai.airulermod.di.DIContainer;
import com.minecraftai.airulermod.di.DaggerDIContainer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;

@Mod(AiRulerMod.MODID)
public class AiRulerMod {
    public static final String MODID = "airulermod";
    private static DIContainer container;

    public AiRulerMod() {
        container = DaggerDIContainer.create();

        MinecraftForge.EVENT_BUS.register(this);

        MinecraftForge.EVENT_BUS.register(container.getBlockEventsHandler());
        MinecraftForge.EVENT_BUS.register(container.getServerEventsHandler());
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
