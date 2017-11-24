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

	@Test
	public void testPut() {
		for (int i = 0; i < 100000; i++) {
			User user = new User();
			user.setId(UUID.randomUUID().toString());
			user.setName("Hong Gildong");
			user.setAge(20);
			user.setBirthday(Timestamp.valueOf(LocalDateTime.now()));

			Map<String, Object> map = jackson2ObjectMapperFactoryBean.getObject().convertValue(user,
					new TypeReference<Map<String, Object>>() {
					});
			String key = String.format("user:id:%s:name:%s", user.getId(), user.getName());
			redisRepository.put(key, map);
		}

		StopWatch sw = new StopWatch();
		sw.start();
		for (Object object : redisRepository.keys("user:id:*:name:*", "name")) {
			System.out.println(object);
		}
		sw.stop();
		System.out.println(sw.toString());

		redisRepository.deleteAll();
	}

	@Test
	public void testSet() throws Exception {
		for (int i = 0; i < 100000; i++) {
			User user = new User();
			user.setId(UUID.randomUUID().toString());
			user.setName("Hong Gildong");
			user.setAge(20);
			user.setBirthday(Timestamp.valueOf(LocalDateTime.now()));

			String key = String.format("user:id:%s:name:%s", user.getId(), user.getName());
			String valueAsString = jackson2ObjectMapperFactoryBean.getObject().writeValueAsString(user);
			redisRepository.set(key, valueAsString);
		}

		StopWatch sw = new StopWatch();
		sw.start();
		for (Object object : redisRepository.keys("user:id:*:name:*")) {
			System.out.println(object);
		}
		sw.stop();
		System.out.println(sw.toString());

		redisRepository.deleteAll();
	}

	@Test
	public void testCodeConstant() throws Exception {
		String value = redisRepository.get("class:code");
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
