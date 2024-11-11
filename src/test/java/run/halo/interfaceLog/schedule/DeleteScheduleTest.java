//package run.halo.dailyActive.schedule;
//
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import reactor.core.publisher.Flux;
//import reactor.core.publisher.Mono;
//import run.halo.dailyActive.entity.InterfaceLogInfo;
//import run.halo.dailyActive.entity.InterfaceLogInfoSpec;
//import run.halo.dailyActive.service.ReactiveExtensionClient;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//
//@ExtendWith(MockitoExtension.class)
//class DeleteScheduleTest {
//
//    @Mock
//    private ReactiveExtensionClient client;
//
//    @InjectMocks
//    private DeleteSchedule deleteSchedule;
//
//    @Test
//    void shouldDeleteOldLogs() {
//        // given
//        when(client.listAll(eq(InterfaceLogInfo.class), any(), any()))
//            .thenReturn(Flux.just(createMockInterfaceLogInfo()));
//
//        when(client.delete(any(InterfaceLogInfo.class)))
//            .thenReturn(Mono.empty());
//
//        // when
//        deleteSchedule.delete();
//
//        // then
//        verify(client).listAll(eq(InterfaceLogInfo.class), any(), any());
//        verify(client).delete(any(InterfaceLogInfo.class));
//    }
//
//    private InterfaceLogInfo createMockInterfaceLogInfo() {
//        InterfaceLogInfo info = new InterfaceLogInfo();
//        InterfaceLogInfoSpec spec = new InterfaceLogInfoSpec();
//        spec.setAccessTime(String.valueOf(System.currentTimeMillis() - 1000000));
//        info.setSpec(spec);
//        return info;
//    }
//}