package win.codingboulder.headWars.game;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import win.codingboulder.headWars.HeadWars;

import java.util.ArrayList;
import java.util.HashMap;

public class ResourceGenerator extends BukkitRunnable {

    private String id;
    private int tier;
    private Block block;
    private ItemStack resource;
    private int genTime;
    private final ArrayList<Item> generatedItems = new ArrayList<>();
    public ItemStack spawningItem;
    public Player placingPlayer;

    public ResourceGenerator(Block block, ItemStack resource, int genTime, String id) {

        this.block = block;
        this.resource = resource;
        this.genTime = genTime;
        this.id = id;

        this.runTaskTimer(HeadWars.getInstance(), 0, genTime);

    }

    @Override
    public void run() {

        //generatedItems.stream().filter(item -> !item.isValid()).forEach(generatedItems::remove);
        //if (generatedItems.size() >= 64) return;
        Item item = block.getWorld().dropItem(block.getLocation().add(0, 1, 0).toCenterLocation(), resource, item1 -> item1.setVelocity(new Vector()));
        //generatedItems.add(item);

    }

    public ResourceGenerator setGenTime(int genTime) {

        this.genTime = genTime;
        this.cancel();

        ResourceGenerator gen = new ResourceGenerator(block, resource, genTime, id);
        gen.spawningItem = spawningItem;
        gen.placingPlayer = placingPlayer;

        return gen;

    }

    public int getGenTime() {
        return genTime;
    }

    public Block getBlock() {
        return block;
    }

    public void setBlock(Block block) {
        this.block = block;
    }

    public ItemStack getResource() {
        return resource;
    }

    public void setResource(ItemStack resource) {
        this.resource = resource;
    }

    public String id() {
        return id;
    }

    public void id(String id) {
        this.id = id;
    }

    public int getTier() {
        return tier;
    }

    public void setTier(int tier) {
        this.tier = tier;
    }

    public Player getPlacingPlayer() {
        return placingPlayer;
    }

    public void setPlacingPlayer(Player placingPlayer) {
        this.placingPlayer = placingPlayer;
    }

    public static HashMap<String, HashMap<Integer, Component>> genUpgradeMessages = new HashMap<>();
    public static HashMap<String, HashMap<Integer, ItemStack>> genUpgradeCost = new HashMap<>();
    public static HashMap<String, HashMap<Integer, Material>> genTierMaterials = new HashMap<>();
    public static HashMap<String, HashMap<Integer, Integer>> genSpeeds = new HashMap<>();

    static {

        HashMap<Integer, Component> ironUpgradeMessages = new HashMap<>();
        ironUpgradeMessages.put(1, MiniMessage.miniMessage().deserialize("<green>Cost: <white>32 Iron <green>| Click again to confirm!"));
        ironUpgradeMessages.put(2, MiniMessage.miniMessage().deserialize("<green>Cost: <gold>32 Gold <green>| Click again to confirm!"));

        HashMap<Integer, Component> goldUpgradeMessages = new HashMap<>();
        goldUpgradeMessages.put(1, MiniMessage.miniMessage().deserialize("<green>Cost: <gold>32 Gold <green>| Click again to confirm!"));
        goldUpgradeMessages.put(2, MiniMessage.miniMessage().deserialize("<green>Cost: <aqua>24 Diamond <green>| Click again to confirm!"));

        HashMap<Integer, Component> diamondUpgradeMessages = new HashMap<>();
        diamondUpgradeMessages.put(1, MiniMessage.miniMessage().deserialize("<green>Cost: <aqua>32 Diamond <green>| Click again to confirm!"));
        diamondUpgradeMessages.put(2, MiniMessage.miniMessage().deserialize("<green>Cost: <aqua>64 Diamond <green>| Click again to confirm!"));

        genUpgradeMessages.put("iron_generator", ironUpgradeMessages);
        genUpgradeMessages.put("gold_generator", goldUpgradeMessages);
        genUpgradeMessages.put("diamond_generator", diamondUpgradeMessages);

        HashMap<Integer, ItemStack> ironUpgradeC = new HashMap<>();
        ironUpgradeC.put(1, ItemStack.of(Material.IRON_INGOT, 32));
        ironUpgradeC.put(2, ItemStack.of(Material.GOLD_INGOT, 32));

        HashMap<Integer, ItemStack> goldUpgradeC = new HashMap<>();
        goldUpgradeC.put(1, ItemStack.of(Material.GOLD_INGOT, 32));
        goldUpgradeC.put(2, ItemStack.of(Material.DIAMOND, 24));

        HashMap<Integer, ItemStack> diamondUpgradeC = new HashMap<>();
        diamondUpgradeC.put(1, ItemStack.of(Material.DIAMOND, 32));
        diamondUpgradeC.put(2, ItemStack.of(Material.DIAMOND, 64));

        genUpgradeCost.put("iron_generator", ironUpgradeC);
        genUpgradeCost.put("gold_generator", goldUpgradeC);
        genUpgradeCost.put("diamond_generator", diamondUpgradeC);

        HashMap<Integer, Material> ironGenMaterials = new HashMap<>();
        ironGenMaterials.put(1, Material.LIGHT_GRAY_STAINED_GLASS);
        ironGenMaterials.put(2, Material.IRON_ORE);
        ironGenMaterials.put(3, Material.IRON_BLOCK);

        HashMap<Integer, Material> goldGenMaterials = new HashMap<>();
        goldGenMaterials.put(1, Material.YELLOW_STAINED_GLASS);
        goldGenMaterials.put(2, Material.GOLD_ORE);
        goldGenMaterials.put(3, Material.GOLD_BLOCK);

        HashMap<Integer, Material> diamondGenMaterials = new HashMap<>();
        diamondGenMaterials.put(1, Material.LIGHT_BLUE_STAINED_GLASS);
        diamondGenMaterials.put(2, Material.DIAMOND_ORE);
        diamondGenMaterials.put(3, Material.DIAMOND_BLOCK);

        genTierMaterials.put("iron_generator", ironGenMaterials);
        genTierMaterials.put("gold_generator", goldGenMaterials);
        genTierMaterials.put("diamond_generator", diamondGenMaterials);

        HashMap<Integer, Integer> ironGenSpeeds = new HashMap<>();
        ironGenSpeeds.put(1, 40);
        ironGenSpeeds.put(2, 20);
        ironGenSpeeds.put(3, 10);

        HashMap<Integer, Integer> goldGenSpeeds = new HashMap<>();
        goldGenSpeeds.put(1, 60);
        goldGenSpeeds.put(2, 20);
        goldGenSpeeds.put(3, 20);

        HashMap<Integer, Integer> diamondGenSpeeds = new HashMap<>();
        diamondGenSpeeds.put(1, 80);
        diamondGenSpeeds.put(2, 60);
        diamondGenSpeeds.put(3, 40);

        genSpeeds.put("iron_generator", ironGenSpeeds);
        genSpeeds.put("gold_generator", goldGenSpeeds);
        genSpeeds.put("diamond_generator", diamondGenSpeeds);

    }

}
