package win.codingboulder.headWars.maps;

import com.google.gson.GsonBuilder;
import io.papermc.paper.math.BlockPosition;
import io.papermc.paper.math.FinePosition;
import io.papermc.paper.math.Position;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.commons.lang3.tuple.Pair;
import win.codingboulder.headWars.HeadWars;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

@SuppressWarnings("UnstableApiUsage")
public class HeadWarsMap {

    private final String ID;
    private String name;
    private String world;

    private FinePosition lobbySpawn;
    private Pair<FinePosition, FinePosition> mapBounds;
    private ArrayList<BlockPosition> unprotectedBlocks;
    private ArrayList<BlockPosition> protectedBlocks;

    private int playersPerTeam;
    private final ArrayList<HeadWarsTeam> teams;

    // <ButtonPos, SpawnPos>
    private final HashMap<BlockPosition, FinePosition> clickGenerators;

    private final ArrayList<BlockPosition> emeraldGenerators;

    private final ArrayList<UUID> itemShops;
    private final ArrayList<UUID> upgradeShops;

    public HeadWarsMap(String ID, String name, String world, int playersPerTeam, ArrayList<HeadWarsTeam> teams, HashMap<BlockPosition, FinePosition> clickGenerators, ArrayList<BlockPosition> emeraldGenerators, ArrayList<UUID> itemShops, ArrayList<UUID> upgradeShops, FinePosition lobbySpawn, Pair<FinePosition, FinePosition> mapBounds, ArrayList<BlockPosition> unprotectedBlocks, ArrayList<BlockPosition> protectedBlocks) {

        this.ID = ID;
        this.name = name;
        this.world = world;
        this.playersPerTeam = playersPerTeam;
        this.teams = teams;
        this.clickGenerators = clickGenerators;
        this.emeraldGenerators = emeraldGenerators;
        this.itemShops = itemShops;
        this.upgradeShops = upgradeShops;
        this.lobbySpawn = lobbySpawn;
        this.mapBounds = mapBounds;
        this.unprotectedBlocks = unprotectedBlocks;
        this.protectedBlocks = protectedBlocks;

    }

    public void createTeam(NamedTextColor teamColor) {

        HeadWarsTeam newTeam = new HeadWarsTeam(teamColor, Position.FINE_ZERO, Position.BLOCK_ZERO, Position.BLOCK_ZERO, Position.BLOCK_ZERO, Position.BLOCK_ZERO, new ArrayList<>());
        teams.add(newTeam);

    }

    public HeadWarsTeam getTeam(NamedTextColor teamColor) {

        for (HeadWarsTeam team : teams) if (team.getTeamColor() == teamColor) return team;

        return null;

    }

    public void updateMapFile() {

        File mapFile = new File(HeadWars.getMapsFolder(), ID + ".json");

        try {
            FileWriter writer = new FileWriter(mapFile);
            new GsonBuilder().setPrettyPrinting().create().toJson(this, writer);
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public String getID() {
        return ID;
    }

    public String getName() {
        return name;
    }

    public String getWorld() {
        return world;
    }

    public int getPlayersPerTeam() {
        return playersPerTeam;
    }

    public ArrayList<HeadWarsTeam> teams() {
        return teams;
    }

    public HashMap<BlockPosition, FinePosition> clickGenerators() {
        return clickGenerators;
    }

    public ArrayList<BlockPosition> emeraldGenerators() {
        return emeraldGenerators;
    }

    public ArrayList<UUID> itemShops() {
        return itemShops;
    }

    public ArrayList<UUID> upgradeShops() {
        return upgradeShops;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setWorld(String world) {
        this.world = world;
    }

    public void setPlayersPerTeam(int playersPerTeam) {
        this.playersPerTeam = playersPerTeam;
    }

    public FinePosition lobbySpawn() {
        return lobbySpawn;
    }

    public void lobbySpawn(FinePosition lobbySpawn) {
        this.lobbySpawn = lobbySpawn;
    }

}
