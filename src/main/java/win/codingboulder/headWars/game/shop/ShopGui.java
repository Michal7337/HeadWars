package win.codingboulder.headWars.game.shop;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.*;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import win.codingboulder.headWars.HeadWars;
import win.codingboulder.headWars.game.HeadWarsGame;
import win.codingboulder.headWars.game.HeadWarsGameManager;
import win.codingboulder.headWars.game.shop.items.*;

import java.util.*;

public class ShopGui implements InventoryHolder, Listener {

    public static final String namespace = "headwars";
    public static final NamespacedKey itemIdKey = new NamespacedKey(namespace, "itemid");
    public static final NamespacedKey shopActionKey = new NamespacedKey(namespace, "shop_action");
    public static final NamespacedKey shopPriceKey = new NamespacedKey(namespace, "shop_price");
    public static final NamespacedKey shopItemKey = new NamespacedKey(namespace, "shop_item");
    public static final NamespacedKey shopMenuKey = new NamespacedKey(namespace, "shop_menu");

    public static NamespacedKey shopPriceItemKey = new NamespacedKey(namespace, "item");
    public static NamespacedKey shopPriceAmountKey = new NamespacedKey(namespace, "amount");

    private final ItemShop itemShop;
    private final Inventory inventory;
    private final Player player;
    private HeadWarsGame game;

    public static HashMap<String, CustomShopItem> customItemHandlers = new HashMap<>();
    static {

        customItemHandlers.put("wool", new wool());
        customItemHandlers.put("tool_pickaxe", new tool_pickaxe());
        customItemHandlers.put("sword", new sword());

        customItemHandlers.put("armor_helmet", new armor(EquipmentSlot.HEAD));
        customItemHandlers.put("armor_chestplate", new armor(EquipmentSlot.CHEST));
        customItemHandlers.put("armor_leggings", new armor(EquipmentSlot.LEGS));
        customItemHandlers.put("armor_boots", new armor(EquipmentSlot.FEET));

        customItemHandlers.put("upgrade_genLimit", new upgrade_genLimit());
        customItemHandlers.put("upgrade_protection", new upgrade_protection());

    }

