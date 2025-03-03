package win.codingboulder.headWars.game;

import io.papermc.paper.math.BlockPosition;
import io.papermc.paper.math.FinePosition;
import io.papermc.paper.math.Position;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.*;
import org.jetbrains.annotations.NotNull;
import win.codingboulder.headWars.HeadWars;
import win.codingboulder.headWars.maps.HeadWarsMap;

import java.util.ArrayList;
import java.util.HashMap;

@SuppressWarnings("UnstableApiUsage")
public class HeadWarsGame implements Listener {

    private final World world;
    private final HeadWarsMap map;

    private final ArrayList<GameTeam> teams;
    private final ArrayList<Player> players;
    private Audience allPlayersAudience;
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

    /**
     * An empty constructor ONLY FOR REGISTERING EVENTS, DO NOT USE
     */
    public HeadWarsGame() {
        this.world = null;
        this.map = null;
        this.teams = new ArrayList<>();
        this.players = new ArrayList<>();
        this.clickGenerators = new HashMap<>();
    }

    private void initialise() {

        map.teams().forEach((color, team) -> teams.add(new GameTeam(team))); //create game teams
        maxPlayers = teams.size() * map.getPlayersPerTeam();
        allPlayersAudience = Audience.audience(players);

        map.clickGenerators().forEach(generator -> clickGenerators.put(generator.left().asPosition(), generator.right().asPosition()));

    }

    private int teamAddingNumber;
    public void addPlayer(Player player) {

        if (isStarted) return;
        if (players.size() == maxPlayers) { startGame(); return; }

        teams.get(teamAddingNumber).players().add(player);
        teamAddingNumber++;
        if (teamAddingNumber >= teams.size()) teamAddingNumber = 0;
        players.add(player);

        player.teleport(map.lobbySpawn().asPosition().toLocation(world));

        if (players.size() == maxPlayers) startGame();

    }

    private void startGame() {

        isStarted = true;

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

    @EventHandler
    public void onBlockRightClick(@NotNull PlayerInteractEvent event) {

        if (event.getClickedBlock() == null) return;
        if (!event.getClickedBlock().getWorld().equals(world)) return;

        Location location = event.getClickedBlock().getLocation();

        //If clicked the button generator
        if (clickGenerators.containsKey(Position.block(location))) {

            Location buttonPos = location.toBlock().toLocation(world);
            Location itemSpawnPos = clickGenerators.get(Position.block(location)).toLocation(world);

            Item item = world.createEntity(itemSpawnPos, Item.class);
            item.setItemStack(new ItemStack(Material.IRON_INGOT));
            item.spawnAt(itemSpawnPos);

            buttonPos.getBlock().setType(buttonPos.getBlock().getType());

        }

    }

}
