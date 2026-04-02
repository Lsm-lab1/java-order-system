package com.example.ordersystem.controller;

import com.example.ordersystem.entity.Order;
import com.example.ordersystem.entity.Product;
import com.example.ordersystem.service.OrderService;
import com.example.ordersystem.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductService productService;

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("products", productService.getAllProducts());
        return "order/create";
    }

    @PostMapping("/submit")
    @ResponseBody
    public Map<String, Object> submit(@RequestParam Long productId, @RequestParam Integer quantity) {
        Map<String, Object> result = new HashMap<>();
        try {
            Product product = productService.getProductById(productId);
            if (product == null) {
                result.put("success", false);
                result.put("message", "Product not found");
                return result;
            }

            if (quantity > product.getStock()) {
                result.put("success", false);
                result.put("message", "Insufficient stock");
                return result;
            }

            orderService.createOrder(product, quantity);
            result.put("success", true);
            result.put("message", "Order submitted to queue");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Submit failed: " + e.getMessage());
        }
        return result;
    }

    @GetMapping("/list")
    public String list(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "20") int size,
                       Model model) {
        int safePage = Math.max(0, page);
        int safeSize = size > 0 ? size : 20;

        Page<Order> orderPage = orderService.getOrdersPage(safePage, safeSize);
        if (safePage >= orderPage.getTotalPages() && orderPage.getTotalPages() > 0) {
            safePage = orderPage.getTotalPages() - 1;
            orderPage = orderService.getOrdersPage(safePage, safeSize);
        }

        model.addAttribute("orders", orderPage.getContent());
        model.addAttribute("currentPage", safePage);
        model.addAttribute("pageSize", safeSize);
        model.addAttribute("totalPages", orderPage.getTotalPages());
        model.addAttribute("totalElements", orderPage.getTotalElements());
        return "order/list";
    }

    @GetMapping("/status")
    @ResponseBody
    public Map<String, Object> status() {
        Map<String, Object> result = new HashMap<>();
        result.put("queueSize", orderService.getQueueSize());
        result.put("processedCount", orderService.getProcessedCount());
        return result;
    }

    @PostMapping("/batch")
    @ResponseBody
    public Map<String, Object> batchCreate(@RequestParam Long productId,
                                           @RequestParam Integer quantity,
                                           @RequestParam Integer count) {
        Map<String, Object> result = new HashMap<>();
        try {
            Product product = productService.getProductById(productId);
            if (product == null) {
                result.put("success", false);
                result.put("message", "Product not found");
                return result;
            }

            for (int i = 0; i < count; i++) {
                orderService.createOrder(product, quantity);
            }

            result.put("success", true);
            result.put("message", "Submitted " + count + " orders to queue");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Batch submit failed: " + e.getMessage());
        }
        return result;
    }

    @PostMapping("/confirm-receive")
    @ResponseBody
    public Map<String, Object> confirmReceive(@RequestParam Long orderId) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = orderService.confirmReceive(orderId);
            result.put("success", success);
            result.put("message", success ? "Receive confirmed" : "Order not found or status invalid");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Confirm failed: " + e.getMessage());
        }
        return result;
    }

    @GetMapping("/shipped")
    @ResponseBody
    public Map<String, Object> getShippedOrders(@RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "20") int size) {
        Map<String, Object> result = new HashMap<>();
        try {
            int safePage = Math.max(0, page);
            int safeSize = size > 0 ? size : 20;

            Page<Order> shippedPage = orderService.findOrdersByStatus(Order.STATUS_SHIPPED, safePage, safeSize);
            if (safePage >= shippedPage.getTotalPages() && shippedPage.getTotalPages() > 0) {
                safePage = shippedPage.getTotalPages() - 1;
                shippedPage = orderService.findOrdersByStatus(Order.STATUS_SHIPPED, safePage, safeSize);
            }

            result.put("orders", shippedPage.getContent());
            result.put("currentPage", shippedPage.getNumber());
            result.put("pageSize", shippedPage.getSize());
            result.put("totalPages", shippedPage.getTotalPages());
            result.put("totalElements", shippedPage.getTotalElements());
            result.put("success", true);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Load shipped orders failed: " + e.getMessage());
        }
        return result;
    }

    @GetMapping("/pending")
    @ResponseBody
    public Map<String, Object> getPendingOrders() {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Order> pendingOrders = orderService.findOrdersByStatus(Order.STATUS_PENDING);
            List<Order> processingOrders = orderService.findOrdersByStatus(Order.STATUS_PROCESSING);

            List<Order> allUnfinishedOrders = new ArrayList<>();
            allUnfinishedOrders.addAll(pendingOrders);
            allUnfinishedOrders.addAll(processingOrders);

            result.put("orders", allUnfinishedOrders);
            result.put("success", true);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Load unfinished orders failed: " + e.getMessage());
        }
        return result;
    }
}
