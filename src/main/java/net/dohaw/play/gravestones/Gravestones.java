package net.dohaw.play.gravestones;

import com.google.inject.Inject;
import net.dohaw.play.gravestones.files.GravestoneConfigManager;
import net.dohaw.play.gravestones.timers.GravestonesTimer;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.asset.Asset;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
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
        startGravestoneTimers();
    }

    private void registerListeners(){
        logger.info("Registering listeners..");
        Sponge.getGame().getEventManager().registerListeners(this, new GravestoneListener(this));
    }

    /*
        Method to load all assets needed for this plugin
     */
    private void loadAssets(){

        logger.info("Loading assets..");
        String[] assetNames = {"config.conf", "gravestones.conf"};
        for(String assetName : assetNames){
            Path path = Paths.get("config/gravestones/" + assetName);
            Optional<Asset> optAsset = container.getAsset(assetName);
            if(optAsset.isPresent()){
                try {
                    optAsset.get().copyToFile(path, false, true);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else{
                logger.warn("Could not find asset " + assetName);
            }
        }
    }

    private void startGravestoneTimers(){
        GravestoneConfigManager gcm = new GravestoneConfigManager();
        List<String> gravestoneUUIDs = gcm.getGravestoneUUIDs();

        logger.info("Starting timers..");
        for(String uuid : gravestoneUUIDs){
            Location<World> gravestoneLocation = gcm.getGravestoneLocation(uuid);
            String ownerUUID = gcm.getOwnerUUID(uuid);
            Task.builder().execute(new GravestonesTimer(this, gravestoneLocation, uuid))
                    .intervalTicks(1200L)
                    .delayTicks(1200L)
                    .name("Gravestone Claim Timer for owner UUID: " + ownerUUID)
                    .submit(this);
        }

    }

}
