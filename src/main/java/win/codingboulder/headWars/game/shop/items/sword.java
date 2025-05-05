package win.codingboulder.headWars.game.shop.items;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;
import win.codingboulder.headWars.game.HeadWarsGame;
import win.codingboulder.headWars.game.shop.CustomShopItem;
import win.codingboulder.headWars.game.shop.ShopGui;

public class sword implements CustomShopItem {

    @Override
    public ItemStack handleRender(@NotNull ItemStack shopItem, Player player, HeadWarsGame game, ShopGui shop) {

        return shopItem;

    }

    @Override
    public ItemStack handleBuy(@NotNull ItemStack shopItem, @NotNull Player player, HeadWarsGame game, ShopGui shop) {

        PlayerInventory inventory = player.getInventory();

        ItemStack boughtSword = ShopGui.getItemBought(shopItem);

        int swordIndex = inventory.first(Material.WOODEN_SWORD);
        if (swordIndex == -1) player.give(boughtSword);
        else inventory.setItem(swordIndex, boughtSword);

        return null;

    }

}
