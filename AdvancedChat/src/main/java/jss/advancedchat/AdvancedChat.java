package jss.advancedchat;

import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import jss.advancedchat.api.AdvancedChatApi;
import jss.advancedchat.commands.AdvancedChatCmd;
import jss.advancedchat.commands.ClearChatCmd;
import jss.advancedchat.commands.MsgCmd;
import jss.advancedchat.commands.MuteCmd;
import jss.advancedchat.commands.UnMuteCmd;
import jss.advancedchat.common.AdvancedChatPlugin;
import jss.advancedchat.common.api.AdvancedChatApiProvider;
import jss.advancedchat.common.update.UpdateSettings;
import jss.advancedchat.config.BadWordFile;
import jss.advancedchat.config.ChatDataFile;
import jss.advancedchat.config.ChatLogFile;
import jss.advancedchat.config.CommandFile;
import jss.advancedchat.config.CommandLogFile;
import jss.advancedchat.config.ConfigFile;
import jss.advancedchat.config.GroupFile;
import jss.advancedchat.config.InventoryDataFile;
import jss.advancedchat.config.MessageFile;
import jss.advancedchat.config.PreConfigLoader;
import jss.advancedchat.config.gui.ChannelGuiFile;
import jss.advancedchat.config.gui.ColorFile;
import jss.advancedchat.config.gui.GradientColorFile;
import jss.advancedchat.config.gui.PlayerGuiFile;
import jss.advancedchat.config.player.PlayerFile;
import jss.advancedchat.listeners.EventLoader;
import jss.advancedchat.listeners.JoinListener;
import jss.advancedchat.listeners.chat.ChatLogListener;
import jss.advancedchat.listeners.chat.CommandListener;
import jss.advancedchat.listeners.inventory.ColorInventoryListener;
import jss.advancedchat.listeners.inventory.ErrorInventoryListener;
import jss.advancedchat.listeners.inventory.GradientInventoryListener;
import jss.advancedchat.listeners.inventory.PlayerInventoryListener;
import jss.advancedchat.listeners.inventory.RainbowInventoryListener;
import jss.advancedchat.listeners.inventory.SettingsInventoryListener;
import jss.advancedchat.manager.ChatManager;
import jss.advancedchat.manager.HookManager;
import jss.advancedchat.storage.MySQL;
import jss.advancedchat.storage.MySQLConnection;
import jss.advancedchat.test.ChatListenerTest;
import jss.advancedchat.utils.EventUtils;
import jss.advancedchat.utils.Logger;
import jss.advancedchat.utils.Settings;
import jss.advancedchat.utils.inventory.InventoryView;
import jss.advancedchat.utils.UpdateChecker;
import jss.advancedchat.utils.Util;
import jss.advancedchat.utils.file.FileManager;

public class AdvancedChat extends AdvancedChatPlugin {
	
	private static AdvancedChat instance;
	private PreConfigLoader preConfigLoad;
	public Logger logger = new Logger();
	private MySQL mySQL ;
	public MySQLConnection connection = new MySQLConnection();
	public EventUtils eventUtils;
	public Metrics metrics;
	public HookManager HookManager;
	public FileManager fileManager = new FileManager(this);
	public ArrayList<InventoryView> inventoryView;
	public ArrayList<ChatManager> chatManagers;
	private MessageFile messageFile = new MessageFile(this, "messages.yml");
	private ConfigFile configFile = new ConfigFile(this, "config.yml");
	private GroupFile groupFile = new GroupFile(this, "groups.yml");
	private BadWordFile badWordFile = new BadWordFile(this, "badword.yml");
	private CommandFile commandFile = new CommandFile(this, "custom-command.yml");
	private ColorFile colorFile = new ColorFile(this, "color-gui.yml", "Gui");
	private PlayerGuiFile playerGuiFile = new PlayerGuiFile(this, "player-gui.yml", "Gui");
	private ChannelGuiFile channelGuiFile = new ChannelGuiFile(this, "channel-gui.yml", "Gui");
	private GradientColorFile gradientColorFile = new GradientColorFile(this, "gradient-gui.yml", "Gui");
	private ChatLogFile chatLogFile = new ChatLogFile(this, "chat.yml", "Log");
	private CommandLogFile commandLogFile = new CommandLogFile(this, "command.yml", "Log");
	private ChatDataFile chatDataFile = new ChatDataFile(this, "chat-log.data", "Data");
	private InventoryDataFile inventoryDataFile = new InventoryDataFile(this, "inventory.data", "Data");
	private PlayerFile playerFile = new PlayerFile(this);
	public boolean uselegacyversion = false;
	public boolean uselatestversion = false;
	public boolean uselatestConfig = false;
	public static boolean debug = true;
	public String latestversion;
	public String nmsversion;
	public AdvancedChatApi advancedChatApi;
	private AdvancedChatApiProvider apiProvider;

	public void onLoad() {
		instance = this;
		Util.setTitle(version);
		if (!getDataFolder().exists()) {
			getDataFolder().mkdirs();
		}
		this.eventUtils = new EventUtils(this);
		inventoryView = new ArrayList<>();
		preConfigLoad = new PreConfigLoader(this);
		HookManager = new HookManager(this);
		getConfigFile().saveDefaultConfig();
		getConfigFile().create();
		if(!getConfigFile().getConfig().getString("Settings.Config-Version").equals("2")) {
			uselatestConfig = true;
		}
		getMessageFile().saveDefault();
		getMessageFile().createFile();
		createFolder("Data");
		createFolder("Data" + File.separator + "Players");
		createFolder("Gui");
		createFolder("Log");
		preConfigLoad.loadConfig();
		preConfigLoad.loadMessage();
		Util.setEndLoad();
		//Logger.debug("Load Conection ");
		///this.connection = new MySQLConnection();
	}
	
	public void onEnable() {
		this.getMetric();
		Util.setEnabled(version);

		nmsversion = Bukkit.getServer().getClass().getPackage().getName();
		nmsversion = nmsversion.substring(nmsversion.lastIndexOf(".") + 1);
		if (nmsversion.equalsIgnoreCase("v1_8_R3")) {
			uselegacyversion = true;
			if (uselegacyversion) {
				Util.sendColorMessage(EventUtils.getConsoleSender(), Util.getPrefix(true) + "&5<|| &c* &7Use " + nmsversion + " &cdisabled &7method &b1.16 &3- &b1.18");
			}
		} else if (nmsversion.startsWith("v1_16_") || nmsversion.startsWith("v1_17_") || nmsversion.startsWith("v1_18_")) {
			uselatestversion = true;
			Util.sendColorMessage(EventUtils.getConsoleSender(), Util.getPrefix(true) + "&5<|| &c* &7Use " + nmsversion + " &aenabled &7method");
		}
				
		if(uselatestConfig) {
			Logger.warning("&e!Please update your config.yml!");
		}

		createAllFiles();
		preConfigLoad.loadGradientInv();
		preConfigLoad.loadColorInv();
		preConfigLoad.loadPlayerInv();
		HookManager.load();
		HookManager.loadProtocol();
		
		onCommands();
		onListeners();
		
		if (Settings.boolean_protocollib) {
			if (HookManager.isLoadProtocolLib()) {
				HookManager.InitPacketListening();
			} else {
				Logger.warning(Settings.message_depend_plugin+ " " + "&e[&bProtocolLib&e]");
			}
		}		
		
		new UpdateChecker(this, 83889).getUpdateVersionSpigot(version -> {
			latestversion = version;
			if (this.getDescription().getVersion().equalsIgnoreCase(version)) {
				Logger.success("&a" + this.name + " is up to date!");
			} else {
				Logger.outline("&5<||" + Util.getLine("&5"));
				Logger.warning("&5<||" + "&b" + this.name + " is outdated!");
				Logger.warning("&5<||" + "&bNewest version: &a" + version);
				Logger.warning("&5<||" + "&bYour version: &d" + this.version);
				Logger.warning("&5<||" + "&bUpdate Here on Spigot: &e" + UpdateSettings.URL_PLUGIN[0]);
				Logger.warning("&5<||" + "&bUpdate Here on Songoda: &e" + UpdateSettings.URL_PLUGIN[1]);
				Logger.warning("&5<||" + "&bUpdate Here on GitHub: &e" + UpdateSettings.URL_PLUGIN[2]);
				Logger.outline("&5<||" + Util.getLine("&5"));
			}
		});
		
		this.apiProvider = new AdvancedChatApiProvider(this);
		apiProvider.ensureApiWasLoadedByPlugin();
		ApiRegistrationUtil.regsiterProvider(apiProvider);
		
	}
	
	public void onDisable() {
		Util.setDisabled(version);
		metrics = null;
		uselegacyversion = false;
		uselatestConfig = false;
		
		ApiRegistrationUtil.unregsiterProvider();
	}
	
