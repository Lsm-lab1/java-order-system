package com.example.ordersystem.entity;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_order")
public class Order {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "order_no", length = 50, nullable = false, unique = true)
    private String orderNo;
    
    @Column(name = "product_id", nullable = false)
    private Long productId;
    
    @Column(name = "product_name", length = 50)
    private String productName;
    
    @Column(name = "product_type", length = 30)
    private String productType;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal price;
    
    @Column(length = 20)
    private String status = "PENDING";
    
    @Column(name = "generator_id")
    private Integer generatorId;
    
    @Column(name = "shipper_id")
    private Integer shipperId = -1;
    
    @Column(name = "fail_reason", length = 100)
    private String failReason;
    
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_PROCESSING = "PROCESSING";
    public static final String STATUS_COMPLETED = "COMPLETED";
    public static final String STATUS_SHIPPED = "SHIPPED";
    public static final String STATUS_RECEIVED = "RECEIVED";
    public static final String STATUS_CANCELLED = "CANCELLED";
    
    @Column(name = "create_time")
    private LocalDateTime createTime;
    
    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
    }
    
    public Order() {}
    
    public Order(String orderNo, Long productId, String productName, String productType, BigDecimal price, Integer generatorId) {
        this.orderNo = orderNo;
        this.productId = productId;
        this.productName = productName;
        this.productType = productType;
        this.price = price;
        this.generatorId = generatorId;
        this.status = "PENDING";
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getOrderNo() {
        return orderNo;
    }
    
    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }
    
    public Long getProductId() {
        return productId;
    }
    
    public void setProductId(Long productId) {
        this.productId = productId;
    }
    
    public String getProductName() {
        return productName;
    }
    
    public void setProductName(String productName) {
        this.productName = productName;
    }
    
    public String getProductType() {
        return productType;
    }
    
    public void setProductType(String productType) {
        this.productType = productType;
    }
    
    public BigDecimal getPrice() {
        return price;
    }
    
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public Integer getGeneratorId() {
        return generatorId;
    }
    
    public void setGeneratorId(Integer generatorId) {
        this.generatorId = generatorId;
    }
    
    public Integer getShipperId() {
        return shipperId;
    }
    
    public void setShipperId(Integer shipperId) {
        this.shipperId = shipperId;
    }
    
    public String getFailReason() {
        return failReason;
    }
    
    public void setFailReason(String failReason) {
        this.failReason = failReason;
    }
    
    public LocalDateTime getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
    
    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", orderNo='" + orderNo + '\'' +
                ", productName='" + productName + '\'' +
                ", status='" + status + '\'' +
                ", generatorId=" + generatorId +
                ", createTime=" + createTime +
                '}';
    }
}
