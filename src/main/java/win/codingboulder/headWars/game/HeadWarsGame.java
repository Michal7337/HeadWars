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
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.TitlePart;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.*;
import org.jetbrains.annotations.NotNull;
import win.codingboulder.headWars.HeadWars;
import win.codingboulder.headWars.game.shop.ItemShop;
import win.codingboulder.headWars.game.shop.ShopManager;
import win.codingboulder.headWars.maps.HeadWarsMap;
import win.codingboulder.headWars.util.Pair;
import win.codingboulder.headWars.util.SimpleBlockPos;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;

@SuppressWarnings("UnstableApiUsage")
public class HeadWarsGame implements Listener {

    private final HeadWars plugin = HeadWars.getInstance();

    private final World world;
    private final HeadWarsMap map;

    private final ArrayList<GameTeam> teams;
    private final ArrayList<Player> players;
    private Audience allPlayersAudience;

    private final HashMap<Player, GameTeam> playerTeams = new HashMap<>(); // utility map for easily accessing player teams
    private final HashMap<BlockPosition, FinePosition> clickGenerators;
    //private final ArrayList<Block> protectedBlocks = new ArrayList<>();

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

            //allPlayersAudience.playSound(Sound.sound().type(Key.key("minecraft:block.note_block.hat")).volume(5).build(), Sound.Emitter.self());
            players.forEach(player -> player.playSound(Sound.sound().type(Key.key("minecraft:block.note_block.hat")).volume(5).build(), Sound.Emitter.self()));

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

    }

    private void createScoreboard() {

        Scoreboard scoreboard = HeadWars.getInstance().getServer().getScoreboardManager().getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("headwars", Criteria.DUMMY, MiniMessage.miniMessage().deserialize("<gradient:gold:yellow>HeadWars</gradient>"));

        //objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        teams.forEach(team -> {

            Team scTeam = scoreboard.registerNewTeam(team.mapTeam().getTeamColor().toString());
            scTeam.addEntities(team.players().toArray(new Player[0]));
            scTeam.setAllowFriendlyFire(true);
            scTeam.setCanSeeFriendlyInvisibles(true);

        });



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

    }

}
