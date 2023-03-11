package com.example.webflux.notify.controller;

import com.example.webflux.component.SseChannel;
import com.example.webflux.notify.dto.NotifyDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.time.Duration;

@Slf4j
@RestController
public class NotifyController {

    private final SseChannel sseChannel = new SseChannel();

    @GetMapping("/notify/connect/{storeCode}")
    public Flux<ServerSentEvent<Object>> connect(@PathVariable("storeCode") String storeCode) {
        Flux<NotifyDTO> userStream = sseChannel.connect(storeCode).toFlux();

        //1초 간격으로 이벤트를 발생하는 스트림
        Flux<String> tickStream = Flux.interval(Duration.ofSeconds(1)).map(tick -> "tick");

        return Flux.merge(userStream, tickStream)
                    .map(str -> ServerSentEvent.builder(str).build());
    }

    @PostMapping("/notify/reserve")
    public void reserve(@RequestBody NotifyDTO notifyDTO) {
        sseChannel.post(notifyDTO);
    }

}
