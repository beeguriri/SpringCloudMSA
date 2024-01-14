package wendy.study.userservice.service;

import org.springframework.security.core.userdetails.UserDetailsService;
import wendy.study.userservice.dto.UserDto;
import wendy.study.userservice.entity.UserEntity;

public interface UserService extends UserDetailsService {
    void createUser(UserDto userDto);
    Iterable<UserEntity> getUserByAll();
    UserDto getUserByUserId(String userId);

}
