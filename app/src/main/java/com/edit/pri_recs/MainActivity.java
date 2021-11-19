package com.edit.pri_recs;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import vpos.apipackage.PosApiHelper;
import vpos.apipackage.PrintInitException;

public class MainActivity extends AppCompatActivity {

    private static final int DRAW_OVER_OTHER_APP_PERMISSION = 2;
    //FileSystemObserverService.DirectoryFileObserver directoryFileObserver;
    Context context;
    @SuppressLint("StaticFieldLeak")
    private static MainActivity inst;
    public static String[] MY_PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE",
            "android.permission.MOUNT_UNMOUNT_FILESYSTEMS"};
    public static final int REQUEST_EXTERNAL_STORAGE = 1;
    final int PRINT_OPEN = 8;
    final int PRINT_BMP = 2;
    int ret = -1;
    int RESULT_CODE = 0;
    public String tag = "MainActivity";
    private boolean m_bThreadFinished = true;
    PosApiHelper posApiHelper = PosApiHelper.getInstance();
    SharedPreferences sp;
    private int voltage_level;
    private int BatteryV;
    //private BroadcastReceiver receiver;
    private final static int ENABLE_RG = 10;
    private final static int DISABLE_RG = 11;
    int IsWorking = 0;
    private TextView textViewMsg;
    String deviceId;
    public Bitmap myBitmap;
    public File pdfFile;
    public ArrayList<Bitmap> myList = new ArrayList<>();
    private String newPath;
    Button button1;
    private static final int CODE_DRAW_OVER_OTHER_APP_PERMISSION = 2084;
    private View mFloatingView;
    int powerLaunch = 0;

    //FileSystemObserverService inst2 = FileSystemObserverService.instance();

    /*Permissions*/
    private void requestPermission() {
        //Check if there is write permission
        int checkCallPhonePermission = ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (checkCallPhonePermission != PackageManager.PERMISSION_GRANTED) {
            //Without the permission to Write, to apply for the permission to Read and Write,
            // the system will pop up the permission dialog
            ActivityCompat.requestPermissions(MainActivity.this, MY_PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_EXTERNAL_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(), "Permissions granted.",
                        Toast.LENGTH_SHORT).show();
            } else {
                requestPermission();
            }
        }
    }

    @SuppressLint("HardwareIds")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        button1 = findViewById(R.id.b1);
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        context = this;
        /*File myFolder = new File(Environment.getExternalStorageDirectory() +
                "/Download/Receipts/");
        String myPath = myFolder.getAbsolutePath();*/
        //inst2.observe();
        newPath = Environment.getExternalStorageDirectory()
                .toString()+"/Download/Receipts";
