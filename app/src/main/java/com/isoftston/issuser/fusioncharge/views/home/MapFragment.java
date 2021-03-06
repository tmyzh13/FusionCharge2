package com.isoftston.issuser.fusioncharge.views.home;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.route.RouteSearch;
import com.corelibs.base.BaseFragment;
import com.corelibs.base.BasePresenter;
import com.corelibs.common.AppManager;
import com.corelibs.subscriber.RxBusSubscriber;
import com.corelibs.utils.PreferencesHelper;
import com.corelibs.utils.ToastMgr;
import com.corelibs.utils.rxbus.RxBus;
import com.google.zxing.integration.android.IntentIntegrator;
import com.isoftston.issuser.fusioncharge.MainActivity;
import com.isoftston.issuser.fusioncharge.R;
import com.isoftston.issuser.fusioncharge.constants.Constant;
import com.isoftston.issuser.fusioncharge.model.UserHelper;
import com.isoftston.issuser.fusioncharge.model.beans.ChargeFeeBean;
import com.isoftston.issuser.fusioncharge.model.beans.HomeAppointmentBean;
import com.isoftston.issuser.fusioncharge.model.beans.HomeChargeOrderBean;
import com.isoftston.issuser.fusioncharge.model.beans.HomeOrderBean;
import com.isoftston.issuser.fusioncharge.model.beans.HomeRefreshBean;
import com.isoftston.issuser.fusioncharge.model.beans.MapDataBean;
import com.isoftston.issuser.fusioncharge.model.beans.MapInfoBean;
import com.isoftston.issuser.fusioncharge.model.beans.MyLocationBean;
import com.isoftston.issuser.fusioncharge.model.beans.PileFeeBean;
import com.isoftston.issuser.fusioncharge.model.beans.UserBean;
import com.isoftston.issuser.fusioncharge.presenter.MapPresenter;
import com.isoftston.issuser.fusioncharge.utils.ActionControl;
import com.isoftston.issuser.fusioncharge.utils.ChoiceManager;
import com.isoftston.issuser.fusioncharge.utils.TimeServiceManager;
import com.isoftston.issuser.fusioncharge.utils.Tools;
import com.isoftston.issuser.fusioncharge.views.ChagerStatueActivity;
import com.isoftston.issuser.fusioncharge.views.ChargeCaptureActivity;
import com.isoftston.issuser.fusioncharge.views.ChargeDetailsActivity;
import com.isoftston.issuser.fusioncharge.views.GuildActivity;
import com.isoftston.issuser.fusioncharge.views.LoginActivity;
import com.isoftston.issuser.fusioncharge.views.ParkActivity;
import com.isoftston.issuser.fusioncharge.views.PayActivity;
import com.isoftston.issuser.fusioncharge.views.TimerService;
import com.isoftston.issuser.fusioncharge.views.interfaces.MapHomeView;
import com.isoftston.issuser.fusioncharge.weights.AppointmentTimeOutDialog;
import com.isoftston.issuser.fusioncharge.weights.ChargeFeeDialog;
import com.isoftston.issuser.fusioncharge.weights.CommonDialog;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * Created by issuser on 2018/4/19.
 */

public class MapFragment extends BaseFragment<MapHomeView, MapPresenter> implements MapHomeView, LocationSource, AMapLocationListener, AMap.OnMapTouchListener {

    @Bind(R.id.map)
    MapView map;
    @Bind(R.id.rl_not_pay)
    RelativeLayout rl_not_pay;
    @Bind(R.id.rl_bottom_detail)
    RelativeLayout rl_bottom_detail;
    @Bind(R.id.ll_fee_all)
    LinearLayout ll_fee_all;
    @Bind(R.id.tv_map_info_name)
    TextView tv_map_info_name;
    @Bind(R.id.tv_map_info_score)
    TextView tv_map_info_score;
    @Bind(R.id.tv_map_info_address)
    TextView tv_map_info_address;
    @Bind(R.id.tv_map_info_distance)
    TextView tv_map_info_distance;
    @Bind(R.id.tv_map_info_pile_num)
    TextView tv_map_info_pile_num;
    @Bind(R.id.tv_map_info_gun_num)
    TextView tv_map_info_gun_num;
    @Bind(R.id.tv_map_info_free_num)
    TextView tv_map_info_free_num;
    @Bind(R.id.tv_map_info_current_fee)
    TextView tv_map_info_current_fee;
    @Bind(R.id.tv_map_info_service_fee)
    TextView tv_map_info_service_fee;
    @Bind(R.id.ll_appointment)
    LinearLayout ll_appontment;
    @Bind(R.id.rl_charger_order)
    RelativeLayout rl_charger_order;


    private AMap aMap;
    private AppointmentTimeOutDialog appointmentTimeOutDialog;
    private Timer timerAppointment;


    @Override
    protected int getLayoutId() {
        return R.layout.fragment_main;
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        map.onCreate(savedInstanceState);
        aMap = map.getMap();

        aMap.setOnMapClickListener(new AMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                rl_bottom_detail.setVisibility(View.GONE);
            }
        });
        timerAppointment = new Timer();
        Intent intent = new Intent(getContext(), TimerService.class);
        getContext().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        location();
        initMapData();

        ll_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("yzh", "---" + (ll_hint.getVisibility() == View.VISIBLE));
                if (ll_hint.getVisibility() == View.VISIBLE) {
                    ll_hint.setVisibility(View.GONE);
                    tv_appointment_address.setText(getString(R.string.home_appointment_hint));
                } else {
                    ll_hint.setVisibility(View.VISIBLE);
                    if (homeAppointmentBean != null) {
                        tv_appointment_address.setText(homeAppointmentBean.chargingAddress);
                        Log.e("yzh", "-----");
                    }

                }
//                CommonDialog dialog=new CommonDialog(getContext(),"","222222222",2);
//                dialog.show();
            }
        });
        appointmentTimeOutDialog = new AppointmentTimeOutDialog(getContext());

        RxBus.getDefault().toObservable(HomeRefreshBean.class, Constant.HOME_STATUE_REFRESH)
                .compose(this.<HomeRefreshBean>bindToLifecycle())
                .subscribe(new RxBusSubscriber<HomeRefreshBean>() {

                    @Override
                    public void receive(HomeRefreshBean data) {

                        //支付成功了 屏蔽未支付提示
                        if (data.type == 0) {
                            rl_not_pay.setVisibility(View.GONE);
                            ActionControl.getInstance(getViewContext()).setHasNoPayOrder(false, null);
                        } else if (data.type == 1) {
                            //预约结束
                            ll_appontment.setVisibility(View.GONE);
                            ActionControl.getInstance(getViewContext()).setHasAppointment(false, null);
                        }

                    }
                });
        RxBus.getDefault().toObservable(Object.class, Constant.APPOINTMENT_TIME_OUT)
                .compose(this.bindToLifecycle())
                .subscribe(new RxBusSubscriber<Object>() {
                    @Override
                    public void receive(Object data) {
                        if (AppManager.getAppManager().currentActivity().getClass().equals(MainActivity.class)) {
                            appointmentTimeOutDialog.show();
                            appointmentTimeOutDialog.setIvDeleteListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    appointmentTimeOutDialog.dismiss();
                                }
                            });
                            appointmentTimeOutDialog.setReAppointment(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    appointmentTimeOutDialog.dismiss();
                                }
                            });
                        }
                    }
                });
        RxBus.getDefault().toObservable(Object.class, Constant.REFRESH_MAP_OR_LIST_DATA)
                .compose(this.<Object>bindToLifecycle())
                .subscribe(new RxBusSubscriber<Object>() {

                    @Override
                    public void receive(Object data) {
                        presenter.getData();
                    }
                });
        RxBus.getDefault().toObservable(Object.class, Constant.REFRESH_HOME_STATUE)
                .compose(this.bindToLifecycle())
                .subscribe(new RxBusSubscriber<Object>() {

                    @Override
                    public void receive(Object data) {
                        if (UserHelper.getSavedUser() != null && !Tools.isNull(UserHelper.getSavedUser().token)) {
                            getHomeStatue();
                        }
                    }
                });

        RxBus.getDefault().toObservable(Object.class, Constant.REFRESH_APPOINTMENT_TIME)
                .compose(this.bindToLifecycle())
                .subscribe(new RxBusSubscriber<Object>() {
                    @Override
                    public void receive(Object data) {
                        Log.e("yzh", "receive--" + Tools.formatMinute(Long.parseLong(PreferencesHelper.getData(Constant.TIME_APPOINTMENT))));
                        tv_appointment_time.setText(Tools.formatMinute(Long.parseLong(PreferencesHelper.getData(Constant.TIME_APPOINTMENT))));
                    }
                });
        if (UserHelper.getSavedUser() != null && !Tools.isNull(UserHelper.getSavedUser().token)) {
            getHomeStatue();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

//            // 检查该权限是否已经获取
//            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.READ_PHONE_STATE}, 10);
//            // 权限是否已经 授权 GRANTED---授权  DINIED---拒绝
////            if (i != PackageManager.PERMISSION_GRANTED) {
////                // 如果没有授予该权限，就去提示用户请求
//////                showDialogTipUserRequestPermission();
////            }
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_PHONE_STATE)
                    != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getContext(), "没有权限,请手动开启定位权限", Toast.LENGTH_SHORT).show();
                // 申请一个（或多个）权限，并提供用于回调返回的获取码（用户定义）
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_PHONE_STATE}, 10);

            }

        }
        timerAppointment.schedule(new TimerTask() {
            @Override
            public void run() {
                if (homeAppointmentBean != null) {
                    if (Tools.isNull(PreferencesHelper.getData(Constant.TIME_APPOINTMENT))) {
                        appointmentTime = 0;
                    } else {
                        appointmentTime = Long.parseLong(PreferencesHelper.getData(Constant.TIME_APPOINTMENT));
                    }
                    appointmentTime -= 1000;
                    Log.e("yzh", "sssss----" + appointmentTime);
                    PreferencesHelper.saveData(Constant.TIME_APPOINTMENT, appointmentTime + "");
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tv_appointment_time.setText(Tools.formatMinute(Long.parseLong(PreferencesHelper.getData(Constant.TIME_APPOINTMENT))));
                        }
                    });
                    if (appointmentTime <= 0) {
                        Log.e("yzh", "cancel");
                        //预约超时
                        if (AppManager.getAppManager().currentActivity().getClass().equals(MainActivity.class)) {
                            appointmentTimeOutDialog.show();
                            appointmentTimeOutDialog.setIvDeleteListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    appointmentTimeOutDialog.dismiss();
                                }
                            });
                            appointmentTimeOutDialog.setReAppointment(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    appointmentTimeOutDialog.dismiss();
                                }
                            });
                        }
//                        cancelTimerAppointment();
                    }
                }
            }
        }, 1000, 1000);
    }

    private long appointmentTime;

    //获取未支付 充电  预约情况
    private void getHomeStatue() {
        presenter.getUserOrderStatue();
        presenter.getUserChargeStatue();
        presenter.getUserAppointment();
    }

    private void initMapData() {
        presenter.getData();
    }


    OnLocationChangedListener mListener;
    AMapLocationClient mlocationClient;
    AMapLocationClientOption mLocationOption;

    private MapDataBean currentMapDataBean;

    private void location() {
        // 设置定位监听
        aMap.setLocationSource(this);
        // 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        aMap.setMyLocationEnabled(true);
        // 设置定位的类型为定位模式，有定位、跟随或地图根据面向方向旋转几种
        aMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);
        aMap.moveCamera(CameraUpdateFactory.zoomTo(14));
        MyLocationStyle myLocationStyle = new MyLocationStyle();
        ImageView imageView = new ImageView(getContext());
        imageView.setImageResource(R.mipmap.location);
        BitmapDescriptor markerIcon = BitmapDescriptorFactory

                .fromView(imageView);
        myLocationStyle.myLocationIcon(markerIcon);
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER);//连续定位、蓝点不会移动到地图中心点，地图依照设备方向旋转，并且蓝点会跟随设备移动。
        aMap.setMyLocationStyle(myLocationStyle);
        //移动时 不让地图到定位点
        aMap.setOnMapTouchListener(this);
        // 定义 Marker 点击事件监听
        AMap.OnMarkerClickListener markerClickListener = new AMap.OnMarkerClickListener() {
            // marker 对象被点击时回调的接口
            // 返回 true 则表示接口已响应事件，否则返回false
            @Override
            public boolean onMarkerClick(Marker marker) {
                MapDataBean bean = list.get(Integer.parseInt(marker.getTitle()));
                currentMapDataBean = bean;
                presenter.getInfo(bean.id, bean.type);
                return true;
            }
        };
        // 绑定 Marker 被点击事件
        aMap.setOnMarkerClickListener(markerClickListener);
    }

    @Override
    protected MapPresenter createPresenter() {
        return new MapPresenter();
    }

    @OnClick(R.id.ll_fee)
    public void showFee() {
        ChargeFeeDialog dialog = new ChargeFeeDialog(getContext());
        dialog.show();
//        List<ChargeFeeBean> list=new ArrayList<>();
//        ChargeFeeBean bean=new ChargeFeeBean();
//        bean.time="00:00~06:00";
//        bean.unit="0.0030度";
//        list.add(bean);
//        list.add(bean);
//        list.add(bean);
//        list.add(bean);
        dialog.setFeeDatas(list_fee);

    }

    //Android6.0申请权限的回调方法
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.e("yzh", "1111232312312===" + requestCode);
        switch (requestCode) {

            // requestCode即所声明的权限获取码，在checkSelfPermission时传入
            case 10:
                Log.e("yzh", "1111111111111");
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 获取到权限，作相应处理（调用定位SDK应当确保相关权限均被授权，否则可能引起定位失败）
                    Log.e("yzh", "eeeeeeeeeeeeeee");
                    presenter.getData();
                } else {
                    // 没有获取到权限，做特殊处理
                    Toast.makeText(getContext(), "获取位置权限失败，请手动开启", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }


    @OnClick(R.id.tv_pay)
    public void goPay() {
        startActivity(PayActivity.getLauncher(getContext(), homeOrderBean.orderRecordNum));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != map) {
            map.onDestroy();
        }

        if (null != mlocationClient) {
            mlocationClient.onDestroy();
        }
        if (null != timerAppointment) {
            timerAppointment = null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        map.onResume();
        Log.e("yzh", "onResume");
//        if (Build.VERSION.SDK_INT >= 23
//                && getContext().getApplicationInfo().targetSdkVersion >= 23) {
//            if (isNeedCheck) {
//                Log.e("yzh","checkPerminssions");
//                checkPermissions(needPermissions);
//            }
//        }


    }


    @Override
    public void onPause() {
        super.onPause();
        map.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        map.onSaveInstanceState(outState);
    }

    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        mListener = onLocationChangedListener;
        if (mlocationClient == null) {
            //初始化定位
            mlocationClient = new AMapLocationClient(getContext());
            //初始化定位参数
            mLocationOption = new AMapLocationClientOption();
            //设置定位回调监听
            mlocationClient.setLocationListener(this);
            mLocationOption.setInterval(10000);//设置间隔时间
            //设置为高精度定位模式
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            //设置定位参数
            mlocationClient.setLocationOption(mLocationOption);
            // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
            // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
            // 在定位结束后，在合适的生命周期调用onDestroy()方法
            // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
            mlocationClient.startLocation();//启动定位
        }
    }

    @Override
    public void deactivate() {
        mListener = null;
        if (mlocationClient != null) {
            mlocationClient.stopLocation();
            mlocationClient.onDestroy();
        }
        mlocationClient = null;
    }

    private boolean followMove = true;

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (mListener != null && aMapLocation != null) {
            if (aMapLocation != null
                    && aMapLocation.getErrorCode() == 0) {
                mListener.onLocationChanged(aMapLocation);// 显示系统小蓝点
                MyLocationBean bean = new MyLocationBean();
                bean.latitude = aMapLocation.getLatitude();
                bean.longtitude = aMapLocation.getLongitude();
                PreferencesHelper.saveData(bean);
                RxBus.getDefault().send(bean, Constant.REFRESH_LOCATION);
                if (followMove) {
                    aMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(aMapLocation.getLatitude(), aMapLocation.getLongitude())));
                }
                if (list == null || list.size() == 0) {
                    presenter.getData();
                }
            } else {
                String errText = "定位失败," + aMapLocation.getErrorCode() + ": " + aMapLocation.getErrorInfo();
                Log.e("AmapErr", errText);
            }
        }
    }

    protected String[] needPermissions = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE
    };
    private static final int PERMISSON_REQUESTCODE = 0;
    private boolean isNeedCheck = true;

    @Override
    public void onTouch(MotionEvent motionEvent) {
        if (followMove) {
            //用户拖动地图后，不再跟随移动，直到用户点击定位按钮
            followMove = false;
        }
    }

    @OnClick(R.id.ll_go_to_detail)
    public void goDetail() {
        //进入对应的充电桩详情

    }

    private List<MapDataBean> list;

    @Override
    public void renderMapData(List<MapDataBean> list) {
        this.list = list;
        MyLocationBean bean = PreferencesHelper.getData(MyLocationBean.class);
        for (int i = 0; i < list.size(); i++) {
            //距离筛选
            if (bean != null) {
                if (Tools.GetDistance(list.get(i).latitude, list.get(i).longitude, bean.latitude, bean.longtitude) > ChoiceManager.getInstance().getDistance()) {
                    continue;
                }
            }
            MarkerOptions markerOption = new MarkerOptions();
            LatLng latLng = new LatLng(list.get(i).latitude, list.get(i).longitude);
            markerOption.position(latLng);
            //点标记的标题和内容；
            markerOption.title(i + "");
            markerOption.draggable(true);//设置Marker可拖动
            markerOption.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                    .decodeResource(getResources(), R.mipmap.home_ic_position)));
            // 将Marker设置为贴地显示，可以双指下拉地图查看效果
            markerOption.setFlat(true);//设置marker平贴地图效果
            aMap.addMarker(markerOption);
        }
    }

    @Override
    public void getMarkInfo(long id, MapInfoBean mapInfoBean) {
        //获取费率数据
        if (mapInfoBean.objType.equals("pile")) {
            presenter.getFeeInfo(id);
        }
        rl_bottom_detail.setVisibility(View.VISIBLE);
        if (mapInfoBean.objType.equals("station")) {
            //充电站
            ll_fee_all.setVisibility(View.GONE);
        } else if (mapInfoBean.objType.equals("pile")) {
            //充电桩
            ll_fee_all.setVisibility(View.VISIBLE);
        }
        tv_map_info_name.setText(mapInfoBean.name);
        tv_map_info_address.setText(mapInfoBean.address);
        if (mapInfoBean.averageScore.equals("-1")) {
            tv_map_info_score.setText("");
        } else {
            tv_map_info_score.setText(mapInfoBean.averageScore + getString(R.string.score));
        }
        //要单算距离
        MyLocationBean bean = PreferencesHelper.getData(MyLocationBean.class);
        tv_map_info_distance.setText(Tools.GetDistance(currentMapDataBean.latitude, currentMapDataBean.longitude, bean.latitude, bean.longtitude) + "KM");
        tv_map_info_pile_num.setText(mapInfoBean.pileNum + "");
        tv_map_info_gun_num.setText(mapInfoBean.gunNum + "");
        tv_map_info_free_num.setText(mapInfoBean.freeNum + "");
    }

    private List<ChargeFeeBean> list_fee;

    @Override
    public void showPileFeeInfo(PileFeeBean bean) {
        list_fee = bean.feeList;
        tv_map_info_service_fee.setText(bean.serviceFee + getString(R.string.yuan_du));
        if (list_fee != null && list_fee.size() != 0) {
            double min = list_fee.get(0).multiFee;
            double max = list_fee.get(0).multiFee;
            for (int i = 1; i < list_fee.size(); i++) {
                if (list_fee.get(i).multiFee > max) {
                    max = list_fee.get(i).multiFee;
                }
                if (list_fee.get(i).multiFee < min) {
                    min = list_fee.get(i).multiFee;
                }
            }
            tv_map_info_current_fee.setText(min + getString(R.string.yuan_du) + "~" + max + getString(R.string.yuan_du));
        }

    }

    private HomeOrderBean homeOrderBean;

    @Override
    public void hasNoPayOrder(boolean has, HomeOrderBean bean) {
        ActionControl.getInstance(getContext()).setHasNoPayOrder(has, bean);
        if (has) {
            rl_not_pay.setVisibility(View.VISIBLE);
            homeOrderBean = bean;

        } else {
            rl_not_pay.setVisibility(View.GONE);
        }

    }


    @Bind(R.id.tv_appointment_address)
    TextView tv_appointment_address;
    @Bind(R.id.tv_appointment_time)
    TextView tv_appointment_time;
    @Bind(R.id.iv_hint)
    ImageView iv_hint;
    @Bind(R.id.ll_hint)
    LinearLayout ll_hint;
    @Bind(R.id.tv_pile_num)
    TextView tv_pile_num;
    @Bind(R.id.tv_pile_name)
    TextView tv_pile_name;
    @Bind(R.id.tv_gun_num)
    TextView tv_gun_num;
    @Bind(R.id.ll_time)
    LinearLayout ll_time;

    private HomeAppointmentBean homeAppointmentBean;

    @Override
    public void renderAppoinmentInfo(boolean has, HomeAppointmentBean bean) {

        ActionControl.getInstance(getContext()).setHasAppointment(has, bean);
        homeAppointmentBean = bean;

        if (has) {
            tv_pile_num.setText(bean.runCode);
            tv_pile_name.setText(bean.chargingPileName);
            tv_gun_num.setText(bean.gunCode);
            tv_appointment_address.setText(getString(R.string.home_appointment_hint));

            if (bean.reserveEndTime <= bean.nowTime) {
                ll_appontment.setVisibility(View.VISIBLE);
            } else {
                ll_appontment.setVisibility(View.VISIBLE);
                long surplusTime = bean.reserveEndTime - bean.nowTime;
                PreferencesHelper.saveData(Constant.TIME_APPOINTMENT, surplusTime + "");
                PreferencesHelper.saveData(Constant.APPOINTMENT_DURING, bean.reserveDuration);
                tv_appointment_time.setText(Tools.formatMinute(surplusTime));

            }
            ll_appontment.setVisibility(View.VISIBLE);
//            TimeServiceManager.getInstance().getTimerService().timeAppointment();
        } else {
            ll_appontment.setVisibility(View.GONE);
        }


    }


    private HomeChargeOrderBean homeChargeOrderBean;

    @Override
    public void renderHomeChargerOrder(boolean has, HomeChargeOrderBean bean) {
        ActionControl.getInstance(getContext()).setHasCharging(has, bean);
        if (has) {
            rl_charger_order.setVisibility(View.VISIBLE);
            homeChargeOrderBean = bean;
            TimeServiceManager.getInstance().getTimerService().timerHour();
        } else {
            rl_charger_order.setVisibility(View.GONE);
        }

    }

    //    private TimerService timerService;
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            TimerService.ServiceBinder binder = (TimerService.ServiceBinder) service;
            TimeServiceManager.getInstance().setTimerService(binder.getService());
