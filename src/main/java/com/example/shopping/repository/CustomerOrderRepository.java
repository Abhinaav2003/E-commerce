package com.example.shopping.repository;

import com.example.shopping.entity.CustomerOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, Long> {
    List<CustomerOrder> findByCustomerUsernameOrderByOrderDateDesc(String username);
    List<CustomerOrder> findAllByOrderByOrderDateDesc();
}
