package win.codingboulder.headWars.game.shop;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import win.codingboulder.headWars.HeadWars;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

public class ShopManager {

    public static HashMap<String, ItemShop> itemShops = new HashMap<>();

    private ShopManager() {}

    public static void loadAllShops() {

        if (HeadWars.getShopsFolder().listFiles() == null) return;
        itemShops = new HashMap<>();
        for (File file : Objects.requireNonNull(HeadWars.getShopsFolder().listFiles())) {

            ItemShop itemShop = ItemShop.readFromFile(file);
            if (itemShop == null) continue;
            itemShops.put(itemShop.id(), itemShop);

        }

    }

    public static @NotNull ItemShop createShop(String id, String name, int rows) {

        if (rows < 1) rows = 1;
        if (rows > 6) rows = 6;
        ItemShop itemShop = new ItemShop(id, name, rows);

        ItemStack[] itemsArr = new ItemStack[rows*9];
        Arrays.fill(itemsArr, null);
        itemShop.items(new ArrayList<>(Arrays.asList(itemsArr)));

        itemShop.storeToFile();
        loadAllShops();
        return itemShop;

    }

}
