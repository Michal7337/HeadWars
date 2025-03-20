package win.codingboulder.headWars.game.shop.items;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Unbreakable;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import win.codingboulder.headWars.HeadWars;
import win.codingboulder.headWars.game.HeadWarsGame;
import win.codingboulder.headWars.game.shop.CustomShopItem;
import win.codingboulder.headWars.game.shop.ShopGui;

import java.util.List;

public class tool_pickaxe implements CustomShopItem {

    public ItemStack handleRender(@NotNull ItemStack shopItem, @NotNull Player player, HeadWarsGame game) {

        ItemStack item = ItemStack.of(Material.WOODEN_PICKAXE);
        item.editPersistentDataContainer(pdc -> {
            pdc.set(new NamespacedKey("headwars", "itemid"), PersistentDataType.STRING, "tool_pickaxe");
            pdc.set(new NamespacedKey("headwars", "shopaction"), PersistentDataType.STRING, "buy");
        });

        if (player.getInventory().contains(Material.WOODEN_PICKAXE)) {

            item = item.withType(Material.STONE_PICKAXE);
            item.lore(List.of(
                Component.text("Cost: ", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)
                    .append(Component.text("48 Iron", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false))
            ));

            item.editPersistentDataContainer(pdc -> pdc.set(
                new NamespacedKey("headwars", "shopprice"),
                PersistentDataType.BYTE_ARRAY, ItemStack.of(Material.IRON_INGOT, 48).serializeAsBytes())
            );

        } else if (player.getInventory().contains(Material.STONE_PICKAXE)) {

            item = item.withType(Material.IRON_PICKAXE);
            item.lore(List.of(
                Component.text("Cost: ", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)
                    .append(Component.text("32 Gold", NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false))
            ));

            item.editPersistentDataContainer(pdc -> pdc.set(
                new NamespacedKey("headwars", "shopprice"),
                PersistentDataType.BYTE_ARRAY, ItemStack.of(Material.GOLD_INGOT, 32).serializeAsBytes())
            );

        } else if (player.getInventory().contains(Material.IRON_PICKAXE)) {

            item = item.withType(Material.DIAMOND_PICKAXE);
            item.lore(List.of(
                Component.text("Cost: ", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)
                    .append(Component.text("24 Diamond", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false))
            ));

            item.editPersistentDataContainer(pdc -> pdc.set(
                new NamespacedKey("headwars", "shopprice"),
                PersistentDataType.BYTE_ARRAY, ItemStack.of(Material.DIAMOND, 24).serializeAsBytes())
            );

        } else {

            item.lore(List.of(
                Component.text("Cost: ", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)
                    .append(Component.text("24 Iron", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false))
            ));
            item.editPersistentDataContainer(pdc -> pdc.set(
                new NamespacedKey("headwars", "shopprice"),
                PersistentDataType.BYTE_ARRAY, ItemStack.of(Material.IRON_INGOT, 24).serializeAsBytes())
            );

        }

        return item;

    }

    public ItemStack handleBuy(@NotNull ItemStack shopItem, @NotNull Player player, Inventory inventory, HeadWarsGame game) {

        if (player.getInventory().contains(Material.WOODEN_PICKAXE)) {

            for (int i = 0; i < player.getInventory().getContents().length; i++) {
                ItemStack item = player.getInventory().getContents()[i];
                if (item != null && item.getType().equals(Material.WOODEN_PICKAXE)) {
                    player.getInventory().setItem(i, item.withType(Material.STONE_PICKAXE));
                    break;
                }
            }

        } else if (player.getInventory().contains(Material.STONE_PICKAXE)) {

            for (int i = 0; i < player.getInventory().getContents().length; i++) {
                ItemStack item = player.getInventory().getContents()[i];
                if (item != null && item.getType().equals(Material.STONE_PICKAXE)) {
                    player.getInventory().setItem(i, item.withType(Material.IRON_PICKAXE));
                    break;
                }
            }

        } else if (player.getInventory().contains(Material.IRON_PICKAXE)) {

            for (int i = 0; i < player.getInventory().getContents().length; i++) {
                ItemStack item = player.getInventory().getContents()[i];
                if (item != null && item.getType().equals(Material.IRON_PICKAXE)) {
                    player.getInventory().setItem(i, item.withType(Material.DIAMOND_PICKAXE));
                    break;
                }
            }

        } else {

            ItemStack item = ItemStack.of(Material.WOODEN_PICKAXE);
            //noinspection UnstableApiUsage
            item.setData(DataComponentTypes.UNBREAKABLE, Unbreakable.unbreakable(false));
            Bukkit.getScheduler().runTaskLater(HeadWars.getInstance(), task -> ShopGui.dummyGUI.reRenderItem(shopItem, player, inventory), 1);
            return item;

        }

        ShopGui.dummyGUI.reRenderItem(shopItem, player, inventory);

        return ItemStack.empty();

    }

}
