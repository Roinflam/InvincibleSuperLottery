package pers.tany.invinciblesuperlottery.utils;

import org.bukkit.inventory.ItemStack;
import pers.tany.invinciblesuperlottery.Main;
import pers.tany.yukinoaapi.interfacepart.item.IItem;
import pers.tany.yukinoaapi.interfacepart.other.IList;
import pers.tany.yukinoaapi.interfacepart.serializer.ISerializer;

import java.util.HashSet;
import java.util.List;

public class ItemUtil {

    public static boolean isAllowedItem(ItemStack itemStack) {
        if (IItem.isEmpty(itemStack)) {
            return false;
        }
        List<String> list = Main.data.getStringList("BetItemList");
        if (Main.config.getBoolean("BetType.Range")) {
            String str = itemStack.getType().toString() + ":" + itemStack.getDurability();
            if (!list.contains(str)) {
                return false;
            }
        } else {
            String str = ISerializer.serializerItemStack(itemStack, true);
            if (!list.contains(str)) {
                return false;
            }
        }
        if (Main.config.getBoolean("RepeatedGambling.Repeated") && itemStack.getItemMeta().hasLore()) {
            List<String> lore = IList.listReplace(Main.config.getStringList("RepeatedGambling.RepeatedLore"), "&", "§");
            return !new HashSet<>(itemStack.getItemMeta().getLore()).containsAll(lore);
        }
        return true;
    }

}
