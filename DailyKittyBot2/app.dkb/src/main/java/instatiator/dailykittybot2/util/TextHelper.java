package instatiator.dailykittybot2.util;

import android.text.TextUtils;

import java.util.LinkedList;
import java.util.List;

public class TextHelper {

    public static String create_list_html(List<String> data) {
        return ul(data);
    }

    private static String ul(List<String> data) {
        return "<ul>" + TextUtils.join("\n", itemise(data)) + "</ul>";
    }

    private static List<String> itemise(List<String> source) {
        List<String> out = new LinkedList<>();
        for (String item : source) {
            out.add("<li>&nbsp;" + item + "</li>");
        }
        return out;
    }
}
