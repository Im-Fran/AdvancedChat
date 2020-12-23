package jss.advancedchat;

import java.util.ArrayList;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import jss.advancedchat.commands.AdvancedChatCmd;
import jss.advancedchat.commands.ClearChatCmd;
//import jss.advancedchat.events.ChatListener;
//import jss.advancedchat.events.EventLoader;
import jss.advancedchat.events.JoinListener;
import jss.advancedchat.utils.FileManager;
import jss.advancedchat.utils.Logger;
import jss.advancedchat.utils.Utils;


public class AdvancedChat extends JavaPlugin{
	
	PluginDescriptionFile jss = getDescription();
	public String name = this.jss.getName();
	public String version = this.jss.getVersion();
	public Metrics metrics;
	public String latestversion;
	public boolean placeholder = false;
	private CommandSender c= Bukkit.getConsoleSender();
	private boolean debug = false;
	private FileManager filemanager = new FileManager(this);
	private PlayerDataFile playerdata = new  PlayerDataFile(this, "players.data", "Data");
	private ConfigFile configfile = new ConfigFile(this, "config.yml");
	private ColorFile colorFile = new ColorFile(this, "color-gui.yml", "Gui");
	private PlayerGuiFile playerGuiFile = new PlayerGuiFile(this, "player-gui.yml", "Gui");
	public String nmsversion;
	public boolean uselegacyversion = false;
	@SuppressWarnings("unused")
	private Logger logger = new Logger(this);
	public ArrayList<String> mute = new ArrayList<String>();
	private static AdvancedChat plugin;
	
	public void onEnable() {
		Utils.setEnabled(version);;
        nmsversion = Bukkit.getServer().getClass().getPackage().getName();
        nmsversion = nmsversion.substring(nmsversion.lastIndexOf(".") + 1);
        if (nmsversion.equalsIgnoreCase("v1_8_R3")) { 
        	uselegacyversion = true;
        	if(uselegacyversion == true) {
        		Utils.sendColorMessage(c, Utils.getPrefix() + " " + "&7Use " + nmsversion + " &cdisabled &7method &b1.16");
        	}
        }else if(nmsversion.equalsIgnoreCase("v1_16_R2")){
        	Utils.sendColorMessage(c, Utils.getPrefix() + " " + "&7Use " + nmsversion + " &aenabled &7method &b1.16");
        }
        plugin = this;
		configfile.saveDefaultConfig();
        configfile.create();
		filemanager.createVoidFolder("Data");
		playerdata.saveDefaultConfig();
		playerdata.create();
		filemanager.createVoidFolder("Gui");
		colorFile.saveDefaultConfig();
		colorFile.create();
		playerGuiFile.saveDefaultConfig();
		playerGuiFile.create();
        metrics = new Metrics(this);
		setupCommands();
		setupEvents();
		SetupSoftDepends();
		/*new UpdateChecker(this, 83889).getUpdateVersion(version ->{
			if(this.getDescription().getVersion().equalsIgnoreCase(version)) {
				logger.Log(Level.SUCCESS, "&a" + this.name + " is up to date!");
			}else {
				logger.Log(Level.OUTLINE, "&5<|" + Utils.getLine("&5"));
				logger.Log(Level.WARNING, "&5<|" + "&b"+ this.name + " is outdated!");
				logger.Log(Level.WARNING, "&5<|" + "&bNewest version: &a" + version);
				logger.Log(Level.WARNING, "&5<|" + "&bYour version: &d" + Settings.VERSION);
                logger.Log(Level.WARNING, "&5<|" + "&bPlease Update Here: &e" + Settings.URL_PLUGIN);
                logger.Log(Level.OUTLINE, "&5<|" + Utils.getLine("&5"));
			}
		});*/
	}
	
	public void onDisable() {
		Utils.setDisabled(version);
		placeholder = false;
		metrics = null;
		uselegacyversion = false;
	}
	
	public void setupCommands() {
		new AdvancedChatCmd(this);
		new ClearChatCmd(this);
	}
	
	public void setupEvents() {
		new JoinListener(this);
	}
	
	public PlayerDataFile getPlayerDataFile() {
		return this.playerdata;
	}
	
	public ConfigFile getConfigfile() {
		return configfile;
	}

	public ColorFile getColorFile() {
		return colorFile;
	}

	public PlayerGuiFile getPlayerGuiFile() {
		return playerGuiFile;
	}

	public boolean getPlaceHolderState() {
		return this.placeholder;
	}

	public boolean setupPlaceHolderAPI(){
		if (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
			placeholder = true;
		}
		return placeholder;
	}
	
	public void SetupSoftDepends() {
		if(setupPlaceHolderAPI()) {
			Utils.sendColorMessage(c, "&e[&d"+ name +"&e] &5 <|============================================|>");
			Utils.sendColorMessage(c, Utils.getPrefix() + " &5<| &ePlaceHolderAPI:&b" + " " + placeholder);
			Utils.sendColorMessage(c, Utils.getPrefix() + " &5<| &eVars PlaceHolderAPI:&a true");
			Utils.sendColorMessage(c, "&e[&d"+ name +"&e] &5 <|============================================|>");
		}else {
			Utils.sendColorMessage(c, "&e[&d"+ name +"&e] &5 <|============================================|>");
			Utils.sendColorMessage(c, Utils.getPrefix() + " &5<| &ePlaceHolderAPI:&b" + " " + placeholder);
			Utils.sendColorMessage(c, Utils.getPrefix() + " &5<| &eVars PlaceHolderAPI:&c false");
			Utils.sendColorMessage(c, "&e[&d"+ name +"&e] &5 <|============================================|>");
		}		
	}
	
	public boolean isDebug() {
		return debug;
	}

	public static AdvancedChat getPlugin() {
		return plugin;
	}
	
	//Test Section
	

}