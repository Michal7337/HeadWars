package win.codingboulder.headWars.game.shop;

import io.papermc.paper.persistence.PersistentDataContainerView;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import win.codingboulder.headWars.HeadWars;
import win.codingboulder.headWars.game.HeadWarsGame;
import win.codingboulder.headWars.game.HeadWarsGameManager;
import win.codingboulder.headWars.game.shop.items.*;

import java.util.HashMap;
import java.util.Objects;

public class ShopGui implements InventoryHolder, Listener {

    public static final String namespace = "headwars";
    public static final NamespacedKey itemIdKey = new NamespacedKey(namespace, "itemid");
    public static final NamespacedKey shopActionKey = new NamespacedKey(namespace, "shop_action");
    public static final NamespacedKey shopPriceKey = new NamespacedKey(namespace, "shop_price");
    public static final NamespacedKey shopItemKey = new NamespacedKey(namespace, "shop_item");

    private ItemShop itemShop;
    private final Inventory inventory;

    public ShopGui(@NotNull ItemShop itemShop, Player player) {

        this.itemShop = itemShop;
        inventory = HeadWars.getInstance().getServer().createInventory(this, itemShop.rows()*9, MiniMessage.miniMessage().deserialize(itemShop.title()));

    }

    /**
     * Empty constructor FOR REGISTERING EVENTS ONLY, DO NOT USE!
     */
    public ShopGui() {
        inventory = null;
    }

    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public static ShopGui dummyGUI = new ShopGui();

    public void renderItems() {



    }

    @EventHandler
    public void onClick(@NotNull InventoryClickEvent event) {

        Inventory inventory = event.getClickedInventory();
        Player player = (Player) event.getWhoClicked();

        if (inventory == null) return;
        if (!(inventory.getHolder() instanceof ShopGui shopGui)) return;

        event.setCancelled(true);

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null) return;

        PersistentDataContainerView pdc = clickedItem.getPersistentDataContainer();

        String shopAction = pdc.get(new NamespacedKey("headwars", "shopaction"), PersistentDataType.STRING);
        if (shopAction == null) return;

        if (shopAction.equals("buy")) {

            ItemStack itemPrice;
            byte[] priceEnc = pdc.get(new NamespacedKey("headwars", "shopprice"), PersistentDataType.BYTE_ARRAY);
            if (priceEnc == null) itemPrice = ItemStack.of(Material.AIR);
            else if (ArrayUtils.isEmpty(priceEnc)) itemPrice = ItemStack.of(Material.AIR);
            else itemPrice = ItemStack.deserializeBytes(priceEnc);

            if (pdc.has(new NamespacedKey("headwars", "shopitem"), PersistentDataType.BYTE_ARRAY)) {

                byte[] itemToGiveArr = pdc.get(new NamespacedKey("headwars", "shopitem"), PersistentDataType.BYTE_ARRAY);
                if (itemToGiveArr == null) return;

                if (!(player.getInventory().containsAtLeast(itemPrice, itemPrice.getAmount()))) {
                    player.sendRichMessage("<red>You can't afford this!");
                    return;
                }

                ItemStack itemToGive = ItemStack.deserializeBytes(itemToGiveArr);
                player.give(itemToGive);

                player.getInventory().removeItem(itemPrice);
                player.playSound(player, Sound.BLOCK_NOTE_BLOCK_PLING, 2, 2);

            } else if (pdc.has(new NamespacedKey("headwars", "itemid"), PersistentDataType.STRING)) {

                String itemId = pdc.get(new NamespacedKey("headwars", "itemid"), PersistentDataType.STRING);
                if (itemId == null) return;

                if (!(player.getInventory().containsAtLeast(itemPrice, itemPrice.getAmount()))) {
                    player.sendRichMessage("<red>You can't afford this!");
                    return;
                }

                ItemStack item = handleCustomItemBuy(clickedItem, player, inventory);
                if (item.getType() != Material.AIR) player.give(item);

                player.getInventory().removeItem(itemPrice);
                player.playSound(player, Sound.BLOCK_NOTE_BLOCK_PLING, 2, 2);

            }

        } else if (shopAction.equals("open-menu")) {

            String shopToOpenS = pdc.get(new NamespacedKey("headwars", "menu"), PersistentDataType.STRING);
            if (shopToOpenS == null) return;
            ItemShop shopToOpen = ShopManager.itemShops.get(shopToOpenS);
            if (shopToOpen == null) return;
            player.playSound(player, Sound.BLOCK_NOTE_BLOCK_HAT, 5, 1);
            Bukkit.getScheduler().runTask(HeadWars.getInstance(), task -> shopToOpen.openShop(player));

        }

    }

    public static HashMap<String, CustomShopItem> customItemHandlers = new HashMap<>();
    static {

        customItemHandlers.put("wool", new wool());
        customItemHandlers.put("tool_pickaxe", new tool_pickaxe());

        customItemHandlers.put("armor_helmet", new armor_helmet());
        customItemHandlers.put("armor_chestplate", new armor_chestplate());
        customItemHandlers.put("armor_leggings", new armor_leggings());
        customItemHandlers.put("armor_boots", new armor_boots());

        customItemHandlers.put("sword_stone", new sword(Material.STONE_SWORD));
        customItemHandlers.put("sword_iron", new sword(Material.IRON_SWORD));
        customItemHandlers.put("sword_diamond", new sword(Material.DIAMOND_SWORD));

    }

    public @NotNull ItemStack handleCustomItemRender(@NotNull ItemStack shopItem, Player player) {

        String itemId = shopItem.getPersistentDataContainer().get(new NamespacedKey("headwars", "itemid"), PersistentDataType.STRING);
        HeadWarsGame game = HeadWarsGameManager.playersInGames.get(player);

        if (!customItemHandlers.containsKey(itemId)) return ItemStack.of(Material.BARRIER);
        return customItemHandlers.get(itemId).handleRender(shopItem, player, game);

    }

    private @NotNull ItemStack handleCustomItemBuy(@NotNull ItemStack shopItem, Player player, Inventory inventory) {

        String itemId = shopItem.getPersistentDataContainer().get(new NamespacedKey("headwars", "itemid"), PersistentDataType.STRING);
        if (itemId == null) return ItemStack.empty();
        HeadWarsGame game = HeadWarsGameManager.playersInGames.get(player);

        if (!customItemHandlers.containsKey(itemId)) return ItemStack.of(Material.AIR);
        return customItemHandlers.get(itemId).handleBuy(shopItem, player, inventory, game);

    }

    public void reRenderItem(ItemStack shopItem, Player player, @NotNull Inventory inventory) {

        for (int i = 0; i < inventory.getSize(); i++) if (Objects.equals(inventory.getContents()[i], shopItem)) inventory.setItem(i, handleCustomItemRender(Objects.requireNonNull(inventory.getContents()[i]), player));

    }

}
