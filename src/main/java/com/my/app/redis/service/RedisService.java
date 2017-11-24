package com.my.app.redis.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.my.app.redis.repository.RedisRepository;

@Service
public class RedisService {

	@Autowired
	private RedisRepository redisRepository;

	public void get(String key) {
		redisRepository.get(key);
	}

}
