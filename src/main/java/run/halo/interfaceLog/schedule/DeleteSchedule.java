package run.halo.interfaceLog.schedule;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.index.query.QueryFactory;
import run.halo.interfaceLog.extension.InterfaceLogInfo;
import run.halo.interfaceLog.extension.InterfaceLogRuleInfo;
import run.halo.interfaceLog.extension.RetentionDurationInfo;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
@EnableScheduling
public class DeleteSchedule {

    private final ReactiveExtensionClient client;

    public DeleteSchedule(ReactiveExtensionClient client) {
        this.client = client;
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void delete() {
        client.fetch(RetentionDurationInfo.class, "interface-log-retention-duration")
                .doOnError(error -> log.error("获取规则失败", error))
                .map(RetentionDurationInfo::getSpec)
                .map(RetentionDurationInfo.RetentionDurationInfoSpec::getDay)
                .defaultIfEmpty(7)
                .flatMap(days -> {
                    long retentionPeriodMillis = days * 24 * 60 * 60 * 1000L;
                    long cutoffTime = System.currentTimeMillis() - retentionPeriodMillis;
                    Instant instant = Instant.ofEpochMilli(cutoffTime);
                    LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    String formattedDateTime = dateTime.format(formatter);
                    return client.listAll(InterfaceLogInfo.class,
                                    ListOptions.builder()
                                            .andQuery(QueryFactory.lessThan("spec.accessTime", formattedDateTime))
                                            .build(),
                                    Sort.unsorted())
                            .flatMap(client::delete)
                            .then(Mono.empty())  // 添加这行来确保返回类型是 Mono
                            .doOnError(error -> log.error("删除任务执行失败", error));
                })
                .subscribe();
    }
}

