package jss.advancedchat.listeners.chat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import com.cryptomorin.xseries.XSound;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;

import jss.advancedchat.AdvancedChat;
import jss.advancedchat.chat.Json;
import jss.advancedchat.hooks.DiscordSRVHook;
import jss.advancedchat.hooks.LuckPermsHook;
import jss.advancedchat.hooks.VaultHook;
import jss.advancedchat.manager.ColorManager;
import jss.advancedchat.manager.GroupManager;
import jss.advancedchat.manager.HookManager;
import jss.advancedchat.manager.PlayerManager;
import jss.advancedchat.storage.MySQL;
import jss.advancedchat.utils.Utils;
import jss.advancedchat.utils.Logger;
import jss.advancedchat.utils.Settings;

public class ChatListener implements Listener {

	private AdvancedChat plugin;
	public Map<String, Long> delaywords = new HashMap<String, Long>();
	private ColorManager colorManager;
	private boolean badword;
	private boolean ismention;
	
	//experimental
	private final Pattern COLOR_REGEX = Pattern.compile("(?i)&([0-9A-F])");
	private final Pattern MAGIC_REGEN = Pattern.compile("(?i)&([K])");
	private final Pattern BOLD_REGEX = Pattern.compile("(?i)&([L])");
	private final Pattern STRIKETHROUGH_REGEX = Pattern.compile("(?i)&([M])");
	private final Pattern UNDERLINE_REGEX = Pattern.compile("(?i)&([N])");
	private final Pattern ITALIC_REGEX = Pattern.compile("(?i)&([O])");
	private final Pattern RESET_REGEX = Pattern.compile("(?i)&([R])");
	
	public ChatListener(AdvancedChat plugin) {
		this.plugin = plugin;
	}
	
