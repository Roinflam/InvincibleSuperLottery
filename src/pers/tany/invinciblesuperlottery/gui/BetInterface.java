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
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import pers.tany.invinciblesuperlottery.Main;
import pers.tany.invinciblesuperlottery.utils.BetUtil;
import pers.tany.invinciblesuperlottery.utils.ItemUtil;
import pers.tany.yukinoaapi.interfacepart.builder.IItemBuilder;
import pers.tany.yukinoaapi.interfacepart.inventory.IInventory;
import pers.tany.yukinoaapi.interfacepart.item.IItem;
import pers.tany.yukinoaapi.interfacepart.other.IList;
import pers.tany.yukinoaapi.interfacepart.other.IRandom;
import pers.tany.yukinoaapi.interfacepart.other.IString;
import pers.tany.yukinoaapi.interfacepart.player.IPlayer;
import pers.tany.yukinoaapi.interfacepart.serializer.ISerializer;
import pers.tany.yukinoaapi.realizationpart.builder.ItemBuilder;
import pers.tany.yukinoaapi.realizationpart.container.KeyToValue;
import pers.tany.yukinoaapi.realizationpart.item.GlassPaneUtil;
import pers.tany.yukinoaapi.realizationpart.item.WoolUtil;
import pers.tany.yukinoaapi.realizationpart.player.Ask;

import java.util.ArrayList;
import java.util.List;

public class BetInterface implements InventoryHolder, Listener {
    private final String serial;
    private final Inventory inventory;
    private final Player player;

    private boolean oddAndEven = false;

    private boolean guessingSize = false;

    private int number = -1;

    private boolean close = false;

    private BetType betType = null;

    public BetInterface(Player player) {
        this(player, -1);
    }

    public BetInterface(Player player, int number) {

        this.player = player;
        this.number = number;
        this.inventory = Bukkit.createInventory(this, 54, IString.color(Main.message.getString("Gui.Title")));
        this.serial = IRandom.createRandomString(8);

        Bukkit.getPluginManager().registerEvents(this, Main.plugin);

        if (number != -1) {
            if (number < 10) {
                betType = BetType.NUMBER_RANGE;
            } else {
                betType = BetType.NUMBER;
            }
        }

        update();
    }

    private void update() {
        inventory.clear();
        IItemBuilder frame = GlassPaneUtil.getStainedGlass(1);
        IItemBuilder odd = WoolUtil.getWool(1);
        IItemBuilder even = WoolUtil.getWool(2);
        IItemBuilder small = WoolUtil.getWool(3);
        IItemBuilder big = WoolUtil.getWool(4);
        IItemBuilder number = WoolUtil.getWool(14);

        IItemBuilder money = new ItemBuilder("GOLD_INGOT");
        IItemBuilder exp;
        try {
            exp = new ItemBuilder("EXP_BOTTLE");
        } catch (Exception e) {
            exp = new ItemBuilder("EXPERIENCE_BOTTLE");
        }
        IItemBuilder item;
        try {
            item = new ItemBuilder("STORAGE_MINECART");
        } catch (Exception e) {
            item = new ItemBuilder("CHEST_MINECRAFT");
        }

        frame.setDisplayName(Main.message.getString("Gui.HelpName")).setLore(Main.message.getStringList("Gui.HelpLore"));
        odd.setDisplayName(Main.message.getString("Gui.OddName")).setLore(Main.message.getStringList("Gui.OddLore"));
        even.setDisplayName(Main.message.getString("Gui.EvenName")).setLore(Main.message.getStringList("Gui.OddLore"));
        small.setDisplayName(Main.message.getString("Gui.SmallName")).setLore(Main.message.getStringList("Gui.SmallLore"));
        big.setDisplayName(Main.message.getString("Gui.BigName")).setLore(Main.message.getStringList("Gui.BigLore"));
        number.setDisplayName(Main.message.getString("Gui.NumberName")).setLore(IList.listReplace(Main.message.getStringList("Gui.NumberLore"), "[number]", (this.number == -1 ? "未选择" : this.number) + ""));

        money.setDisplayName(Main.message.getString("Gui.MoneyName")).setLore(Main.message.getStringList("Gui.MoneyLore"));
        exp.setDisplayName(Main.message.getString("Gui.ExpName")).setLore(Main.message.getStringList("Gui.ExpLore"));
        item.setDisplayName(Main.message.getString("Gui.ItemName")).setLore(Main.message.getStringList("Gui.ItemLore"));

        for (int i = 0; i < 9; i++) {
            inventory.setItem(i, frame.getItemStack());
            inventory.setItem(i + 36, frame.getItemStack());
        }
        inventory.setItem(50, frame.getItemStack());

        inventory.setItem(45, odd.getItemStack());
        inventory.setItem(46, even.getItemStack());
        inventory.setItem(47, small.getItemStack());
        inventory.setItem(48, big.getItemStack());
        inventory.setItem(49, number.getItemStack());
        inventory.setItem(51, money.getItemStack());
        inventory.setItem(52, exp.getItemStack());
        inventory.setItem(53, item.getItemStack());
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
                if (evt.getInventory().getHolder() instanceof BetInterface) {
                    if (evt.getClickedInventory().getHolder() instanceof BetInterface) {
                        if (!IItem.isEmpty(evt.getCurrentItem())) {
                            if (rawSlot < 9 || rawSlot >= 36) {
                                evt.setCancelled(true);
                            }
                            if (rawSlot == 45) {
                                oddAndEven = false;
                                betType = BetType.ODD_AND_EVEN;
                                player.sendMessage(IString.color(Main.message.getString("Gui.Choose")));
                            } else if (rawSlot == 46) {
                                oddAndEven = true;
                                betType = BetType.ODD_AND_EVEN;
                                player.sendMessage(IString.color(Main.message.getString("Gui.Choose")));
                            } else if (rawSlot == 47) {
                                guessingSize = false;
                                betType = BetType.GUESSING_SIZE;
                                player.sendMessage(IString.color(Main.message.getString("Gui.Choose")));
                            } else if (rawSlot == 48) {
                                guessingSize = true;
                                betType = BetType.GUESSING_SIZE;
                                player.sendMessage(IString.color(Main.message.getString("Gui.Choose")));
                            } else if (rawSlot == 49) {
                                player.closeInventory();
                                new BukkitRunnable() {

                                    @Override
                                    public void run() {
                                        player.sendMessage(IString.color(Main.message.getString("Gui.ChooseNumber").replace("[min]", "0").replace("[max]", "99")));
                                        while (true) {
                                            Ask ask = new Ask(player, 15);
                                            String answer = ask.getAnswer();
                                            if (ask.getReason().equals(Ask.Reason.answer)) {
                                                try {
                                                    int number = Integer.parseInt(answer);
                                                    if (number < 0 || number >= 100) {
                                                        throw new NumberFormatException();
                                                    }
                                                    IInventory.openInventory(new BetInterface(player, number), player);
                                                    player.sendMessage(IString.color(Main.message.getString("Gui.Choose")));
                                                    return;
                                                } catch (NumberFormatException e) {
                                                    player.sendMessage(IString.color(Main.message.getString("Gui.ChooseNumber").replace("[min]", "0").replace("[max]", "99")));
                                                }
                                            } else if (ask.getReason().equals(Ask.Reason.when)) {
                                                player.sendMessage(IString.color(Main.message.getString("Gui.Timeout")));
                                                return;
                                            } else {
                                                return;
                                            }
                                        }
                                    }

                                }.runTaskAsynchronously(Main.plugin);
                            } else if (rawSlot == 51) {
                                if (Main.config.getInt("BetType.Money") <= 0) {
                                    player.sendMessage(IString.color(Main.message.getString("Gui.DisableMoney")));
                                    return;
                                }
                                if (betType == null) {
                                    player.sendMessage(IString.color(Main.message.getString("Gui.NotBetType")));
                                    return;
                                }
                                for (int i = 9; i < 36; i++) {
                                    ItemStack itemStack = evt.getInventory().getItem(i);
                                    if (!IItem.isEmpty(itemStack)) {
                                        player.sendMessage(IString.color(Main.message.getString("Gui.DepositProhibited")));
                                        return;
                                    }
                                }
                                player.closeInventory();
                                new BukkitRunnable() {

                                    @Override
                                    public void run() {
                                        player.sendMessage(IString.color(Main.message.getString("Gui.ChooseNumber").replace("[min]", "1").replace("[max]", Main.config.getInt("BetType.Money") + "")));
                                        while (true) {
                                            Ask ask = new Ask(player, 30);
                                            String answer = ask.getAnswer();
                                            if (ask.getReason().equals(Ask.Reason.answer)) {
                                                try {
                                                    int money = Integer.parseInt(answer);
                                                    if (money <= 0 || money > Main.config.getInt("BetType.Money")) {
                                                        throw new NumberFormatException();
                                                    }
                                                    if (Main.economy.has(player, money)) {
                                                        Main.economy.withdrawPlayer(player, money);
                                                        if (betType.equals(BetType.ODD_AND_EVEN)) {
                                                            BetUtil.bet.put(player.getName(), new KeyToValue<>(betType, oddAndEven));
                                                        } else if (betType.equals(BetType.GUESSING_SIZE)) {
                                                            BetUtil.bet.put(player.getName(), new KeyToValue<>(betType, guessingSize));
                                                        } else {
                                                            BetUtil.bet.put(player.getName(), new KeyToValue<>(betType, number));
                                                        }
                                                        BetUtil.betMoney.put(player.getName(), money);
                                                        player.sendMessage(IString.color(Main.message.getString("Gui.Success")));
                                                    } else {
                                                        player.sendMessage(IString.color(Main.message.getString("Gui.NotEnoughMoney").replace("[money]", Main.economy.getBalance(player) + "")));
                                                    }
                                                    return;
                                                } catch (NumberFormatException e) {
                                                    player.sendMessage(IString.color(Main.message.getString("Gui.ChooseNumber").replace("[min]", "1").replace("[max]", Main.config.getInt("BetType.Money") + "")));
                                                }
                                            } else if (ask.getReason().equals(Ask.Reason.when)) {
                                                player.sendMessage(IString.color(Main.message.getString("Gui.Timeout")));
                                                return;
                                            } else {
                                                return;
                                            }
                                        }
                                    }

                                }.runTaskAsynchronously(Main.plugin);
                            } else if (rawSlot == 52) {
                                if (Main.config.getInt("BetType.Exp") <= 0) {
                                    player.sendMessage(IString.color(Main.message.getString("Gui.DisableExp")));
                                    return;
                                }
                                if (betType == null) {
                                    player.sendMessage(IString.color(Main.message.getString("Gui.NotBetType")));
                                    return;
                                }
                                for (int i = 9; i < 36; i++) {
                                    ItemStack itemStack = evt.getInventory().getItem(i);
                                    if (!IItem.isEmpty(itemStack)) {
                                        player.sendMessage(IString.color(Main.message.getString("Gui.DepositProhibited")));
                                        return;
                                    }
                                }
                                player.closeInventory();
                                new BukkitRunnable() {

                                    @Override
                                    public void run() {
                                        player.sendMessage(IString.color(Main.message.getString("Gui.ChooseNumber").replace("[min]", "1").replace("[max]", Main.config.getInt("BetType.Exp") + "")));
                                        while (true) {
                                            Ask ask = new Ask(player, 30);
                                            String answer = ask.getAnswer();
                                            if (ask.getReason().equals(Ask.Reason.answer)) {
                                                try {
                                                    int exp = Integer.parseInt(answer);
                                                    if (exp <= 0 || exp > Main.config.getInt("BetType.Exp")) {
                                                        throw new NumberFormatException();
                                                    }
                                                    if (player.getTotalExperience() >= exp) {
                                                        IPlayer.giveExp(player, -exp);
                                                        if (betType.equals(BetType.ODD_AND_EVEN)) {
                                                            BetUtil.bet.put(player.getName(), new KeyToValue<>(betType, oddAndEven));
                                                        } else if (betType.equals(BetType.GUESSING_SIZE)) {
                                                            BetUtil.bet.put(player.getName(), new KeyToValue<>(betType, guessingSize));
                                                        } else {
                                                            BetUtil.bet.put(player.getName(), new KeyToValue<>(betType, number));
                                                        }
                                                        BetUtil.setBetExp(player.getName(), exp);
                                                        player.sendMessage(IString.color(Main.message.getString("Gui.Success")));
                                                    } else {
                                                        player.sendMessage(IString.color(Main.message.getString("Gui.NotEnoughExp").replace("[exp]", player.getTotalExperience() + "")));
                                                    }
                                                    return;
                                                } catch (NumberFormatException e) {
                                                    player.sendMessage(IString.color(Main.message.getString("Gui.ChooseNumber").replace("[min]", "1").replace("[max]", Main.config.getInt("BetType.Exp") + "")));
                                                }
                                            } else if (ask.getReason().equals(Ask.Reason.when)) {
                                                player.sendMessage(IString.color(Main.message.getString("Gui.Timeout")));
                                                return;
                                            } else {
                                                return;
                                            }
                                        }
                                    }

                                }.runTaskAsynchronously(Main.plugin);
                            } else if (rawSlot == 53) {
                                if (betType == null) {
                                    player.sendMessage(IString.color(Main.message.getString("Gui.NotBetType")));
                                    return;
                                }
                                List<ItemStack> itemStackList = new ArrayList<>();
                                for (int i = 9; i < 36; i++) {
                                    ItemStack itemStack = evt.getInventory().getItem(i);
                                    if (!IItem.isEmpty(itemStack)) {
                                        if (ItemUtil.isAllowedItem(itemStack)) {
                                            itemStackList.add(itemStack);
                                        } else {
                                            player.sendMessage(IString.color(Main.message.getString("Gui.ImpermissibleItem")));
                                            return;
                                        }
                                    }
                                }
                                if (itemStackList.size() > 0) {
                                    List<String> itemBet = BetUtil.getBetItem(player.getName());
                                    for (ItemStack itemStack : itemStackList) {
                                        itemBet.add(ISerializer.serializerItemStack(itemStack));
                                    }
                                    if (betType.equals(BetType.ODD_AND_EVEN)) {
                                        BetUtil.bet.put(player.getName(), new KeyToValue<>(betType, oddAndEven));
                                    } else if (betType.equals(BetType.GUESSING_SIZE)) {
                                        BetUtil.bet.put(player.getName(), new KeyToValue<>(betType, guessingSize));
                                    } else {
                                        BetUtil.bet.put(player.getName(), new KeyToValue<>(betType, number));
                                    }
                                    BetUtil.setBetItem(player.getName(), itemBet);
                                    player.sendMessage(IString.color(Main.message.getString("Gui.Success")));
                                    close = true;
                                    player.closeInventory();
                                } else {
                                    player.sendMessage(IString.color(Main.message.getString("Gui.NotEnoughItem")));
                                }
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
        if (evt.getInventory().getHolder() instanceof BetInterface && evt.getPlayer() instanceof Player) {
            BetInterface betInterface = (BetInterface) evt.getInventory().getHolder();
            if (evt.getPlayer().equals(player) && betInterface.getSerial().equals(serial)) {
                HandlerList.unregisterAll(this);
                if (!close) {
                    for (int i = 9; i < 36; i++) {
                        ItemStack itemStack = evt.getInventory().getItem(i);
                        if (!IItem.isEmpty(itemStack)) {
                            IPlayer.giveItem((Player) evt.getPlayer(), itemStack);
                        }
                    }
                }
            }
        }
    }

    public enum BetType {
        ODD_AND_EVEN,

        GUESSING_SIZE,

        NUMBER_RANGE,

        NUMBER;

        BetType() {
        }
    }
}
