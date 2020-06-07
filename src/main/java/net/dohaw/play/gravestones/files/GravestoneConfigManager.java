package net.dohaw.play.gravestones.files;

import net.dohaw.play.gravestones.Utils;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.spongepowered.api.CatalogTypes;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.enchantment.Enchantment;
import org.spongepowered.api.item.enchantment.EnchantmentType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

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

    public void reloadConfig(){
        try {
            this.config = configLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadConfig() {
        try {
            this.config = configLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addGravestoneToConfig(Location<World> loc, UUID deadPlayerUUID, List<ItemStack> items, UUID gravestoneUUID) {

        config.getNode("Gravestones", gravestoneUUID.toString(), "Owner").setValue(deadPlayerUUID.toString());

        World world = loc.getExtent();
        config.getNode("Gravestones", gravestoneUUID.toString(), "Location", "World").setValue(world.getName());
        config.getNode("Gravestones", gravestoneUUID.toString(), "Location", "X").setValue(loc.getBlockX());
        config.getNode("Gravestones", gravestoneUUID.toString(), "Location", "Y").setValue(loc.getBlockY());
        config.getNode("Gravestones", gravestoneUUID.toString(), "Location", "Z").setValue(loc.getBlockZ());

        DefaultConfigManager dcm = new DefaultConfigManager();
        config.getNode("Gravestones", gravestoneUUID.toString(), "Time").setValue(dcm.getGravestoneClaimLimit());
        config.getNode("Gravestones", gravestoneUUID.toString(), "IsFreeRealEstate").setValue(false);

        int itemNum = 1;
        for(ItemStack item : items){

            String itemNumS = String.valueOf(itemNum);
            ItemType itemType = item.getType();
            String id = itemType.getId();
            int amount = item.getQuantity();


            config.getNode("Gravestones", gravestoneUUID.toString(), "Items", itemNumS, "id").setValue(id);
            config.getNode("Gravestones", gravestoneUUID.toString(), "Items", itemNumS, "Amount").setValue(amount);

            Optional<Integer> opDurability = item.get(Keys.ITEM_DURABILITY);
            if(opDurability.isPresent()){
                int durability = opDurability.get();
                config.getNode("Gravestones", gravestoneUUID.toString(), "Items", itemNumS, "Durability").setValue(durability);
            }

            Optional<Text> opDisplayName = item.get(Keys.DISPLAY_NAME);
            if(opDisplayName.isPresent()){
                Text displayName = opDisplayName.get();
                config.getNode("Gravestones", gravestoneUUID.toString(), "Items", itemNumS, "DisplayName").setValue(displayName);
            }

            Optional<List<Text>> opLore = item.get(Keys.ITEM_LORE);
            if(opLore.isPresent()){
                List<Text> itemLore = opLore.get();
                int lineNum = 1;
                for(Text t : itemLore){
                    config.getNode("Gravestones", gravestoneUUID.toString(), "Items", itemNumS, "Lore", String.valueOf(lineNum)).setValue(t.toString());
                    lineNum++;
                }
            }

            Optional<List<Enchantment>> opEnchants = item.get(Keys.ITEM_ENCHANTMENTS);

            if(item.getType().equals(ItemTypes.ENCHANTED_BOOK)){
                opEnchants = item.get(Keys.STORED_ENCHANTMENTS);
            }

            if(opEnchants.isPresent()){
                List<Enchantment> enchantments = opEnchants.get();
                int enchantmentNum = 1;
                for(Enchantment e : enchantments){
                    String line = e.getType().getId() + ";" + e.getLevel();
                    config.getNode("Gravestones", gravestoneUUID.toString(), "Items", itemNumS, "Enchantments", String.valueOf(enchantmentNum)).setValue(line);
                    enchantmentNum++;
                }
            }

            itemNum++;
        }

        saveConfig();
    }

    /*
        Gets all the gravestone uuids that the player may own
     */
    public List<String> getPlayersGravestoneUUIDs(UUID playerUUID){

        List<String> gravestoneUUIDsOwned = new ArrayList<>();
        Map<Object, ? extends CommentedConfigurationNode> section = config.getNode("Gravestones").getChildrenMap();
        String playerStringUUID = playerUUID.toString();

        for(Object key : section.keySet()){
            String ownerUUID = config.getNode("Gravestones", key, "Owner").getString();
            if(playerStringUUID.equalsIgnoreCase(ownerUUID)){
                gravestoneUUIDsOwned.add((String) key);
            }
        }

        return gravestoneUUIDsOwned;
    }

    /*
        Get a specific player's Gravestone UUID at a location
        SIDE NOTE: You need the location because a player can have multiple gravestones. We don't know which specific one they clicked on without the location
     */
    public String getGravestoneUUID(Location<World> locationClicked, UUID playerUUID){
        List<String> gravestoneUUIDsOwned = getPlayersGravestoneUUIDs(playerUUID);

        for(String gravestoneUUID : gravestoneUUIDsOwned){
            World world = Sponge.getServer().getWorld(config.getNode("Gravestones", gravestoneUUID, "Location", "World").getString()).get();
            int x = config.getNode("Gravestones", gravestoneUUID, "Location", "X").getInt();
            int y = config.getNode("Gravestones", gravestoneUUID, "Location", "Y").getInt();
            int z = config.getNode("Gravestones", gravestoneUUID, "Location", "Z").getInt();
            Location<World> gravestoneLocation = new Location(world, x, y, z);
            Location<World> normalizeLocationClicked = Utils.normalize(locationClicked);
            if(gravestoneLocation.equals(normalizeLocationClicked)){
                return gravestoneUUID;
            }
        }
        return null;
    }

    public List<ItemStack> getGravestoneItems(Location<World> locationClicked){

        String gravestoneUUIDClicked = getGravestoneUUIDFromLocation(locationClicked).toString();
        Map<Object, ? extends CommentedConfigurationNode> section = config.getNode("Gravestones", gravestoneUUIDClicked, "Items").getChildrenMap();
        List<ItemStack> items = new ArrayList<>();

        for(Object key : section.keySet()){

            int amount = config.getNode("Gravestones", gravestoneUUIDClicked, "Items", key, "Amount").getInt();
            String id = config.getNode("Gravestones", gravestoneUUIDClicked, "Items", key, "id").getString();

            Optional<ItemType> opItemType;
            ItemType itemType;

            opItemType = Sponge.getGame().getRegistry().getType(ItemType.class, id);

            if(opItemType.isPresent()){

                itemType = Sponge.getRegistry().getType(ItemType.class, id).get();
                ItemStack item = ItemStack.builder().itemType(itemType).quantity(amount).build();

                if(config.getNode("Gravestones", gravestoneUUIDClicked, "Items", key, "Durability").getValue() != null){
                    int durability = config.getNode("Gravestones", gravestoneUUIDClicked, "Items", key, "Durability").getInt();
                    item.offer(Keys.ITEM_DURABILITY, durability);
                }

                if(config.getNode("Gravestones", gravestoneUUIDClicked, "Items", key, "DisplayName").getValue() != null){
                    String displayName = config.getNode("Gravestones", gravestoneUUIDClicked, "Items", key, "DisplayName").getString();
                    item.offer(Keys.DISPLAY_NAME, Text.of(displayName));
                }

                if(config.getNode("Gravestones", gravestoneUUIDClicked, "Items", key, "Lore").getValue() != null){
                    Map<Object, ? extends CommentedConfigurationNode> loreSection = config.getNode("Gravestones", gravestoneUUIDClicked, "Items", key, "Lore").getChildrenMap();
                    List<Text> lore = new ArrayList<>();
                    for(Object numLore : loreSection.keySet()){
                        lore.add(Text.of(config.getNode("Gravestones", gravestoneUUIDClicked, "Items", key, "Lore", numLore).getString()));
                    }
                    item.offer(Keys.ITEM_LORE, lore);
                }

                if(config.getNode("Gravestones", gravestoneUUIDClicked, "Items", key, "Enchantments").getValue() != null){
                    Map<Object, ? extends CommentedConfigurationNode> enchantsSection = config.getNode("Gravestones", gravestoneUUIDClicked, "Items", key, "Enchantments").getChildrenMap();
                    List<Enchantment> enchantments = new ArrayList<>();
                    for(Object numEnchantment : enchantsSection.keySet()){
                        String line = config.getNode("Gravestones", gravestoneUUIDClicked, "Items", key, "Enchantments", numEnchantment).getString();
                        String[] arr = line.split(";");
                        String stringTypeEnchant = arr[0];
                        int level = Integer.parseInt(arr[1]);
                        EnchantmentType enchantmentType = Sponge.getRegistry().getType(CatalogTypes.ENCHANTMENT_TYPE, stringTypeEnchant).get();
                        Enchantment enchant = Enchantment.builder().type(enchantmentType).level(level).build();
                        enchantments.add(enchant);
                    }
                    if(item.getType().equals(ItemTypes.ENCHANTED_BOOK)){
                        item.offer(Keys.STORED_ENCHANTMENTS, enchantments);
                    }else{
                        item.offer(Keys.ITEM_ENCHANTMENTS, enchantments);
                    }
                }

                items.add(item);

            }

        }

        return items;

    }

    public boolean isAGravestone(Location<World> locationClicked){
        return getGravestoneUUIDFromLocation(locationClicked) != null;
    }

    /*
        - Gets a potential gravestone for the player that clicked at the exact location.
        - If the player isn't the owner of the gravestone, getGravestoneUUID will return null.
        - If there isn't a gravestone at the location clicked, it will return null
     */
    public boolean ifIsGravestoneOwner(Location<World> locationClicked, UUID uuid){
        return getGravestoneUUID(locationClicked, uuid) != null;
    }

    /*
        Decreases the "Time" field in the config file a specific player's Gravestone
     */
    public void decreaseTime(String gravestoneUUID){
        int currentMinutesLeft = getCurrentMinutesLeft(gravestoneUUID);
        config.getNode("Gravestones", gravestoneUUID, "Time").setValue(currentMinutesLeft - 1);
        saveConfig();
    }

    public int getCurrentMinutesLeft(String gravestoneUUID){
        return config.getNode("Gravestones", gravestoneUUID, "Time").getInt();
    }

    public void removeGravestone(String gravestoneUUID){
        config.getNode("Gravestones", gravestoneUUID).setValue(null);
        saveConfig();
    }

    public void setToFreeRealEstate(String gravestoneUUID){
        config.getNode("Gravestones", gravestoneUUID, "IsFreeRealEstate").setValue(true);
        saveConfig();
    }

    public String getOwnerUUID(String gravestoneUUID){
        return config.getNode("Gravestones", gravestoneUUID, "Owner").getString();
    }

    public boolean isFreeRealEstate(Location<World> gravestoneLocation){
        UUID gravestoneUUID = getGravestoneUUIDFromLocation(gravestoneLocation);
        return config.getNode("Gravestones", gravestoneUUID, "IsFreeRealEstate").getBoolean();
    }

    public List<String> getGravestoneUUIDs(){
        List<String> gravestoneUUIDs = new ArrayList<>();
        Map<Object, ? extends CommentedConfigurationNode> section = config.getNode("Gravestones").getChildrenMap();
        for(Object uuid : section.keySet()){
            gravestoneUUIDs.add((String) uuid);
        }
        return gravestoneUUIDs;
    }

    public Location<World> getGravestoneLocation(String gravestoneUUID){
        World world = Sponge.getServer().getWorld(config.getNode("Gravestones", gravestoneUUID, "Location", "World").getString()).get();
        int x = config.getNode("Gravestones", gravestoneUUID, "Location", "X").getInt();
        int y = config.getNode("Gravestones", gravestoneUUID, "Location", "Y").getInt();
        int z = config.getNode("Gravestones", gravestoneUUID, "Location", "Z").getInt();
        return new Location(world, x, y, z);
    }

    public UUID getGravestoneUUIDFromLocation(Location<World> gravestoneLocation){
        Map<Object, ? extends CommentedConfigurationNode> section = config.getNode("Gravestones").getChildrenMap();
        for(Object key : section.keySet()){
            World world = Sponge.getServer().getWorld(config.getNode("Gravestones", key, "Location", "World").getString()).get();
            int x = config.getNode("Gravestones", key, "Location", "X").getInt();
            int y = config.getNode("Gravestones", key, "Location", "Y").getInt();
            int z = config.getNode("Gravestones", key, "Location", "Z").getInt();
            Location<World> configGravestoneLoc = new Location(world, x, y, z);
            Location<World> normalizedGravestoneLocation = Utils.normalize(gravestoneLocation);
            if(configGravestoneLoc.equals(normalizedGravestoneLocation)){
                return UUID.fromString((String) key);
            }
        }
        return null;
    }


}








