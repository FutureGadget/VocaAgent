package hci.com.vocaagent;

import android.content.Context;

import java.util.List;

public class AutoCompleteDictionary {
    private String word;
    private String meaning;

    public AutoCompleteDictionary(String word, String meaning) {
        this.word = word;
        this.meaning = meaning;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getMeaning() {
        return meaning;
    }

    public void setMeaning(String meaning) {
        this.meaning = meaning;
    }

    /*
     * Get auto complete Strings array by querying Database with user input partial text.
     */
    public static String[] getAutoCompleteStrings(Context context, String searchItem) {
        // Querying
        List<AutoCompleteDictionary> list = VocaLab.getVoca(context).getAutoAvailable(searchItem);
        int rowCount = list.size();
        String[] autoStrings = {""};
        if (rowCount > 0) {
            // Build string array
            autoStrings = new String[rowCount];
            for (int i = 0; i < rowCount; ++i) {
                String auto = list.get(i).getWord() + "::" + list.get(i).getMeaning();
                autoStrings[i] = auto;
            }
        }
        return autoStrings;
    }
}
