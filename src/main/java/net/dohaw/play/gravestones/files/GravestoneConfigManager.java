package net.dohaw.play.gravestones.files;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.enchantment.Enchantment;
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

    public void loadConfig() {
        try {
            this.config = configLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addGravestoneToConfig(Location loc, UUID deadPlayerUUID, List<ItemStack> items) {

        World world = (World) loc.getExtent();
        config.getNode("Gravestones", deadPlayerUUID.toString(), "Location", "World").setValue(world.getName());
        config.getNode("Gravestones", deadPlayerUUID.toString(), "Location", "X").setValue(loc.getBlockX());
        config.getNode("Gravestones", deadPlayerUUID.toString(), "Location", "Y").setValue(loc.getBlockY());
        config.getNode("Gravestones", deadPlayerUUID.toString(), "Location", "Z").setValue(loc.getBlockZ());

        DefaultConfigManager dcm = new DefaultConfigManager();
        config.getNode("Gravestones", deadPlayerUUID.toString(), "Time").setValue(dcm.getGravestoneClaimLimit());

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
                config.getNode("Gravestones", deadPlayerUUID.toString(), "Items", itemNumS, "Lore").setValue(itemLore);
            }

            Optional<List<Enchantment>> opEnchants = item.get(Keys.ITEM_ENCHANTMENTS);
            if(opLore.isPresent()){
                List<Enchantment> enchantments = opEnchants.get();
                config.getNode("Gravestones", deadPlayerUUID.toString(), "Items", itemNumS, "Enchantments").setValue(enchantments);
            }
            itemNum++;
        }

        saveConfig();
    }

}








