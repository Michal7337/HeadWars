package win.codingboulder.headWars.game.shop.items;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.DyedItemColor;
import io.papermc.paper.datacomponent.item.ItemAttributeModifiers;
import io.papermc.paper.datacomponent.item.ItemLore;
import io.papermc.paper.datacomponent.item.Unbreakable;
import it.unimi.dsi.fastutil.Pair;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;
import win.codingboulder.headWars.game.HeadWarsGame;
import win.codingboulder.headWars.game.shop.CustomShopItem;
import win.codingboulder.headWars.game.shop.ShopGui;
import win.codingboulder.headWars.util.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class armor implements CustomShopItem {

    public static HashMap<Material, Pair<Material, HashMap<ItemStack, Integer>>> armorUpgrades = new HashMap<>();
    public static HashMap<EquipmentSlot, Pair<Material, HashMap<ItemStack, Integer>>> defaultArmor = new HashMap<>();
    public static ArrayList<Material> leatherArmor = new ArrayList<>(List.of(Material.LEATHER_HELMET, Material.LEATHER_CHESTPLATE, Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS));
    static {

        armorUpgrades.put(Material.LEATHER_HELMET, Pair.of(Material.CHAINMAIL_HELMET, ShopGui.getSingleItemCost(ItemStack.of(Material.IRON_INGOT, 64))));
        armorUpgrades.put(Material.CHAINMAIL_HELMET, Pair.of(Material.IRON_HELMET, ShopGui.getSingleItemCost(ItemStack.of(Material.GOLD_INGOT, 64))));
        armorUpgrades.put(Material.IRON_HELMET, Pair.of(Material.DIAMOND_HELMET, ShopGui.getSingleItemCost(ItemStack.of(Material.DIAMOND, 64))));

        armorUpgrades.put(Material.LEATHER_CHESTPLATE, Pair.of(Material.CHAINMAIL_CHESTPLATE, ShopGui.getSingleItemCost(ItemStack.of(Material.IRON_INGOT, 64))));
        armorUpgrades.put(Material.CHAINMAIL_CHESTPLATE, Pair.of(Material.IRON_CHESTPLATE, ShopGui.getSingleItemCost(ItemStack.of(Material.GOLD_INGOT, 64))));
        armorUpgrades.put(Material.IRON_CHESTPLATE, Pair.of(Material.DIAMOND_CHESTPLATE, ShopGui.getSingleItemCost(ItemStack.of(Material.DIAMOND, 64))));

        armorUpgrades.put(Material.LEATHER_LEGGINGS, Pair.of(Material.CHAINMAIL_LEGGINGS, ShopGui.getSingleItemCost(ItemStack.of(Material.IRON_INGOT, 64))));
        armorUpgrades.put(Material.CHAINMAIL_LEGGINGS, Pair.of(Material.IRON_LEGGINGS, ShopGui.getSingleItemCost(ItemStack.of(Material.GOLD_INGOT, 64))));
        armorUpgrades.put(Material.IRON_LEGGINGS, Pair.of(Material.DIAMOND_LEGGINGS, ShopGui.getSingleItemCost(ItemStack.of(Material.DIAMOND, 64))));

        armorUpgrades.put(Material.LEATHER_BOOTS, Pair.of(Material.CHAINMAIL_BOOTS, ShopGui.getSingleItemCost(ItemStack.of(Material.IRON_INGOT, 64))));
        armorUpgrades.put(Material.CHAINMAIL_BOOTS, Pair.of(Material.IRON_BOOTS, ShopGui.getSingleItemCost(ItemStack.of(Material.GOLD_INGOT, 64))));
        armorUpgrades.put(Material.IRON_BOOTS, Pair.of(Material.DIAMOND_BOOTS, ShopGui.getSingleItemCost(ItemStack.of(Material.DIAMOND, 64))));

        defaultArmor.put(EquipmentSlot.HEAD, Pair.of(Material.LEATHER_HELMET, new HashMap<>()));
        defaultArmor.put(EquipmentSlot.CHEST, Pair.of(Material.LEATHER_CHESTPLATE, new HashMap<>()));
        defaultArmor.put(EquipmentSlot.LEGS, Pair.of(Material.LEATHER_LEGGINGS, new HashMap<>()));
        defaultArmor.put(EquipmentSlot.FEET, Pair.of(Material.LEATHER_BOOTS, new HashMap<>()));

    }

    private final EquipmentSlot armorSlot;

    public armor(EquipmentSlot armorSlot) {
        this.armorSlot = armorSlot;
    }

    @Override
    public ItemStack handleRender(@NotNull ItemStack shopItem, @NotNull Player player, HeadWarsGame game, ShopGui shop) {

        PlayerInventory inventory = player.getInventory();
        Pair<Material, HashMap<ItemStack, Integer>> upgradeData;
        Material invItem = inventory.getItem(armorSlot).getType();

        upgradeData = armorUpgrades.getOrDefault(invItem, defaultArmor.get(armorSlot));
        if (upgradeData == null) return ItemStack.of(Material.BARRIER);

        if (!armorUpgrades.containsKey(invItem) && !invItem.equals(Material.AIR)) {
            ItemStack maxTierArmor = ItemStack.of(defaultArmor.get(armorSlot).key());
            maxTierArmor.setData(DataComponentTypes.ITEM_NAME, Component.text("You can't upgrade this further!", NamedTextColor.RED));
            maxTierArmor.setData(DataComponentTypes.DYED_COLOR, DyedItemColor.dyedItemColor(Color.RED, false));
            maxTierArmor.setData(DataComponentTypes.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.itemAttributes().showInTooltip(false));
            return maxTierArmor;
        }

        ItemStack renderedItem = ItemStack.of(upgradeData.key());

        ShopGui.setItemAction(renderedItem, "buy");
        ShopGui.setItemId(renderedItem, ShopGui.getItemId(shopItem));
        ShopGui.setItemPrice(renderedItem, upgradeData.value());

        renderedItem.setData(DataComponentTypes.LORE, ItemLore.lore(Util.getItemCostLore(upgradeData.value())));
        renderedItem.setData(DataComponentTypes.UNBREAKABLE, Unbreakable.unbreakable(false));
        renderedItem.setData(DataComponentTypes.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.itemAttributes().showInTooltip(false));

        return renderedItem;

    }

    @Override
    public ItemStack handleBuy(@NotNull ItemStack shopItem, @NotNull Player player, HeadWarsGame game, ShopGui shop) {

        PlayerInventory inventory = player.getInventory();
        Pair<Material, HashMap<ItemStack, Integer>> upgradeData;
        Material invItem = inventory.getItem(armorSlot).getType();

        upgradeData = armorUpgrades.getOrDefault(invItem, defaultArmor.get(armorSlot));
        if (upgradeData == null) return null;

        ItemStack item = ItemStack.of(upgradeData.key());
        item.setData(DataComponentTypes.UNBREAKABLE, Unbreakable.unbreakable(false));
        item.setData(DataComponentTypes.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.itemAttributes().showInTooltip(false));

        inventory.setItem(armorSlot, item);

        shop.reRenderItem(shopItem);

        return null;

    }

}
