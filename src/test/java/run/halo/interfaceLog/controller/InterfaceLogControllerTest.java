package run.halo.interfaceLog.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.interfaceLog.endpoint.InterfaceLogEndpoint;
import run.halo.interfaceLog.service.InterfaceLogService;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InterfaceLogControllerTest {

    @Mock
    private ReactiveExtensionClient client;

    @InjectMocks
    private InterfaceLogEndpoint controller;

    @Mock
    private InterfaceLogService interfaceLogService;

    @Test
    void shouldCountInterfaceLogs() throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {


        // given
        long expectedCount = 1;
        when(interfaceLogService.count()).thenReturn(Mono.just(1L));

        // when
        Method count = InterfaceLogEndpoint.class.getDeclaredMethod("count", ServerRequest.class);
        count.setAccessible(true);
        StepVerifier.create((Mono<ServerResponse>) count.invoke(controller, createServerRequest()))
                .assertNext(countResult -> {
                    ServerResponse rep = ServerResponse.ok().bodyValue(expectedCount).block();
                    assertThat(countResult.statusCode()).isEqualTo(rep.statusCode());
                })
                .verifyComplete();
    }

    private ServerRequest createServerRequest() {
        return ServerRequest.create(
                MockServerWebExchange.from(
                        MockServerHttpRequest.get("/")), new ArrayList<>());
    }


} 