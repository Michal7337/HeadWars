package win.codingboulder.headWars.game;

import com.destroystokyo.paper.event.block.BlockDestroyEvent;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.DyedItemColor;
import io.papermc.paper.datacomponent.item.ItemEnchantments;
import io.papermc.paper.datacomponent.item.Unbreakable;
import io.papermc.paper.event.player.AsyncChatEvent;
import io.papermc.paper.math.BlockPosition;
import io.papermc.paper.math.FinePosition;
import io.papermc.paper.math.Position;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import win.codingboulder.headWars.HeadWars;
import win.codingboulder.headWars.game.shop.ItemShop;
import win.codingboulder.headWars.maps.HeadWarsMap;
import win.codingboulder.headWars.maps.HeadWarsTeam;
import win.codingboulder.headWars.util.Pair;
import win.codingboulder.headWars.util.SimpleBlockPos;
import win.codingboulder.headWars.util.Util;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("UnstableApiUsage")
public class HeadWarsGame implements Listener {

    private final HeadWars plugin = HeadWars.getInstance();

    private final World world;
    private final HeadWarsMap map;
    private final File worldFile;

    private final ArrayList<GameTeam> teams;
    private final ArrayList<Player> players;
    private Audience allPlayersAudience;
    private Audience deadPlayersAudience;
    private Scoreboard mainScoreboard;

    private final HashMap<Player, GameTeam> playerTeams = new HashMap<>(); // utility map for easily accessing player teams
    private final HashMap<Player, Scoreboard> playerScoreboards = new HashMap<>();
    private final HashMap<BlockPosition, FinePosition> clickGenerators = new HashMap<>();
    private final ArrayList<GameTeam> liveTeams = new ArrayList<>();
    private final ArrayList<Player> deadPlayers = new ArrayList<>();

    public String gameName = "";
    int maxPlayers;

    private boolean isStarted;
    private boolean areBasesOpen;

    public HeadWarsGame(World world, HeadWarsMap map, File worldFile) {

        this.world = world;
        this.map = map;
        this.teams = new ArrayList<>();
        this.players = new ArrayList<>();
        this.worldFile = worldFile;

        initialise();

    }

    private void initialise() {

        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);

        map.teams().forEach((color, team) -> teams.add(new GameTeam(team, this))); //create game teams
        map.clickGenerators().forEach(generator -> clickGenerators.put(generator.left().asPosition(), generator.right().asPosition()));

