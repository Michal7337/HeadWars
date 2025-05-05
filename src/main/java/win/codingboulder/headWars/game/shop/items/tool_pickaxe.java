package win.codingboulder.headWars.game.shop.items;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemAttributeModifiers;
import io.papermc.paper.datacomponent.item.ItemLore;
import io.papermc.paper.datacomponent.item.Unbreakable;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;
import win.codingboulder.headWars.game.HeadWarsGame;
import win.codingboulder.headWars.game.shop.CustomShopItem;
import win.codingboulder.headWars.game.shop.ShopGui;
import win.codingboulder.headWars.util.Util;

import java.util.HashMap;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class tool_pickaxe implements CustomShopItem {

    public static ItemStack basePickaxe = ItemStack.of(Material.WOODEN_PICKAXE);
    public static HashMap<Material, HashMap<ItemStack, Integer>> pickaxeCosts = new HashMap<>();
    static {

        pickaxeCosts.put(Material.WOODEN_PICKAXE, ShopGui.getSingleItemCost(ItemStack.of(Material.IRON_INGOT, 32)));
        pickaxeCosts.put(Material.STONE_PICKAXE, ShopGui.getSingleItemCost(ItemStack.of(Material.IRON_INGOT, 64)));
        pickaxeCosts.put(Material.IRON_PICKAXE, ShopGui.getSingleItemCost(ItemStack.of(Material.GOLD_INGOT, 32)));
        pickaxeCosts.put(Material.DIAMOND_PICKAXE, ShopGui.getSingleItemCost(ItemStack.of(Material.DIAMOND, 32)));

        basePickaxe.setData(DataComponentTypes.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.itemAttributes().showInTooltip(false).build());
        basePickaxe.setData(DataComponentTypes.UNBREAKABLE, Unbreakable.unbreakable(false));

    }

    @Override
    public ItemStack handleRender(@NotNull ItemStack shopItem, @NotNull Player player, HeadWarsGame game, ShopGui shop) {

        PlayerInventory inventory = player.getInventory();
        ItemStack renderedPickaxe;
        Material playerPickaxe = null;

        //determine what pickaxe the player has
        for (Material material : pickaxeCosts.keySet()) if (inventory.contains(material)) playerPickaxe = material;

        Material nextPickaxe = getNextTier(playerPickaxe);
        if (nextPickaxe == null) nextPickaxe = pickaxeCosts.keySet().stream().toList().getLast(); // if the player has the max pickaxe set to the max pickaxe

        renderedPickaxe = basePickaxe.withType(nextPickaxe);

        HashMap<ItemStack, Integer> cost = pickaxeCosts.get(nextPickaxe);

        renderedPickaxe.setData(DataComponentTypes.LORE, ItemLore.lore(Util.getItemCostLore(cost)));

        ShopGui.setItemAction(renderedPickaxe, "buy");
        ShopGui.setItemId(renderedPickaxe, "tool_pickaxe");
        ShopGui.setItemPrice(renderedPickaxe, cost);

        return renderedPickaxe;

    }

    @Override
    public ItemStack handleBuy(@NotNull ItemStack shopItem, @NotNull Player player, HeadWarsGame game, ShopGui shop) {

        PlayerInventory inventory = player.getInventory();
        ItemStack boughtPickaxe;
        Material playerPickaxe = null;

        //determine what pickaxe the player has
        for (Material material : pickaxeCosts.keySet()) if (inventory.contains(material)) playerPickaxe = material;

        Material nextPickaxe = getNextTier(playerPickaxe);
        if (nextPickaxe == null) nextPickaxe = pickaxeCosts.keySet().stream().toList().getLast(); // if the player has the max pickaxe, set to the max pickaxe

        boughtPickaxe = basePickaxe.withType(nextPickaxe);

        if (playerPickaxe == null) player.give(boughtPickaxe);
        else inventory.setItem(inventory.first(playerPickaxe), boughtPickaxe);

        shop.reRenderItem(shopItem);

        return null;

    }

    public Material getNextTier(Material currentTier) {

        List<Material> materials = pickaxeCosts.keySet().stream().toList();
        int currentIndex = materials.indexOf(currentTier);
        if (currentIndex == -1) return materials.getFirst(); // if the player has no item
        if (currentIndex + 1 == pickaxeCosts.size()) return null; // if the player has the max tier
        return materials.get(currentIndex + 1);

    }

}

