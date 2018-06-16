package com.my.app.redis.listener;

import java.io.UnsupportedEncodingException;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;

public class RedisMessageListener implements MessageListener {

	@Override
	public void onMessage(Message message, byte[] pattern) {
		try {
			System.out.println("Message received: " + message.toString() + ", " + new String(pattern, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

}
