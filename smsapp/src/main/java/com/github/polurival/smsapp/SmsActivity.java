package com.github.polurival.smsapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Polurival
 * on 02.10.2016.
 * <p/>
 * http://stackoverflow.com/a/9494532/5349748
 * http://stackoverflow.com/a/14097569/5349748
 */
public class SmsActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_READ_SMS = 0;
    public static final String SMS_ADDRESS = "address";
    public static final String SMS_BODY = "body";
    public static final String CONTENT_SMS_INBOX_URI = "content://sms/inbox";

    private RecyclerView mRecyclerView;
    private List<SmsEntity> smsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms);

        smsList = new ArrayList<>();

        initViews();
        showSms();
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_READ_SMS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getSmsList();
            } else {
                Toast.makeText(this, "Until we access READ_SMS permission, we cannot display any sms",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private void initViews() {
        mRecyclerView = (RecyclerView) findViewById(R.id.smsList);
        mRecyclerView.setHasFixedSize(true);
    }

    private void showSms() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                checkSelfPermission(
                        Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                    new String[]{Manifest.permission.READ_SMS}, PERMISSIONS_REQUEST_READ_SMS);
        } else {
            getSmsList();
        }
    }

    private void getSmsList() {
        Cursor c = getContentResolver()
                .query(Uri.parse(CONTENT_SMS_INBOX_URI), null, null, null, SMS_ADDRESS);

        if (c != null && c.moveToFirst()) {
            for (int i = 0; i < c.getCount(); i++) {
                String address = c.getString(c.getColumnIndexOrThrow(SMS_ADDRESS));
                String body = c.getString(c.getColumnIndexOrThrow(SMS_BODY));
                SmsEntity sms = new SmsEntity(address, body);
                smsList.add(sms);

                c.moveToNext();
            }

            c.close();
        }

        initRecyclerList();
    }

    private void initRecyclerList() {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        mRecyclerView.setAdapter(new SmsAdapter());
    }

    public class SmsAdapter extends RecyclerView.Adapter<SmsAdapter.SmsHolder> {

        @Override
        public SmsHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getApplicationContext())
                    .inflate(R.layout.item_sms_list, parent, false);
            return new SmsHolder(view);
        }

        @Override
        public void onBindViewHolder(SmsHolder smsHolder, int position) {
            smsHolder.tvAddress.setText(smsList.get(position).getAddress());
            smsHolder.tvBody.setText(smsList.get(position).getBody());
        }

        @Override
        public int getItemCount() {
            return smsList == null ? 0 : smsList.size();
        }

        public class SmsHolder extends RecyclerView.ViewHolder {

            public TextView tvAddress;
            public TextView tvBody;

            public SmsHolder(View itemView) {
                super(itemView);
                tvAddress = (TextView) itemView.findViewById(R.id.address);
                tvBody = (TextView) itemView.findViewById(R.id.body);
            }
        }
    }
}
