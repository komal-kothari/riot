package com.redislabs.riot.redis.writer.map;

import java.util.Map;

import com.redislabs.lettusearch.search.AddOptions;
import com.redislabs.riot.redis.writer.KeyBuilder;

import lombok.Builder;

public class FtAdd extends AbstractFtAdd {

	@Builder
	protected FtAdd(KeyBuilder keyBuilder, boolean keepKeyFields, String index, String score, double defaultScore,
			AddOptions options) {
		super(keyBuilder, keepKeyFields, index, score, defaultScore, options);
	}

	@Override
	protected String payload(Map<String, Object> item) {
		return null;
	}

}