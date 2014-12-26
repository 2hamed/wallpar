package ir.hamedmomeni.localwallchanger.app;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Fragment;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FilenameFilter;

public class MainActivity extends Activity {
    public static String PREFS_NAME = "LocalWallPrefs";
    private final int REQUEST_CODE_PICK_DIR = 1;
    private final int REQUEST_CODE_PICK_FILE = 2;
    EditText et;
    final Activity activityForButton = this;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public class PlaceholderFragment extends Fragment {
        SeekBar time;
        TextView timeTxt;
        AlarmManager alarm;
        PendingIntent pintent;
        SharedPreferences settings;
        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            settings = getActivity().getSharedPreferences(MainActivity.PREFS_NAME, 0);

            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            et = (EditText) rootView.findViewById(R.id.editText);
            et.setText(settings.getString("path", "/LWC/"));
            timeTxt = (TextView) rootView.findViewById(R.id.textView4);
            time = (SeekBar) rootView.findViewById(R.id.seekBar);
            time.setMax(4);
            time.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    switch (progress) {
                        case 0:
                            timeTxt.setText(getString(R.string.once_15));
                            break;
                        case 1:
                            timeTxt.setText(getString(R.string.once_half_hour));
                            break;
                        case 2:
                            timeTxt.setText(getString(R.string.once_hour));
                            break;
                        case 3:
                            timeTxt.setText(getString(R.string.once_half_day));
                            break;
                        case 4:
                            timeTxt.setText(getString(R.string.once_day));
                            break;
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }

            });
            Log.d("period", "period: " + settings.getInt("period", 0));
            time.setProgress(settings.getInt("period", 0));
            Button start = (Button) rootView.findViewById(R.id.button);
            start.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String path = et.getText().toString();
                    if(path == getString(R.string.default_path)){
                        Toast.makeText(getActivity(), getString(R.string.select_a_path_first), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    File directory = new File(path);
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
                    File[] contents = directory.listFiles(imageFilter);
                    if(contents == null){
                        Toast.makeText(getActivity(), getString(R.string.path_is_non_existent), Toast.LENGTH_SHORT).show();
                        return;
                    }else if(contents.length == 0){
                        Toast.makeText(getActivity(), getString(R.string.path_is_empty), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString("path", path);
                    editor.putInt("period", time.getProgress());
                    editor.commit();
                    boolean alarmUp = (PendingIntent.getService(getActivity(), 0, new Intent(getActivity(), TimerService.class),
                            PendingIntent.FLAG_NO_CREATE) != null);
                    Intent intent = new Intent(getActivity(), TimerService.class);
                    pintent = PendingIntent.getService(getActivity(), 0, intent, 0);
                    alarm = (AlarmManager)getActivity().getSystemService(Context.ALARM_SERVICE);
                    if(alarmUp){
                        Toast.makeText(getActivity(), "Already running ... updating", Toast.LENGTH_SHORT).show();
                        alarm.cancel(pintent);
                    }
                    long period = AlarmManager.INTERVAL_FIFTEEN_MINUTES;
                    switch (time.getProgress()){
                        case 0:
                            period = AlarmManager.INTERVAL_FIFTEEN_MINUTES;
                            break;
                        case 1:
                            period = AlarmManager.INTERVAL_HALF_HOUR;
                            break;
                        case 2:
                            period = AlarmManager.INTERVAL_HOUR;
                            break;
                        case 3:
                            period = AlarmManager.INTERVAL_HALF_DAY;
                            break;
                        case 4:
                            period = AlarmManager.INTERVAL_DAY;
                            break;
                    }
                    alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, SystemClock.elapsedRealtime() , period, pintent);
                }
            });
            Button stop = (Button) rootView.findViewById(R.id.button2);
            stop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    boolean alarmUp = (PendingIntent.getService(getActivity(), 0, new Intent(getActivity(), TimerService.class),
                            PendingIntent.FLAG_NO_CREATE) != null);
                    if(alarmUp){
                        Intent intent = new Intent(getActivity(), TimerService.class);
                        pintent = PendingIntent.getService(getActivity(), 0, intent, 0);
                        alarm = (AlarmManager)getActivity().getSystemService(Context.ALARM_SERVICE);
                        alarm.cancel(pintent);
                    }else {
                        Toast.makeText(getActivity(), getString(R.string.service_not_running), Toast.LENGTH_SHORT).show();
                    }
                }
            });
            Button browse = (Button) rootView.findViewById(R.id.button3);
            browse.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent fileExploreIntent = new Intent(
                            FileBrowserActivity.INTENT_ACTION_SELECT_DIR,
                            null,
                            activityForButton,
                            FileBrowserActivity.class
                    );
                    //If the parameter below is not provided the Activity will try to start from sdcard(external storage),
                    // if fails, then will start from roor "/"
                    // Do not use "/sdcard" instead as base address for sdcard use Environment.getExternalStorageDirectory()
//        		fileExploreIntent.putExtra(
//        				ua.com.vassiliev.androidfilebrowser.FileBrowserActivity.startDirectoryParameter,
//        				"/sdcard"
//        				);
                    startActivityForResult(
                            fileExploreIntent,
                            REQUEST_CODE_PICK_DIR
                    );
                }
            });
            return rootView;
        }
        private void setup() {


        }
        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            if (requestCode == REQUEST_CODE_PICK_DIR) {
                if(resultCode == RESULT_OK) {
                    String newDir = data.getStringExtra(
                            FileBrowserActivity.returnDirectoryParameter);
                    Toast.makeText(
                            getActivity(),
                            getString(R.string.received_directory)+"\n"+newDir,
                            Toast.LENGTH_LONG).show();
                    et.setText(newDir);

                } else {//if(resultCode == this.RESULT_OK) {
                    Toast.makeText(
                            getActivity(),
                            "Received NO result from file browser",
                            Toast.LENGTH_LONG).show();
                }//END } else {//if(resultCode == this.RESULT_OK) {
            }//if (requestCode == REQUEST_CODE_PICK_DIR) {

            if (requestCode == REQUEST_CODE_PICK_FILE) {
                if(resultCode == RESULT_OK) {
                    String newFile = data.getStringExtra(
                            FileBrowserActivity.returnFileParameter);
                    Toast.makeText(
                            getActivity(),
                            "Received FILE path from file browser:\n"+newFile,
                            Toast.LENGTH_LONG).show();

                } else {//if(resultCode == this.RESULT_OK) {
                    Toast.makeText(
                            getActivity(),
                            getString(R.string.no_path_selected),
                            Toast.LENGTH_LONG).show();
                }//END } else {//if(resultCode == this.RESULT_OK) {
            }
        }
    }
}