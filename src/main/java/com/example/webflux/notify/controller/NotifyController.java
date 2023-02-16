package com.example.webflux.notify.controller;

import com.example.webflux.notify.dto.NotifyDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RestController
public class NotifyController {

    private SseChannel sseChannel = new SseChannel();

    @GetMapping("/notify/connect/{storeCode}")
    public Flux<ServerSentEvent<Object>> connect(@PathVariable("storeCode") String storeCode) {
        Flux<NotifyDTO> userStream = sseChannel.connect(storeCode).toFlux();
        Flux<String> tickStream = Flux.interval(Duration.ofSeconds(3)).map(tick -> "connect : " + storeCode);

        return Flux.merge(userStream, tickStream)
                    .map(str -> ServerSentEvent.builder(str).build());
    }

    @PostMapping("/notify/reserve")
    public void reserve(@RequestBody NotifyDTO notifyDTO) {
        sseChannel.post(notifyDTO);
    }

    public static class SseChannel {
        private ConcurrentHashMap<String, StroeChannel> map = new ConcurrentHashMap<>();

        public StroeChannel connect(String storeCode) {
            return map.computeIfAbsent(storeCode, key -> new StroeChannel()
                                                                .onClose(() -> map.remove(storeCode)));
        }

        public void post(NotifyDTO notifyDTO) {
            Optional.ofNullable(map.get(notifyDTO.getStoreCode())).ifPresent(ch -> ch.send(notifyDTO));
        }
    }

    public static class StroeChannel {
        private EmitterProcessor<NotifyDTO> processor;
        private Flux<NotifyDTO> flux;
        private FluxSink<NotifyDTO> sink;
        private Runnable closeCallback;

        public StroeChannel() {
            processor = EmitterProcessor.create();

            this.sink = processor.sink();
            this.flux = processor
                    .doOnCancel(() -> {
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

        public StroeChannel onClose(Runnable closeCallback) {
            this.closeCallback = closeCallback;
            return this;
        }
    }
}
