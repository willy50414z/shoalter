package com.shoalter.willy.shoaltertools.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Optional;

@Service
public class TestRedisLuaCrossNodeIssueService
{
//	@Autowired
//	ReactiveRedisTemplate reactiveRedisTemplate;
//	public void testBy(){
//
//		DefaultRedisScript script = new DefaultRedisScript<>();
//		script.setScriptSource(
//				new ResourceScriptSource(new ClassPathResource("/lua/testGet3Keys.lua.lua")));
//		script.setResultType(List.class);
//		Flux<Optional<List<Object>>> result =
//				reactiveRedisTemplate
//						.execute(script, uuidList, params)
//						.map(Optional::of)
//						.defaultIfEmpty(Optional.empty());
//	}
}
