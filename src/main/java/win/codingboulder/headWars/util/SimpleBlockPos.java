package win.codingboulder.headWars.util;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.resolvers.BlockPositionResolver;
import io.papermc.paper.math.BlockPosition;
import io.papermc.paper.math.Position;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@SuppressWarnings("UnstableApiUsage")
public class SimpleBlockPos {

    private int x, y, z;

    public SimpleBlockPos(int x, int y, int z) {

        this.x = x;
        this.y = y;
        this.z = z;

    }

    public static @NotNull SimpleBlockPos pos(int x, int y, int z) {
        return new SimpleBlockPos(x, y, z);
    }

    public static @NotNull SimpleBlockPos pos(@NotNull BlockPosition position) {
        return new SimpleBlockPos(position.blockX(), position.blockY(), position.blockZ());
    }

    public static @NotNull SimpleBlockPos zero() {
        return new SimpleBlockPos(0, 0, 0);
    }

    public BlockPosition asPosition() {
        return Position.block(x, y, z);
    }

    public static @NotNull SimpleBlockPos fromCmdArgument(String argument, @NotNull CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        BlockPosition position = context.getArgument(argument, BlockPositionResolver.class).resolve(context.getSource());
        return new SimpleBlockPos(position.blockX(), position.blockY(), position.blockZ());
    }

    public Location asLocation(World world) {
        return new Location(world, x, y, z);
    }

    public Vector asBukkitVector() {
        return new Vector(x, y, z);
    }

    public static @NotNull SimpleBlockPos fromLocation(@NotNull Location location) {
        return new SimpleBlockPos(location.blockX(), location.blockY(), location.blockZ());
    }

    public Block getBlock(World world) {
        return new Location(world, x, y, z).getBlock();
    }

    public int x() {
        return x;
    }

    public void x(int x) {
        this.x = x;
    }

    public int y() {
        return y;
    }

    public void y(int y) {
        this.y = y;
    }

    public int z() {
        return z;
    }

    public void z(int z) {
        this.z = z;
    }

    @Override
    public String toString() {
        return "SimpleBlockPos{" +
            "x=" + x +
            ", y=" + y +
            ", z=" + z +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        SimpleBlockPos that = (SimpleBlockPos) o;
        return x == that.x && y == that.y && z == that.z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }

}
