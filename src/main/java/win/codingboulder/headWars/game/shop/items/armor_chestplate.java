package win.codingboulder.headWars.game.shop.items;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.DyedItemColor;
import io.papermc.paper.datacomponent.item.ItemAttributeModifiers;
import io.papermc.paper.datacomponent.item.ItemLore;
import io.papermc.paper.datacomponent.item.Unbreakable;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import win.codingboulder.headWars.game.HeadWarsGame;
import win.codingboulder.headWars.game.shop.CustomShopItem;
import win.codingboulder.headWars.game.shop.ShopGui;

import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class armor_chestplate implements CustomShopItem {

    public ItemStack handleRender(@NotNull ItemStack shopItem, @NotNull Player player, HeadWarsGame game) {

        ItemStack item = ItemStack.of(Material.CHAINMAIL_CHESTPLATE);
        item.setData(DataComponentTypes.UNBREAKABLE, Unbreakable.unbreakable(false));
        item.setData(DataComponentTypes.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.itemAttributes().showInTooltip(false).build());

        item.editPersistentDataContainer(pdc -> {
            pdc.set(new NamespacedKey("headwars", "shopaction"), PersistentDataType.STRING, "buy");
            pdc.set(new NamespacedKey("headwars", "itemid"), PersistentDataType.STRING, "armor_chestplate");
        });

        ItemStack price = ItemStack.of(Material.AIR);
        Component costComponentStart = Component.text("Cost: ", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false);
        Component costComponent;
        Material currentTier; if (player.getInventory().getChestplate() == null) currentTier = null; else currentTier = player.getInventory().getChestplate().getType();

        if (currentTier == null) {

            item = item.withType(Material.LEATHER_CHESTPLATE);
            item.setData(DataComponentTypes.DYED_COLOR, DyedItemColor.dyedItemColor(game.playerTeams().get(player).mapTeam().getTeamColor().getColor(), false));
            costComponent = Component.text("free!", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false);

        } else if (currentTier == Material.LEATHER_CHESTPLATE) {

            costComponent = Component.text("24 Iron", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false);
            price = ItemStack.of(Material.IRON_INGOT, 24);

        } else if (currentTier == Material.CHAINMAIL_CHESTPLATE) {

            item = item.withType(Material.IRON_CHESTPLATE);
            price = ItemStack.of(Material.GOLD_INGOT, 24);
            costComponent = Component.text("24 Gold", NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false);

        } else if (currentTier == Material.IRON_CHESTPLATE) {
            item = item.withType(Material.DIAMOND_CHESTPLATE);
            price = ItemStack.of(Material.DIAMOND, 24);
            costComponent = Component.text("24 Diamond", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false);

        } else {

            item = item.withType(Material.LEATHER_CHESTPLATE);
            item.setData(DataComponentTypes.ITEM_NAME, Component.text("You can't upgrade this further!"));
            item.setData(DataComponentTypes.DYED_COLOR, DyedItemColor.dyedItemColor(Color.RED, false));
            item.editPersistentDataContainer(pdc -> pdc.remove(new NamespacedKey("headwars", "shopaction")));
            costComponent = Component.text("You can't buy this!", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false);

        }

        item.setData(DataComponentTypes.LORE, ItemLore.lore(List.of(costComponentStart.append(costComponent))));
        byte[] priceArr; if (price.getType() == Material.AIR) priceArr = new byte[1]; else priceArr = price.serializeAsBytes();
        item.editPersistentDataContainer(pdc -> pdc.set(new NamespacedKey("headwars", "shopprice"), PersistentDataType.BYTE_ARRAY, priceArr));

        return item;
    }

    public ItemStack handleBuy(@NotNull ItemStack shopItem, @NotNull Player player, Inventory inventory, HeadWarsGame game) {

        PlayerInventory playerInventory = player.getInventory();
        Material currentTier; if (playerInventory.getChestplate() == null) currentTier = null; else currentTier = playerInventory.getChestplate().getType();
        ItemStack item = ItemStack.of(Material.LEATHER_CHESTPLATE);
        item.setData(DataComponentTypes.UNBREAKABLE, Unbreakable.unbreakable(false));
        item.setData(DataComponentTypes.DYED_COLOR, DyedItemColor.dyedItemColor(game.playerTeams().get(player).mapTeam().getTeamColor().getColor(), false));

        if (currentTier == null) {
            playerInventory.setItem(EquipmentSlot.CHEST, item);
        } else if (currentTier == Material.LEATHER_CHESTPLATE) {
            playerInventory.setItem(EquipmentSlot.CHEST, item.withType(Material.CHAINMAIL_CHESTPLATE));
        } else if (currentTier == Material.CHAINMAIL_CHESTPLATE) {
            playerInventory.setItem(EquipmentSlot.CHEST, item.withType(Material.IRON_CHESTPLATE));
        } else if (currentTier == Material.IRON_CHESTPLATE) {
            playerInventory.setItem(EquipmentSlot.CHEST, item.withType(Material.DIAMOND_CHESTPLATE));
        } else {
            playerInventory.setItem(EquipmentSlot.CHEST, item);
        }

        ShopGui.dummyGUI.reRenderItem(shopItem, player, inventory);

        return ItemStack.of(Material.AIR);
    }

}
