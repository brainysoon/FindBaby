package cn.brainysoon.popfind.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.LocationSource;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.BitmapDescriptor;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.CameraPosition;
import com.amap.api.maps2d.model.Circle;
import com.amap.api.maps2d.model.CircleOptions;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.brainysoon.popfind.R;
import cn.brainysoon.popfind.util.SensorEventHelper;

public class MainActivity extends AppCompatActivity implements LocationSource, AMapLocationListener {

    /**
     * 地图 相关
     */
    @BindView(R.id.map)
    MapView mMapView = null;
    private AMap aMap = null;

    /**
     * 菜单相关
     */
    @BindView(R.id.action_add)
    FloatingActionButton actionAdd = null;
    @BindView(R.id.action_search)
    FloatingActionButton actionSerach = null;
    @BindView(R.id.action_menu)
    FloatingActionsMenu actionsMenu = null;

    private LocationSource.OnLocationChangedListener mListener;
    private AMapLocationClient mlocationClient;
    private AMapLocationClientOption mLocationOption;

    private static final int STROKE_COLOR = Color.argb(180, 3, 145, 255);
    private static final int FILL_COLOR = Color.argb(10, 0, 0, 180);
    private boolean mFirstFix = false;
    private Marker mLocMarker;
    private SensorEventHelper mSensorHelper;
    private Circle mCircle;
    public static final String LOCATION_MARKER_FLAG = "我的位置";

    private boolean isItemClickAction;
    private boolean isInputKeySearch;
    private LatLng searchLatlon;
    private Marker locationMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);// 不显示程序的标题栏
        setContentView(R.layout.activity_main);

        //BindView
        ButterKnife.bind(this);

        //初始化菜单
        initMenu();

        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
        mMapView.onCreate(savedInstanceState);

        //初始化一下事情
        init();
    }

    private void initMenu() {


        //点击add按钮的时候
        actionAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //创建一个Dialogs
                MaterialDialog.Builder builder = new MaterialDialog.Builder(MainActivity.this);

                //设置属性
                builder.title(getResources().getString(R.string.menu_title_add));
                builder.customView(R.layout.custom_view_add, true);
                builder.positiveText(getResources().getString(R.string.positive_text));
                builder.negativeText(getResources().getString(R.string.negative_text));
                builder.cancelable(false);

                //dialogs
                MaterialDialog dialog = builder.build();

                //自定义View
                View custom_add_view = dialog.getCustomView();

                initCustomAddView(custom_add_view);

                //显示
                dialog.show();
            }
        });


        //点击 find 按钮
        actionSerach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //创建一个Dialogs
                MaterialDialog.Builder builder = new MaterialDialog.Builder(MainActivity.this);

                //设置属性
                builder.title(getResources().getString(R.string.menu_title_find));
                builder.customView(R.layout.custom_view_find, true);
                builder.positiveText(getResources().getString(R.string.positive_text));
                builder.negativeText(getResources().getString(R.string.negative_text));
                builder.cancelable(false);

                builder.onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                        initCustomAddView(dialog.getCustomView());
                    }
                });

                //dialogs
                MaterialDialog dialog = builder.build();

                //显示
                dialog.show();
            }
        });
    }

    private void initCustomFindView(View rootView) {

    }

    private void initCustomAddView(View rootView) {

        //BindView
        EditText textAddName = (EditText) rootView.findViewById(R.id.baby_add_name);
        EditText textAddClass = (EditText) rootView.findViewById(R.id.baby_add_class);
        EditText textAddSecret = (EditText) rootView.findViewById(R.id.baby_add_secret);
        EditText textAddKey = (EditText) rootView.findViewById(R.id.baby_add_key);
        EditText textAddPhone = (EditText) rootView.findViewById(R.id.baby_add_phone);

        //prepare paramarter
        String textName = textAddName.getText().toString().trim();
        String textClass = textAddClass.getText().toString().trim();
        String textSecret = textAddSecret.getText().toString().trim();
        String textKey = textAddKey.getText().toString().trim();
        String textPhone = textAddPhone.getText().toString().trim();
    }

    /**
     * 初始化
     */
    private void init() {
        if (aMap == null) {
            aMap = mMapView.getMap();
            setUpMap();
        }
        mSensorHelper = new SensorEventHelper(this);
        if (mSensorHelper != null) {
            mSensorHelper.registerSensorListener();
        }

        LatLng latLng = aMap.getCameraPosition().target;
        Point screenPosition = aMap.getProjection().toScreenLocation(latLng);
        locationMarker = aMap.addMarker(new MarkerOptions()
                .anchor(0.5f, 0.5f)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)));
        //设置Marker在屏幕上,不跟随地图移动
        locationMarker.setPositionByPixels(screenPosition.x, screenPosition.y);
        locationMarker.setZIndex(1);

        aMap.setOnCameraChangeListener(new AMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {

            }

            @Override
            public void onCameraChangeFinish(CameraPosition cameraPosition) {

                searchLatlon = new LatLng(cameraPosition.target.latitude, cameraPosition.target.longitude);
                if (!isItemClickAction && !isInputKeySearch) {

                    locationMarker.setPosition(searchLatlon);
                }
                isInputKeySearch = false;
                isItemClickAction = false;

                Log.e("location", "marker");
            }
        });
    }

    /**
     * 设置一些amap的属性
     */
    private void setUpMap() {
        aMap.setLocationSource(this);// 设置定位监听
        aMap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
        aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
        if (mSensorHelper != null) {
            mSensorHelper.registerSensorListener();
        }
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onPause() {
        super.onPause();
        if (mSensorHelper != null) {
            mSensorHelper.unRegisterSensorListener();
            mSensorHelper.setCurrentMarker(null);
            mSensorHelper = null;
        }
        mMapView.onPause();
        deactivate();
        mFirstFix = false;
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
        if (null != mlocationClient) {
            mlocationClient.onDestroy();
        }
    }

    /**
     * 定位成功后回调函数
     */
    @Override
    public void onLocationChanged(AMapLocation amapLocation) {
        if (mListener != null && amapLocation != null) {
            if (amapLocation != null
                    && amapLocation.getErrorCode() == 0) {
                LatLng location = new LatLng(amapLocation.getLatitude(), amapLocation.getLongitude());
                if (!mFirstFix) {   //只定位一次就够了
                    mFirstFix = true;
                    addCircle(location, amapLocation.getAccuracy());//添加定位精度圆
                    addMarker(location);//添加定位图标
                    mSensorHelper.setCurrentMarker(mLocMarker);//定位图标旋转

                    //停止定位
                    mlocationClient.stopLocation();
                } else {

                    mCircle.setCenter(location);
                    mCircle.setRadius(amapLocation.getAccuracy());
                    mLocMarker.setPosition(location);
                }

                Log.e("Location:", "Here");

                aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 18));
            } else {
                String errText = "定位失败," + amapLocation.getErrorCode() + ": " + amapLocation.getErrorInfo();
                Log.e("AmapErr", errText);

                Toast.makeText(this, errText, Toast.LENGTH_LONG).show();
            }
        }

        Log.e("LocationPlus:", ">>>>Here");
    }

    /**
     * 激活定位
     */
    @Override
    public void activate(LocationSource.OnLocationChangedListener listener) {
        mListener = listener;
        if (mlocationClient == null) {
            mlocationClient = new AMapLocationClient(this);
            mLocationOption = new AMapLocationClientOption();
            //设置定位监听
            mlocationClient.setLocationListener(this);
            //设置为高精度定位模式
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            //设置定位参数
            mlocationClient.setLocationOption(mLocationOption);
            // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
            // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
            // 在定位结束后，在合适的生命周期调用onDestroy()方法
            // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
            mlocationClient.startLocation();
        }
    }

    /**
     * 停止定位
     */
    @Override
    public void deactivate() {
        mListener = null;
        if (mlocationClient != null) {
            mlocationClient.stopLocation();
            mlocationClient.onDestroy();
        }
        mlocationClient = null;
    }

    private void addCircle(LatLng latlng, double radius) {
        CircleOptions options = new CircleOptions();
        options.strokeWidth(1f);
        options.fillColor(FILL_COLOR);
        options.strokeColor(STROKE_COLOR);
        options.center(latlng);
        options.radius(radius);
        mCircle = aMap.addCircle(options);
    }

    private void addMarker(LatLng latlng) {
        if (mLocMarker != null) {
            return;
        }
        Bitmap bMap = BitmapFactory.decodeResource(this.getResources(),
                R.drawable.navi_map_gps_locked);
        BitmapDescriptor des = BitmapDescriptorFactory.fromBitmap(bMap);

//		BitmapDescriptor des = BitmapDescriptorFactory.fromResource(R.drawable.navi_map_gps_locked);
        MarkerOptions options = new MarkerOptions();
        options.icon(des);
        options.anchor(0.5f, 0.5f);
        options.position(latlng);
        mLocMarker = aMap.addMarker(options);
        mLocMarker.setTitle(LOCATION_MARKER_FLAG);
    }
}
