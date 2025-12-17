package org.reactome.release.verifier;

import com.martiansoftware.jsap.JSAPResult;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 * Created 12/17/2025
 */
public class JsapParsedArguments implements ParsedArguments {
	private final JSAPResult result;

	JsapParsedArguments(JSAPResult result) {
		this.result = result;
	}

	@Override
	public String getString(String name) {
		return result.getString(name);
	}

	@Override
	public int getInt(String name) {
		return result.getInt(name);
	}

	@Override
	public boolean has(String name) {
		return result.contains(name);
	}
}
