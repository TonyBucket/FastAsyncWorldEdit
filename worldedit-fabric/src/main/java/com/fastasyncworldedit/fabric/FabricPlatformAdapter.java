package com.fastasyncworldedit.fabric;

import com.fastasyncworldedit.core.FAWEPlatformAdapterImpl;
import com.fastasyncworldedit.core.queue.IChunkGet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Placeholder Fabric adapter for FAWE platform specifics.
 */
public class FabricPlatformAdapter implements FAWEPlatformAdapterImpl {

    private static final Logger LOGGER = LogManager.getLogger();
    private volatile boolean warned;

    @Override
    public void sendChunk(IChunkGet chunk, int mask, boolean lighting) {
        if (!warned) {
            warned = true;
            LOGGER.warn("Chunk sending is not yet implemented for Fabric FAWE. Updates may not be visible to clients immediately.");
        }
    }
}
