package win.codingboulder.headWars.game.shop;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import win.codingboulder.headWars.HeadWars;

import java.util.ArrayList;
import java.util.Arrays;

public class ShopConfigGUI implements InventoryHolder, Listener {

    private final Inventory inventory;
    private final ItemShop itemShop;

    public ShopConfigGUI(@NotNull ItemShop itemShop) {

        this.itemShop = itemShop;

        inventory = HeadWars.getInstance().getServer().createInventory(this, itemShop.rows()*9, MiniMessage.miniMessage().deserialize(itemShop.title()));
        inventory.setContents(itemShop.items().toArray(new ItemStack[0]));

    }

    /**
     * Empty constructor FOR REGISTERING EVENTS ONLY, DO NOT USE!
     */
    public ShopConfigGUI() {
        inventory = null;
        itemShop = null;
    }

    public @NotNull Inventory getInventory() {
        return inventory;
    }

    @EventHandler
    public void onClose(@NotNull InventoryCloseEvent event) {

        Inventory inventory = event.getInventory();
        if (!(inventory.getHolder() instanceof ShopConfigGUI gui)) return;

        gui.itemShop.items(new ArrayList<>(Arrays.asList(inventory.getContents())));
        gui.itemShop.storeToFile();
        ShopManager.loadAllShops();
        event.getPlayer().sendRichMessage("<green>Saved shop inventory!");

    }

}
