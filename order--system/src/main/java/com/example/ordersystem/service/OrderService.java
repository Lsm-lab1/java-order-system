package com.example.ordersystem.service;

import com.example.ordersystem.concurrent.OrderConsumer;
import com.example.ordersystem.entity.Order;
import com.example.ordersystem.entity.Product;
import com.example.ordersystem.repository.OrderRepository;
import com.example.ordersystem.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderConsumer orderConsumer;

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    private final AtomicInteger orderSequence = new AtomicInteger(0);
    private final Random random = new Random();

    public void createOrder(Product product, int quantity) throws InterruptedException {
        if (product.getStock() < quantity) {
            throw new RuntimeException("Insufficient stock");
        }

        product.setStock(product.getStock() - quantity);
        orderConsumer.addLog("SYSTEM", "Stock reduced: " + product.getProductName() + " (" + quantity + ")");
        productRepository.save(product);

        String orderNo = generateOrderNo(product.getProductType());

        int threadId = random.nextInt(3) + 1;

        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setProductId(product.getId());
        order.setProductName(product.getProductName());
        order.setProductType(product.getProductType());
        order.setPrice(product.getPrice().multiply(new BigDecimal(quantity)));
        order.setStatus(Order.STATUS_PENDING);
        order.setGeneratorId(threadId);

        // Persist pending order first so it can be listed/cancelled before shipping.
        order = orderRepository.save(order);
        orderConsumer.produce(order, threadId);
    }

    private String generateOrderNo(String productType) {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String typeCode = productType.length() >= 2 ? productType.substring(0, 2).toUpperCase() : "XX";
        int seq = orderSequence.incrementAndGet();
        return typeCode + dateStr + String.format("%04d", seq);
    }

    public List<Order> getAllOrders() {
        List<Order> allOrders = orderRepository.findByOrderByCreateTimeDesc();
        if (allOrders.size() > 100) {
            return allOrders.subList(0, 100);
        }
        return allOrders;
    }

    public Page<Order> getOrdersPage(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));
        return orderRepository.findAll(pageable);
    }

    public int getQueueSize() {
        return orderConsumer.getQueueSize();
    }

    public int getProcessedCount() {
        return orderConsumer.getProcessedCount();
    }

    public boolean confirmReceive(Long orderId) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order != null && Order.STATUS_SHIPPED.equals(order.getStatus())) {
            order.setStatus(Order.STATUS_RECEIVED);
            orderRepository.save(order);
            return true;
        }
        return false;
    }

    public List<Order> findOrdersByStatus(String status) {
        return orderRepository.findByStatus(status);
    }

    public Page<Order> findOrdersByStatus(String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return orderRepository.findByStatusOrderByCreateTimeDesc(status, pageable);
    }
}
