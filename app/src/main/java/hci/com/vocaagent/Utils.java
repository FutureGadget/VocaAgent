package hci.com.vocaagent;

import android.os.Environment;
import android.util.Log;

import org.joda.time.DateTime;
import org.joda.time.Days;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public static File getExternalDir(String dirName) {
        // Get the directory for the app's private pictures directory.
        File file = new File(Environment.getExternalStorageDirectory(), dirName);
        if (!file.mkdirs()) {
            Log.e("TEST", "Directory not created");
        }
        return file;
    }

    public static List<String> getListFiles() {
        String pattern = ".*\\.xls"; // only shows .xls files
        Pattern r = Pattern.compile(pattern);
        Matcher m;
        File file;
        List<String> list = new ArrayList<>();
        File[] files;
        if (isExternalStorageWritable()) {
            file = getExternalDir("VocaAgent");
            files = file.listFiles();
            for (File f : files) {
                String fName = f.getName();
                if (r.matcher(fName).matches())
                    list.add(f.getName());
            }
        }
        return list;
    }
}
