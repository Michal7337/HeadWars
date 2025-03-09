package win.codingboulder.headWars.game;

import com.destroystokyo.paper.event.block.BlockDestroyEvent;
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
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;
import org.jetbrains.annotations.NotNull;
import win.codingboulder.headWars.HeadWars;
import win.codingboulder.headWars.game.shop.ItemShop;
import win.codingboulder.headWars.game.shop.ShopManager;
import win.codingboulder.headWars.maps.HeadWarsMap;
import win.codingboulder.headWars.util.Pair;
import win.codingboulder.headWars.util.SimpleBlockPos;
import win.codingboulder.headWars.util.Util;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("UnstableApiUsage")
public class HeadWarsGame implements Listener {

    private final HeadWars plugin = HeadWars.getInstance();

    private final World world;
    private final HeadWarsMap map;

    private final ArrayList<GameTeam> teams;
    private final ArrayList<Player> players;
    private Audience allPlayersAudience;
    private Scoreboard mainScoreboard;

    private final HashMap<Player, GameTeam> playerTeams = new HashMap<>(); // utility map for easily accessing player teams
    private final HashMap<Player, Scoreboard> playerScoreboards = new HashMap<>();
    private final HashMap<BlockPosition, FinePosition> clickGenerators;

    int maxPlayers;

    private boolean isStarted;

    public HeadWarsGame(World world, HeadWarsMap map) {

        this.world = world;
        this.map = map;
        this.teams = new ArrayList<>();
        this.players = new ArrayList<>();
        this.clickGenerators = new HashMap<>();

        initialise();

    }

    private void initialise() {

        Bukkit.getServer().getPluginManager().registerEvents(this, HeadWars.getInstance());

        map.teams().forEach((color, team) -> teams.add(new GameTeam(team, this))); //create game teams
        map.clickGenerators().forEach(generator -> clickGenerators.put(generator.left().asPosition(), generator.right().asPosition()));

        maxPlayers = teams.size() * map.getPlayersPerTeam();
        allPlayersAudience = Audience.audience(players);

    }

