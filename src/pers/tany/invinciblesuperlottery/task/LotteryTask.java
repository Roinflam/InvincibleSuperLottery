package pers.tany.invinciblesuperlottery.task;

import org.apache.logging.log4j.util.Strings;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import pers.tany.invinciblesuperlottery.Main;
import pers.tany.invinciblesuperlottery.gui.BetInterface;
import pers.tany.invinciblesuperlottery.utils.BetUtil;
import pers.tany.yukinoaapi.interfacepart.builder.IItemBuilder;
import pers.tany.yukinoaapi.interfacepart.other.IDouble;
import pers.tany.yukinoaapi.interfacepart.other.IRandom;
import pers.tany.yukinoaapi.interfacepart.other.IString;
import pers.tany.yukinoaapi.interfacepart.player.IPlayer;
import pers.tany.yukinoaapi.interfacepart.serializer.ISerializer;
import pers.tany.yukinoaapi.realizationpart.builder.ItemBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LotteryTask extends BukkitRunnable {
    private int time = 0;
    private int second = 0;
    private boolean startBetting = false;
    private boolean stopBetting = false;

    private int random = 0;

    public int getRandom() {
        return random;
    }

    public void setRandom(int random) {
        this.random = random;
    }

    public boolean isStartBetting() {
        return startBetting;
    }

    public boolean isStopBetting() {
        return stopBetting;
    }

    public void setStopBetting(boolean stopBetting) {
        this.stopBetting = stopBetting;
    }

    public int getSecond() {
        return second;
    }

    public void setSecond(int second) {
        this.second = second;
    }

    @Override
    public void run() {
        second++;
        for (String second : Main.config.getConfigurationSection("Countdown").getKeys(false)) {
            if (this.second == Main.config.getInt("Bet.DrawTime") - Integer.parseInt(second)) {
                Bukkit.broadcastMessage(IString.color(Main.config.getString("Countdown." + second)));
                return;
            }
        }
        if (second == Main.config.getInt("Bet.StartBettingTime")) {
            startBetting = true;
            Bukkit.broadcastMessage(IString.color(Main.message.getString("Bet.StartBetTime")));
            return;
        }
        if (second == Main.config.getInt("Bet.StopBettingTime")) {
            stopBetting = true;
            Bukkit.broadcastMessage(IString.color(Main.message.getString("Bet.StopBetTime")));
            return;
        }
        if (second == Main.config.getInt("Bet.DrawTime")) {
            stopBetting = true;
            time++;

            lottery(true);

            init();
        }
    }

    public void lottery(boolean auto) {
        int random = IRandom.randomNumber(0, 99);

        double winnerTakeProbability = IDouble.percentageNumber(Main.config.getString("BackstageOperation.WinnerTakeProbability"), false);
        winnerTakeProbability += IDouble.percentageNumber(Main.config.getString("BackstageOperation.ExtraWinnerTakeProbability"), false) * BetUtil.bet.size();
        if (time > 0 && time % Main.config.getInt("BackstageOperation.WinnerTakeProbabilityTime") == 0) {
            winnerTakeProbability += IDouble.percentageNumber(Main.config.getString("BackstageOperation.AddWinnerTakeProbabilityTime"), false);
        }
        winnerTakeProbability = Math.min(winnerTakeProbability, IDouble.percentageNumber(Main.config.getString("BackstageOperation.MaxWinnerTakeProbability"), false));
        if (time > 0 && time % Main.config.getInt("BackstageOperation.BetTimeWinnerTake") == 0) {
            winnerTakeProbability = 100;
        }
        if (IRandom.percentageChance(winnerTakeProbability)) {
            random = 100;
        }
        if (this.random > 0) {
            random = this.random;
        }

        HashMap<BetInterface.BetType, List<String>> winnerMap = new HashMap<BetInterface.BetType, List<String>>();
        List<String> winners = new ArrayList<>();
        for (String name : BetUtil.bet.keySet()) {
            BetInterface.BetType betType = BetUtil.bet.get(name).getKey();
            Object value = BetUtil.bet.get(name).getValue();
            if (random < 100) {
                if (betType.equals(BetInterface.BetType.ODD_AND_EVEN)) {
                    boolean oddAndEven = (boolean) value;
                    if ((!oddAndEven && random % 2 == 1) || (oddAndEven && random % 2 == 0)) {
                        giveReward(name, Main.config.getInt("Odds.OddAndEven"));
                        List<String> list = winnerMap.getOrDefault(betType, new ArrayList<>());
                        list.add(name);
                        winnerMap.put(betType, list);
                        winners.add(name);
                    } else {
                        clearReward(name);
                        if (Bukkit.getPlayerExact(name) != null) {
                            Player player = Bukkit.getPlayerExact(name);
                            player.sendMessage(IString.color(Main.message.getString("Lottery.Lose")));
                        }
                    }
                } else if (betType.equals(BetInterface.BetType.GUESSING_SIZE)) {
                    boolean guessingSize = (boolean) value;
                    if ((!guessingSize && random >= 0 && random <= 322) || (guessingSize && random >= 66 && random <= 98)) {
                        giveReward(name, Main.config.getInt("Odds.GuessingSize"));
                        List<String> list = winnerMap.getOrDefault(betType, new ArrayList<>());
                        list.add(name);
                        winnerMap.put(betType, list);
                        winners.add(name);
                    } else {
                        clearReward(name);
                        if (Bukkit.getPlayerExact(name) != null) {
                            Player player = Bukkit.getPlayerExact(name);
                            player.sendMessage(IString.color(Main.message.getString("Lottery.Lose")));
                        }
                    }
                } else if (betType.equals(BetInterface.BetType.NUMBER_RANGE)) {
                    if (random < 10) {
                        giveReward(name, Main.config.getInt("Odds.NumberRangeOdds"));
                        List<String> list = winnerMap.getOrDefault(betType, new ArrayList<>());
                        list.add(name);
                        winnerMap.put(betType, list);
                        winners.add(name);
                    } else {
                        clearReward(name);
                        if (Bukkit.getPlayerExact(name) != null) {
                            Player player = Bukkit.getPlayerExact(name);
                            player.sendMessage(IString.color(Main.message.getString("Lottery.Lose")));
                        }
                    }
                } else if (betType.equals(BetInterface.BetType.NUMBER)) {
                    int number = (int) value;
                    if (random == number) {
                        giveReward(name, Main.config.getInt("Odds.NumberOdds"));
                        List<String> list = winnerMap.getOrDefault(betType, new ArrayList<>());
                        list.add(name);
                        winnerMap.put(betType, list);
                        winners.add(name);
                    } else {
                        clearReward(name);
                        if (Bukkit.getPlayerExact(name) != null) {
                            Player player = Bukkit.getPlayerExact(name);
                            player.sendMessage(IString.color(Main.message.getString("Lottery.Lose")));
                        }
                    }
                }
            } else {
                clearReward(name);
                if (Bukkit.getPlayerExact(name) != null) {
                    Player player = Bukkit.getPlayerExact(name);
                    player.sendMessage(IString.color(Main.message.getString("Lottery.Lose")));
                }
            }
        }
        for (String str : Main.message.getStringList("Lottery.LotteryNotice")) {
            if (auto) {
                str = str.replace("[numbers]", time + "");
                str = str.replace("[time]", Main.config.getInt("Bet.StartBettingTime") + "");
            } else {
                str = str.replace("[numbers]", "手动开奖");
                str = str.replace("[time]", "-1");
            }
            str = str.replace("[random]", random == 100 ? Main.message.getString("Lottery.Hundred") : random + "");
            str = str.replace("[bettors]", BetUtil.bet.size() > 0 ? Strings.join(BetUtil.bet.keySet(), ' ') : Main.message.getString("Lottery.NoBet"));
            str = str.replace("[winners]", winners.size() > 0 ? Strings.join(winners, ' ') : Main.message.getString("Lottery.NoNumber"));

            List<String> oddAndEven = winnerMap.getOrDefault(BetInterface.BetType.ODD_AND_EVEN, new ArrayList<>());
            List<String> guessingSize = winnerMap.getOrDefault(BetInterface.BetType.GUESSING_SIZE, new ArrayList<>());
            List<String> numberRange = winnerMap.getOrDefault(BetInterface.BetType.NUMBER_RANGE, new ArrayList<>());
            List<String> number = winnerMap.getOrDefault(BetInterface.BetType.NUMBER, new ArrayList<>());

            str = str.replace("[oddAndEven]", oddAndEven.size() > 0 ? Strings.join(oddAndEven, ' ') : Main.message.getString("Lottery.NoNumber"));
            str = str.replace("[guessingSize]", guessingSize.size() > 0 ? Strings.join(guessingSize, ' ') : Main.message.getString("Lottery.NoNumber"));
            str = str.replace("[numberRange]", numberRange.size() > 0 ? Strings.join(numberRange, ' ') : Main.message.getString("Lottery.NoNumber"));
            str = str.replace("[number]", number.size() > 0 ? Strings.join(number, ' ') : Main.message.getString("Lottery.NoNumber"));

            Bukkit.broadcastMessage(IString.color(str));
        }
        BetUtil.bet.clear();
        random = 0;
    }

    public void clearReward(String name) {
        BetUtil.betMoney.remove(name);
        BetUtil.setBetExp(name, 0);
        BetUtil.setBetItem(name, new ArrayList<>());
    }

    public void giveReward(String name, int magnification) {
        if (BetUtil.betMoney.containsKey(name)) {
            Main.economy.depositPlayer(name, BetUtil.betMoney.get(name) * magnification);
            if (Bukkit.getPlayerExact(name) != null) {
                Player player = Bukkit.getPlayerExact(name);
                player.sendMessage(IString.color(Main.message.getString("Lottery.Win.Money").replace("[money]", (BetUtil.betMoney.get(name) * magnification) + "")));
            }
            BetUtil.betMoney.remove(name);
        }
        if (BetUtil.getBetExp(name) > 0) {
            if (Bukkit.getPlayerExact(name) != null) {
                Player player = Bukkit.getPlayerExact(name);
                IPlayer.giveExp(player, BetUtil.getBetExp(name) * magnification);
                player.sendMessage(IString.color(Main.message.getString("Lottery.Win.Exp").replace("[exp]", (BetUtil.getBetExp(name) * magnification) + "")));
                BetUtil.setBetExp(name, 0);
            } else {
                BetUtil.setBetExp(name, BetUtil.getBetExp(name) * magnification);
            }
        }
        if (BetUtil.getBetItem(name).size() > 0) {
            List<String> oldList = BetUtil.getBetItem(name);
            List<String> newList = new ArrayList<>();
            for (int i = 0; i < magnification; i++) {
                newList.addAll(oldList);
            }
            BetUtil.setBetItem(name, newList);
            if (Bukkit.getPlayerExact(name) != null) {
                Player player = Bukkit.getPlayerExact(name);
                for (String data : BetUtil.getBetItem(name)) {
                    ItemStack itemStack = ISerializer.deserializeItemStack(data);
                    if (Main.config.getBoolean("RepeatedGambling.Repeated")) {
                        IItemBuilder itemBuilder = new ItemBuilder(itemStack);
                        itemBuilder.addLoreAll(Main.config.getStringList("RepeatedGambling.RepeatedLore"));
                        itemStack = itemBuilder.getItemStack();
                    }
                    IPlayer.giveItem(player, itemStack);
                }
                player.sendMessage(IString.color(Main.message.getString("Lottery.Win.Item")));
                BetUtil.setBetItem(name, new ArrayList<>());
            }
        }
    }

    public void init() {
        second = 0;
        startBetting = false;
        stopBetting = false;
    }

}
