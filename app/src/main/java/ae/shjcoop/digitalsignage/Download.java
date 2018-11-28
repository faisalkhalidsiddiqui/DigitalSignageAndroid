package ae.shjcoop.digitalsignage;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.downloader.Error;
import com.downloader.OnCancelListener;
import com.downloader.OnDownloadListener;
import com.downloader.OnPauseListener;
import com.downloader.OnProgressListener;
import com.downloader.OnStartOrResumeListener;
import com.downloader.PRDownloader;
import com.downloader.PRDownloaderConfig;
import com.downloader.Progress;

import ae.shjcoop.digitalsignage.R;

public class Download extends AppCompatActivity {


    public static Device device;
    public static int apk;
    String apkLink = "http://mobileapi.shjcoop.ae/MobileAppFileServices/Store/ANDROID/digitalsignage/app.apk";



    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder(); StrictMode.setVmPolicy(builder.build());

        FullScreencall();



        TextView progressLabel = (TextView) findViewById(R.id.progressLabel);
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);

        PRDownloader.initialize(getApplicationContext());
// Enabling database for resume support even after the application is killed:
        PRDownloaderConfig config = PRDownloaderConfig.newBuilder()
                .setDatabaseEnabled(true)
                .build();
        PRDownloader.initialize(getApplicationContext(), config);

// Setting timeout globally for the download network requests:0
        PRDownloaderConfig conf = PRDownloaderConfig.newBuilder()
                .setReadTimeout(30_000)
                .setConnectTimeout(30_000)
                .build();
        PRDownloader.initialize(getApplicationContext(), conf);

        String dirPath = Utils.getRootDirPath(getApplicationContext());
        Log.d("7874572","dir : "+dirPath);



        if (apk == 0) {
            int downloadId = PRDownloader.download(device.contentURL, dirPath, "current")
                    .build()
                    .setOnStartOrResumeListener(new OnStartOrResumeListener() {
                        @Override
                        public void onStartOrResume() {
                        }
                    })
                    .setOnPauseListener(new OnPauseListener() {
                        @Override
                        public void onPause() {
                        }
                    })
                    .setOnCancelListener(new OnCancelListener() {
                        @Override
                        public void onCancel() {
                        }
                    })
                    .setOnProgressListener(new OnProgressListener() {
                        @Override
                        public void onProgress(Progress progress) {
                            long progressPercent = progress.currentBytes * 100 / progress.totalBytes;

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    progressLabel.setText(progressPercent + "/100");
                                    progressBar.setProgress((int) progressPercent);
                                }
                            });
                            //          Log.d("7874572","progress : "+progressPercent);

                        }
                    })
                    .start(new OnDownloadListener() {
                        @Override
                        public void onDownloadComplete() {
                            Log.d("7874572", "download completed");

                            SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("Preference", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putBoolean("videoUpdated", true);
                            editor.putString("contentModifiedOn", device.contentModifiedOn);

                            editor.commit();


                            Intent i = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(i);
                        }

                        @Override
                        public void onError(Error error) {


                            Intent i = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(i);


                        }

                    });
        }

        else if(apk == 1){

            int downloadId = PRDownloader.download(apkLink, dirPath, "app.apk")
                    .build()
                    .setOnStartOrResumeListener(new OnStartOrResumeListener() {
                        @Override
                        public void onStartOrResume() {
                        }
                    })
                    .setOnPauseListener(new OnPauseListener() {
                        @Override
                        public void onPause() {
                        }
                    })
                    .setOnCancelListener(new OnCancelListener() {
                        @Override
                        public void onCancel() {
                        }
                    })
                    .setOnProgressListener(new OnProgressListener() {
                        @Override
                        public void onProgress(Progress progress) {
                            long progressPercent = progress.currentBytes * 100 / progress.totalBytes;

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    progressLabel.setText(progressPercent + "/100");
                                    progressBar.setProgress((int) progressPercent);
                                }
                            });
                            //          Log.d("7874572","progress : "+progressPercent);

                        }
                    })
                    .start(new OnDownloadListener() {
                        @Override
                        public void onDownloadComplete() {
                            Log.d("7874572", "download completed");

                            SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("Preference", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPref.edit();
                           // editor.putBoolean("videoUpdated", true);
                          //  editor.putString("contentModifiedOn", device.contentModifiedOn);

                            editor.commit();



Log.d("apkpath","file:///"+dirPath+"/app.apk");
                            Intent promptInstall = new Intent(Intent.ACTION_VIEW)
                                    .setDataAndType(Uri.parse("file:///"+dirPath+"/app.apk"), "application/vnd.android.package-archive");

                            startActivity(promptInstall);


                            System.exit(0);
                        }

                        @Override
                        public void onError(Error error) {


                            Intent i = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(i);


                        }

                    });


        }



    }
    public void FullScreencall() {
        if(Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) { // lower api
            View v = this.getWindow().getDecorView();
            v.setSystemUiVisibility(View.GONE);
        } else if(Build.VERSION.SDK_INT >= 19) {
            //for new api versions.
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }
}
