package win.codingboulder.headWars;

import org.bukkit.plugin.java.JavaPlugin;
import win.codingboulder.headWars.game.GeneratorType;
import win.codingboulder.headWars.game.HeadWarsGame;
import win.codingboulder.headWars.game.HeadWarsGameManager;
import win.codingboulder.headWars.game.ResourceGenerator;
import win.codingboulder.headWars.game.shop.ShopConfigGUI;
import win.codingboulder.headWars.game.shop.ShopGui;
import win.codingboulder.headWars.game.shop.ShopManager;
import win.codingboulder.headWars.maps.HeadWarsMapManager;
import win.codingboulder.headWars.util.Util;

import java.io.File;
import java.io.IOException;

public final class HeadWars extends JavaPlugin {

    private static HeadWars instance;

    private static File mapsFolder;
    private static File shopsFolder;
    private static File generatorsFolder;

    public static String serverName = "codingboulder.win";

    @Override
    public void onEnable() {

        instance = this;

        setupFiles();
        //noinspection UnstableApiUsage
        HeadWarsCommand.registerCommand(getLifecycleManager());

        getServer().getPluginManager().registerEvents(new ShopConfigGUI(), this);
        getServer().getPluginManager().registerEvents(new ShopGui(), this);
        getServer().getPluginManager().registerEvents(new ResourceGenerator(null, null), this);

        HeadWarsMapManager.loadAllMaps();
        ShopManager.loadAllShops();
        GeneratorType.reloadGeneratorTypes();

    }

    @Override
    public void onDisable() {

        HeadWarsGameManager.activeGames().forEach(HeadWarsGame::handleForceStop);

        HeadWarsGameManager.worldFolders.forEach(world -> {
            try { Util.deleteDirectory(world); } catch (IOException e) {throw new RuntimeException(e);}
        });

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

    public static HeadWars getInstance() {return instance;}

    public static File getMapsFolder() {return mapsFolder;}
    public static File getShopsFolder() {return shopsFolder;}
    public static File getGeneratorsFolder() {return generatorsFolder;}

}
