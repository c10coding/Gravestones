package net.dohaw.play.gravestones;

import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.effect.potion.PotionEffectType;
import org.spongepowered.api.effect.potion.PotionEffectTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.List;

public class Utils {

    public static boolean isADeadPlayer(Player p){
        if(p.get(Keys.POTION_EFFECTS).isPresent()){
            List<PotionEffect> effects = p.get(Keys.POTION_EFFECTS).get();
            for(PotionEffect pe : effects){
                PotionEffectType type = pe.getType();
                if(type.equals(PotionEffectTypes.INVISIBILITY)){
                    return true;
                }
            }
        }
        return false;
    }

    public static Location<World> normalize(Location<World> location){
        return new Location(location.getExtent(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

}
