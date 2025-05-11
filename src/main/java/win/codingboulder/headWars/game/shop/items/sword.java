package win.codingboulder.headWars.game.shop.items;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemEnchantments;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;
import win.codingboulder.headWars.game.GameTeam;
import win.codingboulder.headWars.game.HeadWarsGame;
import win.codingboulder.headWars.game.shop.CustomShopItem;
import win.codingboulder.headWars.game.shop.ShopGui;

import java.util.Map;

@SuppressWarnings("UnstableApiUsage")
public class sword implements CustomShopItem {

    @Override
    public ItemStack handleRender(@NotNull ItemStack shopItem, Player player, HeadWarsGame game, ShopGui shop) {

        if (game != null && game.isStarted()) {
            GameTeam team = game.playerTeams().get(player);
            int sharp = team.purchasedUpgrades.getOrDefault("upgrade_sharpness", 0);
            if (sharp > 0) shopItem.setData(DataComponentTypes.ENCHANTMENTS, ItemEnchantments.itemEnchantments(Map.of(Enchantment.SHARPNESS, sharp), true));
        }

        return shopItem;

    }

    @Override
    public ItemStack handleBuy(@NotNull ItemStack shopItem, @NotNull Player player, HeadWarsGame game, ShopGui shop) {

        PlayerInventory inventory = player.getInventory();

        ItemStack boughtSword = ShopGui.getItemBought(shopItem);

        if (game != null && game.isStarted()) {
            GameTeam team = game.playerTeams().get(player);
            int sharp = team.purchasedUpgrades.getOrDefault("upgrade_sharpness", 0);
            if (sharp > 0) boughtSword.setData(DataComponentTypes.ENCHANTMENTS, ItemEnchantments.itemEnchantments(Map.of(Enchantment.SHARPNESS, sharp), true));
        }

        int swordIndex = inventory.first(Material.WOODEN_SWORD);
        if (swordIndex == -1) player.give(boughtSword);
        else inventory.setItem(swordIndex, boughtSword);

        return null;

    }

}
