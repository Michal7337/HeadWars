package win.codingboulder.headWars.maps;

import com.google.gson.GsonBuilder;
import org.bukkit.DyeColor;
import win.codingboulder.headWars.HeadWars;
import win.codingboulder.headWars.util.Pair;
import win.codingboulder.headWars.util.SimpleBlockPos;
import win.codingboulder.headWars.util.SimpleFinePos;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class HeadWarsMap {

    private final String ID;
    private String name;
    private String world;

    private SimpleFinePos lobbySpawn;
    private final Pair<SimpleFinePos, SimpleFinePos> mapBounds;

    private final ArrayList<SimpleBlockPos> protectedBlocks;
    private final ArrayList<Pair<SimpleBlockPos, SimpleBlockPos>> protectedAreas;
    private ArrayList<Pair<SimpleBlockPos, SimpleBlockPos>> noPlaceAreas;

    private int playersPerTeam;
    private final HashMap<DyeColor, HeadWarsTeam> teams;

    private final ArrayList<Pair<SimpleBlockPos, SimpleFinePos>> clickGenerators; // <ButtonPos, SpawnPos>

    private final ArrayList<SimpleBlockPos> emeraldGenerators;

    private final HashMap<UUID, String> itemShops;

    public HeadWarsMap(String ID, String name, String world, int playersPerTeam, HashMap<DyeColor, HeadWarsTeam> teams, ArrayList<Pair<SimpleBlockPos, SimpleFinePos>> clickGenerators, ArrayList<SimpleBlockPos> emeraldGenerators, HashMap<UUID, String> itemShops, SimpleFinePos lobbySpawn, Pair<SimpleFinePos, SimpleFinePos> mapBounds, ArrayList<SimpleBlockPos> protectedBlocks, ArrayList<Pair<SimpleBlockPos, SimpleBlockPos>> protectedAreas, ArrayList<Pair<SimpleBlockPos, SimpleBlockPos>> noPlaceAreas) {

        this.ID = ID;
        this.name = name;
        this.world = world;
        this.playersPerTeam = playersPerTeam;
        this.teams = teams;
        this.clickGenerators = clickGenerators;
        this.emeraldGenerators = emeraldGenerators;
        this.itemShops = itemShops;
        this.lobbySpawn = lobbySpawn;
        this.mapBounds = mapBounds;
        this.protectedBlocks = protectedBlocks;
        this.protectedAreas = protectedAreas;
        this.noPlaceAreas = noPlaceAreas;

    }

    public void createTeam(DyeColor teamColor) {

        HeadWarsTeam newTeam = new HeadWarsTeam(teamColor, SimpleFinePos.zero(), SimpleBlockPos.zero(), SimpleBlockPos.zero(), SimpleBlockPos.zero(), SimpleBlockPos.zero(), new ArrayList<>());
        teams.put(teamColor, newTeam);

    }

    public HeadWarsTeam getTeam(DyeColor teamColor) {

        return teams.get(teamColor);

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

    public HashMap<DyeColor, HeadWarsTeam> teams() {
        return teams;
    }

    public ArrayList<Pair<SimpleBlockPos, SimpleFinePos>> clickGenerators() {
        return clickGenerators;
    }

    public ArrayList<SimpleBlockPos> emeraldGenerators() {
        return emeraldGenerators;
    }

    public HashMap<UUID, String> itemShops() {
        return itemShops;
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

    public SimpleFinePos lobbySpawn() {
        return lobbySpawn;
    }

    public void lobbySpawn(SimpleFinePos lobbySpawn) {
        this.lobbySpawn = lobbySpawn;
    }

    public Pair<SimpleFinePos, SimpleFinePos> mapBounds() {
        return mapBounds;
    }

    public ArrayList<SimpleBlockPos> protectedBlocks() {
        return protectedBlocks;
    }

    public ArrayList<Pair<SimpleBlockPos, SimpleBlockPos>> protectedAreas() {
        return protectedAreas;
    }

    public void setNoPlaceAreas(ArrayList<Pair<SimpleBlockPos, SimpleBlockPos>> noPlaceAreas) {
        this.noPlaceAreas = noPlaceAreas;
    }

    public ArrayList<Pair<SimpleBlockPos, SimpleBlockPos>> noPlaceAreas() {
        return noPlaceAreas;
    }

}
