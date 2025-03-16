package win.codingboulder.headWars.game;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import win.codingboulder.headWars.HeadWars;
import win.codingboulder.headWars.maps.HeadWarsMap;
import win.codingboulder.headWars.util.Util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class HeadWarsGameManager {

    private static final ArrayList<HeadWarsGame> activeGames = new ArrayList<>();
    public static final HashMap<String, HeadWarsGame> activeGameNames = new HashMap<>();
    public static final HashMap<Player, HeadWarsGame> playersInGames = new HashMap<>();

    public static final ArrayList<File> worldFolders = new ArrayList<>();

    private static int gameCount = -1;

    public static void startGame(@NotNull HeadWarsMap map) {

        gameCount++;

        File mapWorld = new File(HeadWars.getInstance().getServer().getWorldContainer(), map.getWorld());
        if (!mapWorld.exists()) return;

        File gameWorld = new File(HeadWars.getInstance().getServer().getWorldContainer(), "headwars-game-" + gameCount);

        World w = Bukkit.getWorld(map.getWorld());
        if (w != null) Bukkit.unloadWorld(w, true);

        try { Util.copyDirectory(mapWorld, gameWorld);
        } catch (IOException e) {HeadWars.getInstance().getLogger().warning("Failed to copy map world!");}
        worldFolders.add(gameWorld);
        new File(gameWorld, "uid.dat").delete(); // delete this file because bukkit complains

        World world = HeadWars.getInstance().getServer().createWorld(WorldCreator.name(gameWorld.getName()));
        if (world == null) {
            HeadWars.getInstance().getLogger().warning("Failed to load world for game 'headwars-game-" + gameCount +"'!");
            return;
        }

        HeadWarsGame headWarsGame = new HeadWarsGame(world, map, gameWorld);
        activeGames.add(headWarsGame);
        activeGameNames.put(map.getID() + "-" + gameCount, headWarsGame);

    }

    public static ArrayList<HeadWarsGame> activeGames() {
        return activeGames;
    }

}
