package com.ocm.smartreceptionsdkdemo

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.ocm.smart_reception_sdk.*
import com.ocm.smart_reception_sdk.BraceletMachineManager.checkSelf
import com.ocm.smart_reception_sdk.Machine.CardDataModel
import com.zyao89.view.zloading.ZLoadingDialog
import com.zyao89.view.zloading.Z_TYPE
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : Activity(), View.OnClickListener {
    internal var TAG = "SMARTRECEPTINOSDKDEMO"
    lateinit var loader:ZLoadingDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initView();
    }

    private fun initView(){
        check_self.setOnClickListener(this)
        fetch_bracelet.setOnClickListener(this)
        back_bracelet.setOnClickListener(this)
        open_back.setOnClickListener(this)
        close_back.setOnClickListener(this)
        loader = ZLoadingDialog(this)
                .setCancelable(false)
                .setCanceledOnTouchOutside(false)
                .setLoadingBuilder(Z_TYPE.CIRCLE)
        BraceletMachineManager.configHost("smartgymdemo.ocmcom.com");
        BraceletMachineManager.bind(this,"123456","123456");
        BraceletMachineManager.setBraceletMachineListener(object : BraceletMachineListener{

            override fun onCurrentNumChange(num: Int) {
                Log.i(TAG, "onCurrentNumChange:$num")
                if (num == 0) {
                    BraceletMachineManager.stopGiveBack()
                    showToast("无手环 暂停设备")
                } else
                    showToast("手环数量变化$num")
            }

            override fun onDisconnect() {
                showToast("设备连接已断开")
            }

            override fun onStateChange(isStop: Boolean) {
                Log.i(TAG, "onStateChange:$isStop")
                if(isStop)
                    showToast("设备暂停");
            }
        })
    }


    fun btnEnable() {
        open_back.setEnabled(true)
        close_back.setEnabled(true)
    }

    override fun onClick(p0: View?) {
        when(p0?.id){
            R.id.check_self ->checkSelf()
            R.id.fetch_bracelet-> fetchBracelet()
            R.id.back_bracelet-> backBracelet()
            R.id.open_back-> {
                open_back.setEnabled(false)
                BraceletMachineManager.sysStartPush(object :BraceletMachineSystemListener{
                    override fun onFail() {
                        btnEnable();
                    }

                    override fun onSuccess() {
                        btnEnable();
                    }

                });
            }
            R.id.close_back-> {
                close_back.setEnabled(false)
                BraceletMachineManager.sysStopPush(object :BraceletMachineSystemListener{
                    override fun onFail() {
                        btnEnable();
                    }
                    override fun onSuccess() {
                        btnEnable();
                    }

                })
            }
        }
    }
    fun checkSelf(){
        //IC手环
        loader.show();
        checkSelf(BraceletMachineManager.CardType.ID,object :CheckSelfCallback{
            override fun onCheckSelfFail(msg: String) {
                super.onCheckSelfFail(msg)
                check_self.setEnabled(true)
                back_bracelet.setEnabled(false)
                fetch_bracelet.setEnabled(false)
                showToast("自检失败$msg");
            }

            override fun onCheckSelfSuccess() {
                super.onCheckSelfSuccess()
                check_self.setEnabled(false)
                back_bracelet.setEnabled(true)
                fetch_bracelet.setEnabled(true)
                btnEnable();
                showToast("自检成功");
            }

            override fun onCompleted() {
                super.onCompleted()
                loader.dismiss();
            }
        });
    }

    internal fun fetchBracelet() {
        BraceletMachineManager.fetchBracelet(object:FetchCallback{
            override fun onCompleted() {
                super.onCompleted()
            }

            override fun onFetchFail(msg: String) {
                super.onFetchFail(msg)
                showToast("取手环失败 :$msg")
            }

            override fun onFetchSuccess(no: String) {
                super.onFetchSuccess(no)
                showToast("取手环成功 手环号:$no")
            }

        });
    }
    internal fun showToast(msg: String) {
        Log.i("smart",msg);
        runOnUiThread { Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show() }
    }
    internal fun backBracelet() {
        showToast("请将手环放置回收口")
        BraceletMachineManager.giveBackBracelet(object:GiveBackCallback{
            override fun checkAllowGiveBack(card: CardDataModel, callback: CheckGiveBackCallback) {
                super.checkAllowGiveBack(card, callback)
                showToast("获取到卡号: ${card.CardNo} 柜号:${card.CabinetNos}")
                //允许归还
                callback.allow( true,"")
            }

            override fun onCompleted() {
                super.onCompleted()
            }

            override fun onGiveBackBusy() {
                super.onGiveBackBusy()
                showToast("设备忙碌中，请稍等")
            }

            override fun onGiveBackFail(msg: String) {
                super.onGiveBackFail(msg)
                showToast("归还手环失败")
            }

            override fun onGiveBackSuccess(no: String) {
                super.onGiveBackSuccess(no)
                showToast("归还手环成功 手环号:$no")
            }

        });
    }
}

