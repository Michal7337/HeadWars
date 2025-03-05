package win.codingboulder.headWars.game;

import io.papermc.paper.math.BlockPosition;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import win.codingboulder.headWars.maps.HeadWarsTeam;

import java.util.ArrayList;

@SuppressWarnings("UnstableApiUsage")
public class GameTeam {

    private final HeadWarsTeam mapTeam;
    private final HeadWarsGame game;
    private final ArrayList<Player> players;

    private final ArrayList<BlockPosition> unbrokenHeads;

    public GameTeam(@NotNull HeadWarsTeam mapTeam, HeadWarsGame game) {

        this.mapTeam = mapTeam;
        this.game = game;
        players = new ArrayList<>();
        unbrokenHeads = new ArrayList<>();

        mapTeam.heads().forEach(head -> unbrokenHeads.add(head.asPosition()));

    }

    public HeadWarsGame game() {
        return game;
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
