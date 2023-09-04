package jp.kitsui87.hzp.command;

import java.util.List;

import org.bukkit.command.CommandSender;

public class CommandSaveLocation extends CommandBase {

	protected CommandSaveLocation() {
		super("saveLocation");
	}

	@Override
	public List<String> getTabComplete(int argPos, String[] args) {
		return null;
	}

	@Override
	public boolean onCommand(CommandSender sender, String[] args) {
		return false;
	}

	@Override
	public String getHelpString() {
		return "";
	}

}
