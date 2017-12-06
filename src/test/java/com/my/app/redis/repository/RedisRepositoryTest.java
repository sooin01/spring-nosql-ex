package com.my.app.redis.repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.text.WordUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.Jackson2ObjectMapperFactoryBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.CaseFormat;
import com.my.app.redis.domain.User;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:spring/root-context.xml", "classpath:spring/redis-context.xml" })
public class RedisRepositoryTest {

	@Autowired
	private RedisRepository redisRepository;

	@Autowired
	private Jackson2ObjectMapperFactoryBean jackson2ObjectMapperFactoryBean;

	private User getUser() throws Exception {
		User user = new User();
		user.setId(UUID.randomUUID().toString());
		user.setName("Hong Gildong");
		user.setAge(20);
		user.setBirthday(Timestamp.valueOf(LocalDateTime.now()));
		return user;
	}

	private String getUserString(User user) throws Exception {
		return jackson2ObjectMapperFactoryBean.getObject().writeValueAsString(user);
	}

	private String getUserString() throws Exception {
		User user = new User();
		user.setId(UUID.randomUUID().toString());
		user.setName("Hong Gildong");
		user.setAge(20);
		user.setBirthday(Timestamp.valueOf(LocalDateTime.now()));
		return jackson2ObjectMapperFactoryBean.getObject().writeValueAsString(user);
	}

	@Test
	public void testPushList() throws Exception {
		redisRepository.rightPush("node:names", getUserString());
	}

	@Test
	public void testPut() throws Exception {
		redisRepository.putHash("node:names", "127.0.0.1", getUserString());
		redisRepository.putHash("node:names", "172.16.0.1", getUserString());
		redisRepository.putHash("node:names", "192.168.0.1", getUserString());
	}

	@Test
	public void testPutHash() throws Exception {
		for (int i = 0; i < 1; i++) {
			User user = getUser();

			Map<String, Object> map = jackson2ObjectMapperFactoryBean.getObject().convertValue(user,
					new TypeReference<Map<String, Object>>() {
					});
			String key = String.format("user:id:%s:user_details", user.getId());
			redisRepository.putAllHash(key, map);
		}

		StopWatch sw = new StopWatch();
		sw.start();
		sw.stop();
		System.out.println(sw.toString());
	}

	@Test
	public void testScanHash() {
		String key = "user:id:0d908f89-8656-4ebb-ac82-a58c0bc190c3";
		redisRepository.scanHash(key);
	}

	@Test
	public void testSetValue() throws Exception {
		for (int i = 0; i < 1; i++) {
			User user = getUser();
			String key = String.format("user:id:%s", user.getId());
			redisRepository.setValue(key, user);
		}

		StopWatch sw = new StopWatch();
		sw.start();
		sw.stop();
		System.out.println(sw.toString());
	}

	@Test
	public void testCodeConstant() throws Exception {
		String value = (String) redisRepository.getValue("class:code");
		ObjectMapper objectMapper = jackson2ObjectMapperFactoryBean.getObject();

		StringBuilder sb = new StringBuilder();
		sb.append("public final class CodeConstant\n");
		sb.append("{\n");
		sb.append("\n");

		List<Map<String, Map<String, String>>> codeList = objectMapper.readValue(value,
				new TypeReference<List<Map<String, Map<String, String>>>>() {
				});

		for (Map<String, Map<String, String>> map : codeList) {
			for (Entry<String, Map<String, String>> entry : map.entrySet()) {
				String columnName = entry.getKey();
				String columnNameEnum = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, columnName);
				sb.append(String.format("\tpublic enum %s\n", columnNameEnum));
				sb.append("\t{\n");

				for (Entry<String, String> entry2 : entry.getValue().entrySet()) {
					String codeId = entry2.getKey();
					String codeValue = entry2.getValue();
					String codeValueEnum = Arrays.stream(codeValue.replaceAll("-|/", " ").split(" "))
							.map(p -> WordUtils.capitalize(p)).collect(Collectors.joining());
					sb.append(
							String.format("\t\t%s(%s, \"%s\"), // %s\n", codeValueEnum, codeId, codeValue, columnName));
				}
				sb.append("\t\t;\n\n");

				sb.append("\t\tprivate long codeId;\n");
				sb.append("\t\tprivate String value;\n");
				sb.append("\n");

				sb.append(String.format("\t\tprivate %s(long codeId, String value)\n", columnNameEnum));
				sb.append("\t\t{\n");
				sb.append("\t\t\tthis.codeId = codeId;\n");
				sb.append("\t\t\tthis.value = value;\n");
				sb.append("\t\t}\n");
				sb.append("\n");

				sb.append("\t\tpublic static long codeId(String value)\n");
				sb.append("\t\t{\n");
				sb.append(String.format(
						"\t\t\treturn Arrays.stream(%s.values()).filter(p -> p.value.equals(value)).findFirst().get().codeId;\n",
						columnNameEnum));
				sb.append("\t\t}\n");
				sb.append("\n");
				sb.append("\t\tpublic static String value(long codeId)\n");
				sb.append("\t\t{\n");
				sb.append(String.format(
						"\t\t\treturn Arrays.stream(%s.values()).filter(p -> p.codeId == codeId).findFirst().get().value;\n",
						columnNameEnum));
				sb.append("\t\t}\n");
				sb.append("\n");

				sb.append("\t}\n");
				sb.append("\n");
			}
		}

		sb.append("}\n");

		System.out.println(sb.toString());
	}

}
