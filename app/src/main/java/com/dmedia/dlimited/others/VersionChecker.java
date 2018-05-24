package com.dmedia.dlimited.others;

import android.os.AsyncTask;
import android.util.Log;

import org.jsoup.Jsoup;

import java.io.IOException;
import java.net.UnknownHostException;

/**
 * Created by jeonghoy on 2016. 12. 12..
 */

public class VersionChecker extends AsyncTask<String, String, String> {

    @Override
    protected String doInBackground(String... params) {
        try {
            return Jsoup.connect("https://play.google.com/store/apps/details?id=" + "com.dmedia.dlimited" + "&hl=en")
                    .timeout(30000)
                    .referrer("http://www.google.com")
                    .get()
                    .select("div[itemprop=softwareVersion]")
                    .first()
                    .ownText();
        } catch (UnknownHostException e2) {
        } catch (Exception e) {
        }
        return null;
    }
}