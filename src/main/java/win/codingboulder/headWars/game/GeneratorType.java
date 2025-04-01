package win.codingboulder.headWars.game;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import win.codingboulder.headWars.HeadWars;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

public class GeneratorType {

    public static HashMap<String, GeneratorType> registeredTypes = new HashMap<>();
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private String id;
    private String name;
    private HashMap<Integer, GeneratorTier> tiers;
    private byte[] resource;

    private ItemStack resourceStack;

    public GeneratorType(String id, String name, HashMap<Integer, GeneratorTier> tiers, byte[] resource) {

        this.id = id;
        this.name = name;
        this.tiers = tiers;
        this.resource = resource;
        if (ArrayUtils.isEmpty(resource)) resourceStack = ItemStack.empty(); else resourceStack = ItemStack.deserializeBytes(resource);

    }

    public static void reloadGeneratorTypes() {

        File[] genFiles = HeadWars.getGeneratorsFolder().listFiles();
        registeredTypes = new HashMap<>();
        if (genFiles == null) return;

        for (File genFile : genFiles) {

            try {

                GeneratorType generatorType = gson.fromJson(new FileReader(genFile), GeneratorType.class);
                registeredTypes.put(generatorType.id(), generatorType);

            } catch (IOException e) {
                HeadWars.getInstance().getLogger().warning("Could not load generator type '" + genFile.getName() +"' - An IO exception occurred");
            } catch (JsonParseException e) {
                HeadWars.getInstance().getLogger().warning("Could not load generator type '" + genFile.getName() +"' - A JSON Parse exception occurred");
            }

        }

    }

    public void saveGeneratorType() {

        File genFile = new File(HeadWars.getGeneratorsFolder(), this.id);

        try {
            gson.toJson(this, new FileWriter(genFile));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public String id() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String name() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public HashMap<Integer, GeneratorTier> tiers() {
        return tiers;
    }

    public void setTiers(HashMap<Integer, GeneratorTier> tiers) {
        this.tiers = tiers;
    }

    public byte[] resourceArray() {
        return resource;
    }

    public void setResourceArray(byte[] resource) {
        if (ArrayUtils.isEmpty(resource)) resourceStack = ItemStack.empty(); else resourceStack = ItemStack.deserializeBytes(resource);
        this.resource = resource;
    }

    public ItemStack resource() {
        return resourceStack;
    }

    public void setResource(@NotNull ItemStack resource) {
        this.resourceStack = resource;
        if (resource.isEmpty()) this.resource = new byte[0]; else this.resource = resource.serializeAsBytes();
    }

    public static class GeneratorTier {

        private int tier;
        private int speed;
        private Material material;
        private String upgradeMessage;
        private byte[] upgradeCost;

        private Component upgradeMessageComponent;
        private ItemStack upgradeCostItem;

        public GeneratorTier(int tier, int speed, Material material, String upgradeMessage, byte[] upgradeCost) {

            this.tier = tier;
            this.speed = speed;
            this.material = material;
            this.upgradeMessage = upgradeMessage;
            this.upgradeCost = upgradeCost;

            this.upgradeMessageComponent = MiniMessage.miniMessage().deserializeOr(upgradeMessage, Component.text("Upgrade message not defined", NamedTextColor.RED));
            if (ArrayUtils.isEmpty(upgradeCost)) upgradeCostItem = ItemStack.empty(); else upgradeCostItem = ItemStack.deserializeBytes(upgradeCost);

        }

        public int tier() {
            return tier;
        }

        public void setTier(int tier) {
            this.tier = tier;
        }

        public int speed() {
            return speed;
        }

        public void setSpeed(int speed) {
            this.speed = speed;
        }

        public Material material() {
            return material;
        }

        public void setMaterial(Material material) {
            this.material = material;
        }

        public String rawUpgradeMessage() {
            return upgradeMessage;
        }

        public void setRawUpgradeMessage(String upgradeMessage) {
            this.upgradeMessage = upgradeMessage;
            this.upgradeMessageComponent = MiniMessage.miniMessage().deserializeOr(upgradeMessage, Component.text("Upgrade message not defined", NamedTextColor.RED));
        }

        public byte[] upgradeCostArray() {
            return upgradeCost;
        }

        public void setUpgradeCostArray(byte[] upgradeCost) {
            this.upgradeCost = upgradeCost;
            if (ArrayUtils.isEmpty(upgradeCost)) upgradeCostItem = ItemStack.empty(); else upgradeCostItem = ItemStack.deserializeBytes(upgradeCost);
        }

        public Component upgradeMessage() {
            return upgradeMessageComponent;
        }

        public void setUpgradeMessage(Component upgradeMessage) {
            this.upgradeMessageComponent = upgradeMessage;
        }

        public ItemStack upgradeCost() {
            return upgradeCostItem;
        }

        public void UpgradeCost(ItemStack upgradeCost) {
            this.upgradeCostItem = upgradeCost;
        }

    }

}
