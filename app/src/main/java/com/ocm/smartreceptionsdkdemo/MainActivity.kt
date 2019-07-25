package com.ocm.smartreceptionsdkdemo

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.ocm.smart_reception_sdk.BraceletMachineListener
import com.ocm.smart_reception_sdk.BraceletMachineManager
import com.ocm.smart_reception_sdk.BraceletMachineSystemListener
import com.ocm.smart_reception_sdk.CheckGiveBackCallback
import com.ocm.smart_reception_sdk.Machine.CardDataModel
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : Activity(), View.OnClickListener {
    internal var TAG = "SMARTRECEPTINOSDKDEMO"
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
        BraceletMachineManager.init(this)
        BraceletMachineManager.setSystemListener(object:BraceletMachineSystemListener{
            override fun onFail() {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                open_back.setEnabled(true)
                close_back.setEnabled(true)
            }

            override fun onSuccess() {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                open_back.setEnabled(true)
                close_back.setEnabled(true)
            }

        })
        BraceletMachineManager.setBraceletMachineListener(object : BraceletMachineListener{
            override fun checkAllowGiveBack(card: CardDataModel, callback: CheckGiveBackCallback) {
                showToast("获取到卡号:" + card.CardNo + "柜号:" + card.CabinetNos)
                //允许归还
                callback.allow( true,"")
                Log.i(TAG, "checkAllowGiveBack")
//                如果不允许归还
//                callback.allow( false,"手环异常");
            }

            override fun onCheckSelfBack(isSuccess: Boolean, msg: String) {
                check_self.setEnabled(false)
                back_bracelet.setEnabled(true)
                fetch_bracelet.setEnabled(true)
                showToast("自检完成: 结果" + if (isSuccess) "成功" else "失败")
            }

            override fun onCurrentNumChange(num: Int) {
                Log.i(TAG, "onCurrentNumChange:$num")
                if (num == 0) {
                    BraceletMachineManager.stopGiveBack()
                    showToast("无手环 暂停设备")
                } else
                    showToast("手环数量变化$num")
            }

            override fun onDisconnect() {
                Log.i(TAG, "onDisconnect")
                showToast("设备连接已断开")
            }

            override fun onFetchFail(msg: String) {
                Log.i(TAG, "onFetchSuccess:$msg")
                showToast("取手环成功 手环号:$msg")
            }

            override fun onFetchSuccess(no: String) {
                Log.i(TAG, "onFetchFail:$no")
                showToast("取手环失败")
            }

            override fun onGiveBackBusy() {
                Log.i(TAG, "onGiveBackBusy")
                showToast("设备忙碌中，请稍等")
            }

            override fun onGiveBackFail(msg: String) {
                Log.i(TAG, "onGiveBackFail:$msg")
                showToast("归还手环失败")
            }

            override fun onGiveBackSuccess(no: String) {
                Log.i(TAG, "onGiveBackSuccess:$no")
                showToast("归还手环成功 手环号:$no")
            }

            override fun onStateChange(isStop: Boolean) {
                Log.i(TAG, "onStateChange:$isStop")
                if(isStop)
                    showToast("设备暂停");
            }

        })
    }

    override fun onClick(p0: View?) {
        when(p0?.id){
            R.id.check_self ->BraceletMachineManager.checkSelf()
            R.id.fetch_bracelet-> fetchBracelet()
            R.id.back_bracelet-> backBracelet()
            R.id.open_back-> {
                BraceletMachineManager.sysStartPush()
                open_back.setEnabled(false)
            }
            R.id.close_back-> {
                BraceletMachineManager.sysStopPush()
                close_back.setEnabled(false)
            }
        }
    }

    internal fun fetchBracelet() {
        if (BraceletMachineManager.checkHasBracelet()) {
            BraceletMachineManager.fetchBracelet()
        } else {
            showToast("取手环失败 暂无手环")
        }
    }
    internal fun showToast(msg: String) {
        runOnUiThread { Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show() }
    }
    internal fun backBracelet() {
        if (!BraceletMachineManager.checkIsFull()) {
            BraceletMachineManager.giveBackBracelet()
        } else {
            showToast("归还手环失败 手环已满")
        }
    }
}

