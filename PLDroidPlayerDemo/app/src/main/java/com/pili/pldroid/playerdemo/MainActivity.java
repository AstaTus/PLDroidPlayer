package com.pili.pldroid.playerdemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.pili.pldroid.player.AVOptions;
import com.pili.pldroid.playerdemo.utils.GetPathFromUri;
import com.pili.pldroid.playerdemo.utils.PermissionChecker;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private static final String DEFAULT_TEST_URL = "http://demo-videos.qnsdk.com/movies/qiniu.mp4";
//private static final String DEFAULT_TEST_URL = "https://sdk-release.qnsdk.com/10037108_065355-hd%20%281%29.mp4";
//        private static final String DEFAULT_TEST_URL = "rtmp://pili-publish.qnsdk.com/sdk-live/6666";
//    private static final String DEFAULT_TEST_URL = "https://playback-vpaas.cloudlinks.cn/vas/playback/m3u8?ownerid=9223372043327116262&deviceid=4689236446&startTime=1648088707&endTime=1648088968&sign=5bb74cbbecc053f3c954ecf75c4ed2df7cf7d3a2&port=39849";
//    private static final String DEFAULT_TEST_URL = "http://cdn.qiniushawn.top/timeshift3.m3u8";
    //private static final String DEFAULT_TEST_URL = "http://ojpjb7lbl.bkt.clouddn.com/h265/2000k/265_test.m3u8";
    private Spinner mActivitySpinner;
    private EditText mEditText;
    private RadioGroup mStreamingTypeRadioGroup;
    private RadioGroup mDecodeTypeRadioGroup;
    private CheckBox mVideoCacheCheckBox;
    private CheckBox mVideoCacheFileNameEncodeCheckBox;
    private CheckBox mLoopCheckBox;
    private CheckBox mVideoDataCallback;
    private CheckBox mAudioDataCallback;
    private CheckBox mDisableCheckBox;
    private RadioButton mLivingCheckBox;
    private RadioButton mPlayCheckBox;
    private LinearLayout mStartSetting;
    private EditText mStartPosEditText;

    public static final String[] TEST_ACTIVITY_ARRAY = {
            "PLMediaPlayerActivity",
            "PLAudioPlayerActivity",
            "PLVideoViewActivity",
            "PLVideoTextureActivity",
            "MultiInstanceActivity",
            "ChangeActivity"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView mVersionInfoTextView = findViewById(R.id.version_info);
        mVersionInfoTextView.setText("版本号: " + BuildConfig.VERSION_NAME + "\n");
        mVersionInfoTextView.append("编译时间： " + getBuildTimeDescription());

        mEditText = findViewById(R.id.VideoPathEdit);
        mEditText.setText(DEFAULT_TEST_URL);

        mStreamingTypeRadioGroup = findViewById(R.id.StreamingTypeRadioGroup);
        mDecodeTypeRadioGroup = findViewById(R.id.DecodeTypeRadioGroup);

        mActivitySpinner = findViewById(R.id.TestSpinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, TEST_ACTIVITY_ARRAY);
        mActivitySpinner.setAdapter(adapter);
        mActivitySpinner.setSelection(2);

        mVideoCacheCheckBox = findViewById(R.id.CacheCheckBox);
        mVideoCacheFileNameEncodeCheckBox = findViewById(R.id.CacheFileNameEncodeCheckBox);
        mLoopCheckBox = findViewById(R.id.LoopCheckBox);
        mVideoDataCallback = findViewById(R.id.VideoCallback);
        mAudioDataCallback = findViewById(R.id.AudioCallback);
        mDisableCheckBox = findViewById(R.id.DisableLog);

        mVideoCacheCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (!isPermissionOK()) {
                    mVideoCacheCheckBox.setChecked(false);
                }
            }
        });

        mLivingCheckBox = findViewById(R.id.RadioLiveStreaming);
        mPlayCheckBox = findViewById(R.id.RadioPlayback);
        mStartSetting = findViewById(R.id.StartSetting);

        mLivingCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mStartSetting.setVisibility(View.INVISIBLE);
            }
        });
        mPlayCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mStartSetting.setVisibility(View.VISIBLE);
            }
        });

        mStartPosEditText = findViewById(R.id.TextStartPos);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public boolean isPermissionOK() {
        PermissionChecker checker = new PermissionChecker(this);
        boolean isPermissionOK = Build.VERSION.SDK_INT < Build.VERSION_CODES.M || checker.checkPermission();
        if (!isPermissionOK) {
            Toast.makeText(this, "Some permissions is not approved !!!", Toast.LENGTH_SHORT).show();
        }
        return isPermissionOK;
    }

    public void onClickLocalFile(View v) {
        if(!isPermissionOK()){
            return;
        }
        Intent intent = new Intent();
        if (Build.VERSION.SDK_INT < 19) {
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("video/*");
        } else {
            intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("video/*");
        }
        startActivityForResult(Intent.createChooser(intent, "选择要导入的视频"), 0);
    }

    public void onClickScanQrcode(View v) {
        if(!isPermissionOK()){
            return;
        }
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
        integrator.setOrientationLocked(true);
        integrator.setCameraId(0);
        integrator.setBeepEnabled(true);
        integrator.initiateScan();
    }

    public void onClickPlay(View v) {
        String videopath = mEditText.getText().toString();
        if (!"".equals(videopath)) {
            jumpToPlayerActivity(videopath, false);
        }
    }

    public void onClickList(View v) {
        String videopath = mEditText.getText().toString();
        if (!"".equals(videopath)) {
            jumpToPlayerActivity(videopath, true);
        }
    }

    public void jumpToPlayerActivity(String videoPath, boolean isList) {
        if (isList) {
            Intent intent = new Intent(this, PLVideoListActivity.class);
            intent.putExtra("videoPath", videoPath);
            startActivity(intent);
            return;
        }
        Class<?> cls;
        switch (mActivitySpinner.getSelectedItemPosition()) {
            case 0:
                cls = PLMediaPlayerActivity.class;
                break;
            case 1:
                cls = PLAudioPlayerActivity.class;
                break;
            case 2:
                cls = PLVideoViewActivity.class;
                break;
            case 3:
                cls = PLVideoTextureActivity.class;
                break;
            case 4:
                cls = MultiInstanceActivity.class;
                break;
            case 5:
                cls = ChangeActivity.class;
                break;
            default:
                return;
        }
        Intent intent = new Intent(this, cls);
        intent.putExtra("videoPath", videoPath);
        if (mDecodeTypeRadioGroup.getCheckedRadioButtonId() == R.id.RadioHWDecode) {
            intent.putExtra("mediaCodec", AVOptions.MEDIA_CODEC_HW_DECODE);
        } else if (mDecodeTypeRadioGroup.getCheckedRadioButtonId() == R.id.RadioSWDecode) {
            intent.putExtra("mediaCodec", AVOptions.MEDIA_CODEC_SW_DECODE);
        } else {
            intent.putExtra("mediaCodec", AVOptions.MEDIA_CODEC_AUTO);
        }
        if (mStreamingTypeRadioGroup.getCheckedRadioButtonId() == R.id.RadioLiveStreaming) {
            intent.putExtra("liveStreaming", 1);
        } else {
            intent.putExtra("liveStreaming", 0);
        }
        intent.putExtra("cache", mVideoCacheCheckBox.isChecked());
        intent.putExtra("cache-filename-encode", mVideoCacheFileNameEncodeCheckBox.isChecked());
        intent.putExtra("loop", mLoopCheckBox.isChecked());
        intent.putExtra("video-data-callback", mVideoDataCallback.isChecked());
        intent.putExtra("audio-data-callback", mAudioDataCallback.isChecked());
        intent.putExtra("disable-log", mDisableCheckBox.isChecked());
        if (!"".equals(mStartPosEditText.getText().toString())) {
            intent.putExtra("start-pos", Integer.valueOf(mStartPosEditText.getText().toString()));
        }
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode == 0) {
            String selectedFilepath = GetPathFromUri.getPath(this, data.getData());
            Log.i(TAG, "Select file: " + selectedFilepath);
            if (selectedFilepath != null && !"".equals(selectedFilepath)) {
                mEditText.setText(selectedFilepath, TextView.BufferType.EDITABLE);
            }
        } else if (requestCode == IntentIntegrator.REQUEST_CODE) {
            IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            if (result != null) {
                if (result.getContents() == null) {
                    Toast.makeText(this, "扫码取消！", Toast.LENGTH_SHORT).show();
                } else {
                    mEditText.setText(result.getContents());
                }
            }
        }
    }

    protected String getBuildTimeDescription() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).format(BuildConfig.BUILD_TIMESTAMP);
    }
}
