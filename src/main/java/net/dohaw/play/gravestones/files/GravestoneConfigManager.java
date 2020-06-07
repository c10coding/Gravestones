package net.dohaw.play.gravestones.files;

import net.dohaw.play.gravestones.Gravestones;
import net.dohaw.play.gravestones.timers.GravestonesTimer;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.spongepowered.api.CatalogTypes;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.enchantment.Enchantment;
import org.spongepowered.api.item.enchantment.EnchantmentType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;

public class GravestoneConfigManager {

    private ConfigurationLoader<CommentedConfigurationNode> configLoader;
    private CommentedConfigurationNode config;

    public GravestoneConfigManager() {
        setLoader();
        loadConfig();
    }

    public CommentedConfigurationNode getConfig() {
        return config;
    }

    public void saveConfig() {
        try {
            configLoader.save(config);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setLoader(){
        this.configLoader = HoconConfigurationLoader.builder().setPath(Paths.get("config/gravestones/gravestones.conf")).build();
    }

    public void loadConfig() {
        try {
            this.config = configLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addGravestoneToConfig(Location<World> loc, UUID deadPlayerUUID, List<ItemStack> items) {

        World world = (World) loc.getExtent();
        config.getNode("Gravestones", deadPlayerUUID.toString(), "Location", "World").setValue(world.getName());
        config.getNode("Gravestones", deadPlayerUUID.toString(), "Location", "X").setValue(loc.getBlockX());
        config.getNode("Gravestones", deadPlayerUUID.toString(), "Location", "Y").setValue(loc.getBlockY());
        config.getNode("Gravestones", deadPlayerUUID.toString(), "Location", "Z").setValue(loc.getBlockZ());

        DefaultConfigManager dcm = new DefaultConfigManager();
        config.getNode("Gravestones", deadPlayerUUID.toString(), "Time").setValue(dcm.getGravestoneClaimLimit());
        config.getNode("Gravestones", deadPlayerUUID.toString(), "IsFreeRealEstate").setValue(false);

        int itemNum = 1;
        for(ItemStack item : items){

            String itemNumS = String.valueOf(itemNum);
            ItemType itemType = item.getType();
            String id = itemType.getId();
            int amount = item.getQuantity();

            config.getNode("Gravestones", deadPlayerUUID.toString(), "Items", itemNumS, "id").setValue(id);
            config.getNode("Gravestones", deadPlayerUUID.toString(), "Items", itemNumS, "Amount").setValue(amount);

            Optional<Integer> opDurability = item.get(Keys.ITEM_DURABILITY);
            if(opDurability.isPresent()){
                int durability = opDurability.get();
                config.getNode("Gravestones", deadPlayerUUID.toString(), "Items", itemNumS, "Durability").setValue(durability);
            }

            Optional<Text> opDisplayName = item.get(Keys.DISPLAY_NAME);
            if(opDisplayName.isPresent()){
                Text displayName = opDisplayName.get();
                config.getNode("Gravestones", deadPlayerUUID.toString(), "Items", itemNumS, "DisplayName").setValue(displayName);
            }

            Optional<List<Text>> opLore = item.get(Keys.ITEM_LORE);
            if(opLore.isPresent()){
                List<Text> itemLore = opLore.get();
                int lineNum = 1;
                for(Text t : itemLore){
                    config.getNode("Gravestones", deadPlayerUUID.toString(), "Items", itemNumS, "Lore", String.valueOf(lineNum)).setValue(t.toString());
                    lineNum++;
                }
            }

            Optional<List<Enchantment>> opEnchants = item.get(Keys.ITEM_ENCHANTMENTS);
            if(opEnchants.isPresent()){
                List<Enchantment> enchantments = opEnchants.get();
                int enchantmentNum = 1;
                for(Enchantment e : enchantments){
                    String line = e.getType().getId() + ";" + e.getLevel();
                    config.getNode("Gravestones", deadPlayerUUID.toString(), "Items", itemNumS, "Enchantments", String.valueOf(enchantmentNum)).setValue(line);
                    enchantmentNum++;
                }
            }
            itemNum++;
        }

        saveConfig();
    }

    public List<ItemStack> getItems(UUID u){
        Map<Object, ? extends CommentedConfigurationNode> section = config.getNode("Gravestones", u.toString(), "Items").getChildrenMap();
        List<ItemStack> items = new ArrayList<>();
        for(Object key : section.keySet()){

            int amount = config.getNode("Gravestones", u.toString(), "Items", key, "Amount").getInt();
            String id = config.getNode("Gravestones", u.toString(), "Items", key, "id").getString();

            Optional<ItemType> opItemType;
            ItemType itemType;

            opItemType = Sponge.getGame().getRegistry().getType(ItemType.class, id);

            if(opItemType.isPresent()){

                itemType = Sponge.getRegistry().getType(ItemType.class, id).get();
                ItemStack item = ItemStack.builder().itemType(itemType).quantity(amount).build();

                if(config.getNode("Gravestones", u.toString(), "Items", key, "Durability").getValue() != null){
                    int durability = config.getNode("Gravestones", u.toString(), "Items", key, "Durability").getInt();
                    item.offer(Keys.ITEM_DURABILITY, durability);
                }

                if(config.getNode("Gravestones", u.toString(), "Items", key, "DisplayName").getValue() != null){
                    String displayName = config.getNode("Gravestones", u.toString(), "Items", key, "DisplayName").getString();
                    item.offer(Keys.DISPLAY_NAME, Text.of(displayName));
                }

                if(config.getNode("Gravestones", u.toString(), "Items", key, "Lore").getValue() != null){
                    Map<Object, ? extends CommentedConfigurationNode> loreSection = config.getNode("Gravestones", u.toString(), "Items", key, "Lore").getChildrenMap();
                    List<Text> lore = new ArrayList<>();
                    for(Object numLore : loreSection.keySet()){
                        lore.add(Text.of(config.getNode("Gravestones", u.toString(), "Items", key, "Lore", numLore).getString()));
                    }
                    item.offer(Keys.ITEM_LORE, lore);
                }

                if(config.getNode("Gravestones", u.toString(), "Items", key, "Enchantments").getValue() != null){
                    Map<Object, ? extends CommentedConfigurationNode> enchantsSection = config.getNode("Gravestones", u.toString(), "Items", key, "Enchantments").getChildrenMap();
                    List<Enchantment> enchantments = new ArrayList<>();
                    for(Object numEnchantment : enchantsSection.keySet()){
                        String line = config.getNode("Gravestones", u.toString(), "Items", key, "Enchantments", numEnchantment).getString();
                        String[] arr = line.split(";");
                        String stringTypeEnchant = arr[0];
                        int level = Integer.parseInt(arr[1]);
                        EnchantmentType enchantmentType = Sponge.getRegistry().getType(CatalogTypes.ENCHANTMENT_TYPE, stringTypeEnchant).get();
                        Enchantment enchant = Enchantment.builder().type(enchantmentType).level(level).build();
                        enchantments.add(enchant);
                    }
                    item.offer(Keys.ITEM_ENCHANTMENTS, enchantments);
                }

                items.add(item);

            }

        }

        return items;

    }

    public boolean hasAGravestone(UUID u){
        return config.getNode("Gravestones", u.toString()).getValue() != null;
    }

    public boolean ifIsGravestoneOwner(Location<World> loc, UUID uuid){
        String stringWorld = config.getNode("Gravestones", uuid.toString(), "Location", "World").getString();
        World world = Sponge.getServer().getWorld(stringWorld).get();
        int x = config.getNode("Gravestones", uuid.toString(), "Location", "X").getInt();
        int y = config.getNode("Gravestones", uuid.toString(), "Location", "Y").getInt();
        int z = config.getNode("Gravestones", uuid.toString(), "Location", "Z").getInt();
        Location<World> ownersGravestone = new Location(world, x, y, z);
        if(ownersGravestone.equals(loc)){
            return true;
        }
        return false;
    }

    /*
        Decreases the "Time" field in the config file a specific player's Gravestone
     */
    public void decreaseTime(UUID u){
        int currentMinutesLeft = getCurrentMinutesLeft(u);
        config.getNode("Gravestones", u.toString(), "Time").setValue(currentMinutesLeft - 1);
        saveConfig();
    }

    public int getCurrentMinutesLeft(UUID u){
        return config.getNode("Gravestones", u.toString(), "Time").getInt();
    }

    public void removeGravestone(UUID u){
        config.getNode("Gravestones", u.toString()).setValue(null);
        saveConfig();
    }

    public void setToFreeRealEstate(UUID u){
        config.getNode("Gravestones", u.toString(), "IsFreeRealEstate").setValue(true);
        saveConfig();
    }

    public boolean isFreeRealEstate(Location<World> gravestoneLocation){
        Map<Object, ? extends CommentedConfigurationNode> section = config.getNode("Gravestones").getChildrenMap();
        for(Object key : section.keySet()){
            World world = Sponge.getServer().getWorld(config.getNode("Gravestones", key, "Location", "World").getString()).get();
            int x = config.getNode("Gravestones", key, "Location", "X").getInt();
            int y = config.getNode("Gravestones", key, "Location", "Y").getInt();
            int z = config.getNode("Gravestones", key, "Location", "Z").getInt();
            Location<World> configGravestoneLoc = new Location(world, x, y, z);
            if(configGravestoneLoc.equals(gravestoneLocation)){
                return config.getNode("Gravestones", key, "IsFreeRealEstate").getBoolean();
            }
        }
        return false;
    }

    public UUID getGravestoneUUIDFromLocation(Location<World> gravestoneLocation){
        Map<Object, ? extends CommentedConfigurationNode> section = config.getNode("Gravestones").getChildrenMap();
        for(Object key : section.keySet()){
            World world = Sponge.getServer().getWorld(config.getNode("Gravestones", key, "Location", "World").getString()).get();
            int x = config.getNode("Gravestones", key, "Location", "X").getInt();
            int y = config.getNode("Gravestones", key, "Location", "Y").getInt();
            int z = config.getNode("Gravestones", key, "Location", "Z").getInt();
            Location<World> configGravestoneLoc = new Location(world, x, y, z);
            if(configGravestoneLoc.equals(gravestoneLocation)){
                return UUID.fromString((String) key);
            }
        }
        return null;
    }


}








