package com.example.ordersystem.repository;

import com.example.ordersystem.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByOrderByCreateTimeDesc();
    List<Order> findByStatus(String status);
    Page<Order> findByStatusOrderByCreateTimeDesc(String status, Pageable pageable);
    Order findByOrderNo(String orderNo);
}
