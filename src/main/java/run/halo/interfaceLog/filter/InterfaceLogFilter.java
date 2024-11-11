package run.halo.interfaceLog.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import run.halo.app.extension.ExtensionUtil;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.index.query.QueryFactory;
import run.halo.app.security.AdditionalWebFilter;
import run.halo.interfaceLog.SnowflakeIdGenerator;
import run.halo.interfaceLog.extension.InterfaceLogInfo;
import run.halo.interfaceLog.extension.InterfaceLogRuleInfo;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class InterfaceLogFilter implements AdditionalWebFilter {

    Logger logger = LoggerFactory.getLogger(InterfaceLogFilter.class);

    private final ReactiveExtensionClient client;

    private final ServerSecurityContextRepository serverSecurityContextRepository;

    private Mono<Map<String, List<String>>> antPathMatchers;

    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    public InterfaceLogFilter(ReactiveExtensionClient reactiveExtensionClient,
                              ServerSecurityContextRepository serverSecurityContextRepository) {
        this.client = reactiveExtensionClient;
        this.serverSecurityContextRepository = serverSecurityContextRepository;
    }

    public void refreshMatcher() {
        antPathMatchers = client.listAll(InterfaceLogRuleInfo.class, ListOptions.builder().andQuery(
                        QueryFactory.all()).build(), Sort.unsorted())
                .mapNotNull(i -> ExtensionUtil.isDeleted(i) ? null : i)
                .collectList()
                .map(interfaceLogRuleInfo -> {
                    Map<String, List<String>> antPathMatchers = new HashMap<>();
                    List<String> include = new ArrayList<>();
                    List<String> exclude = new ArrayList<>();
                    antPathMatchers.put("include", include);
                    antPathMatchers.put("exclude", exclude);

                    interfaceLogRuleInfo.forEach(rule -> {
                        if (rule.getSpec().getIsInclude()) {
                            antPathMatchers.get("include")
                                    .add(rule.getSpec().getRule());
                        } else {
                            antPathMatchers.get("exclude")
                                    .add(rule.getSpec().getRule());
                        }
                    });

                    if (antPathMatchers.get("include").isEmpty()) {
                        antPathMatchers.get("exclude").add("/**");
                    }

                    return antPathMatchers;
                });
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        if (antPathMatchers == null) {
            refreshMatcher();
        }
        return antPathMatchers.flatMap(i -> matchPath(i, exchange.getRequest().getPath().value()))
                .flatMap(matchResult -> {
                    if (!matchResult) {
                        return chain.filter(exchange);
                    }
                    InterfaceLogInfo interfaceLogInfo = new InterfaceLogInfo();
                    return Mono.just(new GenerateInterfaceLogInfoServerHttpRequestDecorator(exchange, interfaceLogInfo))
                            .map(e -> exchange.mutate().request(e).build())
                            .flatMap(requestExchange -> generateLogInfo(requestExchange, interfaceLogInfo)
                                    .flatMap(logInfo -> {
                                        ServerWebExchange mutatedExchange = requestExchange.mutate()
                                                .response(new CustomServerHttpResponseDecorator(
                                                        requestExchange.getResponse(), logInfo))
                                                .build();
                                        return chain.filter(mutatedExchange);
                                    }));
                });
    }

    private Mono<Boolean> matchPath(Map<String, List<String>> patternList, String path) {
        List<String> include = patternList.get("include");
        List<String> exclude = patternList.get("exclude");
        boolean in = include.stream()
                .anyMatch(pattern -> antPathMatcher.match(pattern, path));
        boolean ex = exclude.stream()
                .noneMatch(pattern -> antPathMatcher.match(pattern, path));
        return Mono.just(in && ex);
    }

    private Mono<InterfaceLogInfo> generateLogInfo(ServerWebExchange exchange,
                                                   InterfaceLogInfo interfaceLogInfo) {
        return getRequestBodyAsString(exchange).defaultIfEmpty("")
                .zipWith(getUsername(exchange))
                .map(tuple -> {
                    interfaceLogInfo.getSpec()
                            .setRequestHeader(exchange.getRequest().getHeaders().toString());
                    interfaceLogInfo.getSpec()
                            .setRequestHeader(generateRequestHeader(exchange.getRequest()));
                    interfaceLogInfo.getSpec().setRequestBody(tuple.getT1());
                    interfaceLogInfo.getSpec().setPath(generatePath(exchange.getRequest()));
                    interfaceLogInfo.getSpec()
                            .setRequestType(exchange.getRequest().getMethod().toString());
                    interfaceLogInfo.getSpec().setAccessTime(generateTime());
                    interfaceLogInfo.getSpec().setClientIp(getClientIp(exchange));
                    interfaceLogInfo.getSpec().setUsername(tuple.getT2());
                    interfaceLogInfo.getSpec().setRequestParams(
                            exchange.getRequest().getQueryParams().isEmpty() ? new HashMap<>() : exchange.getRequest().getQueryParams().toSingleValueMap());
                    interfaceLogInfo.setMetadata(new Metadata());
                    interfaceLogInfo.getMetadata().setName(MetadataName.get());
                    return interfaceLogInfo;
                });
    }

    @Override
    public int getOrder() {
        return SecurityWebFiltersOrder.LAST.getOrder();
    }

    private Date generateTime() {
        Instant now = Instant.now();
        ZonedDateTime zonedDateTime = now.atZone(ZoneId.of("Asia/Shanghai"));
        return Date.from(zonedDateTime.toInstant());
    }

    private String generateRequestHeader(ServerHttpRequest request) {
        HttpHeaders headers = request.getHeaders();
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(headers);
        } catch (JsonProcessingException e) {
            logger.error("Error converting headers to JSON", e);
            return "{}";
        }
    }

    private String generatePath(ServerHttpRequest request) {
        return request.getPath().value();
    }

    private Mono<String> getUsername(ServerWebExchange exchange) {
        return serverSecurityContextRepository.load(exchange)
                .map(securityContext -> securityContext.getAuthentication().getName())
                .switchIfEmpty(Mono.just("visitor"));
    }

    private String getClientIp(ServerWebExchange exchange) {
        return Optional.of(exchange)
                .map(ServerWebExchange::getRequest)
                .map(ServerHttpRequest::getRemoteAddress)
                .map(InetSocketAddress::getAddress)
                .map(InetAddress::toString)
                .map(ip -> ip.substring(1))
                .orElse("UNKNOWN");
    }

    private Mono<String> getRequestBodyAsString(ServerWebExchange exchange) {
        return DataBufferUtils.join(exchange.getRequest().getBody())
                .map(dataBuffer -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    DataBufferUtils.release(dataBuffer);
                    return new String(bytes);
                })
                .defaultIfEmpty("");
    }


    class MetadataName {
        private static SnowflakeIdGenerator snowflakeIdGenerator = new SnowflakeIdGenerator(1, 1);

        public static String get() {
            long id = snowflakeIdGenerator.nextId();
            return String.valueOf(id);
        }
    }
}