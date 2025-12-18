package org.reactome.release.verifier;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.Parameter;
import com.martiansoftware.jsap.Switch;

/**
 * @author Joel Weiser
 * Created 12/17/2025
 */
final class JsapParameterMapper {

	static Parameter toJsapParameter(CommandLineParameter option) {
		switch (option.getType()) {
			case STRING:
				return new FlaggedOption(
					option.getName(),
					JSAP.STRING_PARSER,
					defaultValue(option),
					required(option),
					option.getShortFlag(),
					option.getLongFlag(),
					option.getHelpMessage()
				);

			case INTEGER:
				return new FlaggedOption(
					option.getName(),
					JSAP.INTEGER_PARSER,
					defaultValue(option),
					required(option),
					option.getShortFlag(),
					option.getLongFlag(),
					option.getHelpMessage()
				);

			case BOOLEAN:
				return new Switch(
					option.getName(),
					option.getShortFlag(),
					option.getLongFlag(),
					option.getHelpMessage()
				);

			default:
				throw new IllegalArgumentException(
					"Unsupported option type: " + option.getType()
				);
		}
	}

	private static String defaultValue(CommandLineParameter option) {
		return option.getDefaultValue().isEmpty()
			? JSAP.NO_DEFAULT
			: option.getDefaultValue();
	}

	private static boolean required(CommandLineParameter option) {
		return option.isRequired()
			? JSAP.REQUIRED
			: JSAP.NOT_REQUIRED;
	}
}
