package com.example.webflux.component;

import com.example.webflux.notify.dto.NotifyDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class SseChannel {

    private ConcurrentHashMap<String, StoreChannel> channelMap = new ConcurrentHashMap<>();

    public StoreChannel connect(String storeCode) {
        return channelMap.computeIfAbsent(storeCode, key -> new StoreChannel()
                .onClose(() -> channelMap.remove(storeCode)));
    }

    public void post(NotifyDTO notifyDTO) {
        //연결된 스토어 채널이 있는 경우
        Optional.ofNullable(channelMap.get(notifyDTO.getStoreCode()))
                .ifPresent(ch -> ch.send(notifyDTO));
    }

    public static class StoreChannel {
        private EmitterProcessor<NotifyDTO> processor;
        private Flux<NotifyDTO> flux;
        private FluxSink<NotifyDTO> sink;
        private Runnable closeCallback;

        public StoreChannel() {
            processor = EmitterProcessor.create();

            this.sink = processor.sink();
            this.flux = processor.doOnCancel(() -> {
                                    log.info("doOnCancel, downstream - {}", processor.downstreamCount());

                                    if (processor.downstreamCount() == 1) close();
                                })
                                .doOnTerminate(() -> {
                                    log.info("doOnTerminate, downstream - {}", processor.downstreamCount());
                                });
        }

        public void send(NotifyDTO notifyDTO) {
            log.info("message : {}", notifyDTO.getMessage());
            sink.next(notifyDTO);
        }

        public Flux<NotifyDTO> toFlux() {
            return flux;
        }

        private void close() {
            if (closeCallback != null) closeCallback.run();
            sink.complete();
        }

        public StoreChannel onClose(Runnable closeCallback) {
            this.closeCallback = closeCallback;
            return this;
        }
    }
}
