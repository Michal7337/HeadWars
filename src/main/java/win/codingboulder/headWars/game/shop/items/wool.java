package win.codingboulder.headWars.game.shop.items;

import io.papermc.paper.datacomponent.DataComponentTypes;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import win.codingboulder.headWars.game.HeadWarsGame;
import win.codingboulder.headWars.game.shop.CustomShopItem;
import win.codingboulder.headWars.game.shop.ShopGui;
import win.codingboulder.headWars.util.Util;

@SuppressWarnings("UnstableApiUsage")
public class wool implements CustomShopItem {

    @Override
    public ItemStack handleRender(@NotNull ItemStack shopItem, Player player, HeadWarsGame game, ShopGui shop) {

        if (game != null) {

            Component name = Component.text("Wool", Util.getNamedColor(game.getPlayerColor(player)));
            ItemStack newItem = shopItem.withType(Util.getWoolFromColor(game.getPlayerColor(player)));
            newItem.setData(DataComponentTypes.ITEM_NAME, name);

            return newItem;

        } else return shopItem.withType(Material.WHITE_WOOL);

    }

    @Override
    public ItemStack handleBuy(@NotNull ItemStack shopItem, Player player, HeadWarsGame game, ShopGui shop) {

        if (game != null) {

         return ItemStack.of(Util.getWoolFromColor(game.getPlayerColor(player)), shopItem.getAmount());

        } else return ItemStack.of(Material.WHITE_WOOL, shopItem.getAmount());

    }


    /*
    public ItemStack handleRender(@NotNull ItemStack shopItem, Player player, HeadWarsGame game) {

        ItemStack itemStack = shopItem.withType(Material.WHITE_WOOL);
        itemStack.lore(List.of(
            Component.text("Cost: ").color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)
                .append(Component.text("4 Iron", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false))
        ));
        itemStack.editMeta(itemMeta -> itemMeta.displayName(
                Component.text("Wool")
                    .color(Util.getNamedColor(DyeColor.WHITE))
                    .decoration(TextDecoration.ITALIC, false)
            )
        );

        if (game == null) {
            player.sendRichMessage("<red>You must be in a game to buy this!");
            return itemStack;
        }

        DyeColor color = game.playerTeams().get(player).mapTeam().getTeamColor();
        itemStack = itemStack.withType(Util.getWoolFromColor(color));
        itemStack.editMeta(itemMeta -> itemMeta.displayName(
                Component.text("Wool")
                    .color(Util.getNamedColor(color))
                    .decoration(TextDecoration.ITALIC, false)
            )
        );

        return itemStack;
    }

    public ItemStack handleBuy(@NotNull ItemStack shopItem, Player player, Inventory inventory, HeadWarsGame game) {

        Material woolColor;

        if (game == null) {
            player.sendRichMessage("<red>You must be in a game to buy this!");
            woolColor = Material.WHITE_WOOL;
        } else woolColor = Util.getWoolFromColor(game.playerTeams().get(player).mapTeam().getTeamColor());

        return ItemStack.of(woolColor, 16);

    }
     */

}