//            TimeServiceManager.getInstance().getTimerService().timerHour();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            TimeServiceManager.getInstance().getTimerService().cancelTimerHour();
            TimeServiceManager.getInstance().setTimerService(null);
        }
    };

    @OnClick(R.id.iv_appointment_guaild)
    public void goAppointmentGuaild() {
        startActivity(GuildActivity.getLauncher(getContext(), homeAppointmentBean.latitude, homeAppointmentBean.longitude, homeAppointmentBean, false));
    }

    @OnClick(R.id.iv_guaild)
    public void goGuaild() {
        if (currentMapDataBean != null) {
            boolean choiceNotAppointment = false;
            if (homeAppointmentBean != null) {
                if (homeAppointmentBean.latitude != currentMapDataBean.latitude || homeAppointmentBean.longitude != currentMapDataBean.longitude) {
                    choiceNotAppointment = true;
                } else {
                    choiceNotAppointment = false;
                }
            } else {
                choiceNotAppointment = false;
            }
            startActivity(GuildActivity.getLauncher(getContext(), currentMapDataBean.latitude, currentMapDataBean.longitude, null, choiceNotAppointment));
//            startActivity(ParkActivity.getLauncher(getContext()));
        }

    }

    @OnClick(R.id.enter_charge_station_iv)
    public void enterChargeStation() {
        //获取详情要token 所以判断
        if (UserHelper.getSavedUser() == null || Tools.isNull(UserHelper.getSavedUser().token)) {
            startActivity(LoginActivity.getLauncher(getContext()));
        } else {
            startActivity(ChargeDetailsActivity.getLauncher(getActivity(), currentMapDataBean.id + "", currentMapDataBean.type));
        }

    }

    @OnClick(R.id.iv_scan)
    public void goScan() {

        if (UserHelper.getSavedUser() == null || Tools.isNull(UserHelper.getSavedUser().token)) {
            startActivity(LoginActivity.getLauncher(getContext()));
        } else {
            //进入扫一扫界面
            new IntentIntegrator(getActivity())
                    .setCaptureActivity(ChargeCaptureActivity.class)
                    .setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)// 扫码的类型,可选：一维码，二维码，一/二维码
                    .setPrompt(getString(R.string.please_take_qrcode))// 设置提示语
                    .setCameraId(0)// 选择摄像头,可使用前置或者后置
                    .setBeepEnabled(true)// 是否开启声音,扫完码之后会"哔"的一声
                    .setBarcodeImageEnabled(false)// 扫完码之后生成二维码的图片
                    .initiateScan();// 初始化扫码
        }
    }

    @OnClick(R.id.tv_go_charger)
    public void goCharger() {
        startActivity(ChagerStatueActivity.getLauncher(getContext(), homeChargeOrderBean));
    }

    @Override
    public void goLogin() {
        UserHelper.clearUserInfo(UserBean.class);
        ll_appontment.setVisibility(View.GONE);
        rl_charger_order.setVisibility(View.GONE);
        rl_not_pay.setVisibility(View.GONE);
        startActivity(LoginActivity.getLauncher(getContext()));
    }
}
