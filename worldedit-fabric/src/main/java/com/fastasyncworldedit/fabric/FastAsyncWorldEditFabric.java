package com.fastasyncworldedit.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Fabric entry point for FastAsyncWorldEdit.
 */
public class FastAsyncWorldEditFabric implements ModInitializer {

    private static final Logger LOGGER = LogManager.getLogger();

    private final FaweFabric platform;

    public FastAsyncWorldEditFabric() {
        Path configPath = FabricLoader.getInstance().getConfigDir().resolve("fastasyncworldedit");
        try {
            Files.createDirectories(configPath);
        } catch (IOException e) {
            LOGGER.warn("Failed to create FastAsyncWorldEdit config directory {}", configPath, e);
        }
        this.platform = new FaweFabric(configPath);
    }

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTING.register(platform::onServerStarting);
        ServerLifecycleEvents.SERVER_STARTED.register(platform::onServerStarted);
        ServerLifecycleEvents.SERVER_STOPPING.register(platform::onServerStopping);
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> platform.onServerStopped());
        ServerTickEvents.END_SERVER_TICK.register(platform::tick);
        LOGGER.info("FastAsyncWorldEdit Fabric bootstrap registered lifecycle listeners");
    }
}
