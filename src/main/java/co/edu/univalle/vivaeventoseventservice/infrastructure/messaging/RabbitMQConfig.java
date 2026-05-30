package co.edu.univalle.vivaeventoseventservice.infrastructure.messaging;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "vivaeventos.events";
    public static final String ROUTING_KEY_EVENTO_CANCELADO = "evento.cancelado";
    public static final String QUEUE_EVENTO_CANCELADO = "evento.cancelado";

    @Bean
    public TopicExchange vivaeventosExchange() {

        return new TopicExchange(EXCHANGE, true, false);
    }
    @Bean
    public Queue eventoCanceladoQueue() {

        return QueueBuilder.durable(QUEUE_EVENTO_CANCELADO).build();
    }
    @Bean
    public Binding eventoCanceladoBinding(Queue eventoCanceladoQueue,
                                          TopicExchange vivaeventosExchange) {
        return BindingBuilder
                .bind(eventoCanceladoQueue)
                .to(vivaeventosExchange)
                .with(ROUTING_KEY_EVENTO_CANCELADO);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}