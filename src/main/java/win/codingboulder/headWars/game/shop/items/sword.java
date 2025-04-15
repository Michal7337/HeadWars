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
    public ItemStack handleRender(@NotNull ItemStack shopItem, Player player, HeadWarsGame game) {

        if (cost == null) {
            byte[] priceEnc = shopItem.getPersistentDataContainer().get(new NamespacedKey("headwars", "shopprice"), PersistentDataType.BYTE_ARRAY);
            if (priceEnc == null) cost = ItemStack.of(Material.AIR);
            else if (ArrayUtils.isEmpty(priceEnc)) cost = ItemStack.of(Material.AIR);
            else cost = ItemStack.deserializeBytes(priceEnc);
        }

        ItemStack sword = shopItem.withType(swordMaterial);
        sword.setData(DataComponentTypes.UNBREAKABLE, Unbreakable.unbreakable(false));
        sword.setData(DataComponentTypes.LORE, ItemLore.lore(List.of(Util.getItemCostComponent(cost))));
        sword.setData(DataComponentTypes.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.itemAttributes().showInTooltip(false));

        return sword;

    }

    @Override
    public ItemStack handleBuy(@NotNull ItemStack shopItem, @NotNull Player player, Inventory inventory, HeadWarsGame game) {

        PlayerInventory playerInventory = player.getInventory();

        ItemStack sword = ItemStack.of(swordMaterial);
        sword.setData(DataComponentTypes.UNBREAKABLE, Unbreakable.unbreakable(false));
        sword.setData(DataComponentTypes.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.itemAttributes().showInTooltip(false));

        if (cost == null) {
            byte[] priceEnc = shopItem.getPersistentDataContainer().get(new NamespacedKey("headwars", "shopprice"), PersistentDataType.BYTE_ARRAY);
            if (priceEnc == null) cost = ItemStack.of(Material.AIR);
            else if (ArrayUtils.isEmpty(priceEnc)) cost = ItemStack.of(Material.AIR);
            else cost = ItemStack.deserializeBytes(priceEnc);
        }

        if (playerInventory.containsAtLeast(ResourceGenerator.sword, 1)) {
            for (int i = 0; i < playerInventory.getSize(); i++) {
                ItemStack itemStack = playerInventory.getContents()[i];
                if (itemStack != null && itemStack.getType().equals(Material.WOODEN_SWORD)) {
                    playerInventory.setItem(i, sword);
                }
            }
            return ItemStack.empty();

        } else return sword;

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
