package com.cos.chatapp;

import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@CrossOrigin
@RestController
public class ChatController {

	Sinks.Many<String> sink;

	public ChatController() {
		this.sink  = Sinks.many().multicast().onBackpressureBuffer();
	}
	
	@GetMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
	public Flux<Integer> findAll() {
		return Flux.just(1,2,3,4,5,6).log();
	}
	
	@GetMapping("/send")
	public void send(String message) {
		sink.tryEmitNext(message);
	}
	
	// data : 실제 값 \n\n
	@GetMapping(value = "/sse") 
	public Flux<ServerSentEvent<String>> sse() { // ServerSendEvent의 ContentType은 text event stream
		return sink.asFlux().map(e -> ServerSentEvent.builder(e).build()).doOnCancel(()-> {
			System.out.println("SSE 종료됨");
			sink.asFlux().blockLast();
		}); // 구독
	}
	
}
