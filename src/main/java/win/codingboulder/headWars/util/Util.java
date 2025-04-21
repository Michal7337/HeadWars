package win.codingboulder.headWars.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Comparator;
import java.util.stream.Stream;

public class Util {

    public static void copyDirectory(@NotNull File source, @NotNull File destination) throws IOException {
        String sourceDirectoryLocation = source.getAbsolutePath();
        String destinationDirectoryLocation = destination.getAbsolutePath();
        Files.walk(Paths.get(sourceDirectoryLocation))
            .forEach(sourceI -> {
                Path destinationI = Paths.get(destinationDirectoryLocation, sourceI.toString()
                    .substring(sourceDirectoryLocation.length()));
                try {
                    Files.copy(sourceI, destinationI);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

    }

    public static void deleteDirectory(@NotNull File directory) throws IOException {
        Path pathToBeDeleted = Path.of(directory.toURI());
        try (Stream<Path> paths = Files.walk(pathToBeDeleted)) {
            paths.sorted(Comparator.reverseOrder()).forEach(path -> path.toFile().delete());
        }
    }

    public static Material getWoolFromColor(@NotNull DyeColor color) {

        String materialName = color + "_WOOL";
        return Material.getMaterial(materialName);

    }

    public static TextColor getNamedColor(@NotNull DyeColor color) {

        TextColor textColor;

        switch (color) {
            case WHITE -> textColor = TextColor.fromHexString("#F9FFFE");
            case RED -> textColor = TextColor.fromHexString("#B02E26");
            case BLUE -> textColor = TextColor.fromHexString("#3C44AA");
            case CYAN -> textColor = TextColor.fromHexString("#169C9C");
            case GRAY -> textColor = TextColor.fromHexString("#474F52");
            case LIME -> textColor = TextColor.fromHexString("#80C71F");
            case PINK -> textColor = TextColor.fromHexString("#F38BAA");
            case BLACK -> textColor = TextColor.fromHexString("#1D1D21");
            case BROWN -> textColor = TextColor.fromHexString("#835432");
            case GREEN -> textColor = TextColor.fromHexString("#5E7C16");
            case ORANGE -> textColor = TextColor.fromHexString("#F9801D");
            case PURPLE -> textColor = TextColor.fromHexString("#8932B8");
            case YELLOW -> textColor = TextColor.fromHexString("#FED83D");
            case MAGENTA -> textColor = TextColor.fromHexString("#C74EBD");
            case LIGHT_BLUE -> textColor = TextColor.fromHexString("#3AB3DA");
            case LIGHT_GRAY -> textColor = TextColor.fromHexString("#9D9D97");
            default -> textColor = NamedTextColor.WHITE;
        }

        return textColor;

    }

    public static boolean isWool(@NotNull Block block) {
        return block.getType().toString().contains("_WOOL");
    }

    public static Component getItemCostComponent(@NotNull ItemStack cost) {

        return getItemCostComponent(cost, cost.getAmount());

    }

    public static Component getItemCostComponent(@NotNull ItemStack cost, int amount) {

        Component component;

        switch (cost.getType()) {

            case IRON_INGOT -> component = Component.text("Cost: ", NamedTextColor.GRAY).append(Component.text(amount + " iron", NamedTextColor.WHITE)).decoration(TextDecoration.ITALIC, false);
            case GOLD_INGOT -> component = Component.text("Cost: ", NamedTextColor.GRAY).append(Component.text(amount + " gold", NamedTextColor.GOLD)).decoration(TextDecoration.ITALIC, false);
            case DIAMOND -> component = Component.text("Cost: ", NamedTextColor.GRAY).append(Component.text(amount + " diamond", NamedTextColor.AQUA)).decoration(TextDecoration.ITALIC, false);
            default -> component = Component.text("Cost: ", NamedTextColor.GRAY).append(Component.text(amount + " ")).append(cost.displayName()).decoration(TextDecoration.ITALIC, false);

        }

        return component;

    }

    public static String encodeItemStack(ItemStack item) {
        if (item == null) return "";
        return Base64.getEncoder().encodeToString(item.serializeAsBytes());
    }

    public static ItemStack decodeItemStack(String base64) {
        if (base64 == null || base64.isEmpty()) return ItemStack.empty();
        return ItemStack.deserializeBytes(Base64.getDecoder().decode(base64));
    }

}
