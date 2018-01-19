package instantiator.dailykittybot2.util;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class ListUtils {

    public static boolean sameStrings(List<String> strings1, List<String> strings2) {
        if (strings1 == null && strings2 == null) { return true; }
        if (strings1 == null || strings2 == null) { return false; }
        if (strings1.size() != strings2.size()) { return false; }
        for (int i = 0; i < strings1.size(); i++) {
            if (!StringUtils.equals(strings1.get(i), strings2.get(i))) {
                return false;
            }
        }
        return true;
    }

    public static boolean sameStrings(String[] strings1, List<String> strings2) {
        if (strings1 == null && strings2 == null) { return true; }
        if (strings1 == null || strings2 == null) { return false; }
        if (strings1.length != strings2.size()) { return false; }
        for (int i = 0; i < strings1.length; i++) {
            if (!StringUtils.equals(strings1[i], strings2.get(i))) {
                return false;
            }
        }
        return true;
    }

}
