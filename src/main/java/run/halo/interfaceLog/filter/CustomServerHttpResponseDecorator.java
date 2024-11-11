package run.halo.interfaceLog.filter;

import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import reactor.core.publisher.Mono;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.interfaceLog.SpringContextUtils;
import run.halo.interfaceLog.extension.InterfaceLogInfo;
import java.nio.charset.StandardCharsets;

public class CustomServerHttpResponseDecorator extends ServerHttpResponseDecorator {

    private ReactiveExtensionClient client =
        SpringContextUtils.getBean(ReactiveExtensionClient.class);

    private InterfaceLogInfo interfaceLogInfo;

    public CustomServerHttpResponseDecorator(ServerHttpResponse serverHttpResponse,
        InterfaceLogInfo interfaceLogInfo) {
        super(serverHttpResponse);
        this.interfaceLogInfo = interfaceLogInfo;
    }

    @Override
    public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
        return DataBufferUtils.join(body)
            .map(dataBuffer -> {
                byte[] content = new byte[dataBuffer.readableByteCount()];
                dataBuffer.read(content);
                DataBufferUtils.release(dataBuffer);
                return new String(content, StandardCharsets.UTF_8);
            })
            .defaultIfEmpty("")
            .flatMap(tuple -> {
                interfaceLogInfo.getSpec().setResponseBody(tuple);
                interfaceLogInfo.getSpec()
                    .setResponseHeader(getHeaders().toSingleValueMap().toString());
                interfaceLogInfo.getSpec().setResponseStatus(
                    getStatusCode() == null ? "UNKNOWN" : String.valueOf(getStatusCode().value()));
                return client.create(interfaceLogInfo).thenReturn(tuple);
            })
            .flatMap(response -> {
                byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
                DataBuffer dataBuffer = new DefaultDataBufferFactory().wrap(bytes);
                return getDelegate().writeWith(Mono.just(dataBuffer));
            });
    }
}
