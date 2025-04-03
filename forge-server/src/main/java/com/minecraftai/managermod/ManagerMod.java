package com.minecraftai.managermod;

import com.minecraftai.managermod.handler.BlockEventsHandler;
import com.minecraftai.managermod.handler.ServerEventsHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(ManagerMod.MODID)
public class ManagerMod {
    public static final String MODID = "managermod";

    public ManagerMod(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new BlockEventsHandler());
        MinecraftForge.EVENT_BUS.register(new ServerEventsHandler());
    }
}
