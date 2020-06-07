package net.dohaw.play.gravestones.timers;

import net.dohaw.play.gravestones.Gravestones;
import net.dohaw.play.gravestones.files.DefaultConfigManager;
import net.dohaw.play.gravestones.files.GravestoneConfigManager;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.UUID;
import java.util.function.Consumer;

public class GravestonesTimer implements Consumer<Task> {

    private UUID u;
    private Location<World> gravestoneLocation;
    private GravestoneConfigManager gcm = new GravestoneConfigManager();
    private Gravestones plugin;

    public GravestonesTimer(Gravestones plugin, UUID u, Location<World> gravestoneLocation){
        this.u = u;
        this.gravestoneLocation = gravestoneLocation;
        this.plugin = plugin;
    }

    @Override
    public void accept(Task task) {

        if(!gcm.hasAGravestone(u)){
            task.cancel();
        }

        int currentMinutesLeft = gcm.getCurrentMinutesLeft(u);
        if(currentMinutesLeft > 0){
            gcm.decreaseTime(u);
        }

        if(currentMinutesLeft == 0) {

            gcm.setToFreeRealEstate(u);
            DefaultConfigManager dcm = new DefaultConfigManager();
            gcm.getConfig().getNode("Gravestones", u.toString(), "Time Until Disappearance").setValue(dcm.getGravestoneTimeAdditive());
            gcm.saveConfig();

            Task.builder().execute(new FreeRealEstateTimer(gcm, u, gravestoneLocation))
                    .delayTicks(1200L)
                    .intervalTicks(1200L)
                    .submit(plugin);
            task.cancel();
        }
    }

}
