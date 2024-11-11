package run.halo.interfaceLog.schedule;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.index.query.QueryFactory;
import run.halo.interfaceLog.extension.InterfaceLogInfo;

@Slf4j
@Component
public class DeleteSchedule {

    private final ReactiveExtensionClient client;

    public DeleteSchedule(ReactiveExtensionClient client) {
        this.client = client;
        log.error("DeleteSchedule 构造函数被调用");  // 添加构造函数日志
    }

    @PostConstruct
    public void init() {
        log.error("DeleteSchedule 初始化完成");  // 验证类是否被初始化
    }

    @Scheduled(cron = "0/5 * * * * ?")
    public void delete() {
        log.error("定时任务开始执行");  // 添加任务开始日志

        // 计算7天前的时间戳
        long sevenDaysAgo = System.currentTimeMillis() - (1000);
        log.error("开始执行删除任务，删除时间：{}", sevenDaysAgo);

        client.listAll(InterfaceLogInfo.class,
                        ListOptions.builder()
                                .andQuery(QueryFactory.lessThan("spec.accessTime", String.valueOf(sevenDaysAgo)))
                                .build(),
                        Sort.unsorted())
                .flatMap(client::delete)
                .doOnComplete(() -> log.info("删除任务执行完成"))
                .doOnError(error -> log.error("删除任务执行失败", error))
                .subscribe();
    }
}
