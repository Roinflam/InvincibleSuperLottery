package pers.tany.invinciblesuperlottery.utils;

import pers.tany.invinciblesuperlottery.Main;
import pers.tany.invinciblesuperlottery.gui.BetInterface;
import pers.tany.yukinoaapi.interfacepart.configuration.IConfig;
import pers.tany.yukinoaapi.realizationpart.container.KeyToValue;

import java.util.HashMap;
import java.util.List;

public class BetUtil {

    public static HashMap<String, KeyToValue<BetInterface.BetType, Object>> bet = new HashMap<>();
    public static HashMap<String, Integer> betMoney = new HashMap<>();

    public static void setBetExp(String name, int exp) {
        Main.data.set("Bet.Exp." + name, exp);
        IConfig.saveConfig(Main.plugin, Main.data, "", "data");
    }

    public static int getBetExp(String name) {
        return Main.data.getInt("Bet.Exp." + name);
    }

    public static void setBetItem(String name, List<String> list) {
        Main.data.set("Bet.Item." + name, list);
        IConfig.saveConfig(Main.plugin, Main.data, "", "data");
    }

    public static List<String> getBetItem(String name) {
        return Main.data.getStringList("Bet.Item." + name);
    }
}
