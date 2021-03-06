package com.my.app.redis.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = { "classpath:spring/root-context.xml", "classpath:spring/redis-context.xml" })
public class RedisServiceTest {

	@Autowired
	private RedisService redisService;

	@Test
	public void testGetValue() {
		redisService.getValue("test");
	}

}
