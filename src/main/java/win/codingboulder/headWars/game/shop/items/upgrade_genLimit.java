package win.codingboulder.headWars.game.shop.items;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import win.codingboulder.headWars.game.GameTeam;
import win.codingboulder.headWars.game.HeadWarsGame;
import win.codingboulder.headWars.game.shop.CustomShopItem;
import win.codingboulder.headWars.game.shop.ShopGui;
import win.codingboulder.headWars.util.Util;

import java.util.HashMap;
import java.util.LinkedHashMap;

@SuppressWarnings("UnstableApiUsage")
public class upgrade_genLimit implements CustomShopItem {

    public static int incAmountPerTier = 5;
    public static int maxTier = 4;
    public static LinkedHashMap<Integer, HashMap<ItemStack, Integer>> upgradeCosts = new LinkedHashMap<>();
    static {
        upgradeCosts.put(1, ShopGui.getSingleItemCost(ItemStack.of(Material.EMERALD, 4)));
        upgradeCosts.put(2, ShopGui.getSingleItemCost(ItemStack.of(Material.EMERALD, 6)));
        upgradeCosts.put(3, ShopGui.getSingleItemCost(ItemStack.of(Material.EMERALD, 8)));
        upgradeCosts.put(4, ShopGui.getSingleItemCost(ItemStack.of(Material.EMERALD, 10)));
    }

    @Override
    public ItemStack handleRender(@NotNull ItemStack shopItem, Player player, HeadWarsGame game, ShopGui shop) {

        ItemStack item = ItemStack.of(Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE);

        item.setData(DataComponentTypes.HIDE_ADDITIONAL_TOOLTIP);
        item.setData(DataComponentTypes.ITEM_NAME, Component.text("Increase Generator Limit").color(NamedTextColor.AQUA));

        ItemLore.Builder lore = ItemLore.lore();
        lore.addLine(Component.text("Increases your team's generator limit.", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)).addLine(Component.empty());

        if (game == null || !game.isStarted()) { // prevent buying if player is not in game
            lore.addLine(Component.text("You must be in a game to buy this!", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
            item.setData(DataComponentTypes.LORE, lore);
            return item;
        }

        int currentTier = game.playerTeams().get(player).purchasedUpgrades.getOrDefault("upgrade_genLimit", 0);

        upgradeCosts.forEach((tier, price) -> {

            Component line = Component.text("Tier " + tier + ": +" + incAmountPerTier + " Gens");
            if (tier > currentTier) line = line.append(Component.text(", ")).color(NamedTextColor.GRAY).append(Util.getItemCostLineComponent(price)).decoration(TextDecoration.ITALIC, false);
            else line = line.append(Component.text(" ✔")).color(NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false);
            lore.addLine(line);

        });

        if (currentTier >= maxTier) {
            lore.addLine(Component.empty());
            lore.addLine(Component.text("Upgrade maxed!", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false));
            item.setData(DataComponentTypes.LORE, lore);
            return item;
        }

        item.setData(DataComponentTypes.LORE, lore);

        ShopGui.setItemAction(item, "buy");
        ShopGui.setItemId(item, "upgrade_genLimit");
        ShopGui.setItemPrice(item, upgradeCosts.get(currentTier + 1));

        return item;

    }

    @Override
    public ItemStack handleBuy(@NotNull ItemStack shopItem, Player player, @NotNull HeadWarsGame game, @NotNull ShopGui shop) {

        GameTeam team = game.playerTeams().get(player);
        team.generatorLimit += incAmountPerTier;
        int currentTier = game.playerTeams().get(player).purchasedUpgrades.getOrDefault("upgrade_genLimit", 0);
        team.purchasedUpgrades.put("upgrade_genLimit", currentTier + 1);

        shop.reRenderItem(shopItem);

        return null;

    }

}
