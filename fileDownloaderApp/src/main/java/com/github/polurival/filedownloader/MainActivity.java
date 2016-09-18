package com.github.polurival.filedownloader;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

//http://stackoverflow.com/questions/3028306/download-a-file-with-android-and-showing-the-progress-in-a-progressdialog
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "fileDownloaderApp";
    private static final int PERMISSIONS_REQUEST = 0;

    private ProgressDialog mProgressDialog;
    private EditText etUrl;
    private EditText etExt;

    private String fileUrl;
    private String etExtText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initProgressDialog();

        etUrl = (EditText) findViewById(R.id.etUrl);
        etExt = (EditText) findViewById(R.id.etExt);

        //fileUrl = "https://www.dropbox.com/s/lopkyxsnl29s1r4/A2_L04.pdf";
        fileUrl = "https://encrypted-tbn3.gstatic.com/images?q=tbn:ANd9GcSnm0hdTXs4Mtj0XwlMBfKphsv451dNsqJQ-8_LbxFaK79LT99x";

        checkPermissions();
    }

    private void initProgressDialog() {
        mProgressDialog = new ProgressDialog(MainActivity.this);
        mProgressDialog.setMessage("downloading...");
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setCancelable(true);
    }

    public void downloadFile(View view) {
        final DownloadTask downloadTask = new DownloadTask(MainActivity.this);
        String etUrlText = etUrl.getText().toString();
        etExtText = etExt.getText().toString();

        if (ActivityCompat
                .checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
                || ActivityCompat
                .checkSelfPermission(this, Manifest.permission.WAKE_LOCK)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        if (etUrlText.length() == 0 && etExtText.length() == 0) {
            downloadTask.execute(fileUrl);
        } else {
            downloadTask.execute(etUrlText);
        }

        mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                downloadTask.cancel(true);
            }
        });
    }

    private class DownloadTask extends AsyncTask<String, Integer, String> {

        private Context context;
        private PowerManager.WakeLock mWakeLock;

        public DownloadTask(Context context) {
            this.context = context;
        }

        @Override
        protected String doInBackground(String... sUrl) {
            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;
            try {
                URL url = new URL(sUrl[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                // expect HTTP 200 OK, so we don't mistakenly save error report
                // instead of the file
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return "Server returned HTTP " + connection.getResponseCode()
                            + " " + connection.getResponseMessage();
                }

                // this will be useful to display download percentage
                // might be -1: server did not report the length
                int fileLength = connection.getContentLength();

                // download the file
                input = connection.getInputStream();
                File f = getFile(sUrl[0]);
                //output = new FileOutputStream("/sdcard/file_name.jpg");
                output = new FileOutputStream(f.getPath());

                byte data[] = new byte[4096];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    // allow canceling with back button
                    if (isCancelled()) {
                        input.close();
                        return null;
                    }
                    total += count;
                    // publishing the progress....
                    if (fileLength > 0) // only if total length is known
                        publishProgress((int) (total * 100 / fileLength));
                    output.write(data, 0, count);
                }
            } catch (Exception e) {
                return e.toString();
            } finally {
                try {
                    if (output != null)
                        output.close();
                    if (input != null)
                        input.close();
                } catch (IOException ignored) {
                }

                if (connection != null)
                    connection.disconnect();
            }
            return null;
        }

        //http://stackoverflow.com/questions/18605440/android-canvas-save-always-java-io-ioexception-open-failed-enoent-no-such-fil
        @NonNull
        private File getFile(String url) {
            String extr = Environment.getExternalStorageDirectory().toString();
            File mFolder = new File(extr + "/fileDownloaderApp");
            if (!mFolder.exists()) {
                mFolder.mkdirs();
            }

            String fName;
            if (url != null && url.equals(fileUrl)) {
                fName = "mockImage.jpg";
            } else {
                fName = url.substring(url.length() - 6).replaceAll("()\\W+", "") + "." + etExtText;
            }
            return new File(mFolder.getAbsolutePath(), fName);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // take CPU lock to prevent CPU from going off if the user
            // presses the power button during download
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
            mWakeLock.acquire();
            mProgressDialog.show();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            // if we get here, length is known, now set indeterminate to false
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setMax(100);
            mProgressDialog.setProgress(progress[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            mWakeLock.release();
            mProgressDialog.dismiss();
            if (result != null) {
                Log.d(TAG, "file wasn't downloaded");
                Toast.makeText(context, "Download error: " + result, Toast.LENGTH_LONG).show();
            } else {
                Log.d(TAG, "file was successfully downloaded");
                Toast.makeText(context, "File downloaded", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void checkPermissions() {
        Log.d(TAG, "checkPermissions");
        if (ActivityCompat
                .checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
                || ActivityCompat
                .checkSelfPermission(this, Manifest.permission.WAKE_LOCK)
                != PackageManager.PERMISSION_GRANTED) {
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.WAKE_LOCK},
                    PERMISSIONS_REQUEST);
        }
    }
}
