package cn.brainysoon.popfind.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.alibaba.fastjson.JSONObject;
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
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.Circle;
import com.amap.api.maps.model.CircleOptions;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.offlinemap.OfflineMapManager;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.brainysoon.popfind.R;
import cn.brainysoon.popfind.model.Baby;
import cn.brainysoon.popfind.util.SensorEventHelper;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements LocationSource,
        AMapLocationListener, OfflineMapManager.OfflineMapDownloadListener {

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
    public static final String LOCATION_MARKER_FLAG = "选取的位置";

    private boolean isItemClickAction;
    private boolean isInputKeySearch;
    private LatLng searchLatlon;

    private static final Double DEFAULT_LOCATION_RADIUS = 30D;

    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    private OkHttpClient client = new OkHttpClient();

    private static final String DEFAULT_URL_ADD_BABY = "http://www.coolbhu.cn:8080/addbaby";
    private static final String DEFAULT_URL_FIND_BABY = "http://www.coolbhu.cn:8080/findbaby";

    //进度条的 Dialog
    private MaterialDialog progressDialog = null;

    private static final String JSON_OBJECT_KEY_RESULT = "result";

    //搜索到的宝贝
    private List<Baby> babyList = null;
    private List<Marker> markers = null;


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
                builder.autoDismiss(false);
                builder.cancelable(false);

                builder.onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                        initCustomAddView(dialog.getCustomView(), dialog);
                    }
                });

                builder.onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                        dialog.dismiss();
                    }
                });

                //dialogs
                MaterialDialog dialog = builder.build();

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
                builder.autoDismiss(false);

                builder.onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                        initCustomFindView(dialog.getCustomView(), dialog);
                    }
                });

                builder.onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                        dialog.dismiss();
                    }
                });

                //dialogs
                MaterialDialog dialog = builder.build();

                //显示
                dialog.show();
            }
        });
    }

    private void initCustomFindView(View rootView, MaterialDialog dialog) {


        //拿到控件
        EditText textFindClass = (EditText) rootView.findViewById(R.id.baby_find_class);
        EditText textFindCircle = (EditText) rootView.findViewById(R.id.baby_find_circle);

        //text
        String textCircle = textFindCircle.getText().toString().trim();

        if (textCircle.equals("")) {

            textCircle = "1000";
        }

        //拼接url
        String url = DEFAULT_URL_FIND_BABY + "?Long=" + searchLatlon.longitude +
                "&Lat=" + searchLatlon.latitude + "&Circle=" + textCircle;

        //出现进度条
        MaterialDialog.Builder builder = new MaterialDialog.Builder(MainActivity.this);
        builder.title(R.string.progress_title)
                .content(R.string.progress_hint)
                .progress(true, 0)
                .cancelable(false)
                .progressIndeterminateStyle(false);

        dialog.dismiss();
        progressDialog = builder.show();

        new FindBabyAsyncTask().execute(url);
    }

    private void initCustomAddView(View rootView, MaterialDialog dialog) {

        //BindView
        EditText textAddName = (EditText) rootView.findViewById(R.id.baby_add_name);
        EditText textAddClass = (EditText) rootView.findViewById(R.id.baby_add_class);
        EditText textAddSecret = (EditText) rootView.findViewById(R.id.baby_add_secret);
        EditText textAddKey = (EditText) rootView.findViewById(R.id.baby_add_key);
        EditText textAddPhone = (EditText) rootView.findViewById(R.id.baby_add_phone);
        TextView textView = (TextView) rootView.findViewById(R.id.err_add_text);

        //prepare paramarter
        String textName = textAddName.getText().toString().trim();
        String textClass = textAddClass.getText().toString().trim();
        String textSecret = textAddSecret.getText().toString().trim();
        String textKey = textAddKey.getText().toString().trim();
        String textPhone = textAddPhone.getText().toString().trim();

        //判断都不为空
        if (textName.equals("") ||
                textClass.equals("") ||
                textSecret.equals("") ||
                textKey.equals("") ||
                textPhone.equals("")) {

            textView.setVisibility(View.VISIBLE);

            return;
        }

        RequestBody formBody = new FormBody.Builder()
                .add("babyname", textName)
                .add("babyclass", textClass)
                .add("passsecret", textSecret)
                .add("secretkey", textKey)
                .add("findphone", textPhone)
                .add("babylong", searchLatlon.longitude + "")
                .add("babylat", searchLatlon.latitude + "")
                .build();

        //出现进度条
        MaterialDialog.Builder builder = new MaterialDialog.Builder(MainActivity.this);
        builder.title(R.string.progress_title)
                .content(R.string.progress_hint)
                .progress(true, 0)
                .cancelable(false)
                .progressIndeterminateStyle(false);

        dialog.dismiss();
        progressDialog = builder.show();

        new AddBabyAsyncTask().execute(formBody);
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

        aMap.setOnCameraChangeListener(new AMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {

            }

            @Override
            public void onCameraChangeFinish(CameraPosition cameraPosition) {

                searchLatlon = new LatLng(cameraPosition.target.latitude, cameraPosition.target.longitude);
                if (!isItemClickAction && !isInputKeySearch) {

                    mLocMarker.setPosition(searchLatlon);

                    mCircle.setRadius(DEFAULT_LOCATION_RADIUS);
                    mCircle.setCenter(searchLatlon);
                }
                isInputKeySearch = false;
                isItemClickAction = false;

                Log.e("location", "marker");
            }
        });

        aMap.setOnMarkerClickListener(new AMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                String str = marker.getSnippet();

                try {

                    final int index = Integer.parseInt(str);

                    final Baby baby = babyList.get(index);

                    MaterialDialog dialog = new MaterialDialog.Builder(MainActivity.this)
                            .title(baby.getBabyName())
                            .content(baby.getBabyPassSecret())
                            .inputType(
                                    InputType.TYPE_CLASS_TEXT
                                            | InputType.TYPE_TEXT_VARIATION_PERSON_NAME
                                            | InputType.TYPE_TEXT_FLAG_CAP_WORDS)
                            .inputRange(2, 16)
                            .positiveText(R.string.positive_text)
                            .input(
                                    R.string.baby_key_text_hint,
                                    0,
                                    false, new MaterialDialog.InputCallback() {
                                        @Override
                                        public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {

                                            //结果
                                            String title = null;
                                            String content = null;

                                            String key = baby.getBabySecretKey().trim();
                                            String inputkey = input.toString().trim();

                                            //答案正确
                                            if (key.compareTo(inputkey) == 0) {

                                                title = "你可以给好心人打电话了";
                                                content = "电话为：" + baby.getBabyFindPhone();
                                            } else {

                                                title = "答案不对";
                                                content = "可能不是你的东西哦，再找找!";
                                            }

                                            MaterialDialog alertDialog = new MaterialDialog.Builder(MainActivity.this)
                                                    .title(title)
                                                    .content(content)
                                                    .cancelable(false)
                                                    .positiveText(R.string.positive_text)
                                                    .build();

                                            dialog.dismiss();
                                            alertDialog.show();
                                        }
                                    })
                            .show();

                } catch (Exception ex) {

                    ex.printStackTrace();
                }

                return false;
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
                searchLatlon = new LatLng(amapLocation.getLatitude(), amapLocation.getLongitude());
                if (!mFirstFix) {   //只定位一次就够了
                    mFirstFix = true;
                    addCircle(searchLatlon, amapLocation.getAccuracy());//添加定位精度圆
                    addMarker(searchLatlon);//添加定位图标
//                    mSensorHelper.setCurrentMarker(mLocMarker);//定位图标旋转

                    //停止定位
                    mlocationClient.stopLocation();

                    //下载离线地图
                    //构造OfflineMapManager对象
                    OfflineMapManager amapManager = new OfflineMapManager(this, this);
                    //按照citycode下载
                    try {

                        amapManager.downloadByCityCode(amapLocation.getCityCode());

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                } else {

                    mCircle.setCenter(searchLatlon);
                    mCircle.setRadius(amapLocation.getAccuracy());
                    mLocMarker.setPosition(searchLatlon);
                }

                Log.e("Location:", "Here");

                aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(searchLatlon, 18));
            } else {
                String errText = "定位失败," + amapLocation.getErrorCode() + ": " + amapLocation.getErrorInfo();
                Log.e("AmapErr", errText);

                Toast.makeText(this, errText, Toast.LENGTH_LONG).show();

                LatLng latLng = aMap.getCameraPosition().target;

                addCircle(latLng, DEFAULT_LOCATION_RADIUS);
                addMarker(latLng);
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
                R.drawable.marker);
        BitmapDescriptor des = BitmapDescriptorFactory.fromBitmap(bMap);

        MarkerOptions options = new MarkerOptions();
        options.icon(des);
        options.anchor(0.5f, 0.75f);
        options.position(latlng);
        mLocMarker = aMap.addMarker(options);
        mLocMarker.setTitle(LOCATION_MARKER_FLAG);
    }

    private void addBabyMarker(Baby baby, int index) {

        LatLng latLng = new LatLng(baby.getBabyLat(), baby.getBabyLong());

        Bitmap bMap = BitmapFactory.decodeResource(this.getResources(),
                R.drawable.baby_marker);
        BitmapDescriptor des = BitmapDescriptorFactory.fromBitmap(bMap);

        MarkerOptions options = new MarkerOptions();
        options.icon(des);
        options.snippet(index + "");
        options.anchor(0.5f, 0.75f);
        options.position(latLng);
        Marker marker = aMap.addMarker(options);
        marker.setTitle(baby.getBabyName());

        markers.add(index, marker);
    }

    //添加宝贝
    class AddBabyAsyncTask extends AsyncTask<RequestBody, Void, String> {

        @Override
        protected String doInBackground(RequestBody... requestBodies) {

            String result = null;

            //发起请求
            try {

                result = post(DEFAULT_URL_ADD_BABY, requestBodies[0]);

            } catch (Exception ex) {

                ex.printStackTrace();

                result = "{result:" + -1 + "}";
            }

            return result;
        }

        @Override
        protected void onPostExecute(String s) {

            if (progressDialog != null) {

                //消除
                progressDialog.setCancelable(true);
                progressDialog.dismiss();
                progressDialog = null;
            }

            JSONObject result = JSONObject.parseObject(s);
            String content = null;

            if (result.getInteger(JSON_OBJECT_KEY_RESULT) > 0) {

                content = "添加成功！";
            } else {

                content = "添加失败,请稍后再试！";
            }

            //结果
            MaterialDialog alertDialog = new MaterialDialog.Builder(MainActivity.this)
                    .title(R.string.alert_result_title)
                    .positiveText(R.string.positive_text)
                    .content(content)
                    .build();

            alertDialog.show();
        }

        String post(String url, RequestBody formBabdy) throws IOException {
            Request request = new Request.Builder()
                    .url(url)
                    .post(formBabdy)
                    .build();
            Response response = client.newCall(request).execute();
            return response.body().string();
        }
    }

    //搜索宝贝
    class FindBabyAsyncTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPostExecute(String s) {

            if (progressDialog != null) {

                //消除
                progressDialog.setCancelable(true);
                progressDialog.dismiss();
                progressDialog = null;
            }

            //转换成数组
            if (s == null) {

                //结果
                MaterialDialog alertDialog = new MaterialDialog.Builder(MainActivity.this)
                        .title(R.string.alert_result_title)
                        .positiveText(R.string.positive_text)
                        .content("请求失败，请稍后再试！")
                        .build();

                alertDialog.show();
            }

            //清空原来的
            if (markers != null) {
                babyList = null;
                for (Marker marker : markers) {

                    marker.remove();
                }

                markers = null;
            }

            //找到的数组
            babyList = JSONObject.parseArray(s, Baby.class);
            markers = new ArrayList<>();

            if (babyList == null) {

                return;
            }

            //更新地图，地图秒点
            for (int i = 0; i < babyList.size(); i++) {

                addBabyMarker(babyList.get(i), i);
            }
        }

        @Override
        protected String doInBackground(String... strings) {

            String result = null;

            try {

                result = get(strings[0]);

            } catch (Exception ex) {

                ex.printStackTrace();
            }

            return result;
        }

        String get(String url) throws IOException {
            Request request = new Request.Builder()
                    .url(url)
                    .build();

            Response response = client.newCall(request).execute();
            return response.body().string();
        }
    }

    @Override
    public void onDownload(int i, int i1, String s) {

    }

    @Override
    public void onCheckUpdate(boolean b, String s) {

    }

    @Override
    public void onRemove(boolean b, String s, String s1) {

    }
}