//        new DirectoryFileObserver(newPath).startWatching();
        System.out.println("My device id " + deviceId);
        //Check if the application has draw over other apps permission or not?
        //This permission is by default available for API<23. But for API > 23
        //you have to ask for the permission in runtime.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {

            //If the draw over permission is not available open the settings screen
            //to grant the permission.
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, CODE_DRAW_OVER_OTHER_APP_PERMISSION);
            requestPermission();
        } else {
            requestPermission();
            initializeView();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.item1) {
            Toast.makeText(MainActivity.this, "Action clicked", Toast.LENGTH_LONG).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static MainActivity instance() {
        return inst;
    }

    /**
     * Set and initialize the view elements.
     */
    private void initializeView() {
        findViewById(R.id.notify_me).setOnClickListener(view -> {
            startService(new Intent(MainActivity.this, FloatingViewService.class));
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CODE_DRAW_OVER_OTHER_APP_PERMISSION) {

            //Check if the permission is granted or not.
            if (resultCode == RESULT_OK) {
                initializeView();
            } else {
                //Permission is not available
                Toast.makeText(this,
                        "Draw over other app permission not available. Closing the application",
                        Toast.LENGTH_SHORT).show();

                finish();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onDestroy() {
        PosApiHelper.getInstance().SysSetPower(0);
        super.onDestroy();
    }

    public void onRestart(View v) {
        Intent myIntent = new Intent(context, FileSystemObserverService.class);
        context.startService(myIntent);
    }

    public void onClickPrint (View v) throws IOException {
        //prtReceipt();
    }

    public void onQuit(View v) {
        onDestroy();
        finishAffinity();
        System.exit(0);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (deviceId.equals("6e358d0938f83f83")) {
            /*Original: e7171c1fe9945676
            * TecnoF1: fcf52d5c63cb4676*/
            powerLaunch = 1;
            PosApiHelper.getInstance().SysSetPower(powerLaunch);
        } else {
            Toast.makeText(context, "Admin access needed", Toast.LENGTH_LONG).show();
        }
        inst = this;
    }

//    @Override
//    protected void onPause() {
//        super.onPause();
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(this))
//        {
//            startService(new Intent(MainActivity.this,
//                    BubbleService.class).putExtra("activity_background", true));
//            finish();
//        }
//    }

    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(true);

            return true;
        }
        return false;
    }

    /*public class DirectoryFileObserver extends FileObserver {
        String absolutePath;
        public DirectoryFileObserver(String path) { super(path, FileObserver.CREATE);
            absolutePath = path;
        }
        @Override public void onEvent(int event, String path) {
            event &= FileObserver.ALL_EVENTS;
            switch (event) {
                case FileObserver.DELETE_SELF:
                    Log.e("FileObserver ","File Deleted");
                case FileObserver.DELETE:
                    Log.e("FileObserver ","File Deleted");
                    break;

                case FileObserver.CREATE:
                    Log.e("FileObserver ","File Created");
                    //Call for printing
                    try {
                        prtReceipt();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e("onEvent: Create", "Call print receipt func fails.");
                    }
                    break;

                case FileObserver.MOVED_TO:
                    Log.e("FileObserver: ","File Moved here");
                    try {
                        prtReceipt();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e("onEvent: MovedTo", "Call print receipt func fails.");
                    }

                default:
                    Log.e("FileObserver: ","Some Other Shit went down");
            }
            /*if(path != null) {
                Log.e("FileObserver: ","File Created");
                try {
                    prtReceipt();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }*/

    //Get latest modified file
    public static File getLastModified(String directoryFilePath)
    {
        File directory = new File(directoryFilePath);
        File[] files = directory.listFiles(File::isFile);
        long lastModifiedTime = Long.MIN_VALUE;
        File chosenFile = null;

        if (files != null)
        {
            for (File file : files)
            {
                if (file.lastModified() > lastModifiedTime)
                {
                    chosenFile = file;
                    lastModifiedTime = file.lastModified();
                }
            }
        }

        return chosenFile;
    }

    public void pdfToBitmap(File pdfFile) {
        //ArrayList<Bitmap> bitmaps = new ArrayList<>();

        try {
//            Log.e("Before rendererOne ", String.valueOf(myList));
            if (!myList.isEmpty()) {
                Log.e("Before renderer ", String.valueOf(myList));
                myList.clear();
            }
            /*if (!myList.get(0).isRecycled()) {
                Log.e("Before renderer bitmap ", String.valueOf(myBitmap));
                myList.get(0).recycle();
                java.lang.System.gc();
                Log.e("AftNullify bitmap ", String.valueOf(myBitmap));
            }*/
            Log.e("RendererPdfFile ", String.valueOf(pdfFile));
            Log.e("JustBefore renderer ", String.valueOf(myList));
            //Log.e("JustB4 renderer bitmap ", String.valueOf(myBitmap));

            PdfRenderer renderer = new PdfRenderer(ParcelFileDescriptor.open(pdfFile,
                    ParcelFileDescriptor.MODE_READ_ONLY));
            Log.e("Renderer ", String.valueOf(renderer));

            Bitmap bitmap;
            final int pageCount = renderer.getPageCount();
            for (int i = 0; i < pageCount; i++) {
                PdfRenderer.Page page = renderer.openPage(i);

                /*int width = getResources().getDisplayMetrics().densityDpi / 72 * page.getWidth();
                System.out.println("My width" + page.getWidth());
                int height = getResources().getDisplayMetrics().densityDpi / 72 * page.getHeight();
                System.out.println("My height" + page.getHeight());
                bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);*/
                bitmap = Bitmap.createBitmap(384, 500, Bitmap.Config.ARGB_4444);

                page.render(bitmap, null, null,
                        PdfRenderer.Page.RENDER_MODE_FOR_PRINT);

                myList.add(bitmap);
                Log.e("pdfToBitmap ", "My list: " + myList);

                // close the page
                page.close();

            }

            // close the renderer
            renderer.close();
        } catch (Exception ex) {
            Log.e("pdfToBitmap ", "PDFRenderer fails");
            ex.printStackTrace();
        }

        //return bitmaps;

    }

    public void prtReceipt() throws IOException {
        //Call receipt print
        pdfFile = new File(getLastModified(newPath).getAbsolutePath());

        /*Troubleshoot----------
        Log.d("Files", "Path: " + newPath);
        File directory = new File(newPath);
        File[] files = directory.listFiles();
        assert files != null;
        Log.d("Files", "Size: " + files.length);
        //int newFile = files.length;
        for (int i = 0; i < files.length; i++) {
            Log.d("Files", "FileName:" + files[i]);
        }
        ---------End*/

        /*Picked receipt*/
        Log.e("Picked Receipt: ", pdfFile.getAbsolutePath());
        pdfToBitmap(pdfFile);
        Log.e("myListAfterRenderer ", String.valueOf(myList));
        //pdfRendererTwo(pdfFile);
        if (!myList.isEmpty()) {
            //myBitmap = myList.get(0);
            onClickPrintBMP();
            Log.e("myList ", String.valueOf(myList));
            Log.e("myBitmap ", String.valueOf(myBitmap));
        } else {
            Log.e("prtReceipt ", "My bitmap list is empty");

            //Initiate after failure
            myList.clear();
            java.lang.System.gc();
            prtReceipt();
        }
    }

    /*public void pdfRendererTwo (File pdfFile) throws IOException {
        String FILENAME = String.valueOf(pdfFile);
        File file = new File(getCacheDir(), FILENAME);
        //File file = new File(String.valueOf(pdfFile));
        FileInputStream fileInputStream = null;
        FileOutputStream output = null;
        try {
            fileInputStream = new FileInputStream(pdfFile);
            output = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        final byte[] buffer = new byte[1024];
        int size;
        while (true) {
            assert fileInputStream != null;
            if ((size = fileInputStream.read(buffer)) == -1) break;
            assert output != null;
            output.write(buffer, 0, size);
        }
        fileInputStream.close();
        assert output != null;
        output.close();
        //}
        ParcelFileDescriptor mFileDescriptor = ParcelFileDescriptor.open(file,
         ParcelFileDescriptor.MODE_READ_ONLY);
        // This is the PdfRenderer we use to render the PDF.
        PdfRenderer renderer = null;
        if (mFileDescriptor != null) {
            renderer = new PdfRenderer(mFileDescriptor);
            Bitmap bitmap;
            final int pageCount = renderer.getPageCount();
            for (int i = 0; i < pageCount; i++) {
                PdfRenderer.Page page = renderer.openPage(i);

                // say we render for printing
                bitmap = Bitmap.createBitmap(384, 500, Bitmap.Config.ARGB_4444);
                page.render(bitmap, null, null,
                        PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                myList.add(bitmap);
                Log.e("pdfToBitmap: ", "My list: " + myList);

                // close the page
                page.close();
            }

            // close the renderer
            renderer.close();
        }
    }*/

    public void onClickPrintBMP() {
        System.out.println("My bug: Initiate printing");
        if (printThread != null && printThread.isThreadFinished()) {
            Log.e(tag, "Thread is still running...");
            return;
        }

        printThread = new Print_Thread(PRINT_BMP);
        printThread.start();
    }

    MainActivity.Print_Thread printThread = null;

    public class Print_Thread extends Thread {

        int type;

        public boolean isThreadFinished() {
            return !m_bThreadFinished;
        }

        public Print_Thread(int type) {
            this.type = type;
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        public void run() {
            Log.d("Print_Thread[ run ]", "run() begin");
            Message msg = Message.obtain();
            Message msg1 = new Message();

            synchronized (this) {

                m_bThreadFinished = false;
                try {
                    ret = posApiHelper.PrintInit();
                } catch (PrintInitException e) {
                    e.printStackTrace();
                    int initRet = e.getExceptionCode();
                    Log.e(tag, "initRer : " + initRet);
                }

                Log.e(tag, "init code:" + ret);

                ret = getValue();
                Log.e(tag, "getValue():" + ret);

                posApiHelper.PrintSetGray(ret);

                //posApiHelper.PrintSetVoltage(BatteryV * 2 / 100);

                ret = posApiHelper.PrintCheckStatus();
                if (ret == -1) {
                    RESULT_CODE = -1;
                    Log.e(tag, "Lib_PrnCheckStatus fail, ret = " + ret);
                    SendMsg("Error, No Paper!!");
                    m_bThreadFinished = true;
                    return;
                } else if (ret == -2) {
                    RESULT_CODE = -1;
                    Log.e(tag, "Lib_PrnCheckStatus fail, ret = " + ret);
                    SendMsg("Error, Printer Too Hot ");
                    m_bThreadFinished = true;
                    return;
                } else if (ret == -3) {
                    RESULT_CODE = -1;
                    Log.e(tag, "voltage = " + (BatteryV * 2));
//                    SendMsg("Battery less :" + (BatteryV * 2));
                    SendMsg("Battery is " + voltage_level + "%" + " Connect to power");
                    //System.out.println("Battery less :" + (BatteryV * 2));
                    m_bThreadFinished = true;
                    return;
                }
                /* else if (voltage_level < 5) {
                    RESULT_CODE = -1;
                    Log.e(tag, "voltage_level = " + voltage_level);
                    SendMsg("Battery capacity less : " + voltage_level);
                    m_bThreadFinished = true;
                    return;
                }*/
                else {
                    RESULT_CODE = 0;
                }

                switch (type) {
                    case PRINT_OPEN:
                        msg.what = DISABLE_RG;
                        handler.sendMessage(msg);
                        try {
                            ret = posApiHelper.PrintOpen();
                        } catch (PrintInitException e) {
                            e.printStackTrace();
                        }

                        msg1.what = ENABLE_RG;
                        handler.sendMessage(msg1);

                        Log.d("", "Lib_PrnStart ret = " + ret);
                        if (ret != 0) {
                            RESULT_CODE = -1;
                            Log.e("PrismApp", "Lib_PrnStart fail, ret = " + ret);
                            if (ret == -1) {
                                SendMsg("No Print Paper ");
                            } else if (ret == -2) {
                                SendMsg("too hot");
                            } else if (ret == -3) {
                                SendMsg("low voltage");
                            } else {
                                SendMsg("Print fail");
                            }
                        } else {
                            RESULT_CODE = 0;
                            SendMsg("Print Finish");
                        }

                        break;

                    case PRINT_BMP:
                        msg.what = DISABLE_RG;
                        handler.sendMessage(msg);
                        //0 left，1 middle ，2 right
                        //Print.Lib_PrnSetAlign(0);
                        //Bitmap bmp = BitmapFactory.decodeResource(MainActivity.this.getResources()
                        // , R.mipmap.metrolinx1bitdepth);
                        ret = posApiHelper.PrintBmp(myList.get(0));
                        /*posApiHelper.PrintStr("= = = = = = = = = = = = = = \n");
                        posApiHelper.PrintStr("(c)2021. A product of Schoolink\n");
                        posApiHelper.PrintStr("Technologies. 0794703337/\n");
                        posApiHelper.PrintStr("0723591149/0720832086\n");
                        posApiHelper.PrintStr("***End of Transaction Receipt***\n");*/
                        posApiHelper.PrintStr("\n");
                        posApiHelper.PrintStr("\n");
                        if (ret == 0) {
                            posApiHelper.PrintStr("\n\n\n");
                            posApiHelper.PrintStr("                                         \n");
                            posApiHelper.PrintStr("                                         \n");

                            ret = posApiHelper.PrintStart();

                            msg1.what = ENABLE_RG;
                            handler.sendMessage(msg1);

                            Log.d("", "Lib_PrnStart ret = " + ret);
                            if (ret != 0) {
                                RESULT_CODE = -1;
                                Log.e("PrismApp", "Lib_PrnStart fail, ret = " + ret);
                                if (ret == -1) {
                                    SendMsg("No Print Paper ");
                                } else if (ret == -2) {
                                    SendMsg("too hot");
                                } else if (ret == -3) {
                                    SendMsg("low voltage");
                                } else {
                                    SendMsg("Print fail");
                                }
                            } else {
                                RESULT_CODE = 0;
                                SendMsg("Print Finish");
                                //myBitmap.recycle();
                                myList.clear();
                                java.lang.System.gc();
                                /*Log.e("After print bitmap: ", myBitmap.toString());
                                Log.e("After print list: ", myList.toString());*/
                            }
                        } else {
                            RESULT_CODE = -1;
                            SendMsg("Lib_PrnBmp Failed");
                        }
                        break;

                    default:
                        break;
                }
                m_bThreadFinished = true;

                Log.e(tag, "goToSleep2...");
            }
        }
    }

    /*Sets up the density of printing*/
    private int getValue() {
        sp = getSharedPreferences("Gray", MODE_PRIVATE);
        return sp.getInt("value", 3);
    }

    /*Handles catching of error messages from print process*/
    public void SendMsg(String strInfo) {
        Message msg = new Message();
        Bundle b = new Bundle();
        b.putString("MSG", strInfo);
        msg.setData(b);
        handler.sendMessage(msg);
    }

    @SuppressLint("HandlerLeak")
    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case DISABLE_RG:
                    IsWorking = 1;
                    break;

                case ENABLE_RG:
                    IsWorking = 0;
                    break;

                default:
                    Bundle b = msg.getData();
                    final String strInfo = b.getString("MSG");
                    textViewMsg = findViewById(R.id.catchErr);
                    textViewMsg.setText(strInfo);
                    int shortAnimationDuration = 2000;
                    textViewMsg.setAlpha(0f);
                    textViewMsg.setVisibility(View.VISIBLE);
                    textViewMsg.animate()
                            .alpha(1f)
                            .setDuration(shortAnimationDuration)
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    textViewMsg.setVisibility(View.INVISIBLE);
                                }
                            });
                    break;
            }
        }
    };

    /*Handles Battery functionality*/
    public class BatteryReceiver extends BroadcastReceiver {
        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        public void onReceive(Context context, Intent intent) {
            voltage_level = Objects.requireNonNull(intent.getExtras()).getInt("level");
            //System.out.println("Battery shitOne" + voltage_level);
            Log.e("wbw", "current  = " + voltage_level);
            BatteryV = intent.getIntExtra("voltage", 0);
            //System.out.println("Battery shitTwo" + BatteryV);
            Log.e("wbw", "BatteryV  = " + BatteryV);
            Log.e("wbw", "V  = " + BatteryV * 2 / 100);
            //	m_voltage = (int) (65+19*voltage_level/100);
            //   Log.e("wbw","m_voltage  = " + m_voltage );
        }
    }
}