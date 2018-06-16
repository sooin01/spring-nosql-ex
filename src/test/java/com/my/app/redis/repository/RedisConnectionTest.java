package com.my.app.redis.repository;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:spring/root-context.xml", "classpath:spring/redis-context.xml" })
public class RedisConnectionTest {

	@Autowired
	private RedisTemplate<String, Object> redisTemplate;

	@Resource(name = "redisTemplate")
	private ValueOperations<String, Object> valueOps;

	@Resource(name = "redisTemplate")
	private ListOperations<String, Object> listOps;

	@Autowired
	private ChannelTopic channelTopic;

	@Test
	public void testGet() {
		Object value = valueOps.get("user:test");
		System.out.println(value);
	}

	@Test
	public void testPublish() throws Exception {
		for (int i = 0; i < 10; i++) {
			redisTemplate.convertAndSend(channelTopic.getTopic(), "message " + i);
			Thread.sleep(1000);
		}

		Thread.sleep(5000);
	}

}
