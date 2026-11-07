package com.howcode.aichat.core.port.in;

import com.howcode.aichat.core.domain.model.UserInfo;

/** 入站端口：用户用例。 */
public interface UserUseCase {
    UserInfo login(String userName, String userAvatar);

    void logout(String userId);

    void offline(String userId);
}
