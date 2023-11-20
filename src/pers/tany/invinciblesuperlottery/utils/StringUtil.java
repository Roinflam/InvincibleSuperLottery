package pers.tany.invinciblesuperlottery.utils;

import java.util.Iterator;
import java.util.Objects;

public class StringUtil {

    public static String join(Iterable<?> var0, char var1) {
        return var0 == null ? null : join(var0.iterator(), var1);
    }

    public static String join(Iterator<?> var0, char var1) {
        if (var0 == null) {
            return null;
        } else if (!var0.hasNext()) {
            return "";
        } else {
            Object var2 = var0.next();
            if (!var0.hasNext()) {
                return Objects.toString(var2);
            } else {
                StringBuilder var3 = new StringBuilder(256);
                if (var2 != null) {
                    var3.append(var2);
                }

                while(var0.hasNext()) {
                    var3.append(var1);
                    Object var4 = var0.next();
                    if (var4 != null) {
                        var3.append(var4);
                    }
                }

                return var3.toString();
            }
        }
    }

}
