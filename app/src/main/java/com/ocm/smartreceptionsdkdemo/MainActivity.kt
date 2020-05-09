package com.ocm.smartreceptionsdkdemo

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.CompoundButton
import android.widget.Toast
import com.ocm.bracelet_machine_sdk.*
import com.ocm.bracelet_machine_sdk.BraceletMachineManager.SectorType
import com.ocm.bracelet_machine_sdk.Machine.CardDataModel
import com.zyao89.view.zloading.ZLoadingDialog
import com.zyao89.view.zloading.Z_TYPE
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : Activity(), View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    internal var TAG = "SMARTRECEPTINOSDKDEMO"
    internal var APPKEY ="填写您的APPKEY";
    internal var APPSECRET ="填写您的APPSECRET";

    lateinit var loader:ZLoadingDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initView();
    }

    private fun initView(){
        check_self.setOnClickListener(this)
        fetch_bracelet.setOnClickListener(this)
        fetch_mul_bracelet.setOnClickListener(this)
        back_bracelet.setOnClickListener(this)
        open_back.setOnClickListener(this)
        close_back.setOnClickListener(this)
        switch_qrcode.setOnCheckedChangeListener(this)
        switch_led_light.setOnCheckedChangeListener(this)
        switch_ir_light.setOnCheckedChangeListener(this)
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        loader = ZLoadingDialog(this)
                .setCancelable(false)
                .setCanceledOnTouchOutside(false)
                .setLoadingBuilder(Z_TYPE.CIRCLE)

        mac.setText(MacHelper.getLocalMac(this));
    }

    fun bind(isOnline: Boolean) {
        BraceletMachineManager.isDebug = false
        if (isOnline) BraceletMachineManager.bind(this, APPKEY, APPSECRET) else BraceletMachineManager.bind(this)
        setMode(isOnline)
        BraceletMachineManager.setBraceletMachineListener(object : BraceletMachineListener {
            override fun onDisconnect() {
                Log.i(TAG, "onDisconnect")
                showToast("设备连接已断开")
            }

            override fun onCurrentNumChange(i: Int) {
                Log.i(TAG, "onCurrentNumChange:$i")
                if (i == 0) {
                    BraceletMachineManager.stopGiveBack()
                    showToast("无手环 暂停设备")
                } else showToast("手环数量变化$i")
            }

            override fun onStateChange(b: Boolean) {
                Log.i(TAG, "onStateChange:$b")
                showToast("设备已暂停")
            }
        })
    }

    fun setMode(isOnline: Boolean) {
        BraceletMachineManager.setBraceletMode(if (isOnline) BraceletMachineManager.BraceletMode.ONLINE else BraceletMachineManager.BraceletMode.OFFLINE,
                object : DefaultCallback {
                    override fun onSuccess(s: String) {
                        showToast("切换模式成功:$s")
                    }

                    override fun onFail(s: String) {
                        showToast("切换模式失败:$s")
                    }

                    override fun onCompleted() {
                        checkSelf()
                    }
                })
    }


    fun checkSelf() {
        loader.show()
        //如需离线，请在自检前切换模式
        showToast("开始自检")
        //IC手环
        BraceletMachineManager.checkSelf(BraceletMachineManager.CardType.IC, object : CheckSelfCallback {
            override fun onCompleted() {
                showToast("自检完成")
                loader.dismiss()
            }

            override fun onCheckSelfSuccess() {
                showToast("自检成功")
                setBtnEnable(true)
                loader.dismiss()
            }

            override fun onCheckSelfFail(s: String) {
                setBtnEnable(false)
                showToast("自检失败:$s")
                loader.dismiss()
            }
        })
    }
    
    fun setBtnEnable(enable: Boolean) {
        check_self.setEnabled(!enable)
        back_bracelet.setEnabled(enable)
        fetch_bracelet.setEnabled(enable)
        fetch_mul_bracelet.setEnabled(enable)
        open_back.setEnabled(enable)
        close_back.setEnabled(enable)
    }

    fun btnEnable() {
        open_back.setEnabled(true)
        close_back.setEnabled(true)
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        when(buttonView?.id){
            R.id.switch_qrcode->{
                showToast("二维码$isChecked")
                if (isChecked) BraceletMachineManager.openQR()
                else BraceletMachineManager.closeQR()
            }
            R.id.switch_led_light->{
                showToast("LED灯$isChecked")
                if (isChecked)BraceletMachineManager.openLedLight()
                else BraceletMachineManager.closeLedLight()
            }
            R.id.switch_ir_light->{
                showToast("红外灯$isChecked")
                if (isChecked)BraceletMachineManager.openIRLight()
                else BraceletMachineManager.closeIRLight()
            }
        }
    }
    override fun onClick(p0: View?) {
        when(p0?.id){
            R.id.check_self ->bind(switch_mode.isChecked())
            R.id.fetch_bracelet-> fetchBracelet()
            R.id.back_bracelet-> backBracelet()
            R.id.fetch_mul_bracelet-> fetchMulBracelet()
            R.id.open_back-> {
                open_back.setEnabled(false)
                BraceletMachineManager.sysStartPush(object : BraceletMachineSystemListener {
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

    internal fun fetchBracelet() {
        BraceletMachineManager.fetchBracelet(object: FetchCallback {
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

    fun fetchMulBracelet() {
        val braceletCount = 3
        val pwd = "FFFFFFFFFFFF"
        val blockData = "1234567890ABCEDF"
        BraceletMachineManager.fetchMultiBracelet(braceletCount, SectorType.SECTOR2, pwd, blockData, object : FetchCallback {
            override fun onFetchSuccess(s: String) {
                showToast("取手环成功 手环号:$s")
            }

            override fun onFetchFail(s: String) {
                showToast("取手环失败:$s")
            }

            override fun onRemainingFetch(i: Int) {
                showToast("取手环" + braceletCount + "个,其中有" + i + "个未发出")
            }

            override fun onCompleted() {}
        })
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

