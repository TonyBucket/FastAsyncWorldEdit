package com.fastasyncworldedit.fabric;

import com.fastasyncworldedit.core.util.TaskManager;
import net.minecraft.server.MinecraftServer;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * TaskManager implementation backed by Fabric server tick scheduling.
 */
public class FabricTaskManager extends TaskManager {

    private final ScheduledExecutorService asyncExecutor;
    private final AtomicInteger taskIds = new AtomicInteger(1);
    private final Map<Integer, ScheduledFuture<?>> asyncTasks = new ConcurrentHashMap<>();
    private final Map<Integer, TickTask> syncTasks = new ConcurrentHashMap<>();
    private MinecraftServer server;

    FabricTaskManager() {
        ThreadFactory factory = r -> {
            Thread thread = new Thread(r, "FAWE-Fabric-Async");
            thread.setDaemon(true);
            return thread;
        };
        this.asyncExecutor = Executors.newScheduledThreadPool(4, factory);
    }

    void bind(MinecraftServer server) {
        this.server = server;
    }

    void shutdown() {
        asyncTasks.values().forEach(future -> future.cancel(false));
        asyncTasks.clear();
    }

    void reset() {
        syncTasks.clear();
        asyncTasks.clear();
        this.server = null;
    }

    void tick() {
        if (server == null) {
            return;
        }
        Iterator<Map.Entry<Integer, TickTask>> iterator = syncTasks.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, TickTask> entry = iterator.next();
            TickTask task = entry.getValue();
            if (task.cancelled) {
                iterator.remove();
                continue;
            }
            if (--task.remainingTicks <= 0) {
                task.runnable.run();
                if (task.repeating) {
                    task.remainingTicks = task.intervalTicks;
                } else {
                    iterator.remove();
                }
            }
        }
    }

    @Override
    public int repeat(@Nonnull Runnable runnable, int interval) {
        Objects.requireNonNull(runnable, "runnable");
        int id = taskIds.getAndIncrement();
        syncTasks.put(id, new TickTask(runnable, Math.max(1, interval), true));
        return id;
    }

    @Override
    public int repeatAsync(@Nonnull Runnable runnable, int interval) {
        Objects.requireNonNull(runnable, "runnable");
        int id = taskIds.getAndIncrement();
        int effectiveInterval = Math.max(1, interval);
        ScheduledFuture<?> future = asyncExecutor.scheduleAtFixedRate(
                runnable,
                effectiveInterval * 50L,
                effectiveInterval * 50L,
                TimeUnit.MILLISECONDS
        );
        asyncTasks.put(id, future);
        return id;
    }

    @Override
    public void async(@Nonnull Runnable runnable) {
        Objects.requireNonNull(runnable, "runnable");
        asyncExecutor.execute(runnable);
    }

    @Override
    public void task(@Nonnull Runnable runnable) {
        Objects.requireNonNull(runnable, "runnable");
        if (server == null) {
            runnable.run();
            return;
        }
        if (server.isOnThread()) {
            runnable.run();
        } else {
            server.execute(runnable);
        }
    }

    @Override
    public void later(@Nonnull Runnable runnable, int delay) {
        Objects.requireNonNull(runnable, "runnable");
        int id = taskIds.getAndIncrement();
        syncTasks.put(id, new TickTask(runnable, Math.max(1, delay), false));
    }

    @Override
    public void laterAsync(@Nonnull Runnable runnable, int delay) {
        Objects.requireNonNull(runnable, "runnable");
        asyncExecutor.schedule(runnable, Math.max(0, delay) * 50L, TimeUnit.MILLISECONDS);
    }

    @Override
    public void cancel(int task) {
        TickTask tickTask = syncTasks.remove(task);
        if (tickTask != null) {
            tickTask.cancelled = true;
        }
        ScheduledFuture<?> future = asyncTasks.remove(task);
        if (future != null) {
            future.cancel(false);
        }
    }

    private static class TickTask {
        private final Runnable runnable;
        private final int intervalTicks;
        private int remainingTicks;
        private final boolean repeating;
        private volatile boolean cancelled;

        private TickTask(Runnable runnable, int intervalTicks, boolean repeating) {
            this.runnable = runnable;
            this.intervalTicks = Math.max(1, intervalTicks);
            this.remainingTicks = this.intervalTicks;
            this.repeating = repeating;
        }
    }
}
