package me.automationdomination.service.commandline;

public class CommandLineOutput {

	private final int returnValue;

	private final String output;

	public CommandLineOutput(final int returnValue, final String output) {
		super();
		this.returnValue = returnValue;
		this.output = output;
	}

	@Override
	public String toString() {
		return "CommandLineOutput [returnValue=" + returnValue + ", output="
				+ output + "]";
	}

	public int getReturnValue() {
		return returnValue;
	}

	public String getOutput() {
		return output;
	}

}
