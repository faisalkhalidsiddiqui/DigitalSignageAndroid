package ae.shjcoop.digitalsignage;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;
import android.widget.VideoView;

import com.downloader.Error;
import com.downloader.OnCancelListener;
import com.downloader.OnDownloadListener;
import com.downloader.OnPauseListener;
import com.downloader.OnProgressListener;
import com.downloader.OnStartOrResumeListener;
import com.downloader.PRDownloader;
import com.downloader.PRDownloaderConfig;
import com.downloader.Progress;
import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends YouTubeBaseActivity implements Client.ClientDelegate,YouTubePlayer.PlaybackEventListener, Client.AppUpdateDelegate {
    public VideoView video;
    private MediaPlayer mediaPlayer;

    YouTubePlayer youtubePlayer;
    SharedPreferences sharedPref;
    String contentModifiedOn = "-1";
    Context context;
    Timer t;
    Client client;
    Boolean isSoundOn = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("engvsaus","called");
        YouTubePlayerView youTubePlayerView =
                (YouTubePlayerView) findViewById(R.id.youtubeplayer);

        FullScreencall();
        context = getApplicationContext();
        sharedPref = this.getSharedPreferences("Preference",MODE_PRIVATE);

        contentModifiedOn = sharedPref.getString("contentModifiedOn","-1");

        WifiManager wm = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        String macAddress =  Utils.getMACAddress("wlan0");
        String ipAddress =  Utils.getIPAddress(true);
         client = new Client(getApplicationContext());
        client.mCallback = this;
        client.mAppUpdateCallback = this;
        t = new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {

                try {
                    client.ping(getApplicationContext(), Utils.getDeviceName(), Utils.getDeviceUniqueID(MainActivity.this), macAddress);

                    client.getDevice(context,macAddress);

                    checkForUpdates();

                    Log.d("9385647","client");


                }
                catch(Exception e){

                    Log.d("9385647",e.getLocalizedMessage());
                }
            }
        }, 0, 5000);


        video = (VideoView) findViewById(R.id.video);



        Boolean isYoutube = sharedPref.getBoolean("isYoutube",false);
        String youtubeID = sharedPref.getString("youtubeID","");

        Boolean videoUpdated = sharedPref.getBoolean("videoUpdated",false);

        if(isYoutube) {


            Log.d("videotype","youtube");
            video.setVisibility(View.INVISIBLE);
            youTubePlayerView.setVisibility(View.VISIBLE);
            youTubePlayerView.initialize("AIzaSyCBbt03592cv6bDp08GW9Ox--NnaoGwxoU",
                    new YouTubePlayer.OnInitializedListener() {
                        @Override
                        public void onInitializationSuccess(YouTubePlayer.Provider provider,
                                                            YouTubePlayer youTubePlayer, boolean b) {
                            youTubePlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.CHROMELESS);

                            // do any work here to cue video, play video, etc.
                            youTubePlayer.loadVideo(youtubeID);
    youtubePlayer = youTubePlayer;


                        }
                        @Override
                        public void onInitializationFailure(YouTubePlayer.Provider provider,
                                                            YouTubeInitializationResult youTubeInitializationResult) {

                        }
                    });
            return;
        }



        else if(videoUpdated){

            video.setVisibility(View.VISIBLE);
            youTubePlayerView.setVisibility(View.INVISIBLE);
            Log.d("videoUpdated", "videoUpdated true");
            video.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {


                    mediaPlayer = mp;
                    Log.d("videostatus","prepared");


                    if(isSoundOn){
                        mediaPlayer.setVolume(1.0f,1.0f);
                    }
                    else {
                        mediaPlayer.setVolume(0.0f,0.0f);

                    }
                }
            });


            video.setOnCompletionListener ( new MediaPlayer.OnCompletionListener() {

                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {

                    Log.d("videostatus","completed");

                    if(isSoundOn){
                        Log.d("sound123","sound is on");
                        mediaPlayer.setVolume(1.0f,1.0f);
                    }
                    else {
                        mediaPlayer.setVolume(0.0f,0.0f);

                    }


                    video.seekTo(0);
                    video.start();



                }
            });




            video.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                    return true;
                }
            });

            Boolean isLive = sharedPref.getBoolean("isLive",false);
            String liveLink = sharedPref.getString("liveLink","");
            String UrlPath="file://"+Utils.getRootDirPath(getApplicationContext())+"/current";




            if(isLive && liveLink!="" ){
                 UrlPath = liveLink;

                Log.d("videotype","mu8");
            }

            else {

                Log.d("videotype","video");
            }
            Uri videoUri = Uri.parse(UrlPath);
            try {
                video.setVideoURI(videoUri);
                video.start();
            } catch (Exception e) {
                e.printStackTrace();
            }


        }



    }


    @Override
    public void onClientDeviceResponse(Device device) {
        Log.d("videotype",device.contentURL);

Log.d("sound123",device.isSound.toString());
        isSoundOn = device.isSound;



        SharedPreferences.Editor editor = sharedPref.edit();


        Log.d("zubairsheikh",device.contentURL);

        if(device.contentURL.contentEquals("")) {


            video.setVideoURI(null);

            editor.putBoolean("videoUpdated", false);

            editor.commit();


        }

        if (device.contentURL.contentEquals("") && device.contentModifiedOn == null){
    Log.d("contentURL","contentURL is empty");
    return;
}



            if ( Integer.parseInt(contentModifiedOn) < Integer.parseInt(device.contentModifiedOn) || !sharedPref.getBoolean("videoUpdated",false)){

                 if(device.contentURL.contains(".m3u8")){
                     editor.putBoolean("isYoutube",false);
                    editor.putBoolean("isLive",true);
                     editor.putString("liveLink",device.contentURL);
                    editor.putBoolean("isRepeatable",device.isRepeatable);
                    editor.putString("contentModifiedOn",device.contentModifiedOn);
                    editor.commit();
                     Log.d("videotype", "m3u8 hai");


                    video.setVisibility(View.VISIBLE);

                     Uri videoUri = Uri.parse(device.contentURL);
                     try {
                         video.setVideoURI(videoUri);
                         video.start();
                     } catch (Exception e) {
                         e.printStackTrace();
                     }


                }

                else if(device.contentURL.contains("youtube.com/watch?v=")){
                     editor.putBoolean("isLive",false);
                     t.purge();
                     t.cancel();
                      String[] split = device.contentURL.split("=");

                     Log.d("videotype", "youtube hai");

                     if(split.length == 2){
                        Log.d("youtube123",split[1]);

                         editor.putBoolean("isYoutube",true);
                         editor.putString("youtubeID",split[1]);
                         editor.putString("contentModifiedOn",device.contentModifiedOn);
                         editor.commit();
                         Intent i = new Intent(getApplicationContext(),MainActivity.class);
                         startActivity(i);

                     }
                 }
                else {
                     t.purge();
                     t.cancel();

                     editor.putBoolean("isLive",false);
                     editor.putBoolean("isYoutube",false);
                     editor.putString("liveLink","");
                    editor.putBoolean("videoUpdated", false);
                    contentModifiedOn = device.contentModifiedOn;
                    editor.putBoolean("isRepeatable", device.isRepeatable);
                    editor.commit();
                    Intent i = new Intent(this, Download.class);
                    Download.device = device;
                    Download.apk = 0;

                     Log.d("videotype", "video hai");


                     startActivity(i);



                    return;

                }
            }


    }

    public void checkForUpdates(){


        try {
            client.getAppVersion(context);
        } catch (Exception e) {
            e.printStackTrace();
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

    @Override
    public void onPaused() {
        youtubePlayer.play();
    }

    @Override
    public void onPlaying() {

    }

    @Override
    public void onStopped() {

    }

    @Override
    public void onBuffering(boolean b) {

    }

    @Override
    public void onSeekTo(int i) {

    }


    @Override
    public void onUpdateVersionResponse(int version) {

        int currentVersionCode = BuildConfig.VERSION_CODE;
        int updateVersionCode = version;
        if(currentVersionCode < updateVersionCode) {
            Intent i = new Intent(this, Download.class);
            Download.apk = 1;
            t.cancel();
            t.purge();
            startActivity(i);
        }
    }
}
