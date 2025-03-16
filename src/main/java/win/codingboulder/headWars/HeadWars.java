package win.codingboulder.headWars;

import org.bukkit.plugin.java.JavaPlugin;
import win.codingboulder.headWars.game.HeadWarsGame;
import win.codingboulder.headWars.game.HeadWarsGameManager;
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

    public static String serverName = "CodingBoulder";

    @Override
    public void onEnable() {

        instance = this;

        setupFiles();
        //noinspection UnstableApiUsage
        HeadWarsCommand.registerCommand(getLifecycleManager());

        getServer().getPluginManager().registerEvents(new ShopConfigGUI(), this);
        getServer().getPluginManager().registerEvents(new ShopGui(), this);

        HeadWarsMapManager.loadAllMaps();
        ShopManager.loadAllShops();


    }

    @Override
    public void onDisable() {

        HeadWarsGameManager.activeGames().forEach(HeadWarsGame::handleGameStop);

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

    }

    public static HeadWars getInstance() {return instance;}

    public static File getMapsFolder() {return mapsFolder;}
    public static File getShopsFolder() {return shopsFolder;}

}
