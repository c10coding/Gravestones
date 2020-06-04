package net.dohaw.play.gravestones.files;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;

import java.io.IOException;
import java.nio.file.Paths;

public class DefaultConfigManager {

    private ConfigurationLoader<CommentedConfigurationNode> configLoader;
    private CommentedConfigurationNode config;

    public DefaultConfigManager(){
        this.configLoader = HoconConfigurationLoader.builder().setPath(Paths.get("config/gravestones/config.conf")).build();
        loadConfig();
    }

    public CommentedConfigurationNode getConfig() {
        return config;
    }

    public void saveConfig() {
        try {
            configLoader.save(config);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadConfig() {
        try {
            config = configLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getGravestoneClaimLimit(){
        return config.getNode("Gravestone Claim Time Limit").getInt();
    }

}
