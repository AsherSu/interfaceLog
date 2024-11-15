package run.halo.interfaceLog.service;

import reactor.core.publisher.Mono;
import run.halo.interfaceLog.extension.InterfaceLogRuleInfo;
import run.halo.interfaceLog.vo.InterfaceLogRuleFileVO;

import java.util.List;

public interface InterfaceLogRuleService {

    Mono<InterfaceLogRuleInfo> updateInterfaceLogRule(InterfaceLogRuleInfo interfaceLogRuleInfo);

    Mono<InterfaceLogRuleInfo> createInterfaceLogRule(InterfaceLogRuleInfo interfaceLogRuleInfo);

    Mono<InterfaceLogRuleInfo> deleteInterfaceLogRule(InterfaceLogRuleInfo interfaceLogRuleInfo);

    Mono<List<InterfaceLogRuleInfo>> getInterfaceLogRule();

    Mono<Boolean> importRules(InterfaceLogRuleFileVO ruleFileVO);

    Mono<InterfaceLogRuleFileVO> exportRules();

}
