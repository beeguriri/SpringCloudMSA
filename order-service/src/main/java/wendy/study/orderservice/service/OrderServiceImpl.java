package wendy.study.orderservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.stereotype.Service;
import wendy.study.orderservice.dto.OrderDto;
import wendy.study.orderservice.entity.OrderEntity;
import wendy.study.orderservice.repository.OrderRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService{

    private final OrderRepository orderRepository;

    @Override
    public OrderDto getOrderByOrderId(String orderId) {

        OrderEntity orderEntity = orderRepository.findByOrderId(orderId);

        return new ModelMapper().map(orderEntity, OrderDto.class);
    }

    @Override
    public Iterable<OrderEntity> getOrdersByUser(String userId) {
        return orderRepository.findByUserId(userId);
    }

    @Override
    public OrderDto createOrder(OrderDto orderDetails) {

        //orderDto에 관련 데이터 세팅
        String orderId = UUID.randomUUID().toString();
        orderDetails.setOrderId(orderId);
        orderDetails.setTotalPrice(orderDetails.getQty() * orderDetails.getUnitPrice());

        log.info("[Service] orderDto = {}", orderDetails);

        //strict 처리 해주지않으면
        //set_id, get_id 관련 메서드가 각 세개씩 있어서 mapping에러 발생함!!
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        OrderEntity order = mapper.map(orderDetails, OrderEntity.class);
        orderRepository.save(order);

        OrderEntity savedOrder = orderRepository.findByOrderId(orderId);

        return mapper.map(savedOrder, OrderDto.class);
    }
}
