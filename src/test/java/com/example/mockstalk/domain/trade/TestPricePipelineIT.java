package com.example.mockstalk.domain.trade;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.awaitility.Awaitility.await;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import com.example.mockstalk.common.config.RabbitConfig;
import com.example.mockstalk.domain.stock.entity.Stock;
import com.example.mockstalk.domain.stock.repository.StockRepository;
import com.example.mockstalk.domain.trade.dto.StockPriceEventDto;
import com.example.mockstalk.domain.trade.listener.StockPriceListener;
import com.example.mockstalk.domain.trade.service.TestPricePublishService;
import com.example.mockstalk.domain.trade.service.TradeService;

/**
 * Real Redis + RabbitMQ integration path for the test price pipeline:
 * TestPricePublishService → Redis {@code stockPrice::{code}} + Rabbit publish
 * → StockPriceListener → TradeService.onPriceUpdated.
 * <p>
 * Requires Docker. Skipped automatically when Docker is unavailable
 * ({@code disabledWithoutDocker = true}). Matching logic is not exercised beyond the listener call.
 */
@Testcontainers(disabledWithoutDocker = true)
class TestPricePipelineIT {

	@Container
	@SuppressWarnings("resource")
	static final GenericContainer<?> REDIS = new GenericContainer<>(DockerImageName.parse("redis:7"))
		.withExposedPorts(6379);

	@Container
	@SuppressWarnings("resource")
	static final GenericContainer<?> RABBIT = new GenericContainer<>(DockerImageName.parse("rabbitmq:3-management"))
		.withExposedPorts(5672)
		.withEnv("RABBITMQ_DEFAULT_USER", "guest")
		.withEnv("RABBITMQ_DEFAULT_PASS", "guest");

	private LettuceConnectionFactory redisConnectionFactory;
	private RedisTemplate<String, Object> redisTemplate;
	private CachingConnectionFactory rabbitConnectionFactory;
	private RabbitTemplate rabbitTemplate;
	private SimpleMessageListenerContainer listenerContainer;
	private TradeService tradeService;
	private TestPricePublishService testPricePublishService;

	@BeforeEach
	void setUp() {
		redisConnectionFactory = new LettuceConnectionFactory(REDIS.getHost(), REDIS.getMappedPort(6379));
		redisConnectionFactory.afterPropertiesSet();
		redisTemplate = new RedisTemplate<>();
		redisTemplate.setConnectionFactory(redisConnectionFactory);
		redisTemplate.setKeySerializer(new StringRedisSerializer());
		redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
		redisTemplate.afterPropertiesSet();

		rabbitConnectionFactory = new CachingConnectionFactory(RABBIT.getHost(), RABBIT.getMappedPort(5672));
		rabbitConnectionFactory.setUsername("guest");
		rabbitConnectionFactory.setPassword("guest");
		MessageConverter converter = new Jackson2JsonMessageConverter();
		rabbitTemplate = new RabbitTemplate(rabbitConnectionFactory);
		rabbitTemplate.setMessageConverter(converter);

		RabbitAdmin admin = new RabbitAdmin(rabbitConnectionFactory);
		TopicExchange exchange = new TopicExchange(RabbitConfig.EXCHANGE_NAME);
		Queue queue = new Queue(RabbitConfig.QUEUE_NAME);
		admin.declareExchange(exchange);
		admin.declareQueue(queue);
		admin.declareBinding(BindingBuilder.bind(queue).to(exchange).with(RabbitConfig.ROUTING_KEY));

		tradeService = Mockito.mock(TradeService.class);
		StockPriceListener listener = new StockPriceListener(tradeService);

		listenerContainer = new SimpleMessageListenerContainer(rabbitConnectionFactory);
		listenerContainer.setQueueNames(RabbitConfig.QUEUE_NAME);
		listenerContainer.setMessageListener(message -> {
			StockPriceEventDto event = (StockPriceEventDto)converter.fromMessage(message);
			listener.onPriceUpdated(event);
		});
		listenerContainer.start();

		StockRepository stockRepository = mock(StockRepository.class);
		Stock stock = Stock.builder().id(1L).stockCode("005930").stockName("삼성전자").build();
		when(stockRepository.findById(1L)).thenReturn(Optional.of(stock));

		testPricePublishService = new TestPricePublishService(stockRepository, redisTemplate, rabbitTemplate);
	}

	@AfterEach
	void tearDown() {
		if (listenerContainer != null) {
			listenerContainer.stop();
		}
		if (rabbitConnectionFactory != null) {
			rabbitConnectionFactory.destroy();
		}
		if (redisConnectionFactory != null) {
			redisConnectionFactory.destroy();
		}
	}

	@Test
	@DisplayName("테스트 시세: Redis stockPrice:: 저장 + Rabbit stock.price → Listener → onPriceUpdated")
	void publishTestPrice_reachesRedisAndListener() {
		BigDecimal price = new BigDecimal("72500");

		testPricePublishService.publishTestPrice(1L, price);

		Object cached = redisTemplate.opsForValue().get("stockPrice::005930");
		assertThat(cached).isNotNull();
		assertThat(cached.toString()).contains("72500");

		await()
			.atMost(Duration.ofSeconds(10))
			.untilAsserted(() -> verify(tradeService).onPriceUpdated(eq(1L), argThat(
				p -> p != null && p.compareTo(price) == 0)));
	}
}
