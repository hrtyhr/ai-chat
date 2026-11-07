package com.howcode.aichat.infrastructure.persistence;

import com.howcode.aichat.core.domain.model.UserInfo;
import com.howcode.aichat.core.port.out.UserRepository;
import com.howcode.aichat.infrastructure.persistence.entity.UserEntity;
import com.howcode.aichat.infrastructure.persistence.mapper.UserMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

/** MySQL 用户仓储适配器。 */
@Component
public class MyBatisUserRepository implements UserRepository {

    @Resource
    private UserMapper userMapper;

    @Override
    public void save(UserInfo user) {
        UserEntity entity = new UserEntity();
        entity.setUserId(user.getUserId());
        entity.setUserName(user.getUserName());
        entity.setUserAvatar(user.getUserAvatar());
        userMapper.insert(entity);
    }

    @Override
    public UserInfo findById(String userId) {
        UserEntity entity = userMapper.selectById(userId);
        if (null == entity) {
            return null;
        }
        UserInfo user = new UserInfo();
        user.setUserId(entity.getUserId());
        user.setUserName(entity.getUserName());
        user.setUserAvatar(entity.getUserAvatar());
        return user;
    }
}
