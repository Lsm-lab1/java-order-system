package com.example.ordersystem.concurrent;

import com.example.ordersystem.entity.Order;

public class OrderTask {
    
    private final Order order;
    private final int threadId;
    
    public OrderTask(Order order, int threadId) {
        this.order = order;
        this.threadId = threadId;
    }
    
    public Order getOrder() {
        return order;
    }
    
    public int getThreadId() {
        return threadId;
    }
    
    @Override
    public String toString() {
        return "OrderTask{" +
                "orderNo='" + order.getOrderNo() + '\'' +
                ", threadId=" + threadId +
                '}';
    }
}
