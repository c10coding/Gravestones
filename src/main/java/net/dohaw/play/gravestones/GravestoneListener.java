package net.dohaw.play.gravestones;

import net.dohaw.play.gravestones.files.GravestoneConfigManager;
import org.spongepowered.api.Game;
import org.spongepowered.api.GameRegistry;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.Property;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.DisplayNameData;
import org.spongepowered.api.data.manipulator.mutable.PotionEffectData;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.effect.potion.PotionEffectType;
import org.spongepowered.api.effect.potion.PotionEffectTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.Agent;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.action.InteractEvent;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.event.entity.ai.SetAITargetEvent;
import org.spongepowered.api.event.entity.living.humanoid.player.RespawnPlayerEvent;
import org.spongepowered.api.event.item.inventory.ChangeInventoryEvent;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.*;
import org.spongepowered.api.item.inventory.entity.MainPlayerInventory;
import org.spongepowered.api.item.inventory.type.CarriedInventory;
import org.spongepowered.api.item.inventory.type.GridInventory;
import org.spongepowered.api.item.potion.PotionTypes;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public class GravestoneListener {

    private GhostTeamMaker ghost = new GhostTeamMaker();

    public GravestoneListener(){
        ghost.makeScoreboard();
    }

    @Listener
    public void onPlayerDeath(DestructEntityEvent e){
        Entity entityDied = e.getTargetEntity();
        if(entityDied instanceof Player){
            Player deadPlayer = (Player) entityDied;

            Location<World> locationDied = deadPlayer.getLocation();

            locationDied.setBlockType(BlockTypes.COBBLESTONE_WALL);
            locationDied.offer(Keys.DISPLAY_NAME, Text.of("Gravestone: " + deadPlayer.getName()));
            locationDied.offer(Keys.CUSTOM_NAME_VISIBLE, true);

            Inventory inv = deadPlayer.getInventory().query(MainPlayerInventory.class);

            List<ItemStack> playerItems = new ArrayList<>();
            for (Inventory slot : inv.slots()) {
               if(slot.peek().isPresent()){
                    ItemStack item = slot.peek().get();
                    playerItems.add(item);
               }
            }
            GravestoneConfigManager gcm = new GravestoneConfigManager();
            gcm.addGravestoneToConfig(deadPlayer.getLocation(), deadPlayer.getUniqueId(), playerItems);

        }
    }

    @Listener
    public void onPlayRespawn(RespawnPlayerEvent e){
        Player player = e.getTargetEntity();
        List<PotionEffect> effects = new ArrayList<>();
        effects.add(PotionEffect.builder().particles(false).potionType(PotionEffectTypes.INVISIBILITY).amplifier(1).duration(444444444).build());
        player.offer(Keys.POTION_EFFECTS, effects);
        String itemTypeID = "umm3185118519:sulfur_ore";
        Optional<ItemType> opItem = Sponge.getGame().getRegistry().getType(ItemType.class, itemTypeID);
        if(opItem.isPresent()){
            ItemStackSnapshot itemSnapshot = opItem.get().getTemplate();
            ItemStack item = itemSnapshot.createStack();
            player.getInventory().offer(item);
        }
    }

    @Listener
    public void onBlockBreak(ChangeBlockEvent.Break e) {
        /*
            If the player is "Dead" by having the invisibility effect, cancel any sort of block breaking.
            They are a dead player. They shouldn't be allowed to do anything a normal player would do
         */
        Optional<Player> optPlayer = e.getCause().first(Player.class);
        if(optPlayer.isPresent()){
            Player p = optPlayer.get();
            if(isADeadPlayer(p)){
                e.setCancelled(true);
            }
        }
    }

    @Listener
    public void onBlockPlace(ChangeBlockEvent.Place e){
        Optional<Player> optPlayer = e.getCause().first(Player.class);
        if(optPlayer.isPresent()){
            Player p = optPlayer.get();
            if(isADeadPlayer(p)){
                e.setCancelled(true);
            }
        }
    }

    @Listener
    public void onPlayerInteract(InteractBlockEvent e){
        Optional<Player> optPlayer = e.getCause().first(Player.class);
        if(optPlayer.isPresent()){
            Player p = optPlayer.get();
            if(isADeadPlayer(p)){
                e.setCancelled(true);
            }
        }
    }

    @Listener
    public void onPlayerPickup(ChangeInventoryEvent.Pickup e){
        Optional<Player> optPlayer = e.getCause().first(Player.class);
        if(optPlayer.isPresent()){
            Player p = optPlayer.get();
            if(isADeadPlayer(p)){
                e.setCancelled(true);
            }
        }
    }

    @Listener
    public void onTakeDamage(DamageEntityEvent e){
        Optional<Player> optPlayer = e.getCause().first(Player.class);
        if(optPlayer.isPresent()){
            Player p = optPlayer.get();
            if(isADeadPlayer(p)){
                e.setCancelled(true);
            }
        }
    }

    @Listener
    public void onMobTargetDeadPlayer(SetAITargetEvent e){
        Entity target = e.getTarget().get();
        if(target instanceof Player){
            Player p = (Player) target;
            if(isADeadPlayer(p)){
                e.setCancelled(true);
            }
        }
    }

    @Listener
    public void onPlayerJoin(ClientConnectionEvent.Join e){
        Player playerJoined = e.getTargetEntity();
        ghost.addPlayerToScoreboard(playerJoined);
    }

    private boolean isADeadPlayer(Player p){
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

}
