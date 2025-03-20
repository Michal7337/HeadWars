package win.codingboulder.headWars.game.shop;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Unbreakable;
import io.papermc.paper.persistence.PersistentDataContainerView;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
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
import win.codingboulder.headWars.util.Util;

import java.util.List;
import java.util.Objects;

public class ShopGui implements InventoryHolder, Listener {

    private final Inventory inventory;
    private final ItemShop itemShop;
    private final Player player;

    public ShopGui(@NotNull ItemShop itemShop, Player player) { // add a HeadWarsGame here for game context

        this.itemShop = itemShop;
        this.player = player;

        inventory = HeadWars.getInstance().getServer().createInventory(this, itemShop.rows()*9, MiniMessage.miniMessage().deserialize(itemShop.title()));

        for (int i = 0; i < inventory.getSize(); i++) {

            ItemStack item = itemShop.items().get(i);
            if (item == null) continue;
            PersistentDataContainerView pdc = item.getPersistentDataContainer();
            if (!pdc.has(new NamespacedKey("headwars", "itemid"), PersistentDataType.STRING)) continue;

            itemShop.items().set(i, handleCustomItemRender(item, player));

        }

        inventory.setContents(itemShop.items().toArray(new ItemStack[0]));

    }

    /**
     * Empty constructor FOR REGISTERING EVENTS ONLY, DO NOT USE!
     */
    public ShopGui() {
        inventory = null;
        itemShop = null;
        player = null;
    }

