package run.halo.interfaceLog.filter;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.interfaceLog.extension.InterfaceLogInfo;

public class GenerateInterfaceLogInfoServerHttpRequestDecorator extends ServerHttpRequestDecorator {
    private final ServerWebExchange exchange;
    private final InterfaceLogInfo interfaceLogInfo;
    private Mono<String> cachedBody;

    public GenerateInterfaceLogInfoServerHttpRequestDecorator(ServerWebExchange exchange,
        InterfaceLogInfo interfaceLogInfo) {
        super(exchange.getRequest());
        this.exchange = exchange;
        this.interfaceLogInfo = interfaceLogInfo;
        cacheBody();
    }

    private void cacheBody() {
        cachedBody = DataBufferUtils.join(super.getBody())
            .map(dataBuffer -> {
                byte[] content = new byte[dataBuffer.readableByteCount()];
                dataBuffer.read(content);
                DataBufferUtils.release(dataBuffer);
                String bodyContent = new String(content);
                interfaceLogInfo.getSpec().setRequestBody(bodyContent);
                return bodyContent;
            })
            .cache();
    }

    @Override
    public Flux<DataBuffer> getBody() {
        return cachedBody.map(dataBuffer -> exchange.getResponse()
            .bufferFactory()
            .wrap(dataBuffer.getBytes()))
            .flatMapMany(Flux::just);
    }
}
