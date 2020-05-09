package com.ocm.smartreceptionsdkdemojava;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.ocm.bracelet_machine_sdk.BraceletMachineListener;
import com.ocm.bracelet_machine_sdk.BraceletMachineManager;
import com.ocm.bracelet_machine_sdk.BraceletMachineSystemListener;
import com.ocm.bracelet_machine_sdk.CheckGiveBackCallback;
import com.ocm.bracelet_machine_sdk.CheckSelfCallback;
import com.ocm.bracelet_machine_sdk.DefaultCallback;
import com.ocm.bracelet_machine_sdk.FetchCallback;
import com.ocm.bracelet_machine_sdk.GiveBackCallback;
import com.ocm.bracelet_machine_sdk.Machine.CardDataModel;
import com.zyao89.view.zloading.ZLoadingDialog;
import com.zyao89.view.zloading.Z_TYPE;

import static com.ocm.bracelet_machine_sdk.BraceletMachineManager.INSTANCE;
import static com.ocm.bracelet_machine_sdk.BraceletMachineManager.SectorType;

public class MainActivity extends Activity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    Button fetchBraceletBtn,backBraceletBtn,checkSelfBtn,openBackBtn,closeBackBtn,fetchMulBraceletBtn,refreshBtn;
    TextView mac;
    Switch switchQRCode,switchMode,swtichLedLight,swtichIRLight;
    BraceletMachineManager bmManager;
    ZLoadingDialog loader;
    String TAG = "SMARTRECEPTINOSDKDEMO";
    String APPKEY = "填写您的appkey";
    String APPSECRET = "填写您的appSecret";
    boolean isBinded = false;

    Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }


    void initView(){
        fetchBraceletBtn = (Button)findViewById(R.id.fetch_bracelet);
        backBraceletBtn = (Button)findViewById(R.id.back_bracelet);
        fetchMulBraceletBtn = (Button)findViewById(R.id.fetch_mul_bracelet);
        checkSelfBtn = (Button)findViewById(R.id.check_self);
        refreshBtn = (Button)findViewById(R.id.refresh);
        openBackBtn = (Button)findViewById(R.id.open_back);
        closeBackBtn = (Button)findViewById(R.id.close_back);
        switchQRCode = findViewById(R.id.switch_qrcode);
        switchMode = findViewById(R.id.switch_mode);
        swtichLedLight = findViewById(R.id.switch_led_light);
        swtichIRLight = findViewById(R.id.switch_ir_light);
        mac = findViewById(R.id.mac);
        loader = new ZLoadingDialog(this)
                    .setCancelable(false)
                    .setCanceledOnTouchOutside(false)
                    .setLoadingBuilder(Z_TYPE.CIRCLE);
        switchQRCode.setOnCheckedChangeListener(this);
        swtichLedLight.setOnCheckedChangeListener(this);
        swtichIRLight.setOnCheckedChangeListener(this);
        switchMode.setOnCheckedChangeListener(this);
        checkSelfBtn.setOnClickListener(this);
        fetchBraceletBtn.setOnClickListener(this);
        fetchMulBraceletBtn.setOnClickListener(this);
        backBraceletBtn.setOnClickListener(this);
        openBackBtn.setOnClickListener(this);
        closeBackBtn.setOnClickListener(this);
        refreshBtn.setOnClickListener(this);

        bmManager = INSTANCE;
        mac.setText(MacHelper.getLocalMac(this));
    }

    void bind(boolean isOnline){
        bmManager.setDebug(false);
        if(isOnline)bmManager.bind(this,APPKEY,APPSECRET);
        else bmManager.bind(this);
        setMode(isOnline);
        bmManager.setBraceletMachineListener(new BraceletMachineListener() {
            @Override
            public void onDisconnect() {
                Log.i(TAG,"onDisconnect");
                showToast("设备连接已断开");
            }
            @Override
            public void onCurrentNumChange(int i) {
                Log.i(TAG,"onCurrentNumChange:"+i);

                if(i==0){
                    bmManager.stopGiveBack();
                    showToast("无手环 暂停设备");
                }else
                    showToast("手环数量变化"+i);
            }

            @Override
            public void onStateChange(boolean b) {
                Log.i(TAG,"onStateChange:"+b);
                showToast("设备已暂停");
            }
        });
    }

    void setMode(boolean isOnline){
        bmManager.setBraceletMode(isOnline?
                        BraceletMachineManager.BraceletMode.ONLINE:
                        BraceletMachineManager.BraceletMode.OFFLINE,
                new DefaultCallback() {
                    @Override
                    public void onSuccess(String s) {
                        showToast("切换模式成功:"+s);
                    }

                    @Override
                    public void onFail(String s) {
                        showToast("切换模式失败:"+s);
                    }

                    @Override
                    public void onCompleted() {
                        checkSelf();
                    }
                });
    }
    void checkSelf(){
        loader.show();
        //如需离线，请在自检前切换模式
        showToast("开始自检");
        //IC手环
        bmManager.checkSelf(BraceletMachineManager.CardType.IC,new CheckSelfCallback(){
            @Override
            public void onCompleted() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        showToast("自检完成");
                        loader.dismiss();
                    }
                });
            }

            @Override
            public void onCheckSelfSuccess() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        showToast("自检成功");
                        setBtnEnable(true);
                        loader.dismiss();
                    }
                });
            }

            @Override
            public void onCheckSelfFail(final  String s) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        setBtnEnable(false);
                        showToast("自检失败:"+s);
                        loader.dismiss();
                    }
                });
            }
        });
    }
    void setBtnEnable(boolean enable){
        checkSelfBtn.setEnabled(!enable);
        backBraceletBtn.setEnabled(enable);
        fetchBraceletBtn.setEnabled(enable);
        fetchMulBraceletBtn.setEnabled(enable);
        openBackBtn.setEnabled(enable);
        closeBackBtn.setEnabled(enable);
    }
    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.fetch_bracelet:
                fetchBracelet();
                break;
            case R.id.fetch_mul_bracelet:
                fetchMulBracelet();
                break;
            case R.id.back_bracelet:
                backBracelet();
                break;
            case R.id.check_self:
                bind(switchMode.isChecked());
                break;
            case R.id.open_back:
                openBackBtn.setEnabled(false);
                bmManager.sysStartPush(new BraceletMachineSystemListener() {
                    @Override
                    public void onSuccess() {
                        btnEnable();
                    }

                    @Override
                    public void onFail() {
                        btnEnable();
                    }
                });
                break;
            case R.id.close_back:
                closeBackBtn.setEnabled(false);
                bmManager.sysStopPush(new BraceletMachineSystemListener() {
                    @Override
                    public void onSuccess() {
                        btnEnable();
                    }

                    @Override
                    public void onFail() {
                        btnEnable();
                    }
                });
                break;
        }
    }

    void btnEnable(){
        handler.post(new Runnable() {
            @Override
            public void run() {
                openBackBtn.setEnabled(true);
                closeBackBtn.setEnabled(true);
            }
        });
    }

    void showToast(final String msg){
        Log.i(TAG,msg);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this,msg,Toast.LENGTH_LONG).show();
            }
        });

    }

    void fetchBracelet(){
        bmManager.fetchBracelet(new FetchCallback() {
            @Override
            public void onFetchSuccess(String s) {
                showToast("取手环成功 手环号:"+s);
            }

            @Override
            public void onFetchFail(String s) {
            showToast("取手环失败:"+s);
            }

            @Override
            public void onRemainingFetch(int i) {

            }

            @Override
            public void onCompleted() {

            }
        });
    }
    void fetchMulBracelet(){
        int braceletCount = 3;
        String pwd = "FFFFFFFFFFFF";
        String blockData = "1234567890ABCEDF";
        bmManager.fetchMultiBracelet(braceletCount,SectorType.SECTOR2,pwd,blockData, new FetchCallback() {

            @Override
            public void onFetchSuccess(String s) {
                showToast("取手环成功 手环号:"+s);
            }

            @Override
            public void onFetchFail(String s) {
                showToast("取手环失败:"+s);
            }

            @Override
            public void onRemainingFetch(int i) {
                showToast("取手环"+braceletCount+"个,其中有"+i+"个未发出");
            }

            @Override
            public void onCompleted() {

            }
        });
    }

    void backBracelet(){
        showToast("请将手环放置回收口");
        String sectorPwd = "FFFFFFFFFFFF";
        //3个块的数据 48个字节 字符串长度96
        String blockContent="00112233445566778899AABBCCDDEEFFFFEEDDCCBBAA998877665544332211000123456789ABCDEF0123456789ABCDEF";
        bmManager.giveBackBracelet(SectorType.SECTOR2,sectorPwd,blockContent, new GiveBackCallback() {
            @Override
            public void checkAllowGiveBack(CardDataModel cardDataModel, CheckGiveBackCallback checkGiveBackCallback) {
                showToast("获取到卡号:"+cardDataModel.CardNo+"柜号:"+cardDataModel.CabinetNos);
                //允许归还
                checkGiveBackCallback.allow(true,"OK");
                Log.i(TAG,"checkAllowGiveBack");
            }

            @Override
            public void onGiveBackBusy() {
                showToast("设备忙碌中，请稍等");
            }

            @Override
            public void onGiveBackSuccess(String s) {
                showToast("归还手环成功 手环号:"+s);
            }

            @Override
            public void onGiveBackFail(String s) {
                showToast("归还手环失败");
            }

            @Override
            public void onCompleted() {

            }
        });
    }

    @Override
    protected void onDestroy() {
        bmManager.onDestroy();
        super.onDestroy();
    }


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        int id = buttonView.getId();
        switch (id){
            case R.id.switch_qrcode:
                showToast("二维码"+isChecked);
                if (isChecked)bmManager.openQR();
                else bmManager.closeQR();
            break;
            case R.id.switch_led_light:
                showToast("LED灯"+isChecked);
                if (isChecked)bmManager.openLedLight();
                else bmManager.closeLedLight();
            break;
            case R.id.switch_ir_light:
                showToast("红外灯"+isChecked);
                if (isChecked)bmManager.openIRLight();
                else bmManager.closeIRLight();
            break;
            case R.id.switch_mode:
                showToast("切换模式:"+(isChecked?"在线":"离线"));
                break;

        }
    }

}
