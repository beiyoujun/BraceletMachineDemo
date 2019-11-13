# 1.绑定设备

在**控制台**-》**应用列表 **中 创建应用后可获取到

```java
bmManager.bind(this,"APPKEY","SERCRT");
```



# 2.自检

程序每次启动后都**必须进行自检**，并设置设备所使用手环的类型\(**ID**,**IC**两种\)，如果设置的类型与手环类型不匹配将影响取手环和归还手环，自检成功后才可以进行其他操作。

 

```java
bmManager.checkSelf(BraceletMachineManager.CardType.ID,new CheckSelfCallback(){
            @Override
            public void onCompleted() {
                
            }

            @Override
            public void onCheckSelfSuccess() {
                //自检成功 可进行后续操作
            }

            @Override
            public void onCheckSelfFail(@NotNull final  String s) {
               //自检失败
            }
        });
```



# 3.取手环 

```java
bmManager.fetchBracelet(new FetchCallback() {
            @Override
            public void onFetchSuccess(@NotNull String card_no) {
                //取手环成功 手环号card_no
            }

            @Override
            public void onFetchFail(@NotNull String s) {
            	//取手环失败
            }

            @Override
            public void onCompleted() {

            }
        });
```



# 4.归还手环 

调用归还手环后，将手环放置机器上面归还口，

```java
bmManager.giveBackBracelet(new GiveBackCallback() {
            @Override
            public void checkAllowGiveBack(@NotNull CardDataModel cardDataModel, @NotNull CheckGiveBackCallback checkGiveBackCallback) {
                //获取到卡号cardDataModel.CardNo
                //允许设备归还checkGiveBackCallback.allow(true,"OK");
                
                //不允许设备归还checkGiveBackCallback.allow(false,"OK");
            }

            @Override
            public void onGiveBackBusy() {
               //设备忙碌中，请稍等
            }

            @Override
            public void onGiveBackSuccess(@NotNull String card_no) {
                //归还手环成功 手环号card_no
            }

            @Override
            public void onGiveBackFail(@NotNull String s) {
                //归还手环失败
            }

            @Override
            public void onCompleted() {

            }
        });
```



# 5.打开/关闭归还口 

在不放入手环的时候，也可以主动打开归还口

```java
bmManager.sysStartPush(new BraceletMachineSystemListener() {
    @Override
    public void onSuccess() {
        //成功后 手环归还口打开
    }

    @Override
    public void onFail() {
    }
});

bmManager.sysStopPush(new BraceletMachineSystemListener() {
    @Override
    public void onSuccess() {
        //成功后 手环归还口关闭
    }

    @Override
    public void onFail() {
    }
});
```



# 6.打开\/关闭二维码 

打开二维码模块后，即可对二维码进行扫描，结果将以**模拟键盘输入**的方式返回。

```
bmManager.openQR();//打开二维码模块
bmManager.closeQR();//关闭二维码模块

```