        maxPlayers = teams.size() * map.getPlayersPerTeam();
        allPlayersAudience = Audience.audience(players);
        deadPlayersAudience = Audience.audience(deadPlayers);

    }

    public int gameTimer = 0;
    public final BukkitRunnable runEverySecondTask = new BukkitRunnable() {

        public String nextEvent = "Bases Open";
        public int nextEventTime = 300;

        public void run() {

            if (gameTimer == 300) { // bases open event at 5m
                handleBasesOpenGameEvent();
                nextEvent = "+5 Gens";
                nextEventTime = 300;
            } else if (gameTimer == 600) { // +5 gens at 10m
                handleIncGenLimitGameEvent(5);
                nextEvent = "+5 Gens";
                nextEventTime = 300;
            } else if (gameTimer == 900) { // +5 gens at 15m
                handleIncGenLimitGameEvent(5);
                nextEvent = "+10 Gens";
                nextEventTime = 300;
            } else if (gameTimer == 1200) { // +10 gens at 20m
                handleIncGenLimitGameEvent(10);
                nextEvent = "Heads Destroyed";
                nextEventTime = 300;
            } else if (gameTimer == 1500) { // heads destroyed event at 25m
                handleHeadsDestroyedGameEvent();
                nextEvent = "Game End";
                nextEventTime = 2400;
            }

            nextEventTime --;

            // update player scoreboards
            playerScoreboards.forEach((player, scoreboard) -> {

                Objective objective = scoreboard.getObjective("main");
                GameTeam team = playerTeams.get(player);
                if (objective == null) return;
                if (team == null) return;

                objective.getScore("generators").customName(MiniMessage.miniMessage().deserialize("<white>Gens: <green>" + team.generators + "/" + team.generatorLimit));

                objective.getScore("nextEvent").customName(MiniMessage.miniMessage().deserialize("<aqua>" + nextEvent + "<white> in <green>" + nextEventTime / 60 + "m" + nextEventTime % 60 + "s"));

                teams.forEach(gameTeam -> objective.getScore("team-" + gameTeam.mapTeam().getTeamColor()).customName(Component.text("█: ", gameTeam.getTeamTextColor()).append(gameTeam.getHeadStatusComponent())));

            });

            gameTimer ++;

        }

    };

    private final BukkitRunnable runEveryTickTask = new BukkitRunnable() {

        public void run() {

            if (!areBasesOpen) {

                players.stream().filter(player -> !deadPlayers.contains(player)).forEach(player -> {

                    HeadWarsTeam team = playerTeams.get(player).mapTeam();
                    if (!isLocationInArea2D(player.getLocation(), Pair.of(team.getBasePerimeterPos1(), team.getBasePerimeterPos2()))) {
                        player.teleport(team.getSpawnPosition().asPosition().toLocation(world));
                        player.sendRichMessage("<red>You can't leave your base yet!");
                    }

                });

            }

        }

    };

    public boolean canJoin(Player player) {

        return !isStarted && !HeadWarsGameManager.playersInGames.containsKey(player) && players.size() != maxPlayers && !players.contains(player);

    }

    private int teamAddingNumber;
    public boolean addPlayer(Player player) {

        if (isStarted) return false;
        if (HeadWarsGameManager.playersInGames.containsKey(player)) {player.sendRichMessage("<red>You already are in a game!"); return false;}
        if (players.size() == maxPlayers) {player.sendRichMessage("<red>This game is full!"); return false;}
        if (players.contains(player)) {player.sendRichMessage("<red>You already are in this game!"); return false;}

        teams.get(teamAddingNumber).players().add(player);
        playerTeams.put(player, teams.get(teamAddingNumber));
        teamAddingNumber++;
        if (teamAddingNumber >= teams.size()) teamAddingNumber = 0;
        players.add(player);
        HeadWarsGameManager.playersInGames.put(player, this);

        player.teleport(map.lobbySpawn().asPosition().toLocation(world));

        player.setRespawnLocation(map.lobbySpawn().asPosition().toLocation(world), true);
        if (HeadWars.clearInventoriesBeforeGame) player.getInventory().clear();
        player.clearActivePotionEffects();
        player.setHealth(20);
        player.setGameMode(GameMode.SURVIVAL);

        immunePlayers.add(player); // not allow combat before game start

        if (players.size() == maxPlayers) startCountdown();

        return true;

    }

    private void startCountdown() {

        isStarted = true;

        Bukkit.getScheduler().runTaskLater(plugin, task -> startGame(), 220); // Start the game in 11 seconds (1 extra to be safe)

        final int[] seconds = {10};
        Bukkit.getScheduler().runTaskTimer(plugin, task -> {

            allPlayersAudience.showTitle(Title.title(
                Component.text(seconds[0]).color(NamedTextColor.YELLOW),
                Component.empty(),
                Title.Times.times(Duration.ZERO, Duration.ofMillis(1200), Duration.ZERO)
            ));

            world.playSound(Sound.sound().type(Key.key("minecraft:block.note_block.hat")).volume(1).build(), Sound.Emitter.self());

            seconds[0]--;

            if (seconds[0] == 0) task.cancel();

        }, 0, 20);

    }

    private void startGame() {

        allPlayersAudience.showTitle(Title.title(
            Component.text("START!").color(NamedTextColor.YELLOW),
            Component.empty(),
            Title.Times.times(Duration.ofSeconds(1), Duration.ofSeconds(2), Duration.ofSeconds(1))
        ));

        liveTeams.addAll(teams);
        playerTeams.forEach((player, gameTeam) -> player.teleport(gameTeam.mapTeam().getSpawnPosition().asPosition().toLocation(world)));
        immunePlayers.clear();

        playerTeams.forEach((player, team) -> {

            ItemStack helmet = ItemStack.of(Material.LEATHER_HELMET);
            helmet.setData(DataComponentTypes.DYED_COLOR, DyedItemColor.dyedItemColor(team.mapTeam().getTeamColor().getColor(), false));
            helmet.setData(DataComponentTypes.UNBREAKABLE, Unbreakable.unbreakable(false));
            player.getInventory().setItem(EquipmentSlot.HEAD, helmet);

            ItemStack chestplate = ItemStack.of(Material.LEATHER_CHESTPLATE);
            chestplate.setData(DataComponentTypes.DYED_COLOR, DyedItemColor.dyedItemColor(team.mapTeam().getTeamColor().getColor(), false));
            chestplate.setData(DataComponentTypes.UNBREAKABLE, Unbreakable.unbreakable(false));
            player.getInventory().setItem(EquipmentSlot.CHEST, chestplate);

            ItemStack leggings = ItemStack.of(Material.LEATHER_LEGGINGS);
            leggings.setData(DataComponentTypes.DYED_COLOR, DyedItemColor.dyedItemColor(team.mapTeam().getTeamColor().getColor(), false));
            leggings.setData(DataComponentTypes.UNBREAKABLE, Unbreakable.unbreakable(false));
            player.getInventory().setItem(EquipmentSlot.LEGS, leggings);

            ItemStack boots = ItemStack.of(Material.LEATHER_BOOTS);
            boots.setData(DataComponentTypes.DYED_COLOR, DyedItemColor.dyedItemColor(team.mapTeam().getTeamColor().getColor(), false));
            boots.setData(DataComponentTypes.UNBREAKABLE, Unbreakable.unbreakable(false));
            player.getInventory().setItem(EquipmentSlot.FEET, boots);

        });

        players.forEach(player -> player.give(ResourceGenerator.sword));

        players.forEach(player -> player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, PotionEffect.INFINITE_DURATION, 10, true, false)));

        createScoreboard();

        runEverySecondTask.runTaskTimer(plugin, 0, 20);
        runEveryTickTask.runTaskTimer(plugin, 0, 1);

    }

    private void createScoreboard() {

        mainScoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        teams.forEach(team -> {

            Team scTeam = mainScoreboard.registerNewTeam(team.mapTeam().getTeamColor().toString());
            scTeam.setCanSeeFriendlyInvisibles(true);
            scTeam.setAllowFriendlyFire(false);
            scTeam.color(NamedTextColor.nearestTo(Util.getNamedColor(team.mapTeam().getTeamColor())));
            scTeam.prefix(Component.text("[" + team.mapTeam().getTeamColor() + "] ").color(Util.getNamedColor(team.mapTeam().getTeamColor())));
            scTeam.addEntities(team.players().toArray(new Player[0]));

        });

        players.forEach(player -> {

            /*
                HEAD WARS <title>
                10/03/2025 <date>

                Gens: 10/15 <gens>
                +5 gens in 4m3s <next event>

                RED: ✔✔✔✔ <teams>
                BLUE: ❌❌❌❌
                GREEN: ✔✔❌❌
                YELLOW
                WHITE
                BLACK
                PINK
                AQUA

                <server name>
             */

            Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
            Objective objective = scoreboard.registerNewObjective("main", Criteria.DUMMY, Component.text("HEAD WARS", NamedTextColor.GOLD, TextDecoration.BOLD));
            objective.setDisplaySlot(DisplaySlot.SIDEBAR);

            Score score15 = objective.getScore("date");
            score15.setScore(15);
            Calendar calendar = Calendar.getInstance();
            score15.customName(Component.text(calendar.get(Calendar.DAY_OF_MONTH) + "/" + (calendar.get(Calendar.MONTH) + 1) + "/" + calendar.get(Calendar.YEAR), NamedTextColor.GRAY));

            objective.getScore("line14").setScore(14);
            objective.getScore("line14").customName(Component.text(""));

            objective.getScore("generators").setScore(13);
            objective.getScore("nextEvent").setScore(12);

            objective.getScore("line11").setScore(11);
            objective.getScore("line11").customName(Component.text(""));

            int teamN = 10;
            for (GameTeam team : teams) {
                objective.getScore("team-" + team.mapTeam().getTeamColor()).setScore(teamN);
                teamN --;
            }

            objective.getScore("lineAfterTeams").setScore(teamN);
            objective.getScore("lineAfterTeams").customName(Component.text(""));

            objective.getScore("serverName").setScore(teamN - 1);
            objective.getScore("serverName").customName(Component.text(HeadWars.serverName, NamedTextColor.YELLOW));

            player.setScoreboard(scoreboard);
            playerScoreboards.put(player, scoreboard);

        });

    }

    private void checkWinCondition() {

        for (GameTeam team : teams) {
            if (team.players().isEmpty()) liveTeams.remove(team);
        }

        if (liveTeams.size() == 1) handleTeamWin(liveTeams.getFirst());

    }

    private void handleTeamWin(@NotNull GameTeam team) {

        allPlayersAudience.sendMessage(Component.text("-----------------------------", NamedTextColor.AQUA).appendNewline().appendNewline()
            .append(Component.text("          HEAD WARS", NamedTextColor.GOLD, TextDecoration.BOLD)).appendNewline()
            .append(Component.text("").appendNewline())
            .append(Component.text("Team ", NamedTextColor.DARK_AQUA))
            .append(Component.text(team.mapTeam().getTeamColor().toString().toLowerCase(), Util.getNamedColor(team.mapTeam().getTeamColor())))
            .append(Component.text(" wins!", NamedTextColor.DARK_AQUA)).appendNewline()
            .append(Component.text("").appendNewline())
            .append(Component.text("-----------------------------", NamedTextColor.AQUA))
        );

        allPlayersAudience.sendMessage(Component.text("Instance closes in 30 seconds", NamedTextColor.GRAY));

        Bukkit.getScheduler().runTaskLater(plugin, this::handleGameStop, 600);

    }

    private void handleBasesOpenGameEvent() {

        allPlayersAudience.sendMessage(Component.text("Bases have been opened!", NamedTextColor.DARK_AQUA, TextDecoration.BOLD));
        areBasesOpen = true;

        map.emeraldGenerators().forEach(pos -> ResourceGenerator.placeGenerator(pos.getBlock(world), GeneratorType.registeredTypes.get("emerald")));

    }

    private void handleIncGenLimitGameEvent(int generators) {

        teams.forEach(team -> team.generatorLimit += generators);
        allPlayersAudience.sendMessage(Component.text("Generator limit has been increased by " + generators +"!", NamedTextColor.DARK_AQUA, TextDecoration.BOLD));

    }

    private void handleHeadsDestroyedGameEvent() {

    }

    public void handleGameStop() {

        players.forEach(player -> {
            HeadWarsGameManager.playersInGames.remove(player);
            player.getInventory().clear();
            player.clearActivePotionEffects();
            player.setHealth(20);
            player.setGameMode(GameMode.SURVIVAL);
        });

        if (HeadWars.gameEndAction.equals("teleport")) players.forEach(player -> player.teleport(HeadWars.gameEndTpLocation));
        else players.forEach(player -> player.kick(MiniMessage.miniMessage().deserialize("<red>The game has stopped")));

        runEveryTickTask.cancel();
        runEverySecondTask.cancel();

        playerScoreboards.forEach((player, scoreboard) -> {
            scoreboard.clearSlot(DisplaySlot.SIDEBAR);
            player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        });

        HandlerList.unregisterAll(this);

        Bukkit.unloadWorld(world, false);
        try { Util.deleteDirectory(worldFile); } catch (IOException e) {plugin.getLogger().warning("An error occurred while deleting game world!");}

        HeadWarsGameManager.activeGames().remove(this);
        HeadWarsGameManager.activeGameNames.remove(gameName);
        HeadWarsGameManager.worldFolders.remove(worldFile);

    }

    public void handleForceStop() {

        players.forEach(player -> player.kick(MiniMessage.miniMessage().deserialize("<red>The game has been force stopped.")));

        Bukkit.unloadWorld(world, false);
        try { Util.deleteDirectory(worldFile); } catch (IOException e) {plugin.getLogger().warning("An error occurred while deleting game world!");}

    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isLocationInArea2D(Location location, @NotNull Pair<SimpleBlockPos, SimpleBlockPos> area) {

        int startX, endX;
        if (area.left().x() < area.right().x()) {
            startX = area.left().x();
            endX = area.right().x();
        } else {
            startX = area.right().x();
            endX = area.left().x();
        }

        int startZ, endZ;
        if (area.left().z() < area.right().z()) {
            startZ = area.left().z();
            endZ = area.right().z();
        } else {
            startZ = area.right().z();
            endZ = area.left().z();
        }

        return (location.getX() >= startX && location.getX() <= endX) && (location.getZ() >= startZ && location.getZ() <= endZ);

    }

    public boolean isBlockInArea(Block block, @NotNull Pair<SimpleBlockPos, SimpleBlockPos> area) {

        int startX, endX;
        if (area.left().x() < area.right().x()) {
            startX = area.left().x();
            endX = area.right().x();
        } else {
            startX = area.right().x();
            endX = area.left().x();
        }

        int startY, endY;
        if (area.left().y() < area.right().y()) {
            startY = area.left().y();
            endY = area.right().y();
        } else {
            startY = area.right().y();
            endY = area.left().y();
        }

        int startZ, endZ;
        if (area.left().z() < area.right().z()) {
            startZ = area.left().z();
            endZ = area.right().z();
        } else {
            startZ = area.right().z();
            endZ = area.left().z();
        }

        return (
            (block.getX() >= startX && block.getX() <= endX) &&
            (block.getY() >= startY && block.getY() <= endY) &&
            (block.getZ() >= startZ && block.getZ() <= endZ)
        );

    }

    public boolean isBlockProtected(Block block) {

        for (Pair<SimpleBlockPos, SimpleBlockPos> area : map.protectedAreas()) if (isBlockInArea(block, area)) return true;

        return map.protectedBlocks().contains(SimpleBlockPos.fromLocation(block.getLocation()));

    }

    public boolean isBlockAHead(Block block) {
        for (GameTeam team : teams) if (team.unbrokenHeads().contains(block.getLocation().toBlock())) return true;
        return false;
    }

    public DyeColor getPlayerColor(Player player) {
        return playerTeams.get(player).mapTeam().getTeamColor();
    }

    public boolean isStarted() {
        return isStarted;
    }

    public World world() {
        return world;
    }

    public HashMap<Player, GameTeam> playerTeams() {
        return playerTeams;
    }

    public HeadWarsMap getMap() {
        return map;
    }

    private final HashMap<Player, Block> pendingConfirmUpgrades = new HashMap<>();
    private void handleBlockUpgrade(Block block, Player player) {

        Material blockMat;
        if (Util.isWool(block)) blockMat = Material.WHITE_WOOL; else blockMat = block.getType();
        if (ResourceGenerator.blockUpgradeMaterials.containsKey(blockMat)) {

            if (block.equals(pendingConfirmUpgrades.get(player))) {

                pendingConfirmUpgrades.remove(player);
                ItemStack cost = ResourceGenerator.blockUpgradePrices.get(blockMat);
                if (!player.getInventory().containsAtLeast(cost, cost.getAmount())) {
                    player.sendActionBar(Component.text("You don't have enough resources!", NamedTextColor.RED));
                    return;
                }

                player.getInventory().removeItem(cost);
                block.setType(ResourceGenerator.blockUpgradeMaterials.get(blockMat));
                player.playSound(Sound.sound().type(org.bukkit.Sound.ENTITY_PLAYER_LEVELUP).build(), Sound.Emitter.self());


            } else {

                player.sendActionBar(ResourceGenerator.blockUpgradeMessages.get(blockMat));
                pendingConfirmUpgrades.put(player, block);

            }

        }

    }

    public void handlePlayerDeath(@NotNull Player player) {

        player.teleport(map.lobbySpawn().asPosition().toLocation(world));
        player.setGameMode(GameMode.SPECTATOR);
        PlayerInventory inventory = player.getInventory();
        GameTeam team = playerTeams.get(player);

        ItemStack[] armor = inventory.getArmorContents();

        ItemStack[] newInventory = new ItemStack[inventory.getSize()];
        for (int i = 0; i < newInventory.length; i++) {
            ItemStack item = inventory.getItem(i);

            if (item == null) {
                newInventory[i] = null;
                continue;
            }

            Material itemMaterial = item.getType();
            if (ResourceGenerator.pickaxeTiers.contains(itemMaterial)) {

                if (itemMaterial.equals(ResourceGenerator.pickaxeTiers.getFirst())) {
                    newInventory[i] = item;
                } else {
                    newInventory[i] = item.withType(ResourceGenerator.pickaxeTiers.get(ResourceGenerator.pickaxeTiers.indexOf(itemMaterial) - 1));
                }
                continue;

            }

            if (ResourceGenerator.swordTiers.contains(itemMaterial)) {

                ItemStack sword = item.withType(ResourceGenerator.swordTiers.getFirst());
                int sharp = team.purchasedUpgrades.getOrDefault("upgrade_sharpness", 0);
                if (sharp != 0) sword.setData(DataComponentTypes.ENCHANTMENTS, ItemEnchantments.itemEnchantments(Map.of(Enchantment.SHARPNESS, sharp), true));
                newInventory[i] = item.withType(ResourceGenerator.swordTiers.getFirst());
                continue;

            }

            if (itemMaterial.equals(Material.SHEARS)) {
                newInventory[i] = item;
                continue;
            }

            newInventory[i] = null;

        }

        inventory.clear();

        if (!playerTeams.get(player).unbrokenHeads().isEmpty()) {

            deadPlayers.add(player);

            Bukkit.getScheduler().runTaskLater(plugin, task -> {

                deadPlayers.remove(player);
                player.setGameMode(GameMode.SURVIVAL);
                player.teleport(playerTeams.get(player).mapTeam().getSpawnPosition().asPosition().toLocation(world));

                inventory.setContents(newInventory);
                inventory.setArmorContents(armor);

                player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, PotionEffect.INFINITE_DURATION, 1, false, false, false));

                immunePlayers.add(player);
                Bukkit.getScheduler().runTaskLater(plugin, task2 -> immunePlayers.remove(player), 100);

            }, 100);

            AtomicInteger secs = new AtomicInteger(5);
            Bukkit.getScheduler().runTaskTimer(plugin, task -> {

                player.showTitle(Title.title(Component.text("YOU DIED!", NamedTextColor.YELLOW), Component.text("Respawn in: " + secs)));
                player.playSound(Sound.sound(Key.key("minecraft:block.note_block.hat"), Sound.Source.MASTER, 1000, 1));
                secs.getAndDecrement();

                if (secs.get() == 0) task.cancel();

            }, 0, 20);

        } else {

            player.showTitle(Title.title(Component.text("YOU DIED!", NamedTextColor.YELLOW), Component.text("You can't respawn anymore", NamedTextColor.RED)));
            playerTeams.get(player).players().remove(player);
            deadPlayers.add(player);
            checkWinCondition();

        }

    }

    @EventHandler
    public void onBlockPlace(@NotNull BlockPlaceEvent event) {

        Block block = event.getBlock();

        if (block.getWorld() != world) return;

        if (block.getType() == Material.TNT) {
            block.setType(Material.AIR);
            world.spawnEntity(block.getLocation(), EntityType.TNT, CreatureSpawnEvent.SpawnReason.SPELL, entity -> entity.setVelocity(new Vector()));
            return;
        }

        if (Objects.equals(event.getItemInHand().getPersistentDataContainer().get(new NamespacedKey("headwars", "item"), PersistentDataType.STRING), "fireball")) {
            event.setCancelled(true);
        }

        for (Pair<SimpleBlockPos, SimpleBlockPos> area : map.noPlaceAreas()) if (isBlockInArea(block, area)) event.setCancelled(true);

    }

    @EventHandler
    public void onRightClick(@NotNull PlayerInteractEvent event) {

        Player player = event.getPlayer();
        if (Objects.equals(player.getInventory().getItemInMainHand().getPersistentDataContainer().get(new NamespacedKey("headwars", "item"), PersistentDataType.STRING), "fireball")) {
            event.setCancelled(true);
            player.getInventory().removeItem(player.getInventory().getItemInMainHand().asOne());
            player.launchProjectile(Fireball.class, player.getLocation().getDirection().multiply(2));
        }

        Block block = event.getClickedBlock();
        if (block == null) return;
        if (event.getAction().isLeftClick()) return;
        if (!block.getWorld().equals(world)) return;
        if (event.getPlayer().getGameMode() == GameMode.SPECTATOR) return;

        if (player.isSneaking() && Objects.equals(event.getHand(), EquipmentSlot.HAND) && isBlockInArea(block, playerTeams.get(player).mapTeam().getBasePerimeter())) handleBlockUpgrade(block, event.getPlayer());

        Location location = block.getLocation();

        //If clicked the button generator
        if (clickGenerators.containsKey(Position.block(location)) && !player.isSneaking()) {

            event.setCancelled(true);

            Location itemSpawnPos = clickGenerators.get(Position.block(location)).toLocation(world);

            Item item = world.createEntity(itemSpawnPos, Item.class);
            item.setItemStack(new ItemStack(Material.IRON_INGOT));
            item.spawnAt(itemSpawnPos);

        }

    }

    @EventHandler
    public void onRightClickEntity(@NotNull PlayerInteractEntityEvent event) {

        Entity entity = event.getRightClicked();

        if (!this.map.itemShops().containsKey(entity.getUniqueId())) return;
        if (event.getPlayer().getGameMode() == GameMode.SPECTATOR) return;

        event.setCancelled(true);

        ItemShop shop = ItemShop.registeredItemShops.get(map.itemShops().get(entity.getUniqueId()));
        if (shop == null) return;
        shop.openShop(event.getPlayer());

    }

    @EventHandler
    public void onBlockDestroy(@NotNull BlockDestroyEvent event) {

        Block block = event.getBlock();
        if (block.getWorld() != world) return;

        if (isBlockProtected(block)) event.setCancelled(true);
        if (clickGenerators.containsKey(block.getLocation().toBlock())) event.setCancelled(true);
        if (isBlockAHead(block)) event.setCancelled(true);

    }

    @EventHandler
    public void onWaterFlow(@NotNull BlockFromToEvent event) {

        Block block = event.getToBlock();
        if (block.getWorld() != world) return;

        if (isBlockProtected(block)) event.setCancelled(true);
        if (clickGenerators.containsKey(block.getLocation().toBlock())) event.setCancelled(true);
        if (isBlockAHead(block)) event.setCancelled(true);

    }

    @EventHandler
    public void onBlockExplode(@NotNull EntityExplodeEvent event) {

        if (event.getLocation().getWorld() != world) return;
        event.blockList().removeIf(this::isBlockProtected);
        event.blockList().removeIf(this::isBlockAHead);

    }

    @EventHandler
    public void onBlockBreak(@NotNull BlockBreakEvent event) {

        Block block = event.getBlock();
        if (block.getWorld() != world) return;
        if (isBlockProtected(block)) event.setCancelled(true);

        teams.forEach(team -> {

            if (team.unbrokenHeads().contains(block.getLocation().toBlock())) {

                if (team.players().contains(event.getPlayer())) {
                    event.setCancelled(true);
                } else {
                    event.setDropItems(false);
                    team.unbrokenHeads().remove(block.getLocation().toBlock());
                    team.players().forEach(player -> player.showTitle(Title.title(Component.text("HEAD DESTROYED", NamedTextColor.RED), Component.text("You have " + team.unbrokenHeads().size() + " heads left!", NamedTextColor.YELLOW))));

                    allPlayersAudience.sendMessage(MiniMessage.miniMessage().deserialize("<bold><aqua>HEAD DESTROYED</bold><white> - <yellow>" + event.getPlayer().getName() + "<white> has destroyed team ")
                        .append(team.getColoredTeamName())
                        .append(Component.text("'s head! They have ", NamedTextColor.WHITE))
                        .append(Component.text(team.unbrokenHeads().size(), NamedTextColor.YELLOW))
                        .append(Component.text(" heads left.", NamedTextColor.WHITE))
                    );

                }

            }

        });

    }

    @EventHandler
    public void onPlayerDeath(@NotNull PlayerDeathEvent event) {

        handlePlayerDeath(event.getPlayer());

    }

    @EventHandler
    public void onInventoryClick(@NotNull InventoryClickEvent event) {

        Player player = (Player) event.getWhoClicked();
        if (!players.contains(player)) return;

        if (event.getClickedInventory() == player.getInventory() && ( event.getSlot() == 39 ||
            event.getSlot() == 38 || event.getSlot() == 37 || event.getSlot() == 36)
        ) event.setCancelled(true);

    }

    @EventHandler
    public void onPlayerMove(@NotNull PlayerMoveEvent event) {

        Player player = event.getPlayer();

        if (!player.getWorld().equals(world)) return;

        if (player.getLocation().y() < 0) handlePlayerDeath(player);

    }

    private final ArrayList<Player> immunePlayers = new ArrayList<>();
    @EventHandler
    public void onPlayerDamage(@NotNull EntityDamageByEntityEvent event) {

        if (!(event.getEntity() instanceof Player player)) return;

        if (immunePlayers.contains(player)) {
            event.setCancelled(true);
            return;
        }

        if (!(event.getDamager() instanceof Player damager)) return;

        immunePlayers.remove(damager);

    }

    private final HashMap<UUID, GameTeam> disconnectedPlayers = new HashMap<>();
    private final HashMap<UUID, Player> disconnectedPlayerObjects = new HashMap<>();

    @EventHandler
    public void onPlayerLeave(@NotNull PlayerQuitEvent event) {

        Player player = event.getPlayer();
        if (!players.contains(player)) return;

        players.remove(player);
        playerTeams.get(player).players().remove(player);
        playerTeams.remove(player);

        if (!isStarted) return;

        disconnectedPlayers.put(player.getUniqueId(), playerTeams.get(player));
        disconnectedPlayerObjects.put(player.getUniqueId(), player);

        checkWinCondition();

    }

    @EventHandler
    public void onPlayerJoin(@NotNull PlayerJoinEvent event) {

        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        if (!disconnectedPlayers.containsKey(playerUUID)) return;
        Player oldPlayer = disconnectedPlayerObjects.get(playerUUID);

        players.add(player);
        disconnectedPlayers.get(playerUUID).players().add(player);
        playerTeams.put(player, disconnectedPlayers.get(playerUUID));

        playerScoreboards.put(player, playerScoreboards.get(oldPlayer));
        playerScoreboards.remove(oldPlayer);

        handlePlayerDeath(player);

        disconnectedPlayers.remove(playerUUID);
        disconnectedPlayerObjects.remove(playerUUID);

    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(@NotNull AsyncChatEvent event) {

        if (!HeadWars.isGameChatEnabled) return; // if the game chat is disabled don't touch the event

        Player player = event.getPlayer();
        if (!player.getWorld().equals(world)) return;
        Component message = event.message();

        if (deadPlayers.contains(player)) { // if the player is dead send the message as GHOST

            event.setCancelled(true);
            Component newMessage = Component.text("[GHOST] ", NamedTextColor.GRAY).append(player.displayName().color(NamedTextColor.GRAY)).append(Component.text(": ")).append(message);
            deadPlayersAudience.sendMessage(newMessage);

        } else if (!deadPlayers.contains(player) && players.contains(player)) { // if the player is in the game (alive)

            event.setCancelled(true);
            GameTeam team = playerTeams.get(player);
            Component newMessage = Component.text("[" + team.getTeamName() + "] ", team.getTeamTextColor()).append(player.displayName().color(team.getTeamTextColor())).append(Component.text(": ", NamedTextColor.WHITE).append(message));
            allPlayersAudience.sendMessage(newMessage);

        }

    }

}
