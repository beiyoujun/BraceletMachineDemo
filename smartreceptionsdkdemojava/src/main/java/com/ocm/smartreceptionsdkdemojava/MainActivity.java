package com.ocm.smartreceptionsdkdemojava;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.ocm.smart_reception_sdk.BraceletMachineListener;
import com.ocm.smart_reception_sdk.BraceletMachineManager;
import com.ocm.smart_reception_sdk.BraceletMachineSystemListener;
import com.ocm.smart_reception_sdk.CheckGiveBackCallback;
import com.ocm.smart_reception_sdk.CheckSelfCallback;
import com.ocm.smart_reception_sdk.FetchCallback;
import com.ocm.smart_reception_sdk.GiveBackCallback;
import com.ocm.smart_reception_sdk.Machine.CardDataModel;
import com.zyao89.view.zloading.ZLoadingDialog;
import com.zyao89.view.zloading.Z_TYPE;

import org.jetbrains.annotations.NotNull;

public class MainActivity extends Activity implements View.OnClickListener {

    Button fetchBraceletBtn,backBraceletBtn,checkSelfBtn,openBackBtn,closeBackBtn;
    BraceletMachineManager bmManager;
    ZLoadingDialog loader;
    String TAG = "SMARTRECEPTINOSDKDEMO";

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
        checkSelfBtn = (Button)findViewById(R.id.check_self);
        openBackBtn = (Button)findViewById(R.id.open_back);
        closeBackBtn = (Button)findViewById(R.id.close_back);
        loader = new ZLoadingDialog(this)
                    .setCancelable(false)
                    .setCanceledOnTouchOutside(false)
                    .setLoadingBuilder(Z_TYPE.CIRCLE);

        checkSelfBtn.setOnClickListener(this);
        fetchBraceletBtn.setOnClickListener(this);
        backBraceletBtn.setOnClickListener(this);
        openBackBtn.setOnClickListener(this);
        closeBackBtn.setOnClickListener(this);

        bmManager = BraceletMachineManager.INSTANCE;
        bmManager.setDebug(true);
        bmManager.configHost("smartgymdemo.ocmcom.com");
        bmManager.bind(this,"123456","123456");
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

    void checkSelf(){
        //IC手环
        loader.show();
        showToast("开始自检");
        bmManager.checkSelf(BraceletMachineManager.CardType.ID,new CheckSelfCallback(){

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
                        checkSelfBtn.setEnabled(false);
                        backBraceletBtn.setEnabled(true);
                        fetchBraceletBtn.setEnabled(true);
                        openBackBtn.setEnabled(true);
                        closeBackBtn.setEnabled(true);
                        showToast("自检成功");
                        loader.dismiss();
                    }
                });
            }

            @Override
            public void onCheckSelfFail(@NotNull final  String s) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        checkSelfBtn.setEnabled(true);
                        backBraceletBtn.setEnabled(false);
                        fetchBraceletBtn.setEnabled(false);
                        openBackBtn.setEnabled(false);
                        closeBackBtn.setEnabled(false);
                        showToast("自检失败:"+s);
                        loader.dismiss();
                    }
                });
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.fetch_bracelet:
                fetchBracelet();
                break;
            case R.id.back_bracelet:
                backBracelet();
                break;
            case R.id.check_self:
                checkSelf();
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
            public void onFetchSuccess(@NotNull String s) {
                showToast("取手环成功 手环号:"+s);
            }

            @Override
            public void onFetchFail(@NotNull String s) {
            showToast("取手环失败:"+s);
            }

            @Override
            public void onCompleted() {

            }
        });
    }

    void backBracelet(){
        showToast("请将手环放置回收口");
        bmManager.giveBackBracelet(new GiveBackCallback() {
            @Override
            public void checkAllowGiveBack(@NotNull CardDataModel cardDataModel, @NotNull CheckGiveBackCallback checkGiveBackCallback) {
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
            public void onGiveBackSuccess(@NotNull String s) {
                showToast("归还手环成功 手环号:"+s);
            }

            @Override
            public void onGiveBackFail(@NotNull String s) {
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


}
