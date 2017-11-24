package com.my.app.redis.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Repository;

@Repository
public class RedisRepository {

	@Autowired
	private RedisTemplate<String, String> redisTemplate;

	@Resource(name = "redisTemplate")
	private ValueOperations<String, String> valueOps;

	@Resource(name = "redisTemplate")
	private ListOperations<String, String> listOps;

	@Resource(name = "redisTemplate")
	private HashOperations<String, String, Object> hashOps;

	public String get(String key) {
		return valueOps.get(key);
	}

	public void set(String key, String value) {
		valueOps.set(key, value);
	}

	public List<String> keys(String pattern) {
		List<String> strings = new ArrayList<>();

		for (String key : redisTemplate.keys(pattern)) {
			String string = get(key);

			if (string != null) {
				strings.add(string);
			}
		}

		return strings;
	}

	public List<Object> keys(String pattern, Object hashkey) {
		List<Object> objects = new ArrayList<>();

		for (String key : redisTemplate.keys(pattern)) {
			Object object = get(key, hashkey);

			if (object != null) {
				objects.add(object);
			}
		}

		return objects;
	}

	public Object get(String key, Object hashKey) {
		return hashOps.get(key, hashKey);
	}

	public void put(String key, Map<? extends String, ? extends Object> m) {
		hashOps.putAll(key, m);
	}

	public void delete(String key) {
		redisTemplate.delete(key);
	}

	public void deleteAll() {
		redisTemplate.delete(redisTemplate.keys("*"));
	}

}
