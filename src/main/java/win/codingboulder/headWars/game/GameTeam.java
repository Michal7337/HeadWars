package win.codingboulder.headWars.game;

import io.papermc.paper.math.BlockPosition;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import win.codingboulder.headWars.maps.HeadWarsTeam;
import win.codingboulder.headWars.util.SimpleBlockPos;

import java.util.ArrayList;

@SuppressWarnings("UnstableApiUsage")
public class GameTeam {

    private final HeadWarsTeam mapTeam;
    private final HeadWarsGame game;
    private final ArrayList<Player> players;

    public int generators = 0;
    public int generatorLimit = 10;

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

    public Component getHeadStatusComponent() {

        Component component = Component.text("");
        for (SimpleBlockPos head : mapTeam().heads()) if (unbrokenHeads.contains(head.asPosition())) component = component.append(Component.text("✔", NamedTextColor.GREEN, TextDecoration.BOLD));
        for (SimpleBlockPos head : mapTeam().heads()) if (!unbrokenHeads.contains(head.asPosition())) component = component.append(Component.text("✖", NamedTextColor.RED));
        component.append(Component.text(" " + players.size(), NamedTextColor.YELLOW));
        return component;

    }

}
