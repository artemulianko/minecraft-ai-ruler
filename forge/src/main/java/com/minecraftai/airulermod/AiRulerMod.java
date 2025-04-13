package com.minecraftai.airulermod;

import com.minecraftai.airulermod.di.DIContainer;
import com.minecraftai.airulermod.di.DaggerDIContainer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;

@Mod(AiRulerMod.MODID)
public class AiRulerMod {
    public static final String MODID = "airulermod";

    public AiRulerMod() {
        final DIContainer container = DaggerDIContainer.create();

        // Register event handlers
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(container.getServerEventsHandler());
        MinecraftForge.EVENT_BUS.register(container.getBlockEventsHandler());
        MinecraftForge.EVENT_BUS.register(container.getPlayerEventsHandler());
    }
}
