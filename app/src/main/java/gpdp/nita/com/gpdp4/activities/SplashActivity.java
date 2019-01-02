package gpdp.nita.com.gpdp4.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import gpdp.nita.com.gpdp4.R;
import gpdp.nita.com.gpdp4.helpers.Upload;
import gpdp.nita.com.gpdp4.helpers.Utility;
import gpdp.nita.com.gpdp4.interfaces.OnJsonsDownloaded;
import gpdp.nita.com.gpdp4.repositories.Constants;

public class SplashActivity extends AppCompatActivity {

    SharedPreferences mLoggedIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);


        Upload.filesDownloaded = 0;

        mLoggedIn = this.getSharedPreferences(Constants.REMEMBER_LOGIN, Context.MODE_PRIVATE);

    }

    private void toMain() {
        if (mLoggedIn.getBoolean(Constants.KEY_LOGGED_IN, false)) {
            Intent toMain = new Intent(this, MainActivity.class);
            startActivity(toMain);
            finish();
        } else toLogin();
    }

    private void toLogin() {
        Intent toLogin = new Intent(this, LoginActivity.class);
        startActivity(toLogin);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        boolean result = Utility.checkPermission(SplashActivity.this, 0, "Permission required"
                , "GPDP needs read/write permission to external storage to store and retrieve user data");
        if (result) {
            requestJson();
        }
        Upload.filesDownloaded = 0;
    }


    private void requestJson() {

        final Upload upload = Upload.getInstance();

        upload.setOnJsonDownloadedListener(new OnJsonsDownloaded() {
            @Override
            public void onSuccess() {
                toMain();
            }

            @Override
            public void onError() {
                Toast.makeText(SplashActivity.this, "Cannot proceed volley", Toast.LENGTH_SHORT).show();
            }
        });

        new InternetCheck(new InternetCheck.Consumer() {
            @Override
            public void accept(Boolean internet) {
                if (!internet) {
                    if (!upload.allFilesExist(Constants.master_tables)) {
                        Toast.makeText(SplashActivity.this, "Cannot proceed", Toast.LENGTH_SHORT).show();
                    } else {
                        toMain();
                    }
                } else {
                    for (String tableName : Constants.master_tables) {
                        upload.requestJSONForUpdates(tableName, SplashActivity.this, false);
                    }
                }
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case Utility.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    requestJson();
                } else {
                    Toast.makeText(SplashActivity.this, "Permission denied", Toast.LENGTH_SHORT).show();

                }
                break;
        }
    }

}

class InternetCheck extends AsyncTask<Void, Void, Boolean> {

    private Consumer mConsumer;

    InternetCheck(Consumer consumer) {
        mConsumer = consumer;
        execute();
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        try {
            Socket sock = new Socket();
            sock.connect(new InetSocketAddress("8.8.8.8", 53), 1500);
            sock.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean internet) {
        mConsumer.accept(internet);
    }

    public interface Consumer {
        void accept(Boolean internet);
    }
}