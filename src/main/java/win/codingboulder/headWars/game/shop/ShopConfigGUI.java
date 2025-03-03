package win.codingboulder.headWars.game.shop;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import win.codingboulder.headWars.HeadWars;

public class ShopConfigGUI implements InventoryHolder {

    private final Inventory inventory;
    private final ItemShop itemShop;

    public ShopConfigGUI(@NotNull ItemShop itemShop) {
        this.itemShop = itemShop;

        inventory = HeadWars.getInstance().getServer().createInventory(this, itemShop.size());
        inventory.setContents(itemShop.items().toArray(new ItemStack[0]));

    }

    public @NotNull Inventory getInventory() {
        return inventory;
    }

}
