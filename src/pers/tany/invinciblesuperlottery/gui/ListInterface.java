package pers.tany.invinciblesuperlottery.gui;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import pers.tany.invinciblesuperlottery.Main;
import pers.tany.yukinoaapi.interfacepart.builder.IItemBuilder;
import pers.tany.yukinoaapi.interfacepart.configuration.IConfig;
import pers.tany.yukinoaapi.interfacepart.item.IItem;
import pers.tany.yukinoaapi.interfacepart.other.IRandom;
import pers.tany.yukinoaapi.interfacepart.other.IString;
import pers.tany.yukinoaapi.interfacepart.serializer.ISerializer;
import pers.tany.yukinoaapi.realizationpart.builder.ItemBuilder;
import pers.tany.yukinoaapi.realizationpart.item.GlassPaneUtil;

import java.util.HashMap;
import java.util.List;

public class ListInterface implements InventoryHolder, Listener {
    private final String serial;
    private final Inventory inventory;
    private final Player player;
    private final HashMap<Integer, String> hashMap = new HashMap<>();
    private int page;
    private boolean hasLast;
    private boolean hasNext;

    public ListInterface(Player player, int page) {
        this.inventory = Bukkit.createInventory(this, 54, IString.color(Main.message.getString("List.Title")));
        this.player = player;
        this.page = page;
        this.serial = IRandom.createRandomString(8);

        Bukkit.getPluginManager().registerEvents(this, Main.plugin);

        update(page);
    }

    private void update(int page) {
        inventory.clear();
        hashMap.clear();
        List<String> list = Main.data.getStringList("BetItemList");

        IItemBuilder frame = GlassPaneUtil.getStainedGlass(6);
        IItemBuilder last = GlassPaneUtil.getStainedGlass(11);
        IItemBuilder next = GlassPaneUtil.getStainedGlass(1);

        frame.setDisplayName(Main.message.getString("List.HelpName"));
        last.setDisplayName(Main.message.getString("List.LastName"));
        next.setDisplayName(Main.message.getString("List.NextName"));

        frame.addLoreAll(Main.message.getStringList("List.HelpLore"));
        last.addLoreAll(Main.message.getStringList("List.LastLore"));
        next.addLoreAll(Main.message.getStringList("List.NextLore"));

        if (page > 1) {
            inventory.setItem(45, last.getItemStack());
            hasLast = true;
        } else {
            inventory.setItem(45, frame.getItemStack());
            hasLast = false;
        }
        for (int i = 46; i <= 52; i++) {
            inventory.setItem(i, frame.getItemStack());
        }
        if (list.size() > 45 + (page - 1) * 45) {
            inventory.setItem(53, next.getItemStack());
            hasNext = true;
        } else {
            inventory.setItem(53, frame.getItemStack());
            hasNext = false;
        }

        int index = (page - 1) * 45;
        int location = 0;
        int size = list.size() - 1;
        while (index <= size && index <= 44 + (page - 1) * 45) {
            String str = list.get(index);
            if (Main.config.getBoolean("BetType.Range")) {
                IItemBuilder itemBuilder = new ItemBuilder(str.split(":")[0], 1, Short.parseShort(str.split(":")[1]));
                if (player.isOp()) {
                    itemBuilder.addLore("&f&lShift+点击移除此物品");
                }

                hashMap.put(location, str);
                inventory.setItem(location++, itemBuilder.getItemStack());
            } else {
                IItemBuilder itemBuilder = new ItemBuilder(ISerializer.deserializeItemStack(str));
                if (player.isOp()) {
                    itemBuilder.addLore("&7Shift+点击移除此物品");
                }

                hashMap.put(location, str);
                inventory.setItem(location++, itemBuilder.getItemStack());
            }
            index++;
        }
    }


    @Override
    public Inventory getInventory() {
        return inventory;
    }

    @EventHandler
    private void onInventoryClick(InventoryClickEvent evt) {
        if (evt.getWhoClicked() instanceof Player && evt.getWhoClicked().equals(player)) {
            int rawSlot = evt.getRawSlot();
            if (rawSlot != -999) {
                if (evt.getInventory().getHolder() instanceof ListInterface) {
                    evt.setCancelled(true);
                    if (evt.getClickedInventory().getHolder() instanceof ListInterface) {
                        if (!IItem.isEmpty(evt.getCurrentItem())) {
                            if (rawSlot == 45 && hasLast) {
                                update(--page);
                            } else if (rawSlot == 53 && hasNext) {
                                update(++page);
                            } else if (rawSlot < 45 && evt.isShiftClick() && player.isOp()) {
                                List<String> list = Main.data.getStringList("BetItemList");
                                list.remove(hashMap.get(rawSlot));
                                Main.data.set("BetItemList", list);
                                IConfig.saveConfig(Main.plugin, Main.data, "", "data");
                                update(1);
                            }
                        }
                    }
                }
            }
        }
    }

    public String getSerial() {
        return serial;
    }

    @EventHandler
    private void onInventoryClose(InventoryCloseEvent evt) {
        if (evt.getInventory().getHolder() instanceof ListInterface && evt.getPlayer() instanceof Player) {
            ListInterface listInterface = (ListInterface) evt.getInventory().getHolder();
            if (evt.getPlayer().equals(player) && listInterface.getSerial().equals(serial)) {
                HandlerList.unregisterAll(this);
            }
        }
    }
}
