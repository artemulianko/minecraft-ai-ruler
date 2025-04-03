package com.minecraftai.managermod;

import com.minecraftai.managermod.di.DIContainer;
import com.minecraftai.managermod.di.DaggerDIContainer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;

@Mod(ManagerMod.MODID)
public class ManagerMod {
    public static final String MODID = "managermod";
    private static DIContainer container;

    public ManagerMod() {
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
