package jss.advancedchat.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import jss.advancedchat.AdvancedChat;
import jss.advancedchat.utils.file.FileManager;

public class CommandFile extends FileManager{

    private AdvancedChat plugin;
    private File file;
    private FileConfiguration config;
    private String path;
	
    public CommandFile(AdvancedChat plugin, String path) {
        super(plugin);
        this.plugin = plugin;
        this.file = null;
        this.config = null;
        this.path = path;
    }
    
    public void create() {
        this.file = new File(getDataFolder(), this.path);
        if (!this.file.exists()) {
            getConfig().options().copyDefaults(true);
            saveConfig();
        }
    }

    public FileConfiguration getConfig() {
        if (this.config == null) {
            reloadConfig();
        }
        return this.config;
    }

    public void saveConfig() {
        try {
            this.config.save(this.file);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void reloadConfig() {
        if (this.config == null) {
            this.file = new File(getDataFolder(), this.path);
        }
        this.config = YamlConfiguration.loadConfiguration(this.file);
        Reader defaultConfigStream;
        try {
            defaultConfigStream = new InputStreamReader(getResources(this.path), "UTF8");
            if (defaultConfigStream != null) {
                YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(defaultConfigStream);
                config.setDefaults(defaultConfig);
            }
        }catch(Exception e) {
        	e.printStackTrace();
        }

    }

    public String getPath() {
        return this.path;
    }

    public AdvancedChat getPlugin() {
        return plugin;
    }

    public void saveDefaultConfig() {
        if (this.file == null) {
            this.file = new File(getDataFolder(), this.path);
        }
        if (!this.file.exists()) {
            saveResources(this.path, false);
        }
    }

    public void resetConfig() {
        if (this.file == null) {
            this.file = new File(getDataFolder(), this.path);
        }
        if (!this.file.exists()) {
            saveResources(this.path, true);
        }
    }

    public boolean isFileExists() {
    	this.file = new File(getDataFolder(), this.path);
    	return this.file.exists();
    }

}
