package run.halo.interfaceLog;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.interfaceLog.endpoint.InterfaceLogEndpoint;
import run.halo.interfaceLog.service.InterfaceLogService;

@ExtendWith(MockitoExtension.class)
public class InterfaceLogFilterTest {

    @Mock
    private ReactiveExtensionClient client;

    @InjectMocks
    private InterfaceLogEndpoint controller;

    @Mock
    private InterfaceLogService interfaceLogService;

    @Test
    void filterTest(){

    }
}
