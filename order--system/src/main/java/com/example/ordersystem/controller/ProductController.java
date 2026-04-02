package com.example.ordersystem.controller;

import com.example.ordersystem.entity.Product;
import com.example.ordersystem.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/product")
public class ProductController {
    
    @Autowired
    private ProductService productService;
    
    @GetMapping("/list")
    public String list(Model model) {
        model.addAttribute("products", productService.getAllProducts());
        return "product/list";
    }
    
    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("product", new Product());
        return "product/form";
    }
    
    @PostMapping("/save")
    @ResponseBody
    public Map<String, Object> save(@RequestParam String productName,
                                    @RequestParam String productType,
                                    @RequestParam BigDecimal price,
                                    @RequestParam Integer stock) {
        Map<String, Object> result = new HashMap<>();
        try {
            Product product = new Product(productName, productType, price, stock);
            productService.createProduct(product);
            result.put("success", true);
            result.put("message", "商品创建任务已提交到队列");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "创建失败: " + e.getMessage());
        }
        return result;
    }
    
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id);
        model.addAttribute("product", product);
        return "product/form";
    }
    
    @PostMapping("/update")
    @ResponseBody
    public Map<String, Object> update(@RequestParam Long id,
                                      @RequestParam String productName,
                                      @RequestParam String productType,
                                      @RequestParam BigDecimal price,
                                      @RequestParam Integer stock) {
        Map<String, Object> result = new HashMap<>();
        try {
            Product product = new Product(productName, productType, price, stock);
            product.setId(id);
            productService.updateProduct(product);
            result.put("success", true);
            result.put("message", "商品更新任务已提交到队列");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "更新失败: " + e.getMessage());
        }
        return result;
    }
    
    @PostMapping("/delete/{id}")
    @ResponseBody
    public Map<String, Object> delete(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        try {
            productService.deleteProduct(id);
            result.put("success", true);
            result.put("message", "商品删除任务已提交到队列");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "删除失败: " + e.getMessage());
        }
        return result;
    }
    
    @GetMapping("/status")
    @ResponseBody
    public Map<String, Object> status() {
        Map<String, Object> result = new HashMap<>();
        result.put("queueSize", productService.getQueueSize());
        result.put("processedCount", productService.getProcessedCount());
        return result;
    }
}
