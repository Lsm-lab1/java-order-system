package com.example.ordersystem.service;

import com.example.ordersystem.concurrent.ProductConsumer;
import com.example.ordersystem.concurrent.ProductTask;
import com.example.ordersystem.entity.Product;
import com.example.ordersystem.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private ProductConsumer productConsumer;
    
    public void createProduct(Product product) throws InterruptedException {
        ProductTask task = ProductTask.create(product);
        productConsumer.produce(task);
    }
    
    public void updateProduct(Product product) throws InterruptedException {
        ProductTask task = ProductTask.update(product);
        productConsumer.produce(task);
    }
    
    public void deleteProduct(Long id) throws InterruptedException {
        ProductTask task = ProductTask.delete(id);
        productConsumer.produce(task);
    }
    
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }
    
    public Product getProductById(Long id) {
        return productRepository.findById(id).orElse(null);
    }
    
    public int getQueueSize() {
        return productConsumer.getQueueSize();
    }
    
    public int getProcessedCount() {
        return productConsumer.getProcessedCount();
    }
}
