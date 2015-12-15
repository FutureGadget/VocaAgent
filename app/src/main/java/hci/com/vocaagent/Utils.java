package hci.com.vocaagent;

import org.joda.time.DateTime;
import org.joda.time.Days;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils {
    public static int getDateDiff(String recentTestDate) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date recent = null;
        Date now = null;
        try {
            recent = formatter.parse(recentTestDate);
            now = formatter.parse(VocaLab.getToday());
        } catch (Exception e) {
        }

        return Days.daysBetween(new DateTime(recent), new DateTime(now)).getDays();
    }
}
