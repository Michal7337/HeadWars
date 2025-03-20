package win.codingboulder.headWars.game.shop.items;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import win.codingboulder.headWars.game.HeadWarsGame;
import win.codingboulder.headWars.game.shop.CustomShopItem;

public class tool_pickaxe implements CustomShopItem {

    public ItemStack handleRender(@NotNull ItemStack shopItem, Player player, HeadWarsGame game) {
        return null;
    }

    public ItemStack handleBuy(@NotNull ItemStack shopItem, Player player, Inventory inventory, HeadWarsGame game) {
        return null;
    }

}
