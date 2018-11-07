package twork.bollywood.videostatus.utils;

import android.os.Environment;

/**
 * Created by Jiyan on 5/4/2018.
 */

public class CheckForSDCard {
    //Check If SD Card is present or not method
    public boolean isSDCardPresent() {
        if (Environment.getExternalStorageState().equals(

                Environment.MEDIA_MOUNTED)) {
            return true;
        }
        return false;
    }
}
