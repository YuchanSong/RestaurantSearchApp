package uchan.weather;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class SettingActivity extends AppCompatActivity implements View.OnClickListener {

    private AlertDialog dialog;
    private NotificationManager mNotificationManager = null;
    private TextView btnAlert;
    private TextView loca;
    String strName = null;
    String location = null;
    Boolean chk1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        Intent intent = getIntent();
        btnAlert = (TextView) findViewById(R.id.location_alert);
        loca = (TextView) findViewById(R.id.loca);
        SharedPreferences pref = getSharedPreferences("pref", Activity.MODE_PRIVATE); // Shared Preference를 불러옵니다.
        ToggleButton tgb = (ToggleButton) this.findViewById(R.id.tgb);

        // 저장된 값들을 불러옵니다.
        chk1 = pref.getBoolean("check1", false);
        location = pref.getString("location", strName);
        tgb.setChecked(chk1);

        if (location == null || location.equals("초기화"))
            loca.setText("설정된 관심 지역이 없습니다.");
        else
            loca.setText("설정된 관심지역 : " + location);

        btnAlert.setOnClickListener(this);

        tgb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked == true) {
                    Toast.makeText(SettingActivity.this, "상단에 위젯을 표시합니다.", Toast.LENGTH_SHORT).show();
                    createnoti();
                } else {
                    Toast.makeText(SettingActivity.this, "상단에 위젯을 제거합니다.", Toast.LENGTH_SHORT).show();
                    deletenoti();
                }
            }
        });
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.location_alert:
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(
                        SettingActivity.this);
                alertBuilder.setIcon(R.drawable.logo);
                alertBuilder.setTitle("관심지역 하나를 선택해주세요.");

                // List Adapter 생성
                final ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                        SettingActivity.this,
                        android.R.layout.select_dialog_singlechoice);
                adapter.add("초기화");
                adapter.add("서울시");
                adapter.add("경기도");
                adapter.add("강원도");
                adapter.add("충청북도");
                adapter.add("충청남도");
                adapter.add("전라북도");
                adapter.add("전라남도");
                adapter.add("경상북도");
                adapter.add("경상남도");
                adapter.add("제주도");

                // 버튼 생성
                alertBuilder.setNegativeButton("취소",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                // Adapter 셋팅
                alertBuilder.setAdapter(adapter,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                // AlertDialog 안에 있는 AlertDialog

                                strName = adapter.getItem(id);
                                AlertDialog.Builder innBuilder = new AlertDialog.Builder(
                                        SettingActivity.this);
                                innBuilder.setTitle("Weather-Fi");
                                innBuilder.setMessage("\n\t\t관심 지역이 " + strName + "(으)로 설정되었습니다.");
                                innBuilder.setPositiveButton("확인",
                                                new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        SharedPreferences pref = getSharedPreferences("pref", Activity.MODE_PRIVATE); // UI 상태를 저장합니다.
                                                        SharedPreferences.Editor editor = pref.edit(); // Editor를 불러옵니다.

                                                        // 저장할 값들을 입력합니다.
                                                        editor.putString("location", strName);

                                                        // 저장합니다.
                                                        editor.commit();

                                                        // 액티비티 리프레쉬
                                                        Intent intent = getIntent();
                                                        finish();
                                                        intent.putExtra("check",true);

                                                        startActivity(intent);
                                                    }
                                                });
                                innBuilder.show();
                            }
                        });
                alertBuilder.show();
                break;

            default:
                break;
        }
    }

    public void onStop(){ // 어플리케이션이 화면에서 사라질때
        super.onStop();
        SharedPreferences pref = getSharedPreferences("pref", Activity.MODE_PRIVATE); // UI 상태를 저장합니다.
        SharedPreferences.Editor editor = pref.edit(); // Editor를 불러옵니다.

        ToggleButton tgb = (ToggleButton) this.findViewById(R.id.tgb);

        // 저장할 값들을 입력합니다.
        editor.putBoolean("check1", tgb.isChecked());

        // 저장합니다.
        editor.commit();
    }

    public void createnoti() {
        if (location == null || location.equals("초기화")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
            dialog = builder.setMessage("관심 지역을 먼저 설정해주세요.")
                    .setNegativeButton("확인", null)
                    .create();
            dialog.show();
        }
        else {
            mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

            Bitmap mLargeIconForNoti =
                    BitmapFactory.decodeResource(getResources(), R.drawable.logo);

            PendingIntent mPendingIntent = PendingIntent.getActivity(
                    SettingActivity.this, 0,
                    new Intent(getApplicationContext(), MainActivity.class),
                    PendingIntent.FLAG_UPDATE_CURRENT
            );


            android.support.v4.app.NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(SettingActivity.this)
                            .setSmallIcon(R.drawable.logo)
                            .setContentTitle("Weather-Fi")
                            .setContentText(location + ", 최저기온 : -2℃, 최고기온 : 10℃, 날씨 : 맑음")
                            .setLargeIcon(mLargeIconForNoti)
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                            .setOngoing(true)
                            .setContentIntent(mPendingIntent);

            // 확장노티
            android.support.v4.app.NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle(mBuilder);
            inboxStyle.addLine("한줄 한줄");
            inboxStyle.addLine("한땀 한땀");
            inboxStyle.addLine("고이 고이");
            inboxStyle.addLine("적어드립니다.");
            inboxStyle.setSummaryText("더 보기");
            mBuilder.setStyle(inboxStyle);

            mNotificationManager.notify(0, mBuilder.build());

        }
    }

    public void deletenoti() {
        mNotificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotificationManager.cancel(0);
    }
}