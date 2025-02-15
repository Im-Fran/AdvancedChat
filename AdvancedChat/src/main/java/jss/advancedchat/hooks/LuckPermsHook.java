package jss.advancedchat.hooks;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import jss.advancedchat.common.interfaces.IHook;
import jss.advancedchat.manager.HookManager;
import jss.advancedchat.utils.EventUtils;
import jss.advancedchat.utils.Logger;
import jss.advancedchat.utils.Settings;
import jss.advancedchat.utils.Util;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;

public class LuckPermsHook implements IHook {
	
	private HookManager hooksManager;
	private boolean isEnabled;
		
	public LuckPermsHook(HookManager hooksManager) {
		this.hooksManager = hooksManager;
	}

	public void setup() {
		if(!Bukkit.getPluginManager().isPluginEnabled("LuckPerms")) {
			this.isEnabled = false;
			Logger.warning("&eLuckPerms not enabled! - Disable Features...");
			return;
		}
		
		if(!Settings.hook_luckperms) {
			this.isEnabled = false;
			Logger.warning("&eLuckPerms not enabled! - Disable Features...");
			return;
		}
						
		this.isEnabled = true;
		Util.sendColorMessage(EventUtils.getConsoleSender() , Util.getPrefix(true) + "&aLoading LuckPerms features...");
	}
	
	public boolean isEnabled() {
		return isEnabled;
	}

	public HookManager getHooksManager() {
		return hooksManager;
	}
	
	public static LuckPerms getApi() {
		return LuckPermsProvider.get();
	}
	
	public boolean isGroup(Player player, String name){
		LuckPerms api = LuckPermsProvider.get();
		String group = api.getUserManager().getUser(player.getName()).getPrimaryGroup();
		return 	name.equals(group) ? true : false;
	}

	public String getGroup(Player player){
		if(player != null) {
			Logger.debug("&e{Group} &9-> &7Is the player not exists");
		}
		LuckPerms api = LuckPermsProvider.get();
		return api.getUserManager().getUser(player.getName()).getPrimaryGroup();
	}
		
}
