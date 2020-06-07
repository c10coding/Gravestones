package net.dohaw.play.gravestones.runnables;

import net.dohaw.play.gravestones.Utils;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.title.Title;

import java.util.function.Consumer;

public class DeathTitle implements Consumer<Task> {

    private Player deadPlayer;

    public DeathTitle(Player deadPlayer){
        this.deadPlayer = deadPlayer;
    }

    @Override
    public void accept(Task task) {
        Title deathTitle = Title.builder().actionBar(Text.of("You are dead...")).stay(1).build();
        deadPlayer.sendTitle(deathTitle);
        if(!Utils.isADeadPlayer(deadPlayer)){
            deadPlayer.resetTitle();
            task.cancel();
        }

    }
}
