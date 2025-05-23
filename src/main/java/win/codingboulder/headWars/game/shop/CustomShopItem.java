package win.codingboulder.headWars.game.shop;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import win.codingboulder.headWars.game.HeadWarsGame;

public interface CustomShopItem {

    ItemStack handleRender(@NotNull ItemStack shopItem, Player player, HeadWarsGame game, ShopGui shop);

    ItemStack handleBuy(@NotNull ItemStack shopItem, Player player, HeadWarsGame game, ShopGui shop);

}
