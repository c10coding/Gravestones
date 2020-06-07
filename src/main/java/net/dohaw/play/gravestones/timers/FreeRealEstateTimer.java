package net.dohaw.play.gravestones.timers;

import net.dohaw.play.gravestones.files.GravestoneConfigManager;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.function.Consumer;

public class FreeRealEstateTimer implements Consumer<Task> {

    private GravestoneConfigManager gcm;
    private Location<World> gravestoneLocation;
    private String gravestoneUUID;

    public FreeRealEstateTimer(GravestoneConfigManager gcm, String gravestoneUUID, Location<World> gravestoneLocation){
        this.gcm = gcm;
        this.gravestoneLocation = gravestoneLocation;
        this.gravestoneUUID = gravestoneUUID;
    }

    @Override
    public void accept(Task task) {

        gcm.reloadConfig();
        if(!gcm.isAGravestone(gravestoneLocation)){
            task.cancel();
            return;
        }

        int timeUntilDisap = gcm.getConfig().getNode("Gravestones", gravestoneUUID, "Time Until Disappearance").getInt();
        timeUntilDisap--;
        gcm.getConfig().getNode("Gravestones", gravestoneUUID, "Time Until Disappearance").setValue(timeUntilDisap);
        gcm.saveConfig();

        if(timeUntilDisap == 0){
            gcm.removeGravestone(gravestoneUUID);
            gravestoneLocation.removeBlock();
            task.cancel();
        }
    }
}
