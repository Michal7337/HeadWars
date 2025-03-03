package win.codingboulder.headWars.maps;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import win.codingboulder.headWars.HeadWars;
import win.codingboulder.headWars.util.Pair;
import win.codingboulder.headWars.util.SimpleFinePos;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class HeadWarsMapManager {

    private static final HashMap<String, HeadWarsMap> loadedMaps = new HashMap<>();
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private HeadWarsMapManager() {}

    public static void createMap(String id, String name, String world) throws IOException {

        HeadWarsMap newMap = new HeadWarsMap(
                id,
                name,
                world,
                1,
                new HashMap<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new HashMap<>(),
                new HashMap<>(),
                SimpleFinePos.pos(0, 100, 0),
                Pair.of(SimpleFinePos.pos(500, 300, 500), SimpleFinePos.pos(-500, -60, -500)),
                new ArrayList<>(),
                new ArrayList<>()
        );

        File mapFile = new File(HeadWars.getMapsFolder(), id + ".json");
        mapFile.createNewFile();
        FileWriter writer = new FileWriter(mapFile);

        gson.toJson(newMap, writer);
        writer.close();

        loadedMaps.put(id, newMap);

    }

    public static HeadWarsMap loadMap(String id) throws FileNotFoundException {

        File mapFile = new File(HeadWars.getMapsFolder(), id + ".json");
        HeadWarsMap loadedMap = gson.fromJson(new FileReader(mapFile), HeadWarsMap.class);
        loadedMaps.put(id, loadedMap);

        return loadedMap;

    }

    public static void loadAllMaps() {

        if (HeadWars.getMapsFolder().listFiles() == null) return;

        for (File mapFile : Objects.requireNonNull(HeadWars.getMapsFolder().listFiles())) {

            if (!mapFile.isFile()) continue;

            HeadWarsMap headWarsMap;

            try {
                headWarsMap = gson.fromJson(new FileReader(mapFile), HeadWarsMap.class);
            } catch (FileNotFoundException e) {
                continue;
            } catch (JsonParseException e) {
                HeadWars.getInstance().getLogger().warning("Could not load map '" + mapFile.getName() + "'!");
                continue;
            }

            if (headWarsMap == null) continue;
            if (headWarsMap.getID() == null || headWarsMap.getID().isEmpty()) continue;

            loadedMaps.put(headWarsMap.getID(), headWarsMap);

        }

    }

    public static HeadWarsMap getMap(String id) {

        return loadedMaps().get(id);

    }

    public static HashMap<String, HeadWarsMap> loadedMaps() {return loadedMaps;}

}
