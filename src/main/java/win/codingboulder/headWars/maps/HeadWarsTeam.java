package win.codingboulder.headWars.maps;

import io.papermc.paper.math.BlockPosition;
import io.papermc.paper.math.FinePosition;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.ArrayList;

@SuppressWarnings("UnstableApiUsage")
public class HeadWarsTeam {

    private NamedTextColor teamColor;
    private FinePosition spawnPosition;

    private BlockPosition basePos1, basePos2;
    private BlockPosition basePerimeterPos1, basePerimeterPos2;

    private final ArrayList<BlockPosition> heads;

    public HeadWarsTeam(NamedTextColor teamColor, FinePosition spawnPosition, BlockPosition basePos1, BlockPosition basePos2, BlockPosition basePerimeterPos1, BlockPosition basePerimeterPos2, ArrayList<BlockPosition> heads) {

        this.teamColor = teamColor;
        this.spawnPosition = spawnPosition;
        this.basePos1 = basePos1;
        this.basePos2 = basePos2;
        this.basePerimeterPos1 = basePerimeterPos1;
        this.basePerimeterPos2 = basePerimeterPos2;
        this.heads = heads;

    }

    public NamedTextColor getTeamColor() {
        return teamColor;
    }

    public FinePosition getSpawnPosition() {
        return spawnPosition;
    }

    public BlockPosition getBasePos1() {
        return basePos1;
    }

    public BlockPosition getBasePos2() {
        return basePos2;
    }

    public BlockPosition getBasePerimeterPos1() {
        return basePerimeterPos1;
    }

    public BlockPosition getBasePerimeterPos2() {
        return basePerimeterPos2;
    }

    public ArrayList<BlockPosition> heads() {
        return heads;
    }

    public void setTeamColor(NamedTextColor teamColor) {
        this.teamColor = teamColor;
    }

    public void setSpawnPosition(FinePosition spawnPosition) {
        this.spawnPosition = spawnPosition;
    }

    public void setBasePosition(BlockPosition corner1, BlockPosition corner2) {
        basePos1 = corner1;
        basePos2 = corner2;
    }

    public void setBasePerimeter(BlockPosition corner1, BlockPosition corner2) {
        basePerimeterPos1 = corner1;
        basePerimeterPos2= corner2;
    }

    public void setBasePos1(BlockPosition basePos1) {
        this.basePos1 = basePos1;
    }

    public void setBasePos2(BlockPosition basePos2) {
        this.basePos2 = basePos2;
    }

    public void setBasePerimeterPos1(BlockPosition basePerimeterPos1) {
        this.basePerimeterPos1 = basePerimeterPos1;
    }

    public void setBasePerimeterPos2(BlockPosition basePerimeterPos2) {
        this.basePerimeterPos2 = basePerimeterPos2;
    }

}
