package com.example.archiegupta.hotspot_app;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseInstallation;

/**
 * Created by archie gupta on 10-Nov-15.
 */
public class App extends Application {
    public void onCreate() {
        super.onCreate();
        // Enable Local Datastore.
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, "PvEWm2rh5USuUPBnaBOMvvtUjYU1pcJ8Is34tnCY", "G1CbCmbqitmVgAStFuUhGbsRZQN0uHCrkgHXZpAT");
        ParseInstallation.getCurrentInstallation().saveInBackground();
    }
}
