package io.github.xfacthd.simpleminimap.util;

import net.minecraft.util.Util;
import org.apache.commons.lang3.exception.UncheckedInterruptedException;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.function.UnaryOperator;

public final class ThrottledTaskStore {
    private static final int DEFAULT_THREAD_COUNT_DIVIDER = 4;

    private final Executor throttleExecutor = Executors.newVirtualThreadPerTaskExecutor();
    private final Semaphore throttleSemaphore;
    private final Set<TaskHandle> activeTasks = ConcurrentHashMap.newKeySet();

    public ThrottledTaskStore() {
        this(Math.max(Math.ceilDiv(Util.maxAllowedExecutorThreads(), DEFAULT_THREAD_COUNT_DIVIDER), 1));
    }

    public ThrottledTaskStore(int concurrency) {
        this.throttleSemaphore = new Semaphore(concurrency, true);
    }

    public void addTask(UnaryOperator<CompletableFuture<Void>> futureBuilder) {
        TaskHandle handle = new TaskHandle();
        CompletableFuture<Void> rootFuture = CompletableFuture.runAsync(this::acquireTicket, throttleExecutor);
        handle.future = futureBuilder.apply(rootFuture).handle((_, _) -> finishTask(handle));
    }

    public void cancelTasks() {
        activeTasks.removeIf(task -> {
            task.future.cancel(true);
            return true;
        });
    }

    private void acquireTicket() {
        try {
            throttleSemaphore.acquire();
        } catch (InterruptedException e) {
            throw new UncheckedInterruptedException(e);
        }
    }

    private Void finishTask(TaskHandle handle) {
        activeTasks.remove(handle);
        throttleSemaphore.release();
        return null;
    }

    private static final class TaskHandle {
        private CompletableFuture<Void> future = CompletableFuture.completedFuture(null);
    }
}
