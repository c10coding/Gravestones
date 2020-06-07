package net.dohaw.play.gravestones.timers;

import net.dohaw.play.gravestones.Gravestones;
import net.dohaw.play.gravestones.files.DefaultConfigManager;
import net.dohaw.play.gravestones.files.GravestoneConfigManager;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.function.Consumer;

public class GravestonesTimer implements Consumer<Task> {

    private Location<World> gravestoneLocation;
    private GravestoneConfigManager gcm = new GravestoneConfigManager();
    private Gravestones plugin;
    private String gravestoneUUID;

    public GravestonesTimer(Gravestones plugin, Location<World> gravestoneLocation, String gravestoneUUID){
        this.gravestoneLocation = gravestoneLocation;
        this.plugin = plugin;
        this.gravestoneUUID = gravestoneUUID;
    }

    @Override
    public void accept(Task task) {

        gcm.reloadConfig();
        if(!gcm.isAGravestone(gravestoneLocation)){
            task.cancel();
            return;
        }

        int currentMinutesLeft = gcm.getCurrentMinutesLeft(gravestoneUUID);
        currentMinutesLeft--;
        if(currentMinutesLeft > 0){
            gcm.decreaseTime(gravestoneUUID);
        }

        if(currentMinutesLeft == 0) {

            gcm.getConfig().getNode("Gravestones", gravestoneUUID, "Time").setValue(0);
            gcm.setToFreeRealEstate(gravestoneUUID);
            DefaultConfigManager dcm = new DefaultConfigManager();
            gcm.getConfig().getNode("Gravestones", gravestoneUUID, "Time Until Disappearance").setValue(dcm.getGravestoneTimeAdditive());
            gcm.saveConfig();

            Task.builder().execute(new FreeRealEstateTimer(gcm, gravestoneUUID, gravestoneLocation))
                    .delayTicks(1200L)
                    .intervalTicks(1200L)
                    .submit(plugin);
            task.cancel();
        }
    }

}
