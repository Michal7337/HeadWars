package win.codingboulder.headWars;

import org.bukkit.plugin.java.JavaPlugin;
import win.codingboulder.headWars.game.HeadWarsGame;
import win.codingboulder.headWars.maps.HeadWarsMapManager;

import java.io.File;

public final class HeadWars extends JavaPlugin {

    private static HeadWars instance;

    private static File mapsFolder;

    @Override
    public void onEnable() {

        instance = this;

        setupFiles();
        //noinspection UnstableApiUsage
        HeadWarsCommand.registerCommand(getLifecycleManager());

        getServer().getPluginManager().registerEvents(new HeadWarsGame(null, null), this);

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

    }

    public static HeadWars getInstance() {return instance;}

    public static File getMapsFolder() {return mapsFolder;}

}
