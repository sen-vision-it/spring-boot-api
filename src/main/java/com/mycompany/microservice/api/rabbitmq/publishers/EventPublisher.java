package com.mycompany.microservice.api.rabbitmq.publishers;

import static com.mycompany.microservice.api.constants.AppConstants.TRACE_ID_LOG;
import static java.lang.String.format;

import com.mycompany.microservice.api.utils.JsonUtils;
import com.mycompany.microservice.api.utils.TraceUtils;
import io.micrometer.tracing.Tracer;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.MessagePropertiesBuilder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventPublisher {

  private final AmqpTemplate amqpTemplate;
  private final Tracer tracer;

  public void publish(
      @NotNull final String exchange,
      @NotNull final String routingKey,
      @NotNull final Object payload) {

    try {

      final String msg = JsonUtils.serializeToCamelCase(payload);

      final MessageProperties props =
          MessagePropertiesBuilder.newInstance()
              .setContentType(MessageProperties.CONTENT_TYPE_JSON)
              .setContentEncoding(StandardCharsets.UTF_8.toString())
              .setHeader(TRACE_ID_LOG, TraceUtils.getTrace(this.tracer))
              .build();

      log.info("[PUB][{}] headers {} payload {} ", routingKey, props.getHeaders(), payload);

      final Message message = MessageBuilder.withBody(msg.getBytes()).andProperties(props).build();
      this.amqpTemplate.send(exchange, routingKey, message);

    } catch (final Exception ex) {
      log.error(
          format("[PUB][%s] error publishing message with payload %s", routingKey, payload), ex);
    }
  }
}