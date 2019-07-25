package com.ocm.smartreceptionsdkdemojava;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.ocm.smart_reception_sdk.BraceletMachineListener;
import com.ocm.smart_reception_sdk.BraceletMachineManager;
import com.ocm.smart_reception_sdk.BraceletMachineSystemListener;
import com.ocm.smart_reception_sdk.CheckGiveBackCallback;
import com.ocm.smart_reception_sdk.Machine.CardDataModel;

import org.jetbrains.annotations.NotNull;

public class MainActivity extends Activity implements View.OnClickListener {

    Button fetchBraceletBtn,backBraceletBtn,checkSelfBtn,openBackBtn,closeBackBtn;
    BraceletMachineManager bmManager;
    String TAG = "SMARTRECEPTINOSDKDEMO";

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

        checkSelfBtn.setOnClickListener(this);
        fetchBraceletBtn.setOnClickListener(this);
        backBraceletBtn.setOnClickListener(this);
        openBackBtn.setOnClickListener(this);
        closeBackBtn.setOnClickListener(this);

        bmManager = BraceletMachineManager.INSTANCE;
        bmManager.setSystemListener(new BraceletMachineSystemListener() {
            @Override
            public void onSuccess() {
                Log.i(TAG,"System Success");
                openBackBtn.setEnabled(true);
                closeBackBtn.setEnabled(true);
            }

            @Override
            public void onFail() {
                Log.i(TAG,"System Fail");
                openBackBtn.setEnabled(true);
                closeBackBtn.setEnabled(true);
            }
        });
        bmManager.setBraceletMachineListener(new BraceletMachineListener() {
            @Override
            public void checkAllowGiveBack(@NotNull CardDataModel cardDataModel, @NotNull CheckGiveBackCallback checkGiveBackCallback) {
                showToast("获取到卡号:"+cardDataModel.CardNo+"柜号:"+cardDataModel.CabinetNos);
                //允许归还
                checkGiveBackCallback.allow(true,"OK");
                Log.i(TAG,"checkAllowGiveBack");
            }

            @Override
            public void onCheckSelfBack(boolean b, @NotNull String s) {
                Log.i(TAG,s);
                checkSelfBtn.setEnabled(false);
                backBraceletBtn.setEnabled(true);
                fetchBraceletBtn.setEnabled(true);
                openBackBtn.setEnabled(true);
                closeBackBtn.setEnabled(true);
                showToast("自检完成: 结果"+(b?"成功":"失败"));
            }

            @Override
            public void onDisconnect() {
                Log.i(TAG,"onDisconnect");
                showToast("设备连接已断开");
            }

            @Override
            public void onFetchSuccess(@NotNull String s) {
                Log.i(TAG,"onFetchSuccess:"+s);
                showToast("取手环成功 手环号:"+s);
            }

            @Override
            public void onFetchFail(@NotNull String s) {
                Log.i(TAG,"onFetchFail:"+s);
                showToast("取手环失败");
            }

            @Override
            public void onGiveBackBusy() {
                Log.i(TAG,"onGiveBackBusy");
                showToast("设备忙碌中，请稍等");
            }

            @Override
            public void onGiveBackSuccess(@NotNull String s) {
                Log.i(TAG,"onGiveBackSuccess:"+s);
                showToast("归还手环成功 手环号:"+s);
            }

            @Override
            public void onGiveBackFail(@NotNull String s) {
                Log.i(TAG,"onGiveBackFail:"+s);
                showToast("归还手环失败");
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
        bmManager.init(this);
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
                bmManager.checkSelf();
                break;
            case R.id.open_back:
                bmManager.sysStartPush();
                openBackBtn.setEnabled(false);
                break;
            case R.id.close_back:
                bmManager.sysStopPush();
                closeBackBtn.setEnabled(false);
                break;
        }
    }

    void showToast(final String msg){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this,msg,Toast.LENGTH_SHORT).show();
            }
        });

    }

    void fetchBracelet(){
        if(bmManager.checkHasBracelet()){
            bmManager.fetchBracelet();
        }else{
            showToast("取手环失败 暂无手环");
        }
    }

    void backBracelet(){
        if(!bmManager.checkIsFull()){
            bmManager.giveBackBracelet();
        }else{
            showToast("归还手环失败 手环已满");
        }
    }

    @Override
    protected void onDestroy() {
        bmManager.onDestroy();
        super.onDestroy();
    }


}
