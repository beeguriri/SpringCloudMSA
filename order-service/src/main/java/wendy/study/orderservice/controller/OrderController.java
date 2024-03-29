package wendy.study.orderservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import wendy.study.orderservice.dto.OrderDto;
import wendy.study.orderservice.entity.OrderEntity;
import wendy.study.orderservice.message.KafkaProducer;
import wendy.study.orderservice.message.OrderProducer;
import wendy.study.orderservice.service.OrderService;
import wendy.study.orderservice.vo.RequestOrder;
import wendy.study.orderservice.vo.ResponseOrder;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/order-service")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final Environment env;
    private final OrderService orderService;
    private final KafkaProducer kafkaProducer;
    private final OrderProducer orderProducer;

    @GetMapping("/health_check")
    public String status() {
        return String.format("It's Working in Order Service on PORT [%s]",
                env.getProperty("local.server.port"));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ResponseOrder> getOrder(@PathVariable("orderId") String orderId) {
        OrderDto orderDto = orderService.getOrderByOrderId(orderId);
        ResponseOrder responseOrder = new ModelMapper().map(orderDto, ResponseOrder.class);

        return ResponseEntity.status(HttpStatus.OK).body(responseOrder);
    }

    @GetMapping("/{userId}/orders")
    public ResponseEntity<List<ResponseOrder>> getOrders(@PathVariable("userId") String userId) throws Exception {

        log.info("Before retrieve Orders data");

        Iterable<OrderEntity> orders = orderService.getOrdersByUser(userId);
        List<ResponseOrder> result = new ArrayList<>();

        orders.forEach(
                order -> result.add(new ModelMapper().map(order, ResponseOrder.class))
        );

        //zipkin 강제 예외 발생 test
//        try {
//            Thread.sleep(1000);
//            throw new Exception("강제 장애 발생");
//        } catch (InterruptedException e) {
//            log.error(e.getMessage());
//        }

        log.info("After received Orders data");

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @PostMapping("/{userId}/orders")
    public ResponseEntity<ResponseOrder> createOrder (@PathVariable("userId") String userId,
                                                     @RequestBody RequestOrder requestOrder) {

        log.info("[Controller] requestOrder = {}", requestOrder);
        log.info("Before add Orders data");

        //모델 객체 두번 쓸거니까...!!
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        OrderDto orderDto = mapper.map(requestOrder, OrderDto.class);
        orderDto.setUserId(userId);

        log.info("[Controller] orderDto = {}", orderDto);

        /* jpa 사용 */
        OrderDto savedOrder = orderService.createOrder(orderDto);
        ResponseOrder responseOrder = mapper.map(savedOrder, ResponseOrder.class);

        /*kafka 사용 */
//        orderDto.setOrderId(UUID.randomUUID().toString());
//        orderDto.setTotalPrice(requestOrder.getQty() * requestOrder.getUnitPrice());
//
//        /* kafka에 메시지 전달 */
        kafkaProducer.send("example-catalog-topic", orderDto);
//        orderProducer.send("orders", orderDto);

//        ResponseOrder responseOrder = mapper.map(orderDto, ResponseOrder.class);

        log.info("After added Orders data");

        return ResponseEntity.status(HttpStatus.CREATED).body(responseOrder);
    }
}

