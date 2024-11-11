package run.halo.interfaceLog.endpoint;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import run.halo.app.core.extension.endpoint.CustomEndpoint;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.webflux.core.fn.SpringdocRouteBuilder.route;

import reactor.core.publisher.Mono;
import run.halo.app.extension.GroupVersion;
import run.halo.interfaceLog.extension.InterfaceLogRuleInfo;
import run.halo.interfaceLog.service.InterfaceLogRuleService;
import run.halo.interfaceLog.service.SystemInfoService;
import run.halo.interfaceLog.vo.InterfaceLogRuleFileVO;

@Component
public class InterfaceLogRuleEndpoint implements CustomEndpoint {

    private final InterfaceLogRuleService interfaceLogRuleService;

    public InterfaceLogRuleEndpoint(InterfaceLogRuleService interfaceLogRuleService) {
        this.interfaceLogRuleService = interfaceLogRuleService;
    }

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        var tag = "InterfaceLogRuleV1alpha1";
        return route()
            .DELETE("/interfaceLogRule/delete",
                this::delete,
                builder -> builder.operationId("deleteAllInterfaceLogs")
                    .description("delete all interface logs.")
                    .response(responseBuilder().implementation(Boolean.class))
                    .tag(tag))
            .PUT("/interfaceLogRule/update",
                this::update,
                builder -> builder.operationId("updateInterfaceLog")
                    .description("Update interface log rule.")
                    .response(responseBuilder().implementation(Boolean.class))
                    .tag(tag))
            .GET("/interfaceLogRule/get",
                this::get,
                builder -> builder.operationId("getInterfaceLogRule")
                    .description("Get interface log rule.")
                    .response(responseBuilder().implementation(Boolean.class))
                    .tag(tag))
            .POST("/interfaceLogRule/create",
                this::create,
                builder -> builder.operationId("updateInterfaceLog")
                    .description("Update interface log rule.")
                    .response(responseBuilder().implementation(Boolean.class))
                    .tag(tag))
            .POST("/interfaceLogRule/import", this::importRules,
                builder -> builder.operationId("importRules")
                    .description("Import interface log rules.")
                    .response(responseBuilder().implementation(Boolean.class))
                    .tag(tag))
            .GET("/interfaceLogRule/export", this::exportRules,
                builder -> builder.operationId("exportRules")
                    .description("Export interface log rules.")
                    .response(responseBuilder().implementation(InterfaceLogRuleFileVO.class))
                    .tag(tag))
            .GET("/interfaceLogRule/systemInfo", this::systemInfo,
                builder -> builder.operationId("exportRules")
                    .description("Export interface log rules.")
                    .response(responseBuilder().implementation(InterfaceLogRuleFileVO.class))
                    .tag(tag))
            .build();
    }

    @Override
    public GroupVersion groupVersion() {
        return GroupVersion.parseAPIVersion("dailyActive.halo.run/v1alpha1");
    }

    private Mono<ServerResponse> update(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(InterfaceLogRuleInfo.class)
            .flatMap(interfaceLogRuleService::updateInterfaceLogRule)
            .flatMap(result -> ServerResponse.ok().bodyValue(result));
    }

    private Mono<ServerResponse> delete(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(InterfaceLogRuleInfo.class)
            .flatMap(interfaceLogRuleService::deleteInterfaceLogRule)
            .flatMap(result -> ServerResponse.ok().bodyValue(result));
    }

    private Mono<ServerResponse> get(ServerRequest serverRequest) {
        return interfaceLogRuleService.getInterfaceLogRule()
            .flatMap(result -> ServerResponse.ok().bodyValue(result));
    }

    private Mono<ServerResponse> create(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(InterfaceLogRuleInfo.class)
            .flatMap(interfaceLogRuleService::createInterfaceLogRule)
            .flatMap(result -> ServerResponse.ok().bodyValue(result));
    }

    private Mono<ServerResponse> importRules(ServerRequest request) {
        return request.bodyToMono(InterfaceLogRuleFileVO.class)
            .flatMap(interfaceLogRuleService::importRules)
            .flatMap(result -> ServerResponse.ok().bodyValue(result));
    }

    private Mono<ServerResponse> exportRules(ServerRequest request) {
        return interfaceLogRuleService.exportRules()
            .flatMap(rules -> ServerResponse.ok().bodyValue(rules));
    }

    private Mono<ServerResponse> systemInfo(ServerRequest request) {
        return ServerResponse.ok().bodyValue(SystemInfoService.systemInfo());
    }
}