    public ShopGui(ItemShop itemShop, Player player) {

        this.itemShop = itemShop;
        this.player = player;

        if (itemShop == null) {inventory = null; return;}
        inventory = HeadWars.getInstance().getServer().createInventory(this, itemShop.rows()*9, MiniMessage.miniMessage().deserialize(itemShop.title()));

        game = HeadWarsGameManager.playersInGames.get(player);

        renderItems();

    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public ItemShop itemShop() {
        return itemShop;
    }

    public Player player() {
        return player;
    }

    public HeadWarsGame game() {
        return game;
    }

    public void openShop(@NotNull Player player) {

        player.openInventory(inventory);

    }

    public void renderItems() {

        ItemStack[] items = itemShop.itemsArray();
        ItemStack[] renderedItems = new ItemStack[items.length];

        for (int i = 0; i < items.length; i++) {

            ItemStack shopItem = items[i];

            String itemId = getItemId(shopItem);
            if (itemId == null) renderedItems[i] = shopItem;

            if (customItemHandlers.containsKey(itemId)) renderedItems[i] = customItemHandlers.get(itemId).handleRender(shopItem, player, game, this);
            else renderedItems[i] = shopItem;

        }

        inventory.setContents(renderedItems);

    }

    public void reRenderItem(ItemStack item) {

        ItemStack[] items = inventory.getContents();

        for (int i = 0; i < items.length; i++) {

            ItemStack shopItem = items[i];

            if (item == null || shopItem == null || !Objects.equals(item, shopItem)) continue;

            String itemId = getItemId(shopItem);
            if (itemId == null) continue;
            if (!customItemHandlers.containsKey(itemId)) continue;

            ItemStack renderedItem = customItemHandlers.get(itemId).handleRender(shopItem, player, game, this);
            inventory.setItem(i, renderedItem);

        }

    }

    public static void handleItemBuy(ItemStack item, @NotNull ShopGui shop) {

        Player player = shop.player;

        if (!canPlayerAffordItem(player, item)) {
            player.sendRichMessage("<red>You can't afford this!");
            return;
        }

        ItemStack boughtItem;

        String itemId = getItemId(item);
        if (itemId == null) { // if it's a normal item

            boughtItem = getItemBought(item);

        } else { // if it's an item with custom handling

            if (customItemHandlers.containsKey(itemId)) boughtItem = customItemHandlers.get(itemId).handleBuy(item, player, shop.game, shop);
            else boughtItem = ItemStack.empty();

        }

        if (boughtItem != null && !boughtItem.isEmpty()) player.give(boughtItem);
        removeCostFromPlayer(item, player);

        player.playSound(player, Sound.BLOCK_NOTE_BLOCK_PLING, 2, 2);

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

        String shopAction = getItemAction(clickedItem);
        if (shopAction == null) return;

        if (shopAction.equals("buy")) {

            handleItemBuy(clickedItem, shopGui);

        } else if (shopAction.equals("open-menu")) {

            String menu = getItemMenu(clickedItem);
            if (menu == null) return;
            ItemShop shop = ItemShop.registeredItemShops.get(menu);
            if (shop == null) return;

            ShopGui newShop = new ShopGui(shop, player);
            Bukkit.getScheduler().runTaskLater(HeadWars.getInstance(), task -> newShop.openShop(player), 1);
            player.playSound(player, Sound.BLOCK_NOTE_BLOCK_HAT, 5, 1);

        }

    }

    public static String getItemId(@NotNull ItemStack item) {

        return item.getPersistentDataContainer().get(itemIdKey, PersistentDataType.STRING);

    }

    public static String getItemAction(@NotNull ItemStack item) {

        return item.getPersistentDataContainer().get(shopActionKey, PersistentDataType.STRING);

    }

    public static ItemStack getItemBought(@NotNull ItemStack item) {

        byte[] itemArr = item.getPersistentDataContainer().get(shopItemKey, PersistentDataType.BYTE_ARRAY);
        if (itemArr == null) return ItemStack.empty();
        return ItemStack.deserializeBytes(itemArr);

    }

    public static String getItemMenu(@NotNull ItemStack item) {

        return item.getPersistentDataContainer().get(shopMenuKey, PersistentDataType.STRING);

    }

    public static void setItemId(@NotNull ItemStack item, String id) {

        item.editPersistentDataContainer(pdc -> pdc.set(itemIdKey, PersistentDataType.STRING, id));

    }

    public static void setItemAction(@NotNull ItemStack item, String action) {

        item.editPersistentDataContainer(pdc -> pdc.set(shopActionKey, PersistentDataType.STRING, action));

    }

    public static void setItemBought(@NotNull ItemStack item, ItemStack boughtItem) {

        item.editPersistentDataContainer(pdc -> pdc.set(shopItemKey, PersistentDataType.BYTE_ARRAY, boughtItem.serializeAsBytes()));

    }

    public static void setItemMenu(@NotNull ItemStack item, String menu) {

        item.editPersistentDataContainer(pdc -> pdc.set(shopMenuKey, PersistentDataType.STRING, menu));

    }

    public static void addItemPrice(@NotNull ItemStack item, ItemStack price, int amount) {

        item.editPersistentDataContainer(pdc -> {

            PersistentDataContainer newPrice = pdc.getAdapterContext().newPersistentDataContainer();
            newPrice.set(shopPriceAmountKey, PersistentDataType.INTEGER, amount);
            newPrice.set(shopPriceItemKey, PersistentDataType.BYTE_ARRAY, item.serializeAsBytes());

            List<PersistentDataContainer> priceList = pdc.get(shopPriceKey, PersistentDataType.LIST.dataContainers());

            if (priceList == null) {
                pdc.set(shopPriceKey, PersistentDataType.LIST.dataContainers(), List.of(newPrice));
                return;
            }

            priceList.add(newPrice);
            pdc.set(shopPriceKey, PersistentDataType.LIST.dataContainers(), priceList);

        });

    }

    public static @NotNull HashMap<ItemStack, Integer> getItemPrice(@NotNull ItemStack item) {

        HashMap<ItemStack, Integer> itemPrice = new HashMap<>();

        List<PersistentDataContainer> priceList = item.getPersistentDataContainer().get(shopPriceKey, PersistentDataType.LIST.dataContainers());
        if (priceList == null) return itemPrice;

        for (PersistentDataContainer pdc : priceList) {

            byte[] priceArr = pdc.get(shopPriceItemKey, PersistentDataType.BYTE_ARRAY);
            if (priceArr == null) continue;
            ItemStack price = ItemStack.deserializeBytes(priceArr);

            Integer priceAmount = pdc.get(shopPriceAmountKey, PersistentDataType.INTEGER);
            if (priceAmount == null) continue;

            itemPrice.put(price, priceAmount);

        }

        return itemPrice;

    }

    public static void setItemPrice(@NotNull ItemStack item, @NotNull HashMap<ItemStack, Integer> price) {

        ArrayList<PersistentDataContainer> priceList = new ArrayList<>();

        price.forEach((priceItem, priceAmount) -> {

            PersistentDataContainer newPrice = item.getPersistentDataContainer().getAdapterContext().newPersistentDataContainer();
            newPrice.set(shopPriceAmountKey, PersistentDataType.INTEGER, priceAmount);
            newPrice.set(shopPriceItemKey, PersistentDataType.BYTE_ARRAY, priceItem.serializeAsBytes());

            priceList.add(newPrice);

        });

        item.editPersistentDataContainer(pdc -> pdc.set(shopPriceKey, PersistentDataType.LIST.dataContainers(), priceList));

    }

    public static boolean canPlayerAffordItem(@NotNull Player player, ItemStack item) {

        boolean canAfford = true;

        HashMap<ItemStack, Integer> itemPrice = getItemPrice(item);
        PlayerInventory playerInv = player.getInventory();

        for (Map.Entry<ItemStack, Integer> entry : itemPrice.entrySet())
            if (!playerInv.containsAtLeast(entry.getKey(), entry.getValue())) canAfford = false;

        return canAfford;

    }

    public static void removeCostFromPlayer(ItemStack item, @NotNull Player player) {

        HashMap<ItemStack, Integer> itemPrice = getItemPrice(item);

        PlayerInventory playerInventory = player.getInventory();
        itemPrice.forEach((priceItem, priceAmount) -> playerInventory.removeItem(getCostItemsArray(priceItem, priceAmount)));

    }

    public static ItemStack @NotNull [] getCostItemsArray(ItemStack item, int amount) {

        if (amount == 0) return new ItemStack[0];
        int stackSize = item.getMaxStackSize();
        ItemStack priceItem = item.asQuantity(stackSize);

        int fullStacks = amount / stackSize;
        int remainingItems = amount % stackSize;

        int stacks; if (remainingItems != 0) stacks = fullStacks + 1; else stacks = fullStacks;

        ItemStack[] items = new ItemStack[stacks];

        Arrays.fill(items, 0, fullStacks, priceItem);
        if (remainingItems > 0) items[fullStacks] = item.asQuantity(remainingItems);

        return items;

    }

    public static @NotNull HashMap<ItemStack, Integer> getSingleItemCost(ItemStack cost) {
        if (cost == null || cost.isEmpty()) return new HashMap<>();
        return new HashMap<>(Map.of(cost.asOne(), cost.getAmount()));
    }

}
