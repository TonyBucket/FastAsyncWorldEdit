package com.fastasyncworldedit.fabric;

import com.fastasyncworldedit.core.FAWEPlatformAdapterImpl;
import com.fastasyncworldedit.core.Fawe;
import com.fastasyncworldedit.core.IFawe;
import com.fastasyncworldedit.core.queue.implementation.QueueHandler;
import com.fastasyncworldedit.core.queue.implementation.preloader.Preloader;
import com.fastasyncworldedit.core.regions.FaweMaskManager;
import com.fastasyncworldedit.core.util.TaskManager;
import com.fastasyncworldedit.core.util.WEManager;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.management.InstanceAlreadyExistsException;
import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

/**
 * Fabric implementation of the FAWE platform contract.
 */
public class FaweFabric implements IFawe {

    private static final Logger LOGGER = LogManager.getLogger();

    private final Path configPath;
    private final FabricTaskManager taskManager;
    private final FabricQueueHandler queueHandler;
    private final FabricPlatformAdapter platformAdapter;
    private MinecraftServer server;

    public FaweFabric(Path configPath) {
        this.configPath = configPath;
        this.taskManager = new FabricTaskManager();
        this.queueHandler = new FabricQueueHandler();
        this.platformAdapter = new FabricPlatformAdapter();
    }

    void onServerStarting(MinecraftServer server) {
        this.server = server;
        taskManager.bind(server);
        queueHandler.bind(server);
        if (Fawe.instance() == null) {
            try {
                Fawe.set(this);
                Fawe.setupInjector();
            } catch (InstanceAlreadyExistsException e) {
                LOGGER.warn("FastAsyncWorldEdit already initialized, skipping re-initialization", e);
            } catch (IllegalArgumentException e) {
                LOGGER.error("Failed to initialize FastAsyncWorldEdit", e);
            }
        }
    }

    void onServerStarted(MinecraftServer server) {
        WEManager.weManager().addManagers(getMaskManagers());
        LOGGER.info("FastAsyncWorldEdit Fabric initialized for server {}", server.getVersion());
    }

    void onServerStopping(MinecraftServer server) {
        Fawe fawe = Fawe.instance();
        if (fawe != null) {
            fawe.onDisable();
        }
        taskManager.shutdown();
        queueHandler.close();
    }

    void onServerStopped() {
        this.server = null;
        taskManager.reset();
        queueHandler.reset();
    }

    void tick(MinecraftServer server) {
        taskManager.tick();
    }

    @Override
    public File getDirectory() {
        return configPath.toFile();
    }

    @Override
    public TaskManager getTaskManager() {
        return taskManager;
    }

    @Override
    public Collection<FaweMaskManager> getMaskManagers() {
        return Collections.emptyList();
    }

    @Override
    public String getPlatform() {
        return "Fabric";
    }

    @Override
    public UUID getUUID(String name) {
        if (server == null) {
            return null;
        }
        GameProfile profile = server.getUserCache().findByName(name);
        return profile != null ? profile.getId() : null;
    }

    @Override
    public String getName(UUID uuid) {
        if (server == null) {
            return null;
        }
        GameProfile profile = server.getUserCache().getByUuid(uuid);
        return profile != null ? profile.getName() : null;
    }

    @Override
    public String getDebugInfo() {
        StringBuilder builder = new StringBuilder();
        builder.append("# FastAsyncWorldEdit Information\n");
        if (server != null) {
            builder.append("Server Version: ").append(server.getVersion()).append("\n");
            builder.append("Is Dedicated: ").append(server instanceof MinecraftDedicatedServer).append("\n");
        }
        builder.append("Config directory: ").append(getDirectory().getAbsolutePath()).append("\n");
        return builder.toString();
    }

    @Override
    public QueueHandler getQueueHandler() {
        return queueHandler;
    }

    @Override
    public Preloader getPreloader(boolean initialise) {
        return null;
    }

    @Override
    public FAWEPlatformAdapterImpl getPlatformAdapter() {
        return platformAdapter;
    }
}
