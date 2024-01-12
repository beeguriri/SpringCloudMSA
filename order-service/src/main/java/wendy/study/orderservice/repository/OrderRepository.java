package wendy.study.orderservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import wendy.study.orderservice.entity.OrderEntity;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    OrderEntity findByOrderId(String orderId);

    Iterable<OrderEntity> findByUserId (String userId);
}
