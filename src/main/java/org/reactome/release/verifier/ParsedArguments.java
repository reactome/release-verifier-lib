package org.reactome.release.verifier;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 * Created 12/17/2025
 */
public interface ParsedArguments {
	String getString(String name);
	int getInt(String name);
	boolean has(String name);
}
