package win.codingboulder.headWars;

import net.kyori.adventure.text.Component;
import org.bukkit.plugin.java.JavaPlugin;
import win.codingboulder.headWars.game.HeadWarsGame;
import win.codingboulder.headWars.maps.HeadWarsMapManager;

import java.io.File;

public final class HeadWars extends JavaPlugin {

    private static HeadWars instance;

    private static File mapsFolder;
    private static File shopsFolder;

    @Override
    public void onEnable() {

        instance = this;

        setupFiles();
        //noinspection UnstableApiUsage
        HeadWarsCommand.registerCommand(getLifecycleManager());

        getServer().getPluginManager().registerEvents(new HeadWarsGame(), this);

        HeadWarsMapManager.loadAllMaps();

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
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
