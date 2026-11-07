package com.howcode.aichat.core.port.out;

import com.howcode.aichat.core.domain.model.UserInfo;

/** 出站端口：用户持久化（MySQL）。 */
public interface UserRepository {
    void save(UserInfo user);

    UserInfo findById(String userId);
}
