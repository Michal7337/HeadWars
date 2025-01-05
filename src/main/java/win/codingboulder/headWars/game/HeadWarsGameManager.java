package win.codingboulder.headWars.game;

import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.jetbrains.annotations.NotNull;
import win.codingboulder.headWars.HeadWars;
import win.codingboulder.headWars.maps.HeadWarsMap;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;

public class HeadWarsGameManager {

    private static final HashMap<Integer, HeadWarsGame> activeGames = new HashMap<>();
    private static int gameCount;

    public static void startGame(@NotNull HeadWarsMap map) {

        File mapWorld = new File(HeadWars.getInstance().getServer().getWorldContainer(), map.getWorld());
        if (!mapWorld.exists()) return;

        File gameWorld = new File(HeadWars.getInstance().getServer().getWorldContainer(), "headwars-game-" + gameCount);
        gameCount++;

        try {Files.copy(mapWorld.toPath(), gameWorld.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {throw new RuntimeException(e);}

        World world = HeadWars.getInstance().getServer().createWorld(WorldCreator.name(gameWorld.getName()));

        HeadWarsGame headWarsGame = new HeadWarsGame(world, map);
        activeGames.put(gameCount, headWarsGame);

    }

    public HashMap<Integer, HeadWarsGame> activeGames() {
        return activeGames;
    }

}
