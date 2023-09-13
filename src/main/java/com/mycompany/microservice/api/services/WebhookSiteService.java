package com.mycompany.microservice.api.services;

import com.mycompany.microservice.api.clients.http.WebhookSiteHttpClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebhookSiteService {

  private final WebhookSiteHttpClient client;

  public Mono<String> post(final Object request) {
    log.info("HTTP[REQUEST] {}", request);
    return this.client
        .post(request)
        // Deserialization is done at service level to keep code DRY.
        .map(response -> response);
  }
}