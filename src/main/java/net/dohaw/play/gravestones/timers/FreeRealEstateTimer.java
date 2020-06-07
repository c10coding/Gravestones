package net.dohaw.play.gravestones.timers;

import net.dohaw.play.gravestones.files.GravestoneConfigManager;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.UUID;
import java.util.function.Consumer;

public class FreeRealEstateTimer implements Consumer<Task> {

    private UUID u;
    private GravestoneConfigManager gcm;
    private Location<World> gravestoneLocation;

    public FreeRealEstateTimer(GravestoneConfigManager gcm, UUID u, Location<World> gravestoneLocation){
        this.gcm = gcm;
        this.u = u;
        this.gravestoneLocation = gravestoneLocation;
    }

    @Override
    public void accept(Task task) {

        if(!gcm.hasAGravestone(u)){
            task.cancel();
        }

        int timeUntilDisap = gcm.getConfig().getNode("Gravestones", u.toString(), "Time Until Disappearance").getInt();
        timeUntilDisap--;
        gcm.getConfig().getNode("Gravestones", u.toString(), "Time Until Disappearance").setValue(timeUntilDisap);
        gcm.saveConfig();

        if(timeUntilDisap == 0){
            gcm.removeGravestone(u);
            gravestoneLocation.removeBlock();
            task.cancel();
        }
    }
}
