package win.codingboulder.headWars.game;

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

    private static final HashMap<Integer, HeadWarsGame> activeGames = new HashMap<>();
    public static final HashMap<String, HeadWarsGame> activeGameNames = new HashMap<>();
    public static final HashMap<Player, HeadWarsGame> playersInGames = new HashMap<>();

    public static final ArrayList<File> worldFolders = new ArrayList<>();

    private static int gameCount;

    public static void startGame(@NotNull HeadWarsMap map) {

        File mapWorld = new File(HeadWars.getInstance().getServer().getWorldContainer(), map.getWorld());
        if (!mapWorld.exists()) return;

        File gameWorld = new File(HeadWars.getInstance().getServer().getWorldContainer(), "headwars-game-" + gameCount);

        try { Util.copyDirectory(mapWorld, gameWorld);
        } catch (IOException e) {throw new RuntimeException(e);}
        worldFolders.add(gameWorld);

        World world = HeadWars.getInstance().getServer().createWorld(WorldCreator.name(gameWorld.getName()));

        HeadWarsGame headWarsGame = new HeadWarsGame(world, map);
        activeGames.put(gameCount, headWarsGame);
        activeGameNames.put(map.getID() + "-" + gameCount, headWarsGame);

        gameCount++;

    }

    public static HashMap<Integer, HeadWarsGame> activeGames() {
        return activeGames;
    }

}
