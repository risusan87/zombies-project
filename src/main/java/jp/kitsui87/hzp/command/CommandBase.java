package jp.kitsui87.hzp.command;

import java.util.List;

import org.bukkit.command.CommandSender;

public abstract class CommandBase {
	
	private final String command;
	
	protected CommandBase(String command) {
		this.command = command;
	}
	
	public abstract List<String> getTabComplete(int argPos, String[] args);
	public abstract boolean onCommand(CommandSender sender, String[] args);
	public abstract String getHelpString();
	
	public String getCommand() {
		return this.command;
	}
	
}
