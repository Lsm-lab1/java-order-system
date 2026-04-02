package com.example.ordersystem.service;

import com.example.ordersystem.concurrent.OrderConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LogService {
    
    @Autowired
    private OrderConsumer orderConsumer;
    
    public List<String> getAllLogs() {
        return orderConsumer.getLogs();
    }
    
    public List<String> getRecentLogs(int count) {
        return orderConsumer.getRecentLogs(count);
    }
    
    public void clearLogs() {
        orderConsumer.clearLogs();
    }
    
    public int getLogCount() {
        return orderConsumer.getLogCount();
    }
    
    public boolean cancelOrder(String orderNo) {
        return orderConsumer.cancelOrder(orderNo);
    }
}
