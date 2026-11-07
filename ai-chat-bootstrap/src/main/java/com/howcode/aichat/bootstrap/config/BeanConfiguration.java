package com.howcode.aichat.bootstrap.config;

import com.howcode.aichat.core.application.ChatApplicationService;
import com.howcode.aichat.core.application.RoomApplicationService;
import com.howcode.aichat.core.application.UserApplicationService;
import com.howcode.aichat.core.port.in.ChatUseCase;
import com.howcode.aichat.core.port.in.RoomUseCase;
import com.howcode.aichat.core.port.in.UserUseCase;
import com.howcode.aichat.core.port.out.AiGateway;
import com.howcode.aichat.core.port.out.IdGenerator;
import com.howcode.aichat.core.port.out.MessagePublisher;
import com.howcode.aichat.core.port.out.MessageRepository;
import com.howcode.aichat.core.port.out.RoomStore;
import com.howcode.aichat.core.port.out.SessionGateway;
import com.howcode.aichat.core.port.out.UserRepository;
import com.howcode.aichat.core.port.out.UserSessionStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executor;

/**
 * 六边形架构的装配层：把 core 中不依赖框架的应用服务实例化为 Bean，
 * 并注入由 infrastructure / transport 提供的端口实现。
 */
@Configuration
public class BeanConfiguration {

    @Bean
    public UserUseCase userUseCase(UserRepository userRepository,
                                   UserSessionStore userSessionStore,
                                   RoomStore roomStore,
                                   IdGenerator idGenerator,
                                   SessionGateway sessionGateway) {
        return new UserApplicationService(userRepository, userSessionStore, roomStore, idGenerator, sessionGateway);
    }

    @Bean
    public RoomUseCase roomUseCase(RoomStore roomStore,
                                   UserSessionStore userSessionStore,
                                   IdGenerator idGenerator,
                                   SessionGateway sessionGateway) {
        return new RoomApplicationService(roomStore, userSessionStore, idGenerator, sessionGateway);
    }

    @Bean
    public ChatUseCase chatUseCase(RoomStore roomStore,
                                   UserSessionStore userSessionStore,
                                   MessageRepository messageRepository,
                                   IdGenerator idGenerator,
                                   MessagePublisher messagePublisher,
                                   SessionGateway sessionGateway,
                                   AiGateway aiGateway,
                                   @Qualifier("chatExecutor") Executor chatExecutor) {
        return new ChatApplicationService(roomStore, userSessionStore, messageRepository, idGenerator,
                messagePublisher, sessionGateway, aiGateway, chatExecutor);
    }
}
