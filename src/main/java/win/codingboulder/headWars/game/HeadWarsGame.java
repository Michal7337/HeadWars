package win.codingboulder.headWars.game;

import io.papermc.paper.math.Position;
import net.kyori.adventure.audience.Audience;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import win.codingboulder.headWars.maps.HeadWarsMap;

import java.util.ArrayList;

@SuppressWarnings("UnstableApiUsage")
public class HeadWarsGame implements Listener {

    private final World world;
    private final HeadWarsMap map;

    private final ArrayList<GameTeam> teams;
    private final ArrayList<Player> players;
    private Audience allPlayersAudience;

    int maxPlayers;

    private boolean isStarted;

    public HeadWarsGame(World world, HeadWarsMap map) {

        this.world = world;
        this.map = map;
        this.teams = new ArrayList<>();
        this.players = new ArrayList<>();

        initialise();

    }

    private void initialise() {

        map.teams().forEach(team -> teams.add(new GameTeam(team))); //create game teams
        maxPlayers = teams.size() * map.getPlayersPerTeam();
        allPlayersAudience = Audience.audience(players);

    }

    private int teamAddingNumber;
    public void addPlayer(Player player) {

        if (isStarted) return;
        if (players.size() == maxPlayers) { startGame(); return; }

        teams.get(teamAddingNumber).players().add(player);
        teamAddingNumber++;
        if (teamAddingNumber >= teams.size()) teamAddingNumber = 0;
        players.add(player);

        player.teleport(map.lobbySpawn().toLocation(world));

        if (players.size() == maxPlayers) startGame();

    }

    private void startGame() {

        isStarted = true;

    }

    private void updateScoreboard() {



    }

    @EventHandler
    public void onBlockRightClick(@NotNull PlayerInteractEvent event) {

        if (event.getClickedBlock() == null) return;
        if (!event.getClickedBlock().getWorld().equals(world)) return;

        Location location = event.getClickedBlock().getLocation();

        //If clicked the button generator
        if (map.clickGenerators().containsKey(location.toBlock())) {

            Location buttonPos = location.toBlock().toLocation(world);
            Location itemSpawnPos = map.clickGenerators().get(location.toBlock()).toLocation(world);

            Item item = world.createEntity(itemSpawnPos, Item.class);
            item.setItemStack(new ItemStack(Material.IRON_INGOT));
            item.spawnAt(itemSpawnPos);

            buttonPos.getBlock().setType(buttonPos.getBlock().getType());

        }

    }

}
