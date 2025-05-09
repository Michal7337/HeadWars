package win.codingboulder.headWars.game;

import com.destroystokyo.paper.event.block.BlockDestroyEvent;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Unbreakable;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import win.codingboulder.headWars.HeadWars;
import win.codingboulder.headWars.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class ResourceGenerator extends BukkitRunnable implements Listener {

    public static HashMap<Block, ResourceGenerator> generators = new HashMap<>();
    public static HashMap<Block, ResourceGenerator> generatorCarpets = new HashMap<>();

    private GeneratorType type;
    private Block block;

    private int tier;
    public Player placingPlayer;
    public ItemStack spawningItem;
    public boolean saveOnUnload;

    public ResourceGenerator(Block block, GeneratorType type) {

        this.block = block;
        this.type = type;

        //if (type != null && !type.tiers().isEmpty() && block != null) this.runTaskTimer(HeadWars.getInstance(), 20, type.tiers().get(0).speed());

    }

    @Override
    public void run() {

        Item item = block.getWorld().dropItem(block.getLocation().add(0, 1, 0).toCenterLocation(), type.resource(), item1 -> item1.setVelocity(new Vector()));

    }

    public void upgrade(Player player) {

        if (type.tiers().size() < tier + 2) { // if there are no more tiers | 1 -> 2 -- 3 tiers
            player.sendActionBar(Component.text("This generator is at max tier!", NamedTextColor.RED));
            return;
        }

        tier++;
        GeneratorType.GeneratorTier newTier = type.tiers().get(tier);
        if (newTier == null) return;

        ResourceGenerator newGen = new ResourceGenerator(block, type);
        newGen.tier = tier;
        newGen.placingPlayer = placingPlayer;
        newGen.spawningItem = spawningItem;
        newGen.saveOnUnload = saveOnUnload;

        this.cancel();
        newGen.runTaskTimer(HeadWars.getInstance(), newTier.speed(), newTier.speed());
        block.setType(newTier.material());
        generators.put(block, newGen);

    }

    public void upgrade() {

        if (type.tiers().size() < tier + 2) return; // if there are no more tiers | 1 -> 2 -- 3 tiers

        tier++;
        GeneratorType.GeneratorTier newTier = type.tiers().get(tier);
        if (newTier == null) return;

        ResourceGenerator newGen = new ResourceGenerator(block, type);
        newGen.placingPlayer = placingPlayer;
        newGen.spawningItem = spawningItem;

        this.cancel();
        newGen.runTaskTimer(HeadWars.getInstance(), newTier.speed(), newTier.speed());
        block.setType(newTier.material());
        generators.put(block, newGen);

    }

    @EventHandler
    public void onBlockPlace(@NotNull BlockPlaceEvent event) {

        String genId = event.getItemInHand().getPersistentDataContainer().get(new NamespacedKey("headwars", "generator_id"), PersistentDataType.STRING);
        if (genId == null) return;

        GeneratorType generatorType = GeneratorType.registeredTypes.get(genId);
        if (generatorType == null) {event.setCancelled(true); return;}
        if (generatorType.tiers().isEmpty()) {event.setCancelled(true); return;}

        Block placedBlock = event.getBlock();
        Player player = event.getPlayer();

        // logic for handling HeadWars team generator limits. Remove if using generators outside HeadWars
        HeadWarsGame game = HeadWarsGameManager.playersInGames.get(player);
        if (game != null && game.isStarted()) {
            GameTeam team = game.playerTeams().get(player);
            if (team.generators >= team.generatorLimit) {
                player.sendActionBar(Component.text("You can't place any more generators!", NamedTextColor.RED));
                event.setCancelled(true);
                return;
            } else if (!game.isLocationInArea2D(placedBlock.getLocation(), Pair.of(team.mapTeam().getBasePerimeterPos1(), team.mapTeam().getBasePerimeterPos2()))) {
                player.sendActionBar(Component.text("You can't place generators outside of your base!", NamedTextColor.RED));
                event.setCancelled(true);
                return;
            } else team.generators++;
        }

        boolean hasCarpet = generatorType.carpetMaterial() != Material.AIR;
        if (hasCarpet && !placedBlock.getRelative(BlockFace.UP).isEmpty()) {event.setCancelled(true); return;}

        ResourceGenerator generator = new ResourceGenerator(placedBlock, generatorType);

        placedBlock.setType(generator.generatorTier().material());
        if (hasCarpet) placedBlock.getRelative(BlockFace.UP).setType(generatorType.carpetMaterial());
        generator.runTaskTimer(HeadWars.getInstance(), 20, generator.generatorTier().speed());
        generator.spawningItem = event.getItemInHand().asOne();
        generator.placingPlayer = player;

        generators.put(placedBlock, generator);
        if (hasCarpet) generatorCarpets.put(placedBlock.getRelative(BlockFace.UP), generator);

    }

    private static final HashMap<Player, Block> pendingConfirmUpgrades = new HashMap<>();

    @EventHandler
    public void onRightClick(@NotNull PlayerInteractEvent event) {

        Player player = event.getPlayer();
        Block clickedBlock = event.getClickedBlock();

        if (clickedBlock == null) return;
        if (!event.getAction().isRightClick()) return;
        if (Objects.equals(event.getHand(), EquipmentSlot.OFF_HAND)) return;
        if (!player.isSneaking()) return;
        if (!generators.containsKey(clickedBlock) && !generatorCarpets.containsKey(clickedBlock)) return; // if it's not a generator or a carpet

        if (generatorCarpets.containsKey(clickedBlock)) clickedBlock = generatorCarpets.get(clickedBlock).block;

        ResourceGenerator generator = generators.get(clickedBlock);
        if (!generator.type.upgradableByPlayers()) return;

        if (generator.type.tiers().size() < generator.tier + 2) { // if there are no more tiers, ignore upgrade | 1 -> 2 -- 3 tiers
            player.sendActionBar(Component.text("This generator is at max tier!", NamedTextColor.RED));
            return;
        }

        GeneratorType.GeneratorTier generatorTier = generator.generatorTier();

        if (clickedBlock.equals(pendingConfirmUpgrades.get(player))) { // if the player's pending upgrade is the clicked generator

            if (player.getInventory().containsAtLeast(generatorTier.upgradeCost(), generatorTier.upgradeCost().getAmount())) {

                player.getInventory().removeItem(generatorTier.upgradeCost());
                player.sendActionBar(Component.text("Generator upgraded!", NamedTextColor.GREEN));
                player.playSound(Sound.sound().type(org.bukkit.Sound.ENTITY_PLAYER_LEVELUP).build(), Sound.Emitter.self());
                pendingConfirmUpgrades.remove(player);
                generator.upgrade(player);

            } else {

                player.sendActionBar(Component.text("You can't afford this!", NamedTextColor.RED));
                pendingConfirmUpgrades.remove(player);

            }

        } else { // if the player's pending upgrade is empty or a different generator

            pendingConfirmUpgrades.put(player, clickedBlock);
            player.sendActionBar(generatorTier.upgradeMessage());

        }

    }

    @EventHandler
    public void onBlockBreak(@NotNull BlockBreakEvent event) {

        Block brokenBlock = event.getBlock();

        if (generators.containsKey(brokenBlock)) {

            ResourceGenerator generator = generators.get(brokenBlock);

            generator.cancel();
            if (generatorCarpets.containsKey(brokenBlock.getRelative(BlockFace.UP))) brokenBlock.getRelative(BlockFace.UP).setType(Material.AIR);
            event.setDropItems(false);
            brokenBlock.getWorld().dropItem(brokenBlock.getLocation(), generator.spawningItem);
            generators.remove(brokenBlock);

            // logic for handling HeadWars team generator limits. Remove if using generators outside HeadWars
            if (generator.placingPlayer != null && HeadWarsGameManager.playersInGames.containsKey(generator.placingPlayer)
                && HeadWarsGameManager.playersInGames.get(generator.placingPlayer).isStarted()
            ) HeadWarsGameManager.playersInGames.get(generator.placingPlayer).playerTeams().get(generator.placingPlayer).generators--;

        } else if (generatorCarpets.containsKey(brokenBlock)) event.setCancelled(true);

    }

    @EventHandler
    public void onBlockDestroy(@NotNull BlockDestroyEvent event) {

        Block brokenBlock = event.getBlock();

        if (generators.containsKey(brokenBlock)) {

            ResourceGenerator generator = generators.get(brokenBlock);

            generator.cancel();
            if (generatorCarpets.containsKey(brokenBlock.getRelative(BlockFace.UP))) brokenBlock.getRelative(BlockFace.UP).setType(Material.AIR);
            event.setWillDrop(false);
            brokenBlock.getWorld().dropItem(brokenBlock.getLocation(), generator.spawningItem);
            generators.remove(brokenBlock);

            // logic for handling HeadWars team generator limits. Remove if using generators outside HeadWars
            if (generator.placingPlayer != null && HeadWarsGameManager.playersInGames.containsKey(generator.placingPlayer)
                && HeadWarsGameManager.playersInGames.get(generator.placingPlayer).isStarted()
            ) HeadWarsGameManager.playersInGames.get(generator.placingPlayer).playerTeams().get(generator.placingPlayer).generators--;

        } else if (generatorCarpets.containsKey(brokenBlock)) event.setCancelled(true);

    }

    @EventHandler
    public void onBlockExplode(@NotNull EntityExplodeEvent event) {

        event.blockList().removeIf(expBlock -> generators.containsKey(expBlock) || generatorCarpets.containsKey(expBlock));

    }

    @EventHandler
    public void onWorldUnload(@NotNull WorldUnloadEvent event) {

        World world = event.getWorld();
        generators.forEach((block, gen) -> {
            if (block.getWorld().equals(world)) gen.cancel();
        });
        generators.keySet().removeIf(block -> block.getWorld().equals(world));

    }

    public GeneratorType type() {
        return type;
    }

    public void setType(GeneratorType type) {
        this.type = type;
    }

    public int tier() {
        return tier;
    }

    public void setTier(int tier) {
        this.tier = tier;
    }

    public Block block() {
        return block;
    }

    public void setBlock(Block block) {
        this.block = block;
    }

    public GeneratorType.GeneratorTier generatorTier() {
        return this.type.tiers().get(this.tier);
    }

    public static HashMap<String, HashMap<Integer, Component>> genUpgradeMessages = new HashMap<>();
    public static HashMap<String, HashMap<Integer, ItemStack>> genUpgradeCost = new HashMap<>();
    public static HashMap<String, HashMap<Integer, Material>> genTierMaterials = new HashMap<>();
    public static HashMap<String, HashMap<Integer, Integer>> genSpeeds = new HashMap<>();

    public static HashMap<Material, Material> blockUpgradeMaterials = new HashMap<>();
    public static HashMap<Material, ItemStack> blockUpgradePrices = new HashMap<>();
    public static HashMap<Material, Component> blockUpgradeMessages = new HashMap<>();

    public static ArrayList<Material> pickaxeTiers = new ArrayList<>();
    public static ArrayList<Material> swordTiers = new ArrayList<>();

    public static ItemStack sword = ItemStack.of(Material.WOODEN_SWORD);

    static {

        pickaxeTiers.add(Material.WOODEN_PICKAXE);
        pickaxeTiers.add(Material.STONE_PICKAXE);
        pickaxeTiers.add(Material.IRON_PICKAXE);
        pickaxeTiers.add(Material.DIAMOND_PICKAXE);

        swordTiers.add(Material.WOODEN_SWORD);
        swordTiers.add(Material.STONE_SWORD);
        swordTiers.add(Material.IRON_SWORD);
        swordTiers.add(Material.DIAMOND_SWORD);

        //noinspection UnstableApiUsage
        sword.setData(DataComponentTypes.UNBREAKABLE, Unbreakable.unbreakable(false));

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


        blockUpgradeMaterials.put(Material.WHITE_WOOL, Material.WHITE_TERRACOTTA);
        blockUpgradeMaterials.put(Material.WHITE_TERRACOTTA, Material.SMOOTH_STONE);
        blockUpgradeMaterials.put(Material.SMOOTH_STONE, Material.END_STONE);
        blockUpgradeMaterials.put(Material.END_STONE, Material.OBSIDIAN);

        blockUpgradePrices.put(Material.WHITE_WOOL, ItemStack.of(Material.IRON_INGOT, 16));
        blockUpgradePrices.put(Material.WHITE_TERRACOTTA, ItemStack.of(Material.IRON_INGOT, 48));
        blockUpgradePrices.put(Material.SMOOTH_STONE, ItemStack.of(Material.GOLD_INGOT, 32));
        blockUpgradePrices.put(Material.END_STONE, ItemStack.of(Material.DIAMOND, 24));

        blockUpgradeMessages.put(Material.WHITE_WOOL, MiniMessage.miniMessage().deserialize("<green>Cost: <white>16 Iron <green>| Click again to confirm!"));
        blockUpgradeMessages.put(Material.WHITE_TERRACOTTA, MiniMessage.miniMessage().deserialize("<green>Cost: <white>48 Iron <green>| Click again to confirm!"));
        blockUpgradeMessages.put(Material.SMOOTH_STONE, MiniMessage.miniMessage().deserialize("<green>Cost: <gold>32 Gold <green>| Click again to confirm!"));
        blockUpgradeMessages.put(Material.END_STONE, MiniMessage.miniMessage().deserialize("<green>Cost: <aqua>24 Diamond <green>| Click again to confirm!"));

    }

}
