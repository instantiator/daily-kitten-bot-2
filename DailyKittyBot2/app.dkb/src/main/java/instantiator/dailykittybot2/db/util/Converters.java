package instantiator.dailykittybot2.db.util;

import android.arch.persistence.room.TypeConverter;
import android.net.Uri;
import android.text.TextUtils;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import instantiator.dailykittybot2.data.ConditionType;
import instantiator.dailykittybot2.data.OutcomeType;
import instantiator.dailykittybot2.data.TargetType;

public class Converters {

    @TypeConverter
    public static String fromUri(Uri uri) {
        if (uri == null) { return null; }
        return uri.toString();
    }

    @TypeConverter
    public static Uri toUri(String uri_str) {
        if (uri_str == null) { return null; }
        return Uri.parse(uri_str);
    }

    @TypeConverter
    public static String fromConditionType(ConditionType type) {
        if (type == null) { return null; }
        return type.name();
    }

    @TypeConverter
    public static ConditionType toConditionType(String type) {
        if (type == null) { return null; }
        return ConditionType.valueOf(type);
    }

    @TypeConverter
    public static String fromConditionType(OutcomeType type) {
        if (type == null) { return null; }
        return type.name();
    }

    @TypeConverter
    public static OutcomeType toOutcomeType(String type) {
        if (type == null) { return null; }
        return OutcomeType.valueOf(type);
    }

    @TypeConverter
    public static String fromTargetType(TargetType type) {
        if (type == null) { return null; }
        return type.name();
    }

    @TypeConverter
    public static TargetType toTargetType(String type) {
        if (type == null) { return null; }
        return TargetType.valueOf(type);
    }

    @TypeConverter
    public static String fromListUuid(List<UUID> uuids) {
        if (uuids == null) { return null; }
        List<String> uuid_strings = new LinkedList<>();
        for (UUID uuid : uuids) {
            uuid_strings.add(uuid.toString());
        }
        return TextUtils.join(",", uuid_strings);
    }

    @TypeConverter
    public static List<UUID> toListUuid(String string) {
        if (string == null) { return new LinkedList<>(); }
        List<UUID> data = new LinkedList<>();
        String[] items = TextUtils.split(string, ",");
        for (String item : items) {
            data.add(UUID.fromString(item));
        }
        return data;
    }

    @TypeConverter
    public static String fromListString(List<String> data) {
        if (data == null) { return null; }
        return TextUtils.join("_%_", data);
    }

    @TypeConverter
    public static List<String> fromString(String data) {
        if (data == null) { return new LinkedList<String>(); }
        return Arrays.asList(TextUtils.split(data, "_%_"));
    }

    @TypeConverter
    public static Long fromDate(Date date) {
        if (date == null) { return(null); }
        return(date.getTime());
    }

    @TypeConverter
    public static Date toDate(Long millisSinceEpoch) {
        if (millisSinceEpoch == null) { return(null); }
        return(new Date(millisSinceEpoch));
    }

    @TypeConverter
    public static String fromUuid(UUID uuid) {
        if (uuid == null) { return null; }
        return uuid.toString();
    }

    @TypeConverter
    public static UUID toUuid(String uuidStr) {
        if (StringUtils.isEmpty(uuidStr)) { return null; }
        return UUID.fromString(uuidStr);
    }

}