	//Mute chat
	@EventHandler(priority = EventPriority.HIGH)
	public void chatMute(AsyncPlayerChatEvent e) {
		Player j = e.getPlayer();
		PlayerManager playerManager = new PlayerManager(j);
		
		if (Settings.mysql_use) {
			if (j.isOp() || j.hasPermission("AdvancedChat.Mute.Bypass")) return;
				if (MySQL.isMute(plugin, j.getUniqueId().toString())) {
					Utils.sendColorMessage(j, Utils.getVar(j, Settings.message_Alert_Mute));
					e.setCancelled(true);
				}
		} else {
			if (j.isOp() || j.hasPermission("AdvancedChat.Mute.Bypass")) return;
				if (playerManager.isMute()) {
					Utils.sendColorMessage(j, Utils.getVar(j, Settings.message_Alert_Mute));
					e.setCancelled(true);
				}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void chatFormat(AsyncPlayerChatEvent e) {
		FileConfiguration config = plugin.getConfigFile().getConfig();
		GroupManager groupManager = new GroupManager();
		VaultHook vaultHook = HookManager.get().getVaultHook();
		DiscordSRVHook discordSRVHook = HookManager.get().getDiscordSRVHook();
		LuckPermsHook luckPermsHook = HookManager.get().getLuckPermsHook();
		
		Player j = e.getPlayer();
		PlayerManager playerManager = new PlayerManager(j);
		String path = Settings.chatformat_chattype;
		
		boolean isDefault = path.equalsIgnoreCase("default");
		boolean isNormal = path.equalsIgnoreCase("normal");
		boolean isGroup = path.equalsIgnoreCase("group");
		
		String format = config.getString("ChatFormat.Format");
		String message = "";
		
		if(Settings.mysql_use) {
			message = " &r" + colorManager.convertColor(j, MySQL.getColor(plugin, j.getUniqueId().toString()), e.getMessage());
		} else {
			message = " &r" + colorManager.convertColor(j, playerManager.getColor(), e.getMessage());
		}
		
		format = Utils.getVar(j, format);
		message = Utils.getVar(j, message);
		
		if ((j.isOp()) || (j.hasPermission("AdvancedChat.Chat.Color"))) {
			format = Utils.color(format);
			message = Utils.color(message);
		}
		
		if(playerManager.isMute() || this.badword) {
			this.badword = false;
			return;
		}
		
		if(this.ismention) {
			this.ismention = false;
			return;
		}
		
		if(isDefault) {
			return;
		} else if(isNormal) {
			
			Json json = new Json(j, format, message);
			
			boolean hover = config.getString("ChatFormat.HoverEvent.Enabled").equals("true");
			List<String> hovertext = config.getStringList("ChatFormat.HoverEvent.Hover");
			
			boolean click = config.getString("ChatFormat.ClickEvent.Enabled").equals("true");
			String cmd_action = config.getString("ChatFormat.ClickEvent.Actions.Command");
			String click_mode = config.getString("ChatFormat.ClickEvent.Mode");
			String url_action = config.getString("ChatFormat.ClickEvent.Actions.Url");
			String suggest_action = config.getString("ChatFormat.ClickEvent.Actions.Suggest-Command");
			
			cmd_action = Utils.getVar(j, cmd_action);
			suggest_action = Utils.getVar(j, suggest_action);
			
			if (hover) {
				if (click) {
					if (click_mode.equals("command")) {
						json.setHover(hovertext).setExecuteCommand(cmd_action).sendDoubleToAll();
					} else if (click_mode.equals("url")) {
						json.setHover(hovertext).setOpenURL(url_action).sendDoubleToAll();
					} else if (click_mode.equals("suggest")) {
						json.setHover(hovertext).setSuggestCommand(suggest_action).sendDoubleToAll();
					}
				} else {
					json.setHover(hovertext).sendDoubleToAll();
				}
			} else {
				if (click) {
					if (click_mode.equals("command")) {
						json.setExecuteCommand(cmd_action).sendDoubleToAll();
					} else if (click_mode.equals("url")) {
						json.setOpenURL(url_action).sendDoubleToAll();
					} else if (click_mode.equals("suggest")) {
						json.setSuggestCommand(suggest_action).sendDoubleToAll();
					}
				} else {
					json.sendDoubleToAll();
				}
			}
			return;
		} else if(isGroup) {
			
			LuckPerms luckPerms = LuckPermsProvider.get();
			
			String vaultGroup = VaultHook.getVaultHook().getChat().getPrimaryGroup(j);
			String luckpermsGroup = luckPerms.getUserManager().getUser(j.getName()).getPrimaryGroup();
			
			String group = "";
			
			if(vaultHook.isEnabled()) {
				group = vaultGroup;
			}else if(luckPermsHook.isEnabled()) {
				group = luckpermsGroup;
			}else {
				Logger.error("&cThe Vault or LuckPerms could not be found to activate the group system");
				Logger.warning("&eplease check that Vault or LuckPerms is active or inside your plugins folder");
				return;
			}
			
			Json json = new Json(j, format, message);
			
			boolean hover = groupManager.isHover(group);
			List<String> hovertext = groupManager.getHover(group);
			
			boolean click = groupManager.isClick(group);
			String click_mode = groupManager.getClickMode(group);
			String cmd_action = groupManager.getClickCommand(group);
			String url_action = groupManager.getClickUrl(group);
			String suggest_action = groupManager.getClickSuggestCommand(group);
			
			cmd_action = Utils.getVar(j, cmd_action);
			suggest_action = Utils.getVar(j, suggest_action);
			
			if (hover) {
				if (click) {
					if (click_mode.equals("command")) {
						json.setHover(hovertext).setExecuteCommand(cmd_action).sendDoubleToAll();
					} else if (click_mode.equals("url")) {
						json.setHover(hovertext).setOpenURL(url_action).sendDoubleToAll();
					} else if (click_mode.equals("suggest")) {
						json.setHover(hovertext).setSuggestCommand(suggest_action).sendDoubleToAll();
					}
				} else {
					json.setHover(hovertext).sendDoubleToAll();
				}
			} else {
				if (click) {
					if (click_mode.equals("command")) {
						json.setExecuteCommand(cmd_action).sendDoubleToAll();
					} else if (click_mode.equals("url")) {
						json.setOpenURL(url_action).sendDoubleToAll();
					} else if (click_mode.equals("suggest")) {
						json.setSuggestCommand(suggest_action).sendDoubleToAll();
					}
				} else {
					json.sendDoubleToAll();
				}
			}
			return;
		} else {
			e.setFormat("<" + j.getName() + ">" + " " + e.getMessage());
			Logger.error("");
		}		
	}
	
	@EventHandler 
	public void chatMention(AsyncPlayerChatEvent e){
		e.setCancelled(true);
		Player j = e.getPlayer();
		String message = e.getMessage();
		
		if(Settings.mention) {
			Utils.sendColorMessage(j, Settings.mention_send);
			if(message.contains(j.getName())) {
				this.ismention = true;
				
				for(Player p : Bukkit.getOnlinePlayers()) {
					
					p.playSound(p.getLocation(), Sound.valueOf(Settings.mention_sound_name), Settings.mention_sound_volume, Settings.mention_sound_pitch);
					Utils.sendColorMessage(p, Settings.mention_receive);
				}	
			}
		}
	}
	
	private String formatColor(String msg, Player player) {
		if (msg == null)
			return "";

		boolean canReset = false;

		if (!player.hasPermission("AdvancedChat.Chat.Color")) {
			msg = COLOR_REGEX.matcher(msg).replaceAll("\u00A7$1");
			canReset = true;
		}

		if (!player.hasPermission("AdvancedChat.Chat.Magic")) {
			msg = MAGIC_REGEN.matcher(msg).replaceAll("\u00A7$1");
			canReset = true;
		}

		if (!player.hasPermission("AdvancedChat.Chat.Bold")) {
			msg = BOLD_REGEX.matcher(msg).replaceAll("\u00A7$1");
			canReset = true;
		}

		if (!player.hasPermission("AdvancedChat.Chat.Strikethrough")) {
			msg = STRIKETHROUGH_REGEX.matcher(msg).replaceAll("\u00A7$1");
			canReset = true;
		}

		if (!player.hasPermission("AdvancedChat.Chat.Underline")) {
			msg = UNDERLINE_REGEX.matcher(msg).replaceAll("\u00A7$1");
			canReset = true;
		}

		if (!player.hasPermission("AdvancedChat.Chat.italic")) {
			msg = ITALIC_REGEX.matcher(msg).replaceAll("\u00A7$1");
			canReset = true;
		}

		if (canReset) {
			msg = RESET_REGEX.matcher(msg).replaceAll("\u00A7$1");
		}

		return msg;
	}
}