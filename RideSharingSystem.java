import java.util.*;
import java.util.concurrent.*;

class SharedQueue {
    private BlockingQueue<String> taskQueue;

    public SharedQueue(int capacity) {
        this.taskQueue = new LinkedBlockingQueue<>(capacity);
    }

    public void addTask(String task) throws InterruptedException {
        taskQueue.put(task);
    }

    public String getTask() throws InterruptedException {
        return taskQueue.take();
    }
}

class WorkerThread extends Thread {
    private static final String POISON_PILL = "POISON";
    private SharedQueue queue;
    private List<String> resultList;

    public WorkerThread(SharedQueue queue, List<String> resultList) {
        this.queue = queue;
        this.resultList = resultList;
    }

    @Override
    public void run() {
        try {
            while (true) {
                String task = queue.getTask();

                if (task.equals(POISON_PILL)) {
                    System.out.println(Thread.currentThread().getName() + " received poison pill. Exiting.");
                    break;
                }

                System.out.println(Thread.currentThread().getName() + " processing task: " + task);
                Thread.sleep(1000); // Simulate work

                synchronized (resultList) {
                    resultList.add("Result from " + Thread.currentThread().getName() + ": " + task);
                }

                System.out.println(Thread.currentThread().getName() + " completed task: " + task);
            }
        } catch (InterruptedException e) {
            System.err.println("Error in thread " + Thread.currentThread().getName() + ": " + e.getMessage());
        }
    }

    public static String getPoisonPill() {
        return POISON_PILL;
    }
}

public class RideSharingSystem {
    public static void main(String[] args) throws InterruptedException {
        int numWorkers = 4;
        SharedQueue queue = new SharedQueue(20);
        List<String> resultList = Collections.synchronizedList(new ArrayList<>());

        // Add 10 tasks
        for (int i = 1; i <= 10; i++) {
            queue.addTask("Task " + i);
        }

        // Add one poison pill per worker to terminate them
        for (int i = 0; i < numWorkers; i++) {
            queue.addTask(WorkerThread.getPoisonPill());
        }

        // Start workers
        List<WorkerThread> workers = new ArrayList<>();
        for (int i = 0; i < numWorkers; i++) {
            WorkerThread worker = new WorkerThread(queue, resultList);
            workers.add(worker);
            worker.start();
        }

        // Wait for all workers
        for (WorkerThread worker : workers) {
            worker.join();
        }

        // Print results
        System.out.println("\nFinal Results:");
        for (String result : resultList) {
            System.out.println(result);
        }
    }
}