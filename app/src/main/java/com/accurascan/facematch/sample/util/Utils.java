package com.accurascan.facematch.sample.util;

import android.content.Context;
import android.net.Uri;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;

public class Utils {

    public static Uri createImageUri(Context context){
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(System.currentTimeMillis());
        Uri myUri;
        try {
            myUri = Uri.fromFile(File.createTempFile(
                    "IMG_",  // prefix
                    ".jpg",         // suffix
                    context.getCacheDir()      // directory
            ));
        } catch (IOException e) {
            myUri = Uri.fromFile(new File(context.getCacheDir(), "IMG_" + timeStamp + ".jpg"));
        }
        return myUri;
    }
}
