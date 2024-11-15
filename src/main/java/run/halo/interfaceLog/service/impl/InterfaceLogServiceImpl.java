package run.halo.interfaceLog.service.impl;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.extension.ExtensionUtil;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.PageRequestImpl;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.index.query.QueryFactory;
import run.halo.interfaceLog.extension.InterfaceLogInfo;
import run.halo.interfaceLog.request.InterfaceLogRequest;
import run.halo.interfaceLog.service.InterfaceLogService;
import run.halo.interfaceLog.vo.InterfaceLogVO;
import run.halo.interfaceLog.vo.SelectorVO;

@Component
public class InterfaceLogServiceImpl implements InterfaceLogService {
    private final ReactiveExtensionClient client;

    public InterfaceLogServiceImpl(ReactiveExtensionClient client) {
        this.client = client;
    }

    @Override
    public Mono<Boolean> deleteAll() {
        return client.listAll(InterfaceLogInfo.class,
                        ListOptions.builder()
                                .andQuery(QueryFactory.all())
                                .build(),
                        Sort.unsorted())
                .flatMap(client::delete)
                .then(Mono.just(true));
    }

    @Override
    public Flux<SelectorVO> getAllUserInLog(String start) {
        return client.listAll(InterfaceLogInfo.class,
                        ListOptions.builder()
                                .andQuery(QueryFactory.startsWith("spec.username", start))
                                .andQuery(QueryFactory.all("spec.username"))
                                .build(),
                        Sort.by("spec.username").ascending())
                .map(i -> new SelectorVO(i.getSpec().getUsername(), "@" + i.getSpec().getUsername()));
    }

    @Override
    public Flux<SelectorVO> getAllClientIPInLog(String start) {
        return client.listAll(InterfaceLogInfo.class,
                        ListOptions.builder()
                                .andQuery(QueryFactory.startsWith("spec.clientIp", start))
                                .andQuery(QueryFactory.all("spec.clientIp"))
                                .build(),
                        Sort.by("spec.clientIp").ascending())
                .map(i -> new SelectorVO(i.getSpec().getClientIp(), i.getSpec().getClientIp()));
    }

    @Override
    public Flux<SelectorVO> getAllRequestPathInLog(String start) {
        return client.listAll(InterfaceLogInfo.class,
                        ListOptions.builder()
                                .andQuery(QueryFactory.startsWith("spec.path", start))
                                .andQuery(QueryFactory.all("spec.path"))
                                .build(),
                        Sort.by("spec.path").ascending())
                .map(i -> new SelectorVO(i.getSpec().getPath(), i.getSpec().getPath()));
    }

    @Override
    public Mono<ListResult<InterfaceLogVO>> getInterfaceLogByCondition(
            InterfaceLogRequest interfaceLogRequest) {
        ListOptions.ListOptionsBuilder builder = ListOptions.builder();

        // todo 空字符串校验
        if (!ObjectUtils.isEmpty(interfaceLogRequest.getUsername())) {
            builder.andQuery(
                    QueryFactory.in("spec.username", interfaceLogRequest.getUsername()));
        }

        if (!ObjectUtils.isEmpty(interfaceLogRequest.getClientIp())) {
            builder.andQuery(
                    QueryFactory.in("spec.clientIp", interfaceLogRequest.getClientIp()));
        }

        if (!ObjectUtils.isEmpty(interfaceLogRequest.getPath())) {
            builder.andQuery(
                    QueryFactory.in("spec.path", interfaceLogRequest.getPath()));
        }

        if (!ObjectUtils.isEmpty(interfaceLogRequest.getAccessTimes())) {
            builder.andQuery(
                    QueryFactory.between("spec.accessTime", interfaceLogRequest.getAccessTimes().get(0),
                            interfaceLogRequest.getAccessTimes().get(1)));
        }

        ListOptions listOptions = builder.build();

        return client.listBy(InterfaceLogInfo.class,
                        listOptions,
                        PageRequestImpl.of(Integer.parseInt(interfaceLogRequest.getPage()),
                                Integer.parseInt(interfaceLogRequest.getSize()),
                                Sort.by("spec.accessTime").descending()))
                .map(i -> new ListResult<>(i.getPage(), i.getSize(), i.getTotal(), i.get().map(InterfaceLogVO::new).toList()));
    }

    @Override
    public Mono<ListResult<InterfaceLogVO>> getAllInterfaceLog(
            InterfaceLogRequest interfaceLogRequest) {
        return client.listBy(InterfaceLogInfo.class,
                        ListOptions.builder()
                                .andQuery(QueryFactory.all())
                                .build(),
                        PageRequestImpl.of(
                                Integer.parseInt(interfaceLogRequest.getPage()),
                                Integer.parseInt(interfaceLogRequest.getSize()),
                                Sort.by("spec.accessTime").descending()
                        )
                )
                .map(i -> new ListResult<>(i.getPage(), i.getSize(), i.getTotal(), i.get().map(InterfaceLogVO::new).toList()));
    }

    @Override
    public Mono<Long> count() {
        return client.listAll(InterfaceLogInfo.class,
                        ListOptions.builder()
                                .andQuery(QueryFactory.all())
                                .build(),
                        Sort.unsorted())
                .filter(i -> !ExtensionUtil.isDeleted(i))
                .count();
    }
}
