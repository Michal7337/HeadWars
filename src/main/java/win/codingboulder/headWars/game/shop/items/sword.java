package win.codingboulder.headWars.game.shop.items;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemAttributeModifiers;
import io.papermc.paper.datacomponent.item.ItemLore;
import io.papermc.paper.datacomponent.item.Unbreakable;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import win.codingboulder.headWars.game.HeadWarsGame;
import win.codingboulder.headWars.game.ResourceGenerator;
import win.codingboulder.headWars.game.shop.CustomShopItem;
import win.codingboulder.headWars.game.shop.ShopGui;
import win.codingboulder.headWars.util.Util;

import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class sword implements CustomShopItem {

    private Material swordMaterial;
    private ItemStack cost;

    public sword(Material swordMaterial, ItemStack cost) {
        this.swordMaterial = swordMaterial;
        this.cost = cost;
    }

    public sword(Material swordMaterial) {
        this.swordMaterial = swordMaterial;
        this.cost = null;
    }

    @Override
    public ItemStack handleRender(@NotNull ItemStack shopItem, Player player, HeadWarsGame game, ShopGui shop) {
        return null;
    }

    @Override
    public ItemStack handleBuy(@NotNull ItemStack shopItem, Player player, HeadWarsGame game, ShopGui shop) {
        return null;
    }

    public Material swordMaterial() {
        return swordMaterial;
    }

    public void setSwordMaterial(Material swordMaterial) {
        this.swordMaterial = swordMaterial;
    }

    public ItemStack cost() {
        return cost;
    }

    public void setCost(ItemStack cost) {
        this.cost = cost;
    }

}
