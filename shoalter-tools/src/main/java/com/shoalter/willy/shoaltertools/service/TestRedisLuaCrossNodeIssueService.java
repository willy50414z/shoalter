package com.shoalter.willy.shoaltertools.service;

import org.springframework.stereotype.Service;

@Service
public class TestRedisLuaCrossNodeIssueService {
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
