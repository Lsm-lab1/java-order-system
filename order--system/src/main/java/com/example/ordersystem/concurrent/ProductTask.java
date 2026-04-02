package com.example.ordersystem.concurrent;

import com.example.ordersystem.entity.Product;

public class ProductTask {
    
    public enum TaskType {
        CREATE, UPDATE, DELETE
    }
    
    private final TaskType type;
    private final Product product;
    private final Long productId;
    
    private ProductTask(TaskType type, Product product, Long productId) {
        this.type = type;
        this.product = product;
        this.productId = productId;
    }
    
    public static ProductTask create(Product product) {
        return new ProductTask(TaskType.CREATE, product, null);
    }
    
    public static ProductTask update(Product product) {
        return new ProductTask(TaskType.UPDATE, product, null);
    }
    
    public static ProductTask delete(Long productId) {
        return new ProductTask(TaskType.DELETE, null, productId);
    }
    
    public TaskType getType() {
        return type;
    }
    
    public Product getProduct() {
        return product;
    }
    
    public Long getProductId() {
        return productId;
    }
    
    @Override
    public String toString() {
        return "ProductTask{" +
                "type=" + type +
                ", product=" + product +
                ", productId=" + productId +
                '}';
    }
}
