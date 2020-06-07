package net.dohaw.play.gravestones;

import com.google.inject.Inject;
import net.dohaw.play.gravestones.files.GravestoneConfigManager;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.asset.Asset;
import org.spongepowered.api.config.ConfigManager;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@Plugin(
        id = "gravestones",
        name = "Gravestones",
        version = "1.0-SNAPSHOT"
)
public class Gravestones {

    @Inject
    private Logger logger;

    PluginContainer container = Sponge.getPluginManager().getPlugin("gravestones").get();

    @Listener
    public void onServerStart(GameStartedServerEvent event) {

        File gravestonesFolder = new File("config/gravestones");
        if(!gravestonesFolder.exists()){
            gravestonesFolder.mkdirs();
        }

        loadAssets();
        registerListeners();
    }

    private void registerListeners(){
        Sponge.getGame().getEventManager().registerListeners(this, new GravestoneListener(this));
    }

    /*
        Method to load all assets needed for this plugin
     */
    private void loadAssets(){
        String[] assetNames = {"config.conf", "gravestones.conf"};
        for(String assetName : assetNames){
            Path path = Paths.get("config/gravestones/" + assetName);
            Optional<Asset> optAsset = container.getAsset(assetName);
            if(optAsset.isPresent()){
                try {
                    optAsset.get().copyToFile(path, true, false);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else{
                logger.warn("Could not find asset " + assetName);
            }
        }
    }

}