    public @NotNull Inventory getInventory() {
        return inventory;
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
                player.give(item);

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

    private @NotNull ItemStack handleCustomItemRender(@NotNull ItemStack shopItem, Player player) {

        String itemId = shopItem.getPersistentDataContainer().get(new NamespacedKey("headwars", "itemid"), PersistentDataType.STRING);
        HeadWarsGame game = HeadWarsGameManager.playersInGames.get(player);

        switch (itemId) {

            case "wool" -> {

                ItemStack itemStack = shopItem.withType(Material.WHITE_WOOL);
                itemStack.lore(List.of(
                    Component.text("Cost: 4 Iron").color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)
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
            case "tool_pickaxe" -> {

                ItemStack item = ItemStack.of(Material.WOODEN_PICKAXE);
                item.editPersistentDataContainer(pdc -> {
                    pdc.set(new NamespacedKey("headwars", "itemid"), PersistentDataType.STRING, "tool_pickaxe");
                    pdc.set(new NamespacedKey("headwars", "shopaction"), PersistentDataType.STRING, "buy");
                });

                if (player.getInventory().contains(Material.WOODEN_PICKAXE)) {

                    item = item.withType(Material.STONE_PICKAXE);
                    item.lore(List.of(
                        Component.text("Cost: ", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)
                            .append(Component.text("48 Iron", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false))
                    ));

                    item.editPersistentDataContainer(pdc -> pdc.set(
                        new NamespacedKey("headwars", "shopprice"),
                        PersistentDataType.BYTE_ARRAY, ItemStack.of(Material.IRON_INGOT, 48).serializeAsBytes())
                    );

                } else if (player.getInventory().contains(Material.STONE_PICKAXE)) {

                    item = item.withType(Material.IRON_PICKAXE);
                    item.lore(List.of(
                        Component.text("Cost: ", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)
                            .append(Component.text("32 Gold", NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false))
                    ));

                    item.editPersistentDataContainer(pdc -> pdc.set(
                        new NamespacedKey("headwars", "shopprice"),
                        PersistentDataType.BYTE_ARRAY, ItemStack.of(Material.GOLD_INGOT, 32).serializeAsBytes())
                    );

                } else if (player.getInventory().contains(Material.IRON_PICKAXE)) {

                    item = item.withType(Material.DIAMOND_PICKAXE);
                    item.lore(List.of(
                        Component.text("Cost: ", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)
                            .append(Component.text("24 Diamond", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false))
                    ));

                    item.editPersistentDataContainer(pdc -> pdc.set(
                        new NamespacedKey("headwars", "shopprice"),
                        PersistentDataType.BYTE_ARRAY, ItemStack.of(Material.DIAMOND, 24).serializeAsBytes())
                    );

                } else {

                    item.lore(List.of(
                        Component.text("Cost: ", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)
                            .append(Component.text("24 Iron", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false))
                    ));
                    item.editPersistentDataContainer(pdc -> pdc.set(
                        new NamespacedKey("headwars", "shopprice"),
                        PersistentDataType.BYTE_ARRAY, ItemStack.of(Material.IRON_INGOT, 24).serializeAsBytes())
                    );

                }

                return item;

            }

            case null, default -> {
                return ItemStack.empty();
            }
        }

    }

    private @NotNull ItemStack handleCustomItemBuy(@NotNull ItemStack shopItem, Player player, Inventory inventory) {

        String itemId = shopItem.getPersistentDataContainer().get(new NamespacedKey("headwars", "itemid"), PersistentDataType.STRING);
        if (itemId == null) return ItemStack.empty();
        HeadWarsGame game = HeadWarsGameManager.playersInGames.get(player);

        switch (itemId) {

            case "wool" -> {

                Material woolColor;

                if (game == null) {
                    player.sendRichMessage("<red>You must be in a game to buy this!");
                    woolColor = Material.WHITE_WOOL;
                } else woolColor = Util.getWoolFromColor(game.playerTeams().get(player).mapTeam().getTeamColor());

                return ItemStack.of(woolColor, 16);

            }
            case "tool_pickaxe" -> {

                if (player.getInventory().contains(Material.WOODEN_PICKAXE)) {

                    for (int i = 0; i < player.getInventory().getContents().length; i++) {
                        ItemStack item = player.getInventory().getContents()[i];
                        if (item != null && item.getType().equals(Material.WOODEN_PICKAXE)) {
                            player.getInventory().setItem(i, item.withType(Material.STONE_PICKAXE));
                            break;
                        }
                    }

                } else if (player.getInventory().contains(Material.STONE_PICKAXE)) {

                    for (int i = 0; i < player.getInventory().getContents().length; i++) {
                        ItemStack item = player.getInventory().getContents()[i];
                        if (item != null && item.getType().equals(Material.STONE_PICKAXE)) {
                            player.getInventory().setItem(i, item.withType(Material.IRON_PICKAXE));
                            break;
                        }
                    }

                } else if (player.getInventory().contains(Material.IRON_PICKAXE)) {

                    for (int i = 0; i < player.getInventory().getContents().length; i++) {
                        ItemStack item = player.getInventory().getContents()[i];
                        if (item != null && item.getType().equals(Material.IRON_PICKAXE)) {
                            player.getInventory().setItem(i, item.withType(Material.DIAMOND_PICKAXE));
                            break;
                        }
                    }

                } else {

                    ItemStack item = ItemStack.of(Material.WOODEN_PICKAXE);
                    //noinspection UnstableApiUsage
                    item.setData(DataComponentTypes.UNBREAKABLE, Unbreakable.unbreakable(false));
                    Bukkit.getScheduler().runTaskLater(HeadWars.getInstance(), task -> reRenderItem(shopItem, player, inventory), 1);
                    return item;

                }

                reRenderItem(shopItem, player, inventory);

                return ItemStack.empty();

            }

            default -> {
                return ItemStack.empty();
            }

        }

    }

    private void reRenderItem(ItemStack shopItem, Player player, @NotNull Inventory inventory) {

        for (int i = 0; i < inventory.getSize(); i++) if (Objects.equals(inventory.getContents()[i], shopItem)) inventory.setItem(i, handleCustomItemRender(Objects.requireNonNull(inventory.getContents()[i]), player));

    }

}
