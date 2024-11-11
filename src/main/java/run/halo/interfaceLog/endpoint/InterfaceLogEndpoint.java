package run.halo.interfaceLog.endpoint;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.extension.GroupVersion;
import run.halo.interfaceLog.request.InterfaceLogRequest;
import run.halo.interfaceLog.service.InterfaceLogService;

import java.util.ArrayList;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.webflux.core.fn.SpringdocRouteBuilder.route;

@Component
public class InterfaceLogEndpoint implements CustomEndpoint {

    private final InterfaceLogService interfaceLogService;

    public InterfaceLogEndpoint(InterfaceLogService interfaceLogService) {
        this.interfaceLogService = interfaceLogService;
    }

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        var tag = "InterfaceLogV1alpha1";
        return route()
                .DELETE("/interfaceLog/deleteAll",
                        this::deleteAll,
                        builder -> builder.operationId("deleteAllInterfaceLogs")
                                .description("delete all interface logs.")
                                .response(responseBuilder().implementation(Boolean.class))
                                .tag(tag))
                .GET("/interfaceLog/users",
                        this::getAllUserInLog,
                        builder -> builder.operationId("GetAllUserInLog")
                                .description("list all users in interface logs.")
                                .response(responseBuilder().implementation(ArrayList.class))
                                .tag(tag))
                .GET("/interfaceLog/clientIps",
                        this::getAllClientIPInLog,
                        builder -> builder.operationId("GetAllClientIPInLog")
                                .description("Get all client IP in log.")
                                .tag(tag))
                .GET("/interfaceLog/paths",
                        this::getAllRequestPathInLog,
                        builder -> builder.operationId("GetAllClientIPInLog")
                                .description("Get all request path in log.")
                                .tag(tag))
                .POST("/interfaceLog/search",
                        this::getInterfaceLogByCondition,
                        builder -> builder.operationId("GetInerfaceLogByCondition")
                                .description("Get inerface log by condition.")
                                .tag(tag))
                .GET("/interfaceLog/count",
                        this::count,
                        builder -> builder.operationId("GetAllClientIPInLog")
                                .description("Get all request path in log.")
                                .tag(tag))
                .build();
    }

    private Mono<ServerResponse> count(ServerRequest request) {
        return interfaceLogService.count()
                .flatMap(i -> ServerResponse.ok().bodyValue(i));
    }

    private Mono<ServerResponse> getInterfaceLogByCondition(ServerRequest serverRequest) {
        ObjectMapper objectMapper = new ObjectMapper();
        return DataBufferUtils.join(serverRequest.exchange().getRequest().getBody())
                .map(dataBuffer -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    DataBufferUtils.release(dataBuffer);
                    return new String(bytes);
                })
                .flatMap(jsonString -> {
                    try {
                        InterfaceLogRequest interfaceLogRequest =
                                objectMapper.readValue(jsonString, InterfaceLogRequest.class);
                        return Mono.just(interfaceLogRequest);
                    } catch (Exception e) {
                        return Mono.error(e);
                    }
                })
                .flatMap(interfaceLogRequest -> {
                    if (ObjectUtils.isEmpty(interfaceLogRequest.getUsername())
                            && ObjectUtils.isEmpty(interfaceLogRequest.getClientIp())
                            && ObjectUtils.isEmpty(interfaceLogRequest.getPath())
                            && ObjectUtils.isEmpty(interfaceLogRequest.getAccessTimes())) {
                        return interfaceLogService.getAllInterfaceLog(interfaceLogRequest)
                                .flatMap(i -> ServerResponse.ok().bodyValue(i));
                    } else {
                        return interfaceLogService.getInterfaceLogByCondition(interfaceLogRequest)
                                .flatMap(j -> ServerResponse.ok().bodyValue(j));
                    }
                });
    }

    @Override
    public GroupVersion groupVersion() {
        return GroupVersion.parseAPIVersion("dailyActive.halo.run/v1alpha1");
    }

    private Mono<ServerResponse> deleteAll(ServerRequest request) {
        return interfaceLogService.deleteAll()
                .collectList()
                .flatMap(i -> ServerResponse.ok().bodyValue(i));
    }

    private Mono<ServerResponse> getAllUserInLog(ServerRequest request) {
        return interfaceLogService.getAllUserInLog(request.queryParam("username").orElse(""))
                .distinct()
                .take(10)
                .collectList()
                .flatMap(i -> ServerResponse.ok().bodyValue(i));
    }

    private Mono<ServerResponse> getAllClientIPInLog(ServerRequest request) {
        return interfaceLogService.getAllClientIPInLog(
                        request.queryParam("clientIp").orElse(""))
                .distinct()
                .take(10)
                .collectList()
                .flatMap(i -> ServerResponse.ok().bodyValue(i));
    }

    private Mono<ServerResponse> getAllRequestPathInLog(ServerRequest request) {
        return interfaceLogService.getAllRequestPathInLog(request.queryParam("path").orElse(""))
                .distinct()
                .take(10)
                .collectList()
                .flatMap(i -> ServerResponse.ok().bodyValue(i));
    }
}
