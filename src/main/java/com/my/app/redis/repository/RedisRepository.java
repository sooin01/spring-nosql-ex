package com.my.app.redis.repository;

import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Repository;

@Repository
public class RedisRepository {

	@Autowired
	private RedisTemplate<String, Object> redisTemplate;

	@Resource(name = "redisTemplate")
	private ValueOperations<String, Object> valueOps;

	@Resource(name = "redisTemplate")
	private ListOperations<String, Object> listOps;

	@Resource(name = "redisTemplate")
	private SetOperations<String, Object> setOps;

	@Resource(name = "redisTemplate")
	private HashOperations<String, String, Object> hashOps;

	public Object getValue(String key) {
		return valueOps.get(key);
	}

	public void setValue(String key, Object value) {
		valueOps.set(key, value);
	}

	public Object getHash(String key, Object hashKey) {
		return hashOps.get(key, hashKey);
	}

	public void rightPush(String key, Object value) {
		listOps.rightPush(key, value);
	}

	public void putHash(String key, String hashKey, Object value) {
		hashOps.put(key, hashKey, value);
	}

	public void putAllHash(String key, Map<? extends String, ? extends Object> m) {
		hashOps.putAll(key, m);
	}

	public void scanHash(String key) {
		Cursor<Entry<String, Object>> cursor = hashOps.scan(key, ScanOptions.NONE);
		while (cursor.hasNext()) {
			Entry<String, Object> entry = cursor.next();
			System.out.println(entry.getKey() + "=" + entry.getValue());
		}
	}

}
