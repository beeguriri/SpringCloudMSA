package wendy.study.userservice.service;

import wendy.study.userservice.dto.UserDto;
import wendy.study.userservice.entity.UserEntity;

public interface UserService {
    void createUser(UserDto userDto);
    Iterable<UserEntity> getUserByAll();
    UserDto getUserByUserId(String userId);

}
