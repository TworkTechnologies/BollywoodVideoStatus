package twork.bollywood.videostatus.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 * Created by admin on 3/24/2017.
 */

public class AppUtils {

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)

    /**
     * Check internet connectivity
     *
     * @param context current activity context
     * @return return boolioun
     * @see -
     */

    public static boolean isInternetConnected(Context context) {

        ConnectivityManager connec = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        // ARE WE CONNECTED TO THE NET
        try {
            if (connec.getNetworkInfo(0).getState() == NetworkInfo.State.CONNECTED
                    || connec.getNetworkInfo(1).getState() == NetworkInfo.State.CONNECTED) {
                System.out.println("connection 1");
                return true;
            } else {

            }
        } catch (Exception e) {
            e.printStackTrace();
            ConnectivityManager conMgr = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = conMgr.getActiveNetworkInfo();
            if (netInfo != null && netInfo.isConnectedOrConnecting())
                return true;
            else
                return false;

        }
        return false;

    }

    /**
     * Show Alert Dialog
     *
     * @param context current activity context
     * @param alert
     * @param message display alert message  @return return void
     * @see -
     */


    public static void showAlertDialog(Context context, String alert, String message) {

        try{
            AlertDialog.Builder builder1 = new AlertDialog.Builder(context);

            builder1.setMessage(message);
            builder1.setTitle(alert);
            builder1.setCancelable(true);
            builder1.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();

                }
            });
            AlertDialog alert11 = builder1.create();
            alert11.show();
        }catch (Exception e){

        }


    }


    public static void writeToFile(Context context, String data, String fileNmae) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(fileNmae, Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    public static String readFromFile(Context context, String fileNmae) {

        String ret = "";

        try {
            InputStream inputStream = context.openFileInput(fileNmae);

            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ((receiveString = bufferedReader.readLine()) != null) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        } catch (FileNotFoundException e) {
            ret = "";
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            ret = "";
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        return ret;
    }

    public static void showAlertCustomDialog(Context context, String s, String s1) {

    }
}
