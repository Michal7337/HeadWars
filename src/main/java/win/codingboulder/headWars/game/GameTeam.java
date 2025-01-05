package win.codingboulder.headWars.game;

import io.papermc.paper.math.BlockPosition;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import win.codingboulder.headWars.maps.HeadWarsTeam;

import java.util.ArrayList;

@SuppressWarnings("UnstableApiUsage")
public class GameTeam {

    private final HeadWarsTeam mapTeam;
    private final ArrayList<Player> players;

    private final ArrayList<BlockPosition> unbrokenHeads;

    public GameTeam(@NotNull HeadWarsTeam mapTeam) {

        this.mapTeam = mapTeam;
        players = new ArrayList<>();
        unbrokenHeads = new ArrayList<>();

        unbrokenHeads.addAll(mapTeam.heads());

    }

    public HeadWarsTeam mapTeam() {
        return mapTeam;
    }

    public ArrayList<BlockPosition> unbrokenHeads() {
        return unbrokenHeads;
    }

    public ArrayList<Player> players() {
        return players;
    }

}
