package win.codingboulder.headWars.util;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.resolvers.FinePositionResolver;
import io.papermc.paper.math.FinePosition;
import io.papermc.paper.math.Position;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@SuppressWarnings("UnstableApiUsage")
public class SimpleFinePos {

    private double x, y, z;

    public SimpleFinePos(double x, double y, double z) {

        this.x = x;
        this.y = y;
        this.z = z;

    }

    public FinePosition asPosition() {
        return Position.fine(x, y, z);
    }

    public static @NotNull SimpleFinePos pos(double x, double y, double z) {
        return new SimpleFinePos(x, y, z);
    }

    public static @NotNull SimpleFinePos pos(@NotNull FinePosition position) {
        return new SimpleFinePos(position.x(), position.y(), position.z());
    }

    public static @NotNull SimpleFinePos zero() {
        return new SimpleFinePos(0, 0, 0);
    }

    public static @NotNull SimpleFinePos fromCmdArgument(String argument, @NotNull CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        FinePosition position = context.getArgument(argument, FinePositionResolver.class).resolve(context.getSource());
        return new SimpleFinePos(position.x(), position.y(), position.z());
    }

    public double x() {
        return x;
    }

    public void x(double x) {
        this.x = x;
    }

    public double y() {
        return y;
    }

    public void y(double y) {
        this.y = y;
    }

    public double z() {
        return z;
    }

    public void z(double z) {
        this.z = z;
    }

    @Override
    public String toString() {
        return "{\"x\": " + x + ",\"y\": " + y + ",\"z\": " + z + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        SimpleFinePos that = (SimpleFinePos) o;
        return Double.compare(x, that.x) == 0 && Double.compare(y, that.y) == 0 && Double.compare(z, that.z) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }

}
