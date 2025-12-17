package org.reactome.release.verifier;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 * Created 12/17/2025
 */
public class CommandLineParameter {
	private String name;
	private OptionType type;
	private String defaultValue;
	private boolean isRequired;
	private char shortFlag;
	private String longFlag;
	private String helpMessage;

	public static final boolean IS_REQUIRED = true;
	public static final boolean NOT_REQUIRED = false;

	public static CommandLineParameter create(
		String name, OptionType type, String defaultValue, boolean isRequired, char shortFlag, String longFlag,
		String helpMessage) {

		return new CommandLineParameter(name, type, defaultValue, isRequired, shortFlag, longFlag, helpMessage);
	}

	public String getName() {
		return name;
	}

	public OptionType getType() {
		return type;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public boolean isRequired() {
		return isRequired;
	}

	public char getShortFlag() {
		return shortFlag;
	}

	public String getLongFlag() {
		return longFlag;
	}

	public String getHelpMessage() {
		return helpMessage;
	}

	private CommandLineParameter(String name, OptionType type, String defaultValue, boolean isRequired, char shortFlag, String longFlag, String helpMessage) {
		this.name = name;
		this.type = type;
		this.defaultValue = defaultValue;
		this.isRequired = isRequired;
		this.shortFlag = shortFlag;
		this.longFlag = longFlag;
		this.helpMessage = helpMessage;
	}
}
