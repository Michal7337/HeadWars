package win.codingboulder.headWars.maps;

import org.bukkit.DyeColor;
import win.codingboulder.headWars.util.Pair;
import win.codingboulder.headWars.util.SimpleBlockPos;
import win.codingboulder.headWars.util.SimpleFinePos;

import java.util.ArrayList;

public class HeadWarsTeam {

    private DyeColor teamColor;
    private SimpleFinePos spawnPosition;

    private SimpleBlockPos basePos1, basePos2;
    private SimpleBlockPos basePerimeterPos1, basePerimeterPos2;

    private final ArrayList<SimpleBlockPos> heads;

    public HeadWarsTeam(DyeColor teamColor, SimpleFinePos spawnPosition, SimpleBlockPos basePos1, SimpleBlockPos basePos2, SimpleBlockPos basePerimeterPos1, SimpleBlockPos basePerimeterPos2, ArrayList<SimpleBlockPos> heads) {

        this.teamColor = teamColor;
        this.spawnPosition = spawnPosition;
        this.basePos1 = basePos1;
        this.basePos2 = basePos2;
        this.basePerimeterPos1 = basePerimeterPos1;
        this.basePerimeterPos2 = basePerimeterPos2;
        this.heads = heads;

    }

    public DyeColor getTeamColor() {
        return teamColor;
    }

    public SimpleFinePos getSpawnPosition() {
        return spawnPosition;
    }

    public SimpleBlockPos getBasePos1() {
        return basePos1;
    }

    public SimpleBlockPos getBasePos2() {
        return basePos2;
    }

    public SimpleBlockPos getBasePerimeterPos1() {
        return basePerimeterPos1;
    }

    public SimpleBlockPos getBasePerimeterPos2() {
        return basePerimeterPos2;
    }

    public Pair<SimpleBlockPos, SimpleBlockPos> getBasePerimeter() {
        return Pair.of(basePerimeterPos1, basePerimeterPos2);
    }

    public ArrayList<SimpleBlockPos> heads() {
        return heads;
    }

    public void setTeamColor(DyeColor teamColor) {
        this.teamColor = teamColor;
    }

    public void setSpawnPosition(SimpleFinePos spawnPosition) {
        this.spawnPosition = spawnPosition;
    }

    public void setBasePosition(SimpleBlockPos corner1, SimpleBlockPos corner2) {
        basePos1 = corner1;
        basePos2 = corner2;
    }

    public void setBasePerimeter(SimpleBlockPos corner1, SimpleBlockPos corner2) {
        basePerimeterPos1 = corner1;
        basePerimeterPos2= corner2;
    }

    public void setBasePos1(SimpleBlockPos basePos1) {
        this.basePos1 = basePos1;
    }

    public void setBasePos2(SimpleBlockPos basePos2) {
        this.basePos2 = basePos2;
    }

    public void setBasePerimeterPos1(SimpleBlockPos basePerimeterPos1) {
        this.basePerimeterPos1 = basePerimeterPos1;
    }

    public void setBasePerimeterPos2(SimpleBlockPos basePerimeterPos2) {
        this.basePerimeterPos2 = basePerimeterPos2;
    }

}
