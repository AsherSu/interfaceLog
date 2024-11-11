package run.halo.interfaceLog.service;

import reactor.core.publisher.Mono;
import run.halo.interfaceLog.extension.RetentionDurationInfo;
import run.halo.interfaceLog.param.RetentionDurationBodyParam;

public interface RetentionDurationInfoService {
    Mono<Boolean> setRetentionDurationInfo(RetentionDurationBodyParam retentionDuration);

    Mono<RetentionDurationInfo> getRetentionDuration();
} 