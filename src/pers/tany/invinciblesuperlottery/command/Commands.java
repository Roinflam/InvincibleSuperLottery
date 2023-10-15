package pers.tany.invinciblesuperlottery.command;


import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import pers.tany.invinciblesuperlottery.Main;
import pers.tany.invinciblesuperlottery.gui.BetInterface;
import pers.tany.invinciblesuperlottery.gui.ListInterface;
import pers.tany.invinciblesuperlottery.utils.BetUtil;
import pers.tany.yukinoaapi.interfacepart.configuration.IConfig;
import pers.tany.yukinoaapi.interfacepart.inventory.IInventory;
import pers.tany.yukinoaapi.interfacepart.item.IItem;
import pers.tany.yukinoaapi.interfacepart.other.IString;
import pers.tany.yukinoaapi.interfacepart.player.IPlayer;
import pers.tany.yukinoaapi.interfacepart.serializer.ISerializer;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Commands implements CommandExecutor {
    public static HashMap<String, String> bindMap = new HashMap<>();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("reload")) {
                if (!sender.isOp()) {
                    sender.sendMessage("§c你没有权限使用此指令！");
                    return true;
                }
                Main.config = YamlConfiguration.loadConfiguration(new File(Main.plugin.getDataFolder(), "config.yml"));
                Main.data = YamlConfiguration.loadConfiguration(new File(Main.plugin.getDataFolder(), "data.yml"));
                Main.message = YamlConfiguration.loadConfiguration(new File(Main.plugin.getDataFolder(), "message.yml"));
                sender.sendMessage("§a重载成功");
                return true;
            }
            if (args[0].equalsIgnoreCase("start")) {
                if (!sender.isOp()) {
                    sender.sendMessage("§c你没有权限使用此指令！");
                    return true;
                }
                if (Main.lotteryTask.isStartBetting()) {
                    sender.sendMessage("§6§l正在结算开奖...请等待5秒");
                    new BukkitRunnable() {

                        @Override
                        public void run() {
                            Main.lotteryTask.lottery(false);
                        }

                    }.runTaskLater(Main.plugin, 100);
                } else {
                    sender.sendMessage("§c本轮未开始下注，无法开奖！");
                }
                return true;
            }
        }
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("random")) {
                if (!sender.isOp()) {
                    sender.sendMessage("§c你没有权限使用此指令！");
                    return true;
                }
                try {
                    int random = Integer.parseInt(args[1]);
                    if (random < 0 || random > 100) {
                        throw new NumberFormatException();
                    }
                    Main.lotteryTask.setRandom(random);
                    if (random == 100) {
                        sender.sendMessage("§a成功设置下次开奖为§4§l “通吃”");
                    } else {
                        sender.sendMessage("§a成功设置下次开奖号码为§6§l “" + random + "”");
                    }
                } catch (NumberFormatException e) {
                    sender.sendMessage("§c请输入0-100的整数！");
                    return true;
                }
                return true;
            }
        }
        if (sender instanceof ConsoleCommandSender) {
            sender.sendMessage("§7/isl start  §f手动开奖当前轮次");
            sender.sendMessage("§7/isl random 数值  §f手动设置下次开奖数值，范围1-100（§4§l请谨慎使用黑幕！§f）");
            sender.sendMessage("§7/isl reload  §f重载配置文件");
            return true;
        }
        Player player = (Player) sender;
        String name = player.getName();
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("draw")) {
                if (!player.hasPermission("isl.draw")) {
                    player.sendMessage("§c你没有权限使用此指令");
                    return true;
                }
                if (BetUtil.bet.containsKey(name)) {
                    player.sendMessage(IString.color(Main.message.getString("Draw.NotDraw")));
                    return true;
                }
                boolean draw = false;
                if (BetUtil.getBetExp(name) > 0) {
                    IPlayer.giveExp(player, BetUtil.getBetExp(name));
                    BetUtil.setBetExp(name, 0);
                    draw = true;
                }
                if (BetUtil.getBetItem(name).size() > 0) {
                    for (String data : BetUtil.getBetItem(name)) {
                        ItemStack itemStack = ISerializer.deserializeItemStack(data);
                        IPlayer.giveItem(player, itemStack);
                    }
                    BetUtil.setBetItem(name, new ArrayList<>());
                    draw = true;
                }
                if (draw) {
                    player.sendMessage(IString.color(Main.message.getString("Draw.Draw")));
                } else {
                    player.sendMessage(IString.color(Main.message.getString("Draw.NotDraw")));
                }
                return true;
            }
            if (args[0].equalsIgnoreCase("add")) {
                if (!sender.isOp()) {
                    sender.sendMessage("§c你没有权限使用此指令！");
                    return true;
                }
                if (IItem.isEmptyHand(player)) {
                    sender.sendMessage("§c手上物品不能为空！");
                    return true;
                }
                ItemStack itemStack = player.getItemInHand();
                List<String> list = Main.data.getStringList("BetItemList");
                if (Main.config.getBoolean("BetType.Range")) {
                    String str = itemStack.getType().toString() + ":" + itemStack.getDurability();
                    if (list.contains(str)) {
                        sender.sendMessage("§c此物品已经添加过了！");
                        return true;
                    }
                    list.add(str);
                } else {
                    String str = ISerializer.serializerItemStack(itemStack, true);
                    if (list.contains(str)) {
                        sender.sendMessage("§c此物品已经添加过了！");
                        return true;
                    }
                    list.add(str);
                }
                Main.data.set("BetItemList", list);
                IConfig.saveConfig(Main.plugin, Main.data, "", "data");
                sender.sendMessage("§a添加到可下注物品列表成功！");
                return true;
            }
            if (args[0].equalsIgnoreCase("list")) {
                if(!player.hasPermission("isl.list")){
                    player.sendMessage("§c你没有权限使用此指令");
                    return true;
                }
                IInventory.openInventory(new ListInterface(player, 1), player);
                return true;
            }
            if (args[0].equalsIgnoreCase("bet")) {
                if(!player.hasPermission("isl.bet")){
                    player.sendMessage("§c你没有权限使用此指令");
                    return true;
                }
                if (!Main.lotteryTask.isStartBetting()) {
                    player.sendMessage(IString.color(Main.message.getString("Bet.NotBetTime")));
                    return true;
                }
                if (Main.lotteryTask.isStopBetting()) {
                    player.sendMessage(IString.color(Main.message.getString("Bet.StopBetTime")));
                    return true;
                }
                if (BetUtil.bet.containsKey(player.getName())) {
                    player.sendMessage(IString.color(Main.message.getString("Bet.AlreadyBet")));
                    return true;
                }
                if (BetUtil.getBetExp(name) > 0 || BetUtil.getBetItem(name).size() > 0) {
                    player.sendMessage(IString.color(Main.message.getString("Bet.Draw")));
                    return true;
                }
                IInventory.openInventory(new BetInterface(player), player);
                return true;
            }
        }
        if (sender.isOp()) {
            sender.sendMessage("§7/isl draw  §f领取之前下注未开奖物品");
            sender.sendMessage("§7/isl list  §f查看可下注物品");
            sender.sendMessage("§7/isl bet  §f打开下注界面");
            sender.sendMessage("§7/isl add  §f添加手上物品到可下注列表里");
            sender.sendMessage("§7/isl start  §f手动开奖当前轮次");
            sender.sendMessage("§7/isl random 数值  §f手动设置下次开奖数值，范围1-100 （§4§l请谨慎使用黑幕！§f）");
            sender.sendMessage("§7/isl reload  §f重载配置文件");
        } else {
            sender.sendMessage("§7/isl draw  §f领取之前下注未开奖物品");
            sender.sendMessage("§7/isl list  §f查看可下注物品");
            sender.sendMessage("§7/isl bet  §f打开下注界面");
        }
        return true;
    }
}
