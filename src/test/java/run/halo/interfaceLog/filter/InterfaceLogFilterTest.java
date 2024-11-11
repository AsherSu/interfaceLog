//package run.halo.dailyActive.filter;
//
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.http.MediaType;
//import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
//import org.springframework.mock.web.server.MockServerWebExchange;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContext;
//import org.springframework.security.web.server.context.ServerSecurityContextRepository;
//import org.springframework.web.server.ServerWebExchange;
//import org.springframework.web.server.WebFilterChain;
//import reactor.core.publisher.Flux;
//import reactor.core.publisher.Mono;
//import reactor.test.StepVerifier;
//import run.halo.app.extension.ReactiveExtensionClient;
//import run.halo.dailyActive.extension.InterfaceLogRuleInfo;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class InterfaceLogFilterTest {
//
//    @Mock
//    private ReactiveExtensionClient client;
//
//    @Mock
//    private ServerSecurityContextRepository securityContextRepository;
//
//    @InjectMocks
//    private InterfaceLogFilter filter;
//
//    @Test
//    void shouldFilterMatchedPath() {
//        // given
//        ServerWebExchange exchange = MockServerWebExchange.from(
//            MockServerHttpRequest.get("/api/test").build()
//        );
//        WebFilterChain chain = mock(WebFilterChain.class);
//
//        // 模拟规则匹配
//        InterfaceLogRuleInfo rule = new InterfaceLogRuleInfo();
//        rule.setSpec(new InterfaceLogRuleInfo.InterfaceLogRuleInfoSpec());
//        rule.getSpec().setRule("/api/**");
//        rule.getSpec().setIsInclude(true);
//
//        when(client.listAll(eq(InterfaceLogRuleInfo.class), any(), any()))
//            .thenReturn(Flux.just(rule));
//        when(chain.filter(any())).thenReturn(Mono.empty());
//        when(securityContextRepository.load(any()))
//            .thenReturn(Mono.just(createSecurityContext("testUser")));
//
//        // when
//        StepVerifier.create(filter.filter(exchange, chain))
//            .verifyComplete();
//
//        // verify
//        verify(client).listAll(eq(InterfaceLogRuleInfo.class), any(), any());
//        verify(chain).filter(any());
//    }
//
//    @Test
//    void shouldSkipExcludedPath() {
//        // given
//        ServerWebExchange exchange = MockServerWebExchange.from(
//            MockServerHttpRequest.get("/excluded").build()
//        );
//        WebFilterChain chain = mock(WebFilterChain.class);
//
//        InterfaceLogRuleInfo rule = new InterfaceLogRuleInfo();
//        rule.setSpec(new InterfaceLogRuleInfo.InterfaceLogRuleInfoSpec());
//        rule.getSpec().setRule("/excluded/**");
//        rule.getSpec().setIsInclude(false);
//
//        when(client.listAll(eq(InterfaceLogRuleInfo.class), any(), any()))
//            .thenReturn(Flux.just(rule));
//        when(chain.filter(exchange)).thenReturn(Mono.empty());
//
//        // when
//        StepVerifier.create(filter.filter(exchange, chain))
//            .verifyComplete();
//
//        // verify
//        verify(chain).filter(exchange);
//    }
//
//    @Test
//    void shouldHandleRequestWithBody() {
//        // given
//        String requestBody = "{\"key\":\"value\"}";
//        ServerWebExchange exchange = MockServerWebExchange.from(
//            MockServerHttpRequest.post("/api/test")
//                .contentType(MediaType.APPLICATION_JSON)
//                .body(requestBody)
//        );
//        WebFilterChain chain = mock(WebFilterChain.class);
//
//        InterfaceLogRuleInfo rule = new InterfaceLogRuleInfo();
//        rule.setSpec(new InterfaceLogRuleInfo.InterfaceLogRuleInfoSpec());
//        rule.getSpec().setRule("/api/**");
//        rule.getSpec().setIsInclude(true);
//
//        when(client.listAll(eq(InterfaceLogRuleInfo.class), any(), any()))
//            .thenReturn(Flux.just(rule));
//        when(chain.filter(any())).thenReturn(Mono.empty());
//        when(securityContextRepository.load(any()))
//            .thenReturn(Mono.just(createSecurityContext("testUser")));
//
//        // when
//        StepVerifier.create(filter.filter(exchange, chain))
//            .verifyComplete();
//    }
//
//    private SecurityContext createSecurityContext(String username) {
//        Authentication authentication = mock(Authentication.class);
//        when(authentication.getName()).thenReturn(username);
//
//        SecurityContext securityContext = mock(SecurityContext.class);
//        when(securityContext.getAuthentication()).thenReturn(authentication);
//
//        return securityContext;
//    }
//
//    @Test
//    void shouldHandleAnonymousUser() {
//        // given
//        ServerWebExchange exchange = MockServerWebExchange.from(
//            MockServerHttpRequest.get("/api/test").build()
//        );
//        WebFilterChain chain = mock(WebFilterChain.class);
//
//        InterfaceLogRuleInfo rule = new InterfaceLogRuleInfo();
//        rule.setSpec(new InterfaceLogRuleInfo.InterfaceLogRuleInfoSpec());
//        rule.getSpec().setRule("/api/**");
//        rule.getSpec().setIsInclude(true);
//
//        when(client.listAll(eq(InterfaceLogRuleInfo.class), any(), any()))
//            .thenReturn(Flux.just(rule));
//        when(chain.filter(any())).thenReturn(Mono.empty());
//        when(securityContextRepository.load(any()))
//            .thenReturn(Mono.empty());
//
//        // when
//        StepVerifier.create(filter.filter(exchange, chain))
//            .verifyComplete();
//    }
//}