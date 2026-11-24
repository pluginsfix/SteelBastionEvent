package primest.steelbastionevent.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.md_5.bungee.api.ChatColor;

public class ColorUtil {
    private static final Pattern HEX = Pattern.compile("&#([A-Fa-f0-9]{6})");

    public static String color(String text) {
        Matcher m = HEX.matcher(text);
        while (m.find()) {
            text = text.replace(m.group(), ChatColor.of((String)("#" + m.group(1))).toString());
        }
        return ChatColor.translateAlternateColorCodes((char)'&', (String)text);
    }
}

