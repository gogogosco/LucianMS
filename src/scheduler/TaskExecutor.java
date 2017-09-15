package scheduler;

import java.util.HashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author izarooni
 */
public final class TaskExecutor {

    private static final HashMap<Integer, Task> TASKS = new HashMap<>();
    private static final ScheduledThreadPoolExecutor EXECUTOR = new ScheduledThreadPoolExecutor(6, new ThreadFactory() {
        private int threadId = 0;

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "LTask" + (++threadId));
        }
    });

    private static AtomicInteger atomicInteger = new AtomicInteger(1);
    private static Lock lock = new ReentrantLock(true);

    static {
        EXECUTOR.prestartAllCoreThreads();
    }

    private static Task setupTask(ScheduledFuture<?> future) {
        lock.lock();
        try {
            final int id = atomicInteger.getAndIncrement();
            Task task = new Task(future) {
                @Override
                public int getId() {
                    return id;
                }
            };
            if (!TASKS.containsKey(id)) {
                TASKS.put(id, task);
                return task;
            }
            throw new RuntimeException(String.format("Created task with already existing id(%d)", id));
        } finally {
            lock.unlock();
        }
    }

    public static void destroy() {
        lock.lock();
        try {
            EXECUTOR.shutdownNow();
            EXECUTOR.purge();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Creates a task that executes after a specified time
     *
     * @param r a runnable interface
     * @param d the delay before the task begins execution
     * @return A {@code Task} object which is a wrapper for the {@link ScheduledFuture} object
     */
    public static Task createTask(Runnable r, long d) {
        return setupTask(EXECUTOR.schedule(r, d, TimeUnit.MILLISECONDS));
    }

    /**
     * Create a task that infinitely repeats until stopped by invoking the {@link Task#cancel()} method
     *
     * @param r a runnable interface
     * @param i the delay before the task begins its first execution
     * @param d the time between each execution
     * @return A {@code Task} object which is a wrapper for the {@link ScheduledFuture} object
     */
    public static Task createRepeatingTask(Runnable r, long i, long d) {
        return setupTask(EXECUTOR.scheduleWithFixedDelay(r, i, d, TimeUnit.MILLISECONDS));
    }

    /**
     * Create a task that infinitely repeats until stopped by invoking the {@link Task#cancel()} method
     *
     * @param r a runnable interface
     * @param t the interval time and delay in milliseconds the task will execute
     * @return a {@code Task} object which is a wrapper for the {@link ScheduledFuture} object
     */
    public static Task createRepeatingTask(Runnable r, long t) {
        return setupTask(EXECUTOR.scheduleWithFixedDelay(r, t, t, TimeUnit.MILLISECONDS));
    }


    /**
     * Cancels the specified task
     *
     * @param id the id of the task to cancel
     */
    public static void cancelTask(int id) {
        lock.lock();
        try {
            if (TASKS.containsKey(id)) {
                TASKS.get(id).cancel();
            }
        } finally {
            lock.unlock();
        }
    }

    public static Task getTask(int id) {
        lock.lock();
        try {
            return TASKS.get(id);
        } finally {
            lock.unlock();
        }
    }
}
