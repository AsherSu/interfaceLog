package run.halo.interfaceLog.endpoint;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.extension.GroupVersion;
import run.halo.interfaceLog.extension.RetentionDurationInfo;
import run.halo.interfaceLog.param.RetentionDurationBodyParam;
import run.halo.interfaceLog.service.RetentionDurationInfoService;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.webflux.core.fn.SpringdocRouteBuilder.route;

@Component
public class RetentionDurationInfoEndpoint implements CustomEndpoint {

    private final RetentionDurationInfoService retentionDurationInfoService;

    public RetentionDurationInfoEndpoint(RetentionDurationInfoService retentionDurationInfoService) {
        this.retentionDurationInfoService = retentionDurationInfoService;
    }

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        var tag = "RetentionDurationInfoV1alpha1";
        return route()
            .POST("/retentionDuration/set", this::setRetentionDurationInfo,
                builder -> builder.operationId("setRetentionDurationInfo")
                    .description("Set retention duration info.")
                    .response(responseBuilder().implementation(Boolean.class))
                    .tag(tag))
            .GET("/retentionDuration/get", this::getRetentionDuration,
                builder -> builder.operationId("getRetentionDuration")
                    .description("Get retention duration info.")
                    .response(responseBuilder().implementation(RetentionDurationInfo.class))
                    .tag(tag))
            .build();
    }

    @Override
    public GroupVersion groupVersion() {
        return GroupVersion.parseAPIVersion("dailyActive.halo.run/v1alpha1");
    }

    private Mono<ServerResponse> setRetentionDurationInfo(ServerRequest request) {
        return request.bodyToMono(RetentionDurationBodyParam.class)
            .flatMap(retentionDurationInfoService::setRetentionDurationInfo)
            .flatMap(info -> ServerResponse.ok().bodyValue(info))
            .onErrorResume(e -> ServerResponse.badRequest().bodyValue(e.getMessage()));
    }

    private Mono<ServerResponse> getRetentionDuration(ServerRequest request) {
        return retentionDurationInfoService.getRetentionDuration()
            .flatMap(info -> ServerResponse.ok().bodyValue(info))
            .switchIfEmpty(ServerResponse.notFound().build());
    }


} 