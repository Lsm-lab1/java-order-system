package com.example.ordersystem.concurrent;

import com.example.ordersystem.entity.Order;
import com.example.ordersystem.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class OrderConsumer {

    private final BlockingQueue<OrderTask> orderQueue = new LinkedBlockingQueue<>(100);
    private final AtomicInteger processedCount = new AtomicInteger(0);
    private volatile boolean running = true;
    private final List<String> logs = new ArrayList<>();
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private static final int MAX_LOGS = 500;

    private final Set<String> cancelledOrders = ConcurrentHashMap.newKeySet();
    private final Map<String, Thread> processingThreads = new ConcurrentHashMap<>();
    private final List<Thread> consumerThreads = new ArrayList<>();

    @Autowired
    private OrderRepository orderRepository;

    @PostConstruct
    public void init() {
        for (int i = 1; i <= 3; i++) {
            final int threadId = i;
            Thread consumerThread = new Thread(() -> consume(threadId), "Order-Consumer-Thread-" + i);
            consumerThread.setDaemon(true);
            consumerThread.start();
            consumerThreads.add(consumerThread);
            addLog("SYSTEM", "Order consumer thread " + i + " started");
        }
    }

    public void produce(Order order, int threadId) throws InterruptedException {
        OrderTask task = new OrderTask(order, threadId);
        orderQueue.put(task);
        addLog("PRODUCER", "Order queued: " + order.getOrderNo() + " (thread " + threadId + ")");
    }

    private void consume(int threadId) {
        while (running) {
            try {
                OrderTask task = orderQueue.take();
                processTask(task, threadId);
            } catch (InterruptedException e) {
                if (!running) {
                    Thread.currentThread().interrupt();
                    addLog("THREAD-" + threadId, "Consumer stopped");
                    break;
                }
            } catch (Exception e) {
                addLog("THREAD-" + threadId, "Order process error: " + e.getMessage());
            }
        }
    }

    private void processTask(OrderTask task, int threadId) {
        Order taskOrder = task.getOrder();
        String orderNo = taskOrder.getOrderNo();
        processingThreads.put(orderNo, Thread.currentThread());

        try {
            if (cancelledOrders.contains(orderNo)) {
                addLog("THREAD-" + threadId, "Order already cancelled, skip: " + orderNo);
                return;
            }

            Order dbOrder = orderRepository.findByOrderNo(orderNo);
            Order order = dbOrder != null ? dbOrder : taskOrder;

            order.setGeneratorId(threadId);
            order.setStatus(Order.STATUS_PROCESSING);
            orderRepository.save(order);
            addLog("THREAD-" + threadId, "Processing order: " + orderNo);

            Thread.sleep(3000);

            if (cancelledOrders.contains(orderNo)) {
                Order latest = orderRepository.findByOrderNo(orderNo);
                if (latest != null) {
                    latest.setStatus(Order.STATUS_CANCELLED);
                    orderRepository.save(latest);
                }
                addLog("THREAD-" + threadId, "Order cancelled during processing: " + orderNo);
                return;
            }

            order.setStatus(Order.STATUS_SHIPPED);
            order.setShipperId(threadId);
            Order saved = orderRepository.save(order);

            int count = processedCount.incrementAndGet();
            addLog("THREAD-" + threadId, "Order shipped: " + saved.getOrderNo() + ", processed: " + count);
        } catch (InterruptedException e) {
            if (cancelledOrders.contains(orderNo)) {
                Order latest = orderRepository.findByOrderNo(orderNo);
                if (latest != null) {
                    latest.setStatus(Order.STATUS_CANCELLED);
                    orderRepository.save(latest);
                }
                addLog("THREAD-" + threadId, "Order interrupted and cancelled: " + orderNo);
                return;
            }
            Thread.currentThread().interrupt();
            addLog("THREAD-" + threadId, "Thread interrupted");
        } finally {
            processingThreads.remove(orderNo);
        }
    }

    public synchronized void addLog(String source, String message) {
        String timestamp = sdf.format(System.currentTimeMillis());
        String logEntry = "[" + timestamp + "] [" + source + "] " + message;
        logs.add(logEntry);

        if (logs.size() > MAX_LOGS) {
            logs.remove(0);
        }
    }

    public synchronized List<String> getLogs() {
        return new ArrayList<>(logs);
    }

    public synchronized List<String> getRecentLogs(int count) {
        int start = Math.max(0, logs.size() - count);
        return new ArrayList<>(logs.subList(start, logs.size()));
    }

    public synchronized void clearLogs() {
        logs.clear();
    }

    public int getQueueSize() {
        return orderQueue.size();
    }

    public int getProcessedCount() {
        return processedCount.get();
    }

    public int getLogCount() {
        return logs.size();
    }

    public boolean cancelOrder(String orderNo) {
        cancelledOrders.add(orderNo);

        boolean removedFromQueue = orderQueue.removeIf(task -> orderNo.equals(task.getOrder().getOrderNo()));
        if (removedFromQueue) {
            addLog("SYSTEM", "Order removed from queue: " + orderNo);
        }

        Thread processingThread = processingThreads.get(orderNo);
        if (processingThread != null) {
            processingThread.interrupt();
            addLog("SYSTEM", "Interrupted processing thread for order: " + orderNo);
        }

        boolean updatedDb = false;
        try {
            Order order = orderRepository.findByOrderNo(orderNo);
            if (order != null && (Order.STATUS_PENDING.equals(order.getStatus()) || Order.STATUS_PROCESSING.equals(order.getStatus()))) {
                order.setStatus(Order.STATUS_CANCELLED);
                orderRepository.save(order);
                updatedDb = true;
                addLog("SYSTEM", "Order status set to CANCELLED in database: " + orderNo);
            }
        } catch (Exception e) {
            addLog("SYSTEM", "Cancel order error: " + e.getMessage());
        }

        return removedFromQueue || processingThread != null || updatedDb;
    }

    public void stop() {
        running = false;
        for (Thread thread : consumerThreads) {
            thread.interrupt();
        }
    }
}