    private final BukkitRunnable runEverySecondTask = new BukkitRunnable() {

        public int gameTimer = 0;

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
                nextEvent = "Game End";
                nextEventTime = 1800;
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

                teams.forEach(gameTeam -> objective.getScore("team-" + gameTeam.mapTeam().getTeamColor()).customName(Component.text("█: ", Util.getNamedColor(gameTeam.mapTeam().getTeamColor())).append(gameTeam.getHeadStatusComponent())));

            });

            gameTimer ++;

        }

    };

    private int teamAddingNumber;
    public void addPlayer(Player player) {

        if (isStarted) return;
        if (players.size() == maxPlayers) { return; }

        teams.get(teamAddingNumber).players().add(player);
        playerTeams.put(player, teams.get(teamAddingNumber));
        teamAddingNumber++;
        if (teamAddingNumber >= teams.size()) teamAddingNumber = 0;
        players.add(player);
        HeadWarsGameManager.playersInGames.put(player, this);

        player.teleport(map.lobbySpawn().asPosition().toLocation(world));

        if (players.size() == maxPlayers) startCountdown();

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

        playerTeams.forEach((player, gameTeam) -> player.teleport(gameTeam.mapTeam().getSpawnPosition().asPosition().toLocation(world)));

        createScoreboard();

        runEverySecondTask.runTaskTimer(HeadWars.getInstance(), 0, 20);

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

    private void handleBasesOpenGameEvent() {

        allPlayersAudience.sendMessage(Component.text("Bases have been opened!", NamedTextColor.DARK_AQUA, TextDecoration.BOLD));

    }

    private void handleIncGenLimitGameEvent(int generators) {

        teams.forEach(team -> team.generatorLimit += generators);
        allPlayersAudience.sendMessage(Component.text("Generator limit has been increased by " + generators +"!", NamedTextColor.DARK_AQUA, TextDecoration.BOLD));

    }

    private void handleHeadsDestroyedGameEvent() {

    }

    public void handleGameStop() {

        players.forEach(player -> player.kick(MiniMessage.miniMessage().deserialize("<red>The game has abruptly stopped and a lobby hasn't been configured")));
        Bukkit.unloadWorld(world, false);

    }

    public boolean isBlockProtected(Block block) {

        for (Pair<SimpleBlockPos, SimpleBlockPos> area : map.protectedAreas()) {

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

            if ((block.getX() >= startX && block.getX() <= endX) &&
                (block.getY() >= startY && block.getY() <= endY) &&
                (block.getZ() >= startZ && block.getZ() <= endZ)
            ) return true;

        }

        return map.protectedBlocks().contains(SimpleBlockPos.fromLocation(block.getLocation()));

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

    @EventHandler
    public void onBlockRightClick(@NotNull PlayerInteractEvent event) {

        if (event.getClickedBlock() == null) return;
        if (!event.getClickedBlock().getWorld().equals(world)) return;

        Location location = event.getClickedBlock().getLocation();

        //If clicked the button generator
        if (clickGenerators.containsKey(Position.block(location))) {

            event.setCancelled(true);

            //Location buttonPos = location.toBlock().toLocation(world);
            Location itemSpawnPos = clickGenerators.get(Position.block(location)).toLocation(world);

            Item item = world.createEntity(itemSpawnPos, Item.class);
            item.setItemStack(new ItemStack(Material.IRON_INGOT));
            item.spawnAt(itemSpawnPos);

            //buttonPos.getBlock().setType(buttonPos.getBlock().getType());

        }

    }

    @EventHandler
    public void onRightClickEntity(@NotNull PlayerInteractEntityEvent event) {

        Entity entity = event.getRightClicked();

        if (!this.map.itemShops().containsKey(entity.getUniqueId())) return;

        event.setCancelled(true);

        ItemShop shop = ShopManager.itemShops.get(map.itemShops().get(entity.getUniqueId()));
        if (shop == null) return;
        shop.openShop(event.getPlayer());

    }

    @EventHandler
    public void onBlockDestroy(@NotNull BlockDestroyEvent event) {

        Block block = event.getBlock();
        if (block.getWorld() != world) return;
        if (isBlockProtected(block)) event.setCancelled(true);

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
                    team.unbrokenHeads().remove(block.getLocation().toBlock());
                    team.players().forEach(player -> player.showTitle(Title.title(Component.text("HEAD DESTROYED", NamedTextColor.RED), Component.text("You have " + team.unbrokenHeads().size() + "heads left!", NamedTextColor.RED))));
                }

            }

        });

    }

    @EventHandler
    public void onPlayerDeath(@NotNull PlayerDeathEvent event) {

        Player player = event.getPlayer();
        if (player.getLocation().getWorld() != world) return;

        event.setCancelled(true);

        player.setGameMode(GameMode.SPECTATOR);
        player.getInventory().clear(); // todo: Add inventory resetting logic

        if (!playerTeams.get(player).unbrokenHeads().isEmpty()) {

            Bukkit.getScheduler().runTaskLater(HeadWars.getInstance(), task -> {
                player.teleport(playerTeams.get(player).mapTeam().getSpawnPosition().asPosition().toLocation(world)); // todo: Add actual respawning logic
            }, 100);

            AtomicInteger secs = new AtomicInteger(5);
            Bukkit.getScheduler().runTaskTimer(HeadWars.getInstance(), task -> {

                player.showTitle(Title.title(Component.text("YOU DIED!", NamedTextColor.YELLOW), Component.text("Respawn in: " + secs)));
                player.playSound(Sound.sound().type(Key.key("minecraft:block.note_block.hat")).volume(1).build(), Sound.Emitter.self());
                secs.getAndDecrement();

                if (secs.get() == 0) task.cancel();

            }, 0, 20);

        } else {
            player.showTitle(Title.title(Component.text("YOU DIED!", NamedTextColor.YELLOW), Component.text("You can't respawn anymore", NamedTextColor.RED)));
        }

    }

}
