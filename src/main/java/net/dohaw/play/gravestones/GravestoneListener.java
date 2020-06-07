package net.dohaw.play.gravestones;

import net.dohaw.play.gravestones.files.GravestoneConfigManager;
import net.dohaw.play.gravestones.runnables.DeathTitle;
import net.dohaw.play.gravestones.timers.GravestonesTimer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.effect.potion.PotionEffectTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.Villager;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.event.entity.InteractEntityEvent;
import org.spongepowered.api.event.entity.ai.SetAITargetEvent;
import org.spongepowered.api.event.entity.living.humanoid.player.RespawnPlayerEvent;
import org.spongepowered.api.event.item.inventory.ChangeInventoryEvent;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.entity.MainPlayerInventory;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.title.Title;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class GravestoneListener {

    private GhostTeamMaker ghost = new GhostTeamMaker();
    private Gravestones plugin;

    public GravestoneListener(Gravestones plugin){
        ghost.makeScoreboard();
        this.plugin = plugin;
    }

    @Listener
    public void onPlayerDeath(DestructEntityEvent e){
        Entity entityDied = e.getTargetEntity();
        if(entityDied instanceof Player){
            Player deadPlayer = (Player) entityDied;
            Location<World> locationDied = deadPlayer.getLocation();

            locationDied.setBlockType(BlockTypes.COBBLESTONE_WALL);
            Inventory inv = deadPlayer.getInventory().query(MainPlayerInventory.class);

            List<ItemStack> playerItems = new ArrayList<>();
            for (Inventory slot : inv.slots()) {
               if(slot.peek().isPresent()){
                    playerItems.add(slot.peek().get());
               }
            }
            GravestoneConfigManager gcm = new GravestoneConfigManager();

            /*
                Giving the Gravestones a UUID to differentiate between multiple gravestones of a player
             */
            UUID gravestoneUUID = UUID.randomUUID();
            gcm.addGravestoneToConfig(deadPlayer.getLocation(), deadPlayer.getUniqueId(), playerItems, gravestoneUUID);
            Task.builder().execute(new GravestonesTimer(plugin, deadPlayer.getUniqueId(), locationDied, gravestoneUUID))
                    .intervalTicks(1200L)
                    .delayTicks(1200L)
                    .name("Gravestone Claim Timer for owner UUID: " + deadPlayer.getUniqueId().toString())
                    .submit(plugin);


        }
    }

    @Listener
    public void onDeathDroppedItems(DropItemEvent.Destruct e){
        e.setCancelled(true);
    }

    @Listener
    public void onPlayRespawn(RespawnPlayerEvent e){
        Player player = e.getTargetEntity();
        List<PotionEffect> effects = new ArrayList<>();
        effects.add(PotionEffect.builder().particles(false).potionType(PotionEffectTypes.INVISIBILITY).amplifier(1).duration(Integer.MAX_VALUE).build());
        player.offer(Keys.POTION_EFFECTS, effects);
        player.sendMessage(Text.of("You are dead! To be revived, either find a healer or go find your death totem!"));
        giveDeathTitle(player);
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
            if(Utils.isADeadPlayer(p)){
                e.setCancelled(true);
            }
        }
    }

    @Listener
    public void onBlockPlace(ChangeBlockEvent.Place e){
        Optional<Player> optPlayer = e.getCause().first(Player.class);
        if(optPlayer.isPresent()){
            Player p = optPlayer.get();
            if(Utils.isADeadPlayer(p)){
                e.setCancelled(true);
            }
        }
    }

    /*
        Prevents all interactions while the player is dead unless they're right clicking on their gravestone
     */
    @Listener
    public void onPlayerInteract(InteractBlockEvent.Secondary e){
        Optional<Player> optPlayer = e.getCause().first(Player.class);
        if(optPlayer.isPresent()){
            Player p = optPlayer.get();
            GravestoneConfigManager gcm = new GravestoneConfigManager();
            if(!e.getTargetBlock().getState().getType().equals(BlockTypes.COBBLESTONE_WALL)){
                e.setCancelled(true);
            }else{
                if(gcm.hasAGravestone(p.getUniqueId())){

                    Location<World> gravestoneLocation = e.getTargetBlock().getLocation().get();
                    if(gcm.ifIsGravestoneOwner(gravestoneLocation, p.getUniqueId())){
                        List<ItemStack> playerItems = gcm.getItems(p.getUniqueId());

                        for(ItemStack item : playerItems){
                            p.getInventory().offer(item);
                        }

                        if(Utils.isADeadPlayer(p)){
                            p.offer(Keys.POTION_EFFECTS, new ArrayList<>());
                        }

                        gravestoneLocation.setBlockType(BlockTypes.AIR);
                        gcm.removeGravestone(p.getUniqueId());

                    }else if(gcm.isFreeRealEstate(gravestoneLocation)){
                        List<ItemStack> playerItems = gcm.getItems(gcm.getGravestoneUUIDFromLocation(gravestoneLocation));
                        for(ItemStack item : playerItems){
                            p.getInventory().offer(item);
                        }
                        gravestoneLocation.setBlockType(BlockTypes.AIR);
                    }
                }
            }
        }
    }

    @Listener
    public void onRightClickHealer(InteractEntityEvent e){
        Optional<Player> optPlayer = e.getCause().first(Player.class);
        if(optPlayer.isPresent()){
            Player player = optPlayer.get();
            if(e.getTargetEntity() instanceof Villager){
                Villager potentialHealer = (Villager) e.getTargetEntity();
                Optional<Boolean> optIsAIEnabled = potentialHealer.get(Keys.AI_ENABLED);
                boolean isAIEnabled = optIsAIEnabled.get();

                if(optIsAIEnabled.isPresent()) {
                    if(isAIEnabled) {
                        if (Utils.isADeadPlayer(player)) {
                            player.offer(Keys.POTION_EFFECTS, new ArrayList<>());
                            player.sendMessage(Text.of("You have been revived!"));
                        }
                        e.setCancelled(true);
                    }
                }
            }
        }
    }

    @Listener
    public void onPlayerPickup(ChangeInventoryEvent.Pickup.Pre e){
        Optional<Player> optPlayer = e.getCause().first(Player.class);
        if(optPlayer.isPresent()){
            Player p = optPlayer.get();
            if(Utils.isADeadPlayer(p)){
                e.setCancelled(true);
            }
        }
    }

    @Listener
    public void onTakeDamage(DamageEntityEvent e){
        Optional<Player> optPlayer = e.getCause().first(Player.class);
        if(optPlayer.isPresent()){
            Player p = optPlayer.get();
            if(Utils.isADeadPlayer(p)){
                e.setCancelled(true);
            }
        }
    }

    @Listener
    public void onMobTargetDeadPlayer(SetAITargetEvent e){
        Entity target = e.getTarget().get();
        if(target instanceof Player){
            Player p = (Player) target;
            if(Utils.isADeadPlayer(p)){
                e.setCancelled(true);
            }
        }
    }

    @Listener
    public void onPlayerJoin(ClientConnectionEvent.Join e){
        Player playerJoined = e.getTargetEntity();
        ghost.addPlayerToScoreboard(playerJoined);
    }

    private void giveDeathTitle(Player deadPlayer){
        Task.builder().execute(new DeathTitle(deadPlayer)).intervalTicks(5L).delayTicks(0).submit(plugin);
    }

}
