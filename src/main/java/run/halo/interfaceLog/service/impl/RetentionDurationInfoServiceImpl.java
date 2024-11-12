package run.halo.interfaceLog.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.index.query.QueryFactory;
import run.halo.interfaceLog.extension.RetentionDurationInfo;
import run.halo.interfaceLog.param.RetentionDurationBodyParam;
import run.halo.interfaceLog.service.RetentionDurationInfoService;

@Service
@Slf4j
public class RetentionDurationInfoServiceImpl implements RetentionDurationInfoService {

    // 假设有一个 repository 来处理数据存储
    private final ReactiveExtensionClient client;

    public RetentionDurationInfoServiceImpl(ReactiveExtensionClient client) {
        this.client = client;
    }

    @Override
    public Mono<Boolean> setRetentionDurationInfo(RetentionDurationBodyParam retentionDuration) {
        // 处理创建逻辑，例如保存到数据库
        return client.listAll(RetentionDurationInfo.class, ListOptions.builder().andQuery(
                QueryFactory.all()).build(), Sort.unsorted())
            .collectList()
            .flatMap(list -> {
                if (list.isEmpty()) {
                    RetentionDurationInfo info = new RetentionDurationInfo();
                    info.setSpec(new RetentionDurationInfo.RetentionDurationInfoSpec());
                    info.setMetadata(new Metadata());
                    info.getSpec().setDay(retentionDuration.getDays());
                    info.getMetadata().setName("interface-log-retention-duration");
                    return client.create(info).thenReturn(true);
                } else {
                    RetentionDurationInfo existingInfo = list.get(0);
                    existingInfo.getSpec().setDay(retentionDuration.getDays());
                    return client.update(existingInfo).thenReturn(true);
                }
            })
            .onErrorResume(e -> {
                log.error("Failed to set retention duration info", e);
                return Mono.just(false);
            });

    }

    @Override
    public Mono<RetentionDurationInfo> getRetentionDuration() {
        // 处理获取逻辑，例如从数据库中查找
        return client.listAll(RetentionDurationInfo.class, ListOptions.builder().andQuery(
                QueryFactory.all()).build(), Sort.unsorted())
            .collectList()
            .flatMap(list -> {
                if (list.isEmpty()) {
                    RetentionDurationInfo info = new RetentionDurationInfo();
                    info.setSpec(new RetentionDurationInfo.RetentionDurationInfoSpec());
                    info.setMetadata(new Metadata());
                    info.getSpec().setDay(7);
                    info.getMetadata().setName("interface-log-retention-duration");
                    return client.create(info);
                }
                return Mono.just(list.get(0));
            });
    }
} 