package run.halo.interfaceLog.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.extension.ExtensionUtil;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.index.query.QueryFactory;
import run.halo.interfaceLog.filter.InterfaceLogFilter;
import run.halo.interfaceLog.extension.InterfaceLogRuleInfo;
import run.halo.interfaceLog.service.InterfaceLogRuleService;
import run.halo.interfaceLog.vo.InterfaceLogRuleFileVO;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class InterfaceLogRuleServiceImpl implements InterfaceLogRuleService {

    private final ReactiveExtensionClient client;

    private final InterfaceLogFilter interfaceLogFilter;

    @Override
    public Mono<InterfaceLogRuleInfo> updateInterfaceLogRule(
        InterfaceLogRuleInfo interfaceLogRuleInfo) {
        return client.fetch(InterfaceLogRuleInfo.class,
                interfaceLogRuleInfo.getMetadata().getName())
            .flatMap(existing -> {
                // 保持原有的 metadata，只更新 spec
                existing.setSpec(interfaceLogRuleInfo.getSpec());
                return client.update(existing)
                    .doOnSuccess(i -> interfaceLogFilter.refreshMatcher());
            })
            .onErrorResume(e -> {
                log.error("Failed to update interface log rule", e);
                return Mono.just(new InterfaceLogRuleInfo());
            });
    }

    @Override
    public Mono<InterfaceLogRuleInfo> createInterfaceLogRule(
        InterfaceLogRuleInfo interfaceLogRuleInfo) {
        return client.create(interfaceLogRuleInfo)
            .doOnSuccess(i -> interfaceLogFilter.refreshMatcher())
            .onErrorResume(e -> {
                log.error("Failed to update interface log rule", e);
                return Mono.just(new InterfaceLogRuleInfo());
            });
    }

    @Override
    public Mono<InterfaceLogRuleInfo> deleteInterfaceLogRule(
        InterfaceLogRuleInfo interfaceLogRuleInfo) {
        return client.fetch(InterfaceLogRuleInfo.class,
                interfaceLogRuleInfo.getMetadata().getName())
            .flatMap(i -> client.delete(i).doOnSuccess(j -> interfaceLogFilter.refreshMatcher()))
            .onErrorResume(e -> {
                log.error("Failed to update interface log rule", e);
                return Mono.just(new InterfaceLogRuleInfo());
            });
    }

    @Override
    public Mono<List<InterfaceLogRuleInfo>> getInterfaceLogRule() {
        return client.listAll(InterfaceLogRuleInfo.class, ListOptions.builder().andQuery(
                QueryFactory.all()).build(), Sort.unsorted())
            .mapNotNull(i -> ExtensionUtil.isDeleted(i) ? null : i)
            .collectList();
    }


    @Override
    public Mono<Boolean> importRules(InterfaceLogRuleFileVO ruleFileVO) {
        return Mono.just(ruleFileVO)
            .map(InterfaceLogRuleFileVO::getInterfaceLogRules)
            // todo 换成flatMap 这里不执行create
            .flatMapMany(Flux::fromIterable)
            .concatMap(rule -> {
                InterfaceLogRuleInfo interfaceLogRuleInfo = new InterfaceLogRuleInfo();
                interfaceLogRuleInfo.setSpec(new InterfaceLogRuleInfo.InterfaceLogRuleInfoSpec());
                interfaceLogRuleInfo.getSpec().setRule(rule.getRule());
                interfaceLogRuleInfo.getSpec().setId(System.currentTimeMillis());
                interfaceLogRuleInfo.getSpec().setVersion(rule.getVersion());
                interfaceLogRuleInfo.getSpec().setIsInclude(rule.getIsInclude());
                interfaceLogRuleInfo.setMetadata(new Metadata());
                interfaceLogRuleInfo.getMetadata()
                    .setName(String.valueOf(interfaceLogRuleInfo.getSpec().getId()));
                return client.create(interfaceLogRuleInfo)
                    .doOnError(e -> log.error("Failed to import rule", e));
            })
            .doOnNext(i->interfaceLogFilter.refreshMatcher())
            .then(Mono.just(true))
            .onErrorResume(e -> {
                log.error("Failed to import rules", e);
                return Mono.just(false);
            });
    }

    @Override
    public Mono<InterfaceLogRuleFileVO> exportRules() {
        return null;
    }
} 