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

    private Inventory inventory;
    private final ItemShop itemShop;

    public ShopConfigGUI(ItemShop itemShop) {

        this.itemShop = itemShop;
        if (itemShop == null) return;

        inventory = HeadWars.getInstance().getServer().createInventory(this, itemShop.rows()*9, MiniMessage.miniMessage().deserialize(itemShop.title()));
        inventory.setContents(itemShop.getItems().toArray(new ItemStack[0]));

    }

    public @NotNull Inventory getInventory() {
        return inventory;
    }

    @EventHandler
    public void onClose(@NotNull InventoryCloseEvent event) {

        Inventory inventory = event.getInventory();
        if (!(inventory.getHolder() instanceof ShopConfigGUI gui)) return;

        gui.itemShop.setItems(new ArrayList<>(Arrays.asList(inventory.getContents())));
        gui.itemShop.storeToFile();
        ItemShop.loadAllShops();
        event.getPlayer().sendRichMessage("<green>Saved shop inventory!");

    }

}
