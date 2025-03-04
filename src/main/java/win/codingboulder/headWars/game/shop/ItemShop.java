package win.codingboulder.headWars.game.shop;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import win.codingboulder.headWars.HeadWars;

import java.io.*;
import java.util.ArrayList;

public class ItemShop implements Serializable {

    private String id;
    private String title;
    private int rows;

    private ArrayList<ItemStack> items;

    public ItemShop(String id, String title, int rows) {

        this.id = id;
        this.title = title;
        if (rows < 1) rows = 1;
        if (rows > 6) rows = 6;
        this.rows = rows;
        this.items = new ArrayList<>(rows*9);

    }

    @Serial
    private void writeObject(ObjectOutputStream out) throws IOException {

        ArrayList<byte[]> itemStacks = new ArrayList<>();
        for (ItemStack item : items) {
            if (item == null) itemStacks.add(null);
            else itemStacks.add(item.serializeAsBytes());
        }

        out.writeObject(id);
        out.writeObject(title);
        out.write(rows);
        out.writeObject(itemStacks);

    }

    @Serial
    private void readObject(@NotNull ObjectInputStream in) throws IOException, ClassNotFoundException {

        id = (String) in.readObject();
        title = (String) in.readObject();
        rows = in.read();
        items = new ArrayList<>();

        @SuppressWarnings("unchecked") ArrayList<byte[]> itemStacks = (ArrayList<byte[]>) in.readObject();
        itemStacks.forEach(item -> {
            if (item == null) items.add(null);
            else items.add(ItemStack.deserializeBytes(item));
        });

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

    public void openConfigGui(@NotNull Player player) {

        player.openInventory(new ShopConfigGUI(this).getInventory());

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

    public ArrayList<ItemStack> items() {
        return items;
    }

    public void items(ArrayList<ItemStack> items) {
        this.items = items;
    }

}
