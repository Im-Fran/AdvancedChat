package jss.advancedchat;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

//import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import jss.advancedchat.utils.FileManager;
import jss.advancedchat.utils.interfaces.FileHelper;
import jss.advancedchat.utils.interfaces.FolderHelper;

public class PlayerDataFile extends FileManager implements FileHelper, FolderHelper {
	
	private AdvancedChat plugin;
	private File file;
	private FileConfiguration config;
	private String path;
	private String folderpath;

	public PlayerDataFile(AdvancedChat plugin, String path, String folderpath) {
		super(plugin);
		this.plugin = plugin;
		this.path = path;
		this.folderpath = folderpath;
		this.config = null;
		this.file = null;
	}

	public String getFolderPath() {
		return this.folderpath;
	}

	public void create() {
		this.file = new File(getDataFolder() + File.separator + this.folderpath, this.path);
		if(!this.file.exists()) {
			getConfig().options().copyDefaults(true);
			saveConfig();
		}
	}

	public FileConfiguration getConfig() {
		if(this.config == null) {
			reloadConfig();
		}
		return this.config;
	}

	public void reloadConfig() {
		if(this.config == null) {
			this.file = new File(getDataFolder() + File.separator + this.folderpath, this.path);
		}
		this.config = YamlConfiguration.loadConfiguration(this.file);
		Reader defaultConfigStream;
		try {
			defaultConfigStream = new InputStreamReader(getResources(this.path), "UTF8");
			if(defaultConfigStream != null) {
				YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(defaultConfigStream);
				config.setDefaults(defaultConfig);
			}
		}catch(UnsupportedEncodingException ex) {
			ex.printStackTrace();
		}catch(NullPointerException e) {
			e.printStackTrace();
		}
	}

	public void saveConfig() {
		try {
			this.config.save(this.file);
		}catch(IOException ex) {
			ex.printStackTrace();
		}	
	}

	public void saveDefaultConfig() {
		if(this.file == null) {
			this.file = new File(getDataFolder() + File.separator + this.folderpath, this.path);
		}
		if(!this.file.exists()) {
			saveResources(this.folderpath + File.separator + this.path, false);
		}
	}
	
	public void resetConfig() {
		if(this.file == null) {
			this.file = new File(getDataFolder() + File.separator + this.folderpath, this.path);
		}
		if(!this.file.exists()) {
			saveResources(this.folderpath + File.separator + this.path, true);
		}
	}
	
	public String getPath() {
		return this.path;
	}
	

	public AdvancedChat getPlugin() {
		return plugin;
	}
}