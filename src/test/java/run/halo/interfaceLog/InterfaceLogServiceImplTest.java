package run.halo.interfaceLog;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.interfaceLog.extension.InterfaceLogInfo;
import run.halo.interfaceLog.service.impl.InterfaceLogServiceImpl;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class InterfaceLogServiceImplTest {

    @InjectMocks
    private InterfaceLogServiceImpl interfaceLogService;

    @Mock
    private ReactiveExtensionClient client;

    @Test
    void countTest() {
        // deleted
        InterfaceLogInfo deleted = createMockInterfaceLogInfo();
        deleted.getMetadata().setDeletionTimestamp(Instant.now());
        // not deleted
        InterfaceLogInfo noDelete = createMockInterfaceLogInfo();

        when(client.listAll(eq(InterfaceLogInfo.class), any(ListOptions.class), any(Sort.class)))
                .thenReturn(Flux.just(deleted, noDelete));

        StepVerifier.create(interfaceLogService.count())
                .expectNext(1L)
                .verifyComplete();
    }

    @Test
    void deleteAllTest() {
        // deleted
        InterfaceLogInfo deleted = createMockInterfaceLogInfo();
        deleted.getMetadata().setDeletionTimestamp(Instant.now());
        // not deleted
        InterfaceLogInfo noDelete = createMockInterfaceLogInfo();

        when(client.listAll(eq(InterfaceLogInfo.class), any(ListOptions.class), any(Sort.class)))
                .thenReturn(Flux.just(deleted, noDelete));
        when(client.delete(deleted)).thenReturn(Mono.just(deleted));
        when(client.delete(noDelete)).thenReturn(Mono.just(noDelete));

        StepVerifier.create(interfaceLogService.deleteAll())
                .expectNext(true)
                .expectNext(true)
                .verifyComplete();
    }

    private InterfaceLogInfo createMockInterfaceLogInfo() {
        // 创建 InterfaceLogInfo
        InterfaceLogInfo info = new InterfaceLogInfo();

        // 设置 metadata
        Metadata metadata = new Metadata();
        metadata.setName("test-log-" + System.currentTimeMillis());
        metadata.setVersion(1L);
        metadata.setCreationTimestamp(Instant.now());
        info.setMetadata(metadata);

        // 创建并设置 spec
        InterfaceLogInfo.InterfaceLogInfoSpec spec = new InterfaceLogInfo.InterfaceLogInfoSpec();
        spec.setAccessTime(new Date());
        spec.setUsername("test-user");
        spec.setClientIp("127.0.0.1");
        spec.setPath("/api/test");
        spec.setRequestType("GET");
        spec.setResponseStatus("200");
        // 请求参数
        Map<String, String> requestParams = new HashMap<>();
        requestParams.put("param1", "value1");
        requestParams.put("param2", "value2");
        spec.setRequestParams(requestParams);
        // 请求头
        spec.setRequestHeader("");
        // 请求体
        spec.setRequestBody("");
        // 响应头
        spec.setResponseHeader("");
        // 响应体
        spec.setResponseBody("");
        info.setSpec(spec);
        return info;
    }
}
