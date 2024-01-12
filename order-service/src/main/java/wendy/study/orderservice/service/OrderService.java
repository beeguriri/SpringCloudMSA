package wendy.study.orderservice.service;

import wendy.study.orderservice.dto.OrderDto;
import wendy.study.orderservice.entity.OrderEntity;

public interface OrderService {

    OrderDto createOrder(OrderDto orderDetails);

    OrderDto getOrderByOrderId(String orderId);

    Iterable<OrderEntity> getOrdersByUser(String userId);
}
