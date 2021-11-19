package com.edit.pri_recs;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.FileObserver;
import android.os.IBinder;
import android.util.Log;

import java.io.File;
import java.io.IOException;

public class FileSystemObserverService extends Service {

    //private static FileSystemObserverService inst2;
    String TAG = "FileSystemObserverService";
    File str;
    String externalPath;
    MainActivity inst = MainActivity.instance();

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet Implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        observe();
        return super.onStartCommand(intent, flags, startId);
    }

//    public static FileSystemObserverService instance() {
//        return inst2;
//    }

    public File getExternalStoragePath() {
        Log.d(TAG, "getExternalStoragePath: Files exploration started");
        return Environment.getExternalStorageDirectory();
    }

    public void observe() {
        Thread t = new Thread(() -> {
            File myDir = new File(getExternalStoragePath() + "/Download/Receipts/");
            if (myDir.exists()) {
                str = new File(getExternalStoragePath()+ "/Download/Receipts/");
                if (str != null) {
                    externalPath = str.getAbsolutePath();
                    System.out.println("My shit path: " + externalPath);
                    //Beginning of watching
                    new DirectoryFileObserver(externalPath).startWatching();
                    /*new Obsever(externalPath).startWatching();*/
                } else {
                    Log.d(TAG, "No files detected");
                }
            } else {
                //System.out.println("Folder does not exist");
                Log.d(TAG, "Folder does not exist");
            }
        });
        t.setPriority(Thread.MIN_PRIORITY);
        t.start();
    }

    class DirectoryFileObserver extends FileObserver {
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
                    //Start animation
                    //new FloatingViewService().animateButton();
                    break;

                case FileObserver.MOVED_TO:
                    Log.e("FileObserver: ","File Moved here");
                    try {
                        inst.prtReceipt();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e("onEvent: MovedTo", "Call print receipt func fails.");
                    }

                default:
                    Log.e("FileObserver: ","Some Other Shit went down");
            }
        }
    }


    /*class Obsever extends FileObserver {

        List< SingleFileObserver > mObservers;
        String mPath;
        int mMask;
        public Obsever(String path) {
            // TODO Auto-generated constructor stub
            this(path, ALL_EVENTS);
        }
        public Obsever(String path, int mask) {
            super(path, mask);
            mPath = path;
            mMask = mask;
            // TODO Auto-generated constructor stub

        }
        @Override
        public void startWatching() {
            // TODO Auto-generated method stub
            if (mObservers != null)
                return;
            mObservers = new ArrayList < SingleFileObserver > ();
            Stack < String > stack = new Stack < String > ();
            stack.push(mPath);
            while (!stack.empty()) {
                String parent = stack.pop();
                mObservers.add(new SingleFileObserver(parent, mMask));
                File path = new File(parent);
                File[] files = path.listFiles();
                if (files == null) continue;
                for (int i = 0; i < files.length; ++i) {
                    if (files[i].isDirectory() && !files[i].getName().equals(".") && !files[i].getName().equals("..")) {
                        stack.push(files[i].getPath());
                    }
                }
            }
            for (int i = 0; i < mObservers.size(); i++) {
                mObservers.get(i).startWatching();
            }
        }
        @Override
        public void stopWatching() {
            // TODO Auto-generated method stub
            if (mObservers == null)
                return;
            for (int i = 0; i < mObservers.size(); ++i) {
                mObservers.get(i).stopWatching();
            }
            mObservers.clear();
            mObservers = null;
        }
        @Override
        public void onEvent(int event, final String path) {
            if (event == FileObserver.OPEN) {
                //do whatever you want
            } else if (event == FileObserver.CREATE) {
                //do whatever you want
                Log.e("FileObserver ","File Created");
                //Call for printing
                try {
                    inst.prtReceipt();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e("onEvent: Create", "Call print receipt func fails.");
                }
            } else if (event == FileObserver.DELETE_SELF || event == FileObserver.DELETE) {

                //do whatever you want
            } else if (event == FileObserver.MOVE_SELF || event == FileObserver.MOVED_FROM || event == FileObserver.MOVED_TO) {
                //do whatever you want

            }
        }

        private class SingleFileObserver extends FileObserver {
            private String mPath;
            public SingleFileObserver(String path, int mask) {
                super(path, mask);
                // TODO Auto-generated constructor stub
                mPath = path;
            }

            @Override
            public void onEvent(int event, String path) {
                // TODO Auto-generated method stub
                String newPath = mPath + "/" + path;
                Obsever.this.onEvent(event, newPath);
            }

        }

    }*/
}
