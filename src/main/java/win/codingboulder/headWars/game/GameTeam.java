package win.codingboulder.headWars.game;

import io.papermc.paper.math.BlockPosition;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import win.codingboulder.headWars.maps.HeadWarsTeam;
import win.codingboulder.headWars.util.SimpleBlockPos;
import win.codingboulder.headWars.util.Util;

import java.util.ArrayList;
import java.util.HashMap;

@SuppressWarnings("UnstableApiUsage")
public class GameTeam {

    private final HeadWarsTeam mapTeam;
    private final HeadWarsGame game;
    private final ArrayList<Player> players;

    public int generators = 0;
    public int generatorLimit = 10;

    private final ArrayList<BlockPosition> unbrokenHeads;
    public final HashMap<String, Integer> purchasedUpgrades = new HashMap<>();

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
        component = component.append(Component.text(" " + players.size(), NamedTextColor.YELLOW));
        return component;

    }

    public String getTeamName() {
        return mapTeam.getTeamColor().toString().replaceAll("_", " ");
    }

    public Component getColoredTeamName() {
        return Component.text(mapTeam.getTeamColor().toString().toLowerCase(), Util.getNamedColor(mapTeam.getTeamColor()));
    }

    public Component getColoredTeamName(boolean uppercase) {
        return uppercase ? Component.text(mapTeam.getTeamColor().toString(), Util.getNamedColor(mapTeam.getTeamColor())) : Component.text(mapTeam.getTeamColor().toString().toLowerCase(), Util.getNamedColor(mapTeam.getTeamColor()));
    }

    public TextColor getTeamTextColor() {
        return Util.getNamedColor(mapTeam.getTeamColor());
    }

}
