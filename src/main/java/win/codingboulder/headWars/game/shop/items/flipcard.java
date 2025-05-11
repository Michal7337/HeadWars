package win.codingboulder.headWars.game.shop.items;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import win.codingboulder.headWars.game.HeadWarsGame;
import win.codingboulder.headWars.game.shop.CustomShopItem;
import win.codingboulder.headWars.game.shop.ShopGui;

public class flipcard implements CustomShopItem {

    NamespacedKey sideKey = new NamespacedKey("headwars", "flip_side");

    @Override
    public ItemStack handleRender(@NotNull ItemStack shopItem, Player player, HeadWarsGame game, ShopGui shop) {

        if ("back".equals(shopItem.getPersistentDataContainer().get(sideKey, PersistentDataType.STRING))) return ShopGui.getItemBought(shopItem);

        return shopItem;

    }

    @Override
    public ItemStack handleBuy(@NotNull ItemStack shopItem, Player player, HeadWarsGame game, @NotNull ShopGui shop) {

        String side = shopItem.getPersistentDataContainer().getOrDefault(sideKey, PersistentDataType.STRING, "front");
        shopItem.editPersistentDataContainer(pdc -> {
            if (side.equals("front")) pdc.set(sideKey, PersistentDataType.STRING, "back");
            else pdc.set(sideKey, PersistentDataType.STRING, "front");
        });

        shop.reRenderItem(shopItem);

        return null;

    }

}
