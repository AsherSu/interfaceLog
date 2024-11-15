package run.halo.interfaceLog.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.extension.ListResult;
import run.halo.interfaceLog.request.InterfaceLogRequest;
import run.halo.interfaceLog.vo.SelectorVO;
import run.halo.interfaceLog.vo.InterfaceLogVO;

public interface InterfaceLogService {
    Mono<Boolean> deleteAll();

    Flux<SelectorVO> getAllUserInLog(String start);

    Flux<SelectorVO> getAllClientIPInLog(String start);

    Flux<SelectorVO> getAllRequestPathInLog(String start);

    Mono<ListResult<InterfaceLogVO>> getInterfaceLogByCondition(InterfaceLogRequest interfaceLogRequest);

    Mono<ListResult<InterfaceLogVO>> getAllInterfaceLog(InterfaceLogRequest interfaceLogRequest);

    Mono<Long> count();
}
