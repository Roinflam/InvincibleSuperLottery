package pers.tany.invinciblesuperlottery;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import pers.tany.invinciblesuperlottery.command.Commands;
import pers.tany.invinciblesuperlottery.listenevent.Events;
import pers.tany.invinciblesuperlottery.task.LotteryTask;
import pers.tany.invinciblesuperlottery.utils.BetUtil;
import pers.tany.yukinoaapi.interfacepart.configuration.IConfig;
import pers.tany.yukinoaapi.interfacepart.register.IRegister;
import pers.tany.yukinoaapi.realizationpart.VaultUtil;


public class Main extends JavaPlugin {
    public static Plugin plugin;
    public static LotteryTask lotteryTask;
    public static YamlConfiguration config;
    public static YamlConfiguration data;
    public static YamlConfiguration message;
    public static YamlConfiguration logs;
    public static Economy economy;

    @Override
    public void onDisable() {
        for (String name : BetUtil.betMoney.keySet()) {
            economy.depositPlayer(name, BetUtil.betMoney.get(name));
        }
        Bukkit.getConsoleSender().sendMessage("§7「§fInvincibleSuperLottery§7」§c已卸载");
    }

    @Override
    public void onEnable() {
        plugin = this;

        Bukkit.getConsoleSender().sendMessage("§7「§fInvincibleSuperLottery§7」§a已启用");

        IConfig.createResource(this, "", "config.yml", false);
        IConfig.createResource(this, "", "data.yml", false);
        IConfig.createResource(this, "", "message.yml", false);
        IConfig.createResource(this, "", "logs.yml", false);

        config = IConfig.loadConfig(this, "", "config");
        data = IConfig.loadConfig(this, "", "data");
        message = IConfig.loadConfig(this, "", "message");
        logs = IConfig.loadConfig(this, "", "logs");

        IRegister.registerEvents(this, new Events());
        IRegister.registerCommands(this, "InvincibleSuperLottery", new Commands());

        economy = VaultUtil.getEconomy();

        lotteryTask = new LotteryTask();
        lotteryTask.runTaskTimer(Main.plugin, 20, 20);
    }
}
