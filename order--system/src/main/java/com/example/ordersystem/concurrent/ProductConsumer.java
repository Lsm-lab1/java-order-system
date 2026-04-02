package com.example.ordersystem.concurrent;

import com.example.ordersystem.entity.Product;
import com.example.ordersystem.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class ProductConsumer {
    
    private final BlockingQueue<ProductTask> productQueue = new LinkedBlockingQueue<>(100);
    private final AtomicInteger processedCount = new AtomicInteger(0);
    private volatile boolean running = true;
    
    @Autowired
    private ProductRepository productRepository;
    
    @PostConstruct
    public void init() {
        Thread consumerThread = new Thread(this::consume, "Product-Consumer-Thread");
        consumerThread.setDaemon(true);
        consumerThread.start();
        System.out.println("[消费者线程] 商品消费者线程已启动");
    }
    
    public void produce(ProductTask task) throws InterruptedException {
        productQueue.put(task);
        System.out.println("[生产者] 任务已加入队列: " + task.getType() + " - " + 
            (task.getProduct() != null ? task.getProduct().getProductName() : "ID:" + task.getProductId()));
    }
    
    private void consume() {
        while (running) {
            try {
                ProductTask task = productQueue.take();
                System.out.println("[消费者线程] 取出任务: " + task.getType());
                
                synchronized (this) {
                    switch (task.getType()) {
                        case CREATE:
                            createProduct(task.getProduct());
                            break;
                        case UPDATE:
                            updateProduct(task.getProduct());
                            break;
                        case DELETE:
                            deleteProduct(task.getProductId());
                            break;
                    }
                }
                
                int count = processedCount.incrementAndGet();
                System.out.println("[消费者线程] 已处理任务数: " + count);
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("[消费者线程] 被中断");
                break;
            } catch (Exception e) {
                System.out.println("[消费者线程] 处理任务出错: " + e.getMessage());
            }
        }
    }
    
    private void createProduct(Product product) {
        System.out.println("[消费者线程] 正在创建商品: " + product.getProductName());
        Product saved = productRepository.save(product);
        System.out.println("[消费者线程] 商品创建成功, ID: " + saved.getId());
    }
    
    private void updateProduct(Product product) {
        System.out.println("[消费者线程] 正在更新商品: " + product.getId());
        Product existing = productRepository.findById(product.getId()).orElse(null);
        if (existing != null) {
            existing.setProductName(product.getProductName());
            existing.setProductType(product.getProductType());
            existing.setPrice(product.getPrice());
            existing.setStock(product.getStock());
            productRepository.save(existing);
            System.out.println("[消费者线程] 商品更新成功");
        } else {
            System.out.println("[消费者线程] 商品不存在: " + product.getId());
        }
    }
    
    private void deleteProduct(Long productId) {
        System.out.println("[消费者线程] 正在删除商品: " + productId);
        productRepository.deleteById(productId);
        System.out.println("[消费者线程] 商品删除成功");
    }
    
    public int getQueueSize() {
        return productQueue.size();
    }
    
    public int getProcessedCount() {
        return processedCount.get();
    }
    
    public void stop() {
        running = false;
    }
}
