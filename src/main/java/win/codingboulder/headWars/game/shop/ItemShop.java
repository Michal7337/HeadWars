package win.codingboulder.headWars.game.shop;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import win.codingboulder.headWars.HeadWars;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

public class ItemShop implements Serializable {

    public static HashMap<String, ItemShop> registeredItemShops = new HashMap<>();

    private String id;
    private String title;
    private int rows;

    private ArrayList<ItemStack> items;
    private ItemStack[] itemsArray;

    public ItemShop(String id, String title, int rows) {

        this.id = id;
        this.title = title;
        if (rows < 1) rows = 1;
        if (rows > 6) rows = 6;
        this.rows = rows;
        this.items = new ArrayList<>(rows*9);

    }

    @Serial
    private void writeObject(@NotNull ObjectOutputStream out) throws IOException {

        byte[] itemsArray = ItemStack.serializeItemsAsBytes(items);

        out.writeObject(id);
        out.writeObject(title);
        out.write(rows);

        out.writeInt(itemsArray.length);
        out.write(itemsArray);

    }

    @Serial
    private void readObject(@NotNull ObjectInputStream in) throws IOException, ClassNotFoundException {

        id = (String) in.readObject();
        title = (String) in.readObject();
        rows = in.read();

        int arraySize = in.readInt();
        byte[] itemsArrayB = in.readNBytes(arraySize);
        itemsArray = ItemStack.deserializeItemsFromBytes(itemsArrayB);
        items = new ArrayList<>(Arrays.asList(itemsArray));

    }

    public void storeToFile() {

        try { ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(HeadWars.getShopsFolder(), id + ".itemshop")));

            oos.writeObject(this);
            oos.flush();
            oos.close();

        } catch (IOException e) { throw new RuntimeException(e); }

    }

    public static @Nullable ItemShop readFromFile(String id) {

        try {ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File(HeadWars.getShopsFolder(), id + ".itemshop")));

            ItemShop itemShop = (ItemShop) ois.readObject();
            ois.close();
            return itemShop;

        } catch (IOException | ClassNotFoundException e) {HeadWars.getInstance().getLogger().warning(e.getMessage()); return null;}

    }

    public static @Nullable ItemShop readFromFile(File file) {

        try {ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));

            ItemShop itemShop = (ItemShop) ois.readObject();
            ois.close();
            return itemShop;

        } catch (IOException | ClassNotFoundException e) {HeadWars.getInstance().getLogger().warning(e.getMessage()); return null;}

    }

    public static void loadAllShops() {

        if (HeadWars.getShopsFolder().listFiles() == null) return;
        registeredItemShops = new HashMap<>();
        for (File file : Objects.requireNonNull(HeadWars.getShopsFolder().listFiles())) {

            ItemShop itemShop = ItemShop.readFromFile(file);
            if (itemShop == null) continue;
            registeredItemShops.put(itemShop.id(), itemShop);

        }

    }

    public static @NotNull ItemShop createShop(String id, String name, int rows) {

        if (rows < 1) rows = 1;
        if (rows > 6) rows = 6;
        ItemShop itemShop = new ItemShop(id, name, rows);

        ItemStack[] itemsArr = new ItemStack[rows*9];
        Arrays.fill(itemsArr, null);
        itemShop.setItems(new ArrayList<>(Arrays.asList(itemsArr)));

        itemShop.storeToFile();
        loadAllShops();
        return itemShop;

    }

    public void openConfigGui(@NotNull Player player) {

        player.openInventory(new ShopConfigGUI(this).getInventory());

    }

    public void openShop(@NotNull Player player) {

        player.openInventory(new ShopGui(this, player).getInventory());

    }

    public String id() {
        return id;
    }

    public void id(String id) {
        this.id = id;
    }

    public String title() {
        return title;
    }

    public void title(String name) {
        this.title = name;
    }

    public int rows() {
        return rows;
    }

    public void rows(int rows) {
        this.rows = rows;
    }

    public ArrayList<ItemStack> getItems() {
        return items;
    }

    public void setItems(ArrayList<ItemStack> items) {
        this.items = items;
    }

    public ItemStack[] itemsArray() {
        return itemsArray;
    }

}
