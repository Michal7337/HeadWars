package win.codingboulder.headWars.game.shop;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import win.codingboulder.headWars.HeadWars;

public class ShopGui implements InventoryHolder, Listener {

    private final Inventory inventory;
    private final ItemShop itemShop;

    public ShopGui(@NotNull ItemShop itemShop) {

        this.itemShop = itemShop;

        inventory = HeadWars.getInstance().getServer().createInventory(this, itemShop.rows()*9, MiniMessage.miniMessage().deserialize(itemShop.title()));
        inventory.setContents(itemShop.items().toArray(new ItemStack[0]));

    }

    /**
     * Empty constructor FOR REGISTERING EVENTS ONLY, DO NOT USE!
     */
    public ShopGui() {
        inventory = null;
        itemShop = null;
    }

    public @NotNull Inventory getInventory() {
        return inventory;
    }

}
