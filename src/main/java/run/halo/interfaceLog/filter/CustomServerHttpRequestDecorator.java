package run.halo.interfaceLog.filter;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class CustomServerHttpRequestDecorator extends ServerHttpRequestDecorator {
    private final ServerWebExchange exchange;
    private Mono<String> cachedBody;

    public CustomServerHttpRequestDecorator(ServerWebExchange exchange) {
        super(exchange.getRequest());
        this.exchange = exchange;
        cacheBody();
    }

    private void cacheBody() {
        cachedBody = DataBufferUtils.join(super.getBody())
            .map(dataBuffer -> {
                byte[] content = new byte[dataBuffer.readableByteCount()];
                dataBuffer.read(content);
                DataBufferUtils.release(dataBuffer);
                return new String(content);
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
