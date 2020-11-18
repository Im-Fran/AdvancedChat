package jss.advancedchat.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import jss.advancedchat.AdvancedChat;
import jss.advancedchat.utils.Utils;

public class MuteCmd implements CommandExecutor {

	private AdvancedChat plugin;

	public MuteCmd(AdvancedChat plugin) {
		this.plugin = plugin;
		plugin.getCommand("Mute").setExecutor(this);
	}

	@SuppressWarnings("null")
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Player j = (Player) sender;
		if (sender instanceof Player) {
			FileConfiguration config = plugin.getConfigfile().getConfig();
			Player p = Bukkit.getPlayer(args[0]);
			if (p == null) {
				Utils.sendColorMessage(j, config.getString("AdvancedChat.No-Online-Player").replace("<name>", p.getName()));
			}
			if (!plugin.mute.contains(p.getName())) {
				Utils.sendColorMessage(!plugin.mute.contains(p.getName())+"");
			} else {
				plugin.mute.add(p.getName());
			}

		} else {
			Utils.sendColorMessage(j, "Test Use /mute <player>");
		}
		return false;
	}

}