	public void onCommands() {
		new AdvancedChatCmd(this);
		new ClearChatCmd(this);
		new MuteCmd(this);
		new UnMuteCmd(this);
		new MsgCmd();
	}
		
	public void onListeners() {
		registerListeners(
		new JoinListener(),
		new ChatListenerTest(),
		//new ChatListener(),
		new CommandListener(),
		new ChatLogListener(),
		new ColorInventoryListener(),
		new GradientInventoryListener(),
		new PlayerInventoryListener(),
		new SettingsInventoryListener(),
		new RainbowInventoryListener());
		new ErrorInventoryListener();
		EventLoader ev = new EventLoader();
		ev.runClearChat();
	}
	
	public void reloadAllFiles() {
		this.preConfigLoad.loadConfig();
		this.preConfigLoad.loadMessage();
		this.getConfigFile().reloadConfig();
		this.getChatDataFile().reloadConfig();
		this.getChatLogFile().reloadConfig();
		this.getPlayerGuiFile().reloadConfig();
		this.getColorFile().reloadConfig();
		this.getGradientColorFile().reloadConfig();
		this.getChannelGuiFile().reloadConfig();
		this.getCommandLogFile().reloadConfig();
		this.getInventoryDataFile().reloadConfig();
		this.getGroupFile().reloadConfig();
		this.getBadWordFile().reloadConfig();
		this.getMessageFile().reload();
	}
	
	public void createAllFiles() {
		inventoryDataFile.create();
		chatLogFile.create();
		commandLogFile.create();
		groupFile.saveDefault();
		groupFile.create();
		colorFile.create();
		gradientColorFile.create();
		playerGuiFile.create();
		chatDataFile.create();
		channelGuiFile.create();
	}
	
	public void getMetric() {
		metrics = new Metrics(this, 8826);
	}
	
	public void addInventoryView(Player player, String inventoryname) {
		if (this.getInventoryView(player) == null) {
			this.inventoryView.add(new InventoryView(player, inventoryname));
		}
	}

	public void removeInvetoryView(Player player) {
		for (int i = 0; i < inventoryView.size(); i++) {
			if (((InventoryView) this.inventoryView.get(i)).getPlayer().getName().equals(player.getName())) {
				this.inventoryView.remove(i);
			}
		}
	}

	public InventoryView getInventoryView(Player player) {
		for (int i = 0; i < inventoryView.size(); i++) {
			if (((InventoryView) this.inventoryView.get(i)).getPlayer().getName().equals(player.getName())) {
				return (InventoryView) this.inventoryView.get(i);
			}
		}
		return null;
	}
	
	public PlayerFile getPlayerFile() {
		return playerFile;
	}
	

	
	public HookManager getHookManager() {
		return HookManager;
	}

	public boolean isDebug() {
		return true;
	}
	
	public static AdvancedChat get() {
		return instance;
	}
	
	public ArrayList<ChatManager> getChatManagers() {
		return this.chatManagers;
	}

	public void setChatManagers(ChatManager chat) {
		this.chatManagers.add(chat);
	}

	public EventUtils getEventUtils() {
		return this.eventUtils;
	}
	
	public PreConfigLoader getPreConfigLoader() {
		return this.preConfigLoad;
	}
	
	public MessageFile getMessageFile() {
		return messageFile;
	}
	
	public GroupFile getGroupFile() {
		return groupFile;
	}
	
	public BadWordFile getBadWordFile() {
		return badWordFile;
	}
	
	public GradientColorFile getGradientColorFile() {
		return gradientColorFile;
	}
	
	public InventoryDataFile getInventoryDataFile() {
		return this.inventoryDataFile;
	}	

	public ConfigFile getConfigFile() {
		return configFile;
	}

	public ColorFile getColorFile() {
		return colorFile;
	}

	public PlayerGuiFile getPlayerGuiFile() {
		return playerGuiFile;
	}

	public ChatDataFile getChatDataFile() {
		return chatDataFile;
	}

	public ChatLogFile getChatLogFile() {
		return chatLogFile;
	}

	public CommandLogFile getCommandLogFile() {
		return commandLogFile;
	}

	public ChannelGuiFile getChannelGuiFile() {
		return channelGuiFile;
	}
	
	public CommandFile getCommandFile() {
		return commandFile;
	}
	
	public MySQL getMySQL() {
		return mySQL;
	}
	
	public Connection getConnection() {
		return connection.getConnetion();
	}
	
}
