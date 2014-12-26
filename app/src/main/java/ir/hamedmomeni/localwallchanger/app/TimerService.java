package ir.hamedmomeni.localwallchanger.app;

import android.app.Service;
import android.app.WallpaperManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Random;
import java.util.TimerTask;

/**
 * Created by hamedpc on 5/2/14.
 */
public class TimerService extends Service
{
    int period;
    String path;
    File[] files;
    int length , counter;
    SharedPreferences settings;
    public IBinder onBind(Intent arg0)
    {
        return null;
    }

    public void onCreate()
    {
        super.onCreate();
    }
    private class mainTask extends TimerTask
    {
        public void run()
        {
            toastHandler.sendEmptyMessage(0);
        }
    }

    public void onDestroy()
    {
        super.onDestroy();
        Toast.makeText(this, "Service Stopped ...", Toast.LENGTH_SHORT).show();
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
            settings = getSharedPreferences(MainActivity.PREFS_NAME, 0);
            path = settings.getString("path", "/LWC/");
            Log.d("lwc", path);
            File file = new File(path);
        FilenameFilter imageFilter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                String lowercaseName = name.toLowerCase();
                if (lowercaseName.endsWith(".png")
                        || lowercaseName.endsWith(".jpg")
                        || lowercaseName.endsWith(".jpeg")
                        || lowercaseName.endsWith(".gif")) {
                    return true;
                } else {
                    return false;
                }
            }
        };
            if(file.isDirectory()){
                Log.d("lwc", "is dir: "+ file.toString());
                try{
                    files = file.listFiles(imageFilter);
                    length = files.length;
                    counter = 0;
                    Log.d("lwc", "length: "+files.length);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }else {
                Log.d("lwc", "is not  dir:"+ file.toString());
                return -1;
            }

        new mainTask().run();
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_NOT_STICKY;
    }

    private final Handler toastHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            Toast.makeText(getApplicationContext(), getString(R.string.wallpaper_changed), Toast.LENGTH_SHORT).show();
            WallpaperManager wallpaperManager = WallpaperManager.getInstance(getApplicationContext());
            Bitmap bmp = BitmapFactory.decodeFile(files[randInt(0, files.length-1)].getAbsolutePath());
            if(counter == length)
                counter = 0;
            try {
                wallpaperManager.setBitmap(bmp);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };
    public static Bitmap drawableToBitmap (Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable)drawable).getBitmap();
        }

        int width = drawable.getIntrinsicWidth();
        width = width > 0 ? width : 1;
        int height = drawable.getIntrinsicHeight();
        height = height > 0 ? height : 1;

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }
    public static int randInt(int min, int max) {

        // Usually this should be a field rather than a method variable so
        // that it is not re-seeded every call.
        Random rand = new Random();

        // nextInt is normally exclusive of the top value,
        // so add 1 to make it inclusive
        int randomNum = rand.nextInt((max - min) + 1) + min;

        return randomNum;
    }
}
