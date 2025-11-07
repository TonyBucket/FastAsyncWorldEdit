package com.fastasyncworldedit.fabric;

import com.fastasyncworldedit.core.queue.implementation.QueueHandler;
import net.minecraft.server.MinecraftServer;

import java.util.Objects;

/**
 * Fabric-specific queue handler.
 */
public class FabricQueueHandler extends QueueHandler {

    void bind(MinecraftServer server) {
        Objects.requireNonNull(server, "server");
    }

    void close() {
        // No-op for now. Placeholder for future cleanup.
    }

    void reset() {
        // No state to reset yet.
    }

    @Override
    public void startUnsafe(boolean parallel) {
        // Fabric requires main-thread world modifications, so no additional flags needed here.
    }

    @Override
    public void endUnsafe(boolean parallel) {
        // No special handling required currently.
    }
}
