package wendy.study.userservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import wendy.study.userservice.client.OrderServiceClient;
import wendy.study.userservice.dto.UserDto;
import wendy.study.userservice.entity.UserEntity;
import wendy.study.userservice.repository.UserRepository;
import wendy.study.userservice.vo.ResponseOrder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService{

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder;
//    private final Environment env;
//    private final RestTemplate restTemplate;
    private final OrderServiceClient orderServiceClient;
    private final CircuitBreakerFactory circuitBreakerFactory;

    @Override
    public void createUser(UserDto userDto) {
        userDto.setUserId(UUID.randomUUID().toString());

        //Mapper를 사용하여 UserDto 객체를 UserEntity로 바꿔주기 (DB에 저장하기 위하여)
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        UserEntity userEntity = modelMapper.map(userDto, UserEntity.class);
        userEntity.setEncryptedPassword(encoder.encode(userDto.getPassword()));
        userRepository.save(userEntity);
    }

    @Override
    public Iterable<UserEntity> getUserByAll() {
        return userRepository.findAll();
    }

    @Override
    public UserDto getUserByUserId(String userId) {

        UserEntity findUser = userRepository.findByUserId(userId);

        if(findUser == null)
            throw new UsernameNotFoundException("사용자가 없습니다.");

        UserDto userDto = new ModelMapper().map(findUser, UserDto.class);

        //1. RestTemplate를 사용하여 Order-service 호출
//        String orderUrl = "http://127.0.0.1:8000/order-service/%s/orders";
//        String orderUrl = String.format(env.getProperty("order-service.url"), userId);
//
//        ResponseEntity<List<ResponseOrder>> orderListResponse = restTemplate.exchange(
//                orderUrl, HttpMethod.GET, null,
//                new ParameterizedTypeReference<>() {}
//        );
//        List<ResponseOrder> orderList = orderListResponse.getBody();

        //2. Openfeign사용하여 order service 호출
        //예외처리 추가
//        List<ResponseOrder> orderList = null;
//        try{
//            orderList = orderServiceClient.getOrders(userId);
//        } catch (FeignException ex) {
//            log.error(ex.getMessage());
//        }

        //1. FeignErrorDecoder 사용
//        List<ResponseOrder> orderList = orderServiceClient.getOrders(userId);

        //2. CircuitBreakerFactory 사용
        //order service에 문제가 생겼을 때 빈 order list를 가져옴
        log.info("Before Call Orders micro service");
        CircuitBreaker circuitbreaker = circuitBreakerFactory.create("circuitbreaker");
        List<ResponseOrder> orderList = circuitbreaker.run(() ->
                            orderServiceClient.getOrders(userId),
                           throwable -> new ArrayList<>()
        );
        log.info("After Called Orders micro service");

        userDto.setOrders(orderList);

        return userDto;
    }

    //security 관련 메서드 상속
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        UserEntity userEntity = userRepository.findByEmail(username);

        if(userEntity == null)
            throw new UsernameNotFoundException(username);

        return new User(
                userEntity.getEmail(), userEntity.getEncryptedPassword(),
                //enabled, accountNonExpired, credentialsNonExpired, accontNonLocked
                true, true, true, true,
                new ArrayList<>()
        );
    }

    @Override
    public UserDto getUserByEmail(String email) {

        UserEntity findUser = userRepository.findByEmail(email);

        if(findUser == null)
            throw new UsernameNotFoundException("사용자가 없습니다.");

        //mapper를 strict로 설정해주지 않았기 때문에
        //UserDto에 password, Encrypted_password 모두 Encrypted_password로 들어감
        return new ModelMapper().map(findUser, UserDto.class);
    }
}
