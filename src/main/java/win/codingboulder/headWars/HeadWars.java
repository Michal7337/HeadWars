package win.codingboulder.headWars;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import win.codingboulder.headWars.game.GeneratorType;
import win.codingboulder.headWars.game.HeadWarsGame;
import win.codingboulder.headWars.game.HeadWarsGameManager;
import win.codingboulder.headWars.game.ResourceGenerator;
import win.codingboulder.headWars.game.shop.ItemShop;
import win.codingboulder.headWars.game.shop.ShopConfigGUI;
import win.codingboulder.headWars.game.shop.ShopGui;
import win.codingboulder.headWars.maps.HeadWarsMapManager;

import java.io.File;

public final class HeadWars extends JavaPlugin {

    private static HeadWars instance;

    private static File mapsFolder;
    private static File shopsFolder;
    private static File generatorsFolder;

    public static String serverName;

    @Override
    public void onEnable() {

        instance = this;

        setupFiles();
        //noinspection UnstableApiUsage
        HeadWarsCommand.registerCommand(getLifecycleManager());

        getServer().getPluginManager().registerEvents(new ShopConfigGUI(null), this);
        getServer().getPluginManager().registerEvents(new ShopGui(null ,null), this);
        getServer().getPluginManager().registerEvents(new ResourceGenerator(null, null), this);

        HeadWarsMapManager.loadAllMaps();
        ItemShop.loadAllShops();
        GeneratorType.reloadGeneratorTypes();

        loadConfig();

    }

    @Override
    public void onDisable() {

        HeadWarsGameManager.activeGames().forEach(HeadWarsGame::handleForceStop);

    }

    private void setupFiles() {

        if (getDataFolder().mkdir()) getLogger().info("Created plugin data folder");

        mapsFolder = new File(getDataFolder(), "maps");
        if (mapsFolder.mkdir()) getLogger().info("Created maps folder");

        shopsFolder = new File(getDataFolder(), "shops");
        if (shopsFolder.mkdir()) getLogger().info("Created shops folder");

        generatorsFolder = new File(getDataFolder(), "generators");
        if (generatorsFolder.mkdir()) getLogger().info("Created generators folder");

    }

    public static String gameEndAction;
    public static Location gameEndTpLocation;
    public static boolean isGameChatEnabled;
    public static boolean clearInventoriesBeforeGame;

    public void loadConfig() {

        saveDefaultConfig();
        FileConfiguration config = getConfig();

        String gameEndAction = config.getString("headwars.game-end.action", "kick");
        String gameEndTpWorld = config.getString("headwars.game-end.location.world", "world");
        int gameEndTpX = config.getInt("headwars.game-end.location.x", 0);
        int gameEndTpY = config.getInt("headwars.game-end.location.x", 100);
        int gameEndTpZ = config.getInt("headwars.game-end.location.x", 0);
        Location gameEndLocation;

        if (gameEndAction.equals("teleport")) {

            World gameEndWorld = Bukkit.createWorld(WorldCreator.name(gameEndTpWorld));
            gameEndLocation = new Location(gameEndWorld, gameEndTpX, gameEndTpY, gameEndTpZ);

        } else {

            gameEndAction = "kick";
            gameEndLocation = null;

        }

        HeadWars.gameEndAction = gameEndAction;
        gameEndTpLocation = gameEndLocation;

        serverName = config.getString("headwars.server-name", "codingboulder.win");
        isGameChatEnabled = config.getBoolean("headwars.enable-game-chat", true);
        clearInventoriesBeforeGame = config.getBoolean("headwars.clear-inventories-before-game", true);

    }

    public static HeadWars getInstance() {return instance;}

    public static File getMapsFolder() {return mapsFolder;}
    public static File getShopsFolder() {return shopsFolder;}
    public static File getGeneratorsFolder() {return generatorsFolder;}

}
