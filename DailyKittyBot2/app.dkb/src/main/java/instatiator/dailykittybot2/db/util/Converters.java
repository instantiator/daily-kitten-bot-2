package instatiator.dailykittybot2.db.util;

import android.arch.persistence.room.TypeConverter;

import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.UUID;

public class Converters {

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
