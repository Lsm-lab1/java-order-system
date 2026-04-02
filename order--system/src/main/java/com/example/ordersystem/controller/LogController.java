package com.example.ordersystem.controller;

import com.example.ordersystem.service.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/log")
public class LogController {
    
    @Autowired
    private LogService logService;
    
    @GetMapping("/all")
    public Map<String, Object> getAllLogs() {
        Map<String, Object> result = new HashMap<>();
        result.put("logs", logService.getAllLogs());
        result.put("count", logService.getLogCount());
        return result;
    }
    
    @GetMapping("/recent")
    public Map<String, Object> getRecentLogs(@RequestParam(defaultValue = "50") int count) {
        Map<String, Object> result = new HashMap<>();
        result.put("logs", logService.getRecentLogs(count));
        result.put("count", logService.getLogCount());
        return result;
    }
    
    @PostMapping("/clear")
    public Map<String, Object> clearLogs() {
        Map<String, Object> result = new HashMap<>();
        logService.clearLogs();
        result.put("success", true);
        result.put("message", "日志已清空");
        return result;
    }
    
    @GetMapping("/count")
    public Map<String, Object> getLogCount() {
        Map<String, Object> result = new HashMap<>();
        result.put("count", logService.getLogCount());
        return result;
    }
    
    @PostMapping("/cancel")
    public Map<String, Object> cancelOrder(@RequestParam String orderNo) {
        Map<String, Object> result = new HashMap<>();
        boolean success = logService.cancelOrder(orderNo);
        result.put("success", success);
        result.put("message", success ? "订单取消成功" : "订单未找到或已处理");
        return result;
    }
}
