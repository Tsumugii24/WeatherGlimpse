package com.yui.weatherglimpse;

import static com.yui.weatherglimpse.location.CityFinder.getCityNameUsingNetwork;
import static com.yui.weatherglimpse.location.CityFinder.setLongitudeLatitude;
import static com.yui.weatherglimpse.network.InternetConnectivity.isInternetConnected;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.yui.weatherglimpse.adapter.DaysAdapter;
import com.yui.weatherglimpse.databinding.ActivityHomeBinding;
import com.yui.weatherglimpse.location.LocationCord;
import com.yui.weatherglimpse.toast.Toaster;
import com.yui.weatherglimpse.update.UpdateUI;
import com.yui.weatherglimpse.url.URL;
import com.yui.weatherglimpse.utils.WeatherData;
import com.yui.weatherglimpse.utils.WeatherDescriptionTranslator;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class HomeActivity extends AppCompatActivity {

    private final int WEATHER_FORECAST_APP_UPDATE_REQ_CODE = 101;   // for app update
    private static final int PERMISSION_CODE = 1;                   // for user location permission
    private String name, updated_at, description, temperature, min_temperature, max_temperature, pressure, wind_speed, humidity;
    private int condition;
    private long update_time, sunset, sunrise;
    private String city = "";
    private final int REQUEST_CODE_EXTRA_INPUT = 101;
    private ActivityHomeBinding binding;

    private MediaPlayer mediaPlayer;
    // 背景图片切换
    private ImageView backgroundImage1;
    private ImageView backgroundImage2;
    private final int[] backgroundImages = {R.drawable.bg_image1, R.drawable.bg_image2, R.drawable.bg_image3, R.drawable.bg_image6, R.drawable.bg_image5, R.drawable.bg_image6, R.drawable.bg_image7, R.drawable.bg_image8, R.drawable.bg_image9};
    private int currentBackgroundIndex = 0;
    private boolean isImage1Showing = true;

    private List<WeatherData> futureWeatherData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        futureWeatherData = new ArrayList<>();

        Locale locale = new Locale("zh");
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());

        super.onCreate(savedInstanceState);

        // binding
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        // set navigation bar color
        setNavigationBarColor();

        //check for new app update
        checkUpdate();

        // set refresh color schemes
        setRefreshLayoutColor();

        // when user do search and refresh
        listeners();

        // getting data using internet connection
        getDataUsingNetwork();

        // 初始化MediaPlayer并开始播放音乐
        initializeMediaPlayer();

        backgroundImage1 = findViewById(R.id.background_image1);
        backgroundImage2 = findViewById(R.id.background_image2);

        loadSavedBackground();

        // 设置背景图片的点击事件
        binding.switchBackgroundButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchBackground(v);
            }
        });

        binding.toggleThemeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleTheme();
            }
        });

        binding.openGithubButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGitHub(v);
            }
        });

        binding.drawTrendButton.setOnClickListener(v -> drawTrend(futureWeatherData));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkConnection();
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    private void initializeMediaPlayer() {
        mediaPlayer = MediaPlayer.create(this, R.raw.background_music);
        mediaPlayer.setLooping(true); // 设置循环播放
        mediaPlayer.start(); // 开始播放
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_EXTRA_INPUT) {
            if (resultCode == RESULT_OK && data != null) {
                ArrayList<String> arrayList = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                binding.layout.cityEt.setText(Objects.requireNonNull(arrayList).get(0).toUpperCase());
                searchCity(binding.layout.cityEt.getText().toString());
            }
        }
    }


    private void setNavigationBarColor() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(getResources().getColor(R.color.navBarColor));
        }
    }

    private void setUpDaysRecyclerView() {
        DaysAdapter daysAdapter = new DaysAdapter(this);
        binding.dayRv.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );
        binding.dayRv.setAdapter(daysAdapter);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void listeners() {
        binding.layout.mainLayout.setOnTouchListener((view, motionEvent) -> {
            hideKeyboard(view);
            return false;
        });
        binding.layout.searchBarIv.setOnClickListener(view -> searchCity(binding.layout.cityEt.getText().toString()));
        binding.layout.searchBarIv.setOnTouchListener((view, motionEvent) -> {
            hideKeyboard(view);
            return false;
        });
        binding.layout.cityEt.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_GO) {
                searchCity(binding.layout.cityEt.getText().toString());
                hideKeyboard(textView);
                return true;
            }
            return false;
        });
        binding.layout.cityEt.setOnFocusChangeListener((view, b) -> {
            if (!b) {
                hideKeyboard(view);
            }
        });
        binding.mainRefreshLayout.setOnRefreshListener(() -> {
            checkConnection();
            Log.i("refresh", "Refresh Done.");
            binding.mainRefreshLayout.setRefreshing(false);  //for the next time
        });
        //Mic Search
        /*
        binding.layout.micSearchId.setOnClickListener(view -> {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, Locale.getDefault());
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, REQUEST_CODE_EXTRA_INPUT);
            try {
                startActivityForResult(intent, REQUEST_CODE_EXTRA_INPUT);
            } catch (Exception e) {
                Log.d("Error Voice", "Mic Error:  " + e);
            }
        });
        */

        // 添加新的音乐控制代码
        binding.layout.musicControlButton.setOnClickListener(view -> {
            togglePlayPause();
        });
    }

    private void togglePlayPause() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                binding.layout.musicControlButton.setImageResource(android.R.drawable.ic_media_play);
            } else {
                mediaPlayer.start();
                binding.layout.musicControlButton.setImageResource(android.R.drawable.ic_media_pause);
            }
        }
    }

    private void setRefreshLayoutColor() {
        binding.mainRefreshLayout.setProgressBackgroundColorSchemeColor(
                getResources().getColor(R.color.textColor)
        );
        binding.mainRefreshLayout.setColorSchemeColors(
                getResources().getColor(R.color.navBarColor)
        );
    }

    private void searchCity(String cityName) {
        if (cityName == null || cityName.isEmpty()) {
            Toaster.errorToast(this, "请输入城市名称再查询");
        } else {
            setLatitudeLongitudeUsingCity(cityName);
        }
    }

    private void getDataUsingNetwork() {
        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(this);
        //check permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_CODE);
        } else {
            client.getLastLocation().addOnSuccessListener(location -> {
                setLongitudeLatitude(location);
                city = getCityNameUsingNetwork(this, location);
                getTodayWeatherInfo(city);
            });
        }
    }

    private void setLatitudeLongitudeUsingCity(String cityName) {
        URL.setCity_url(cityName);
        RequestQueue requestQueue = Volley.newRequestQueue(HomeActivity.this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, URL.getCity_url(), null, response -> {
            try {
                LocationCord.lat = response.getJSONObject("coord").getString("lat");
                LocationCord.lon = response.getJSONObject("coord").getString("lon");
                getTodayWeatherInfo(cityName);
                // After the successfully city search the cityEt(editText) is Empty.
                binding.layout.cityEt.setText("");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> Toaster.errorToast(this, "请输入正确的城市名称"));
        requestQueue.add(jsonObjectRequest);
    }

    @SuppressLint("DefaultLocale")
    private void getTodayWeatherInfo(String name) {
        URL url = new URL();
        futureWeatherData.clear();
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url.getLink(), null, response -> {
            try {
                this.name = name;
                update_time = response.getJSONObject("current").getLong("dt");
                updated_at = new SimpleDateFormat("EEEE hh:mm a", Locale.ENGLISH).format(new Date(update_time * 1000));

                condition = response.getJSONArray("daily").getJSONObject(0).getJSONArray("weather").getJSONObject(0).getInt("id");
                sunrise = response.getJSONArray("daily").getJSONObject(0).getLong("sunrise");
                sunset = response.getJSONArray("daily").getJSONObject(0).getLong("sunset");
                description = response.getJSONObject("current").getJSONArray("weather").getJSONObject(0).getString("main");

                temperature = String.valueOf(Math.round(response.getJSONObject("current").getDouble("temp") - 273.15));
                min_temperature = String.format("%.0f", response.getJSONArray("daily").getJSONObject(0).getJSONObject("temp").getDouble("min") - 273.15);
                max_temperature = String.format("%.0f", response.getJSONArray("daily").getJSONObject(0).getJSONObject("temp").getDouble("max") - 273.15);
                pressure = response.getJSONArray("daily").getJSONObject(0).getString("pressure");
                wind_speed = response.getJSONArray("daily").getJSONObject(0).getString("wind_speed");
                humidity = response.getJSONArray("daily").getJSONObject(0).getString("humidity");

                for (int i = 1; i <= 6; i++) {
                    JSONObject dayData = response.getJSONArray("daily").getJSONObject(i);
                    WeatherData data = new WeatherData(
                        dayData.getJSONObject("temp").getDouble("max") - 273.15,
                        dayData.getJSONObject("temp").getDouble("min") - 273.15,
                        dayData.getDouble("pressure"),
                        dayData.getDouble("wind_speed"),
                        dayData.getDouble("humidity")
                    );
                    futureWeatherData.add(data);
                }

                updateUI();
                hideProgressBar();
                setUpDaysRecyclerView();

                binding.drawTrendButton.setOnClickListener(v -> drawTrend(futureWeatherData));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, null);
        requestQueue.add(jsonObjectRequest);
        Log.i("json_req", "Day 0");
    }

    @SuppressLint("SetTextI18n")
    private void updateUI() {
        binding.layout.nameTv.setText(name);
        updated_at = translate(updated_at);
        binding.layout.updatedAtTv.setText(updated_at);
        binding.layout.conditionIv.setImageResource(
                getResources().getIdentifier(
                        UpdateUI.getIconID(condition, update_time, sunrise, sunset),
                        "drawable",
                        getPackageName()
                ));

        binding.layout.conditionDescTv.setText(WeatherDescriptionTranslator.translate(description));
        //binding.layout.conditionDescTv.setText(description);
        binding.layout.tempTv.setText(temperature + "°C");
        binding.layout.minTempTv.setText(min_temperature + "°C");
        binding.layout.maxTempTv.setText(max_temperature + "°C");
        binding.layout.pressureTv.setText(pressure + " mb");
        binding.layout.windTv.setText(wind_speed + " km/h");
        binding.layout.humidityTv.setText(humidity + "%");
    }

    private String translate(String dayToTranslate) {
        String[] dayToTranslateSplit = dayToTranslate.split(" ");
        dayToTranslateSplit[0] = UpdateUI.TranslateDay(dayToTranslateSplit[0].trim(), getApplicationContext());
        return dayToTranslateSplit[0].concat(" " + dayToTranslateSplit[1]);
    }

    private void hideProgressBar() {
        binding.progress.setVisibility(View.GONE);
        binding.layout.mainLayout.setVisibility(View.VISIBLE);
    }

    private void hideMainLayout() {
        binding.progress.setVisibility(View.VISIBLE);
        binding.layout.mainLayout.setVisibility(View.GONE);
    }

    private void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) view.getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void checkConnection() {
        if (!isInternetConnected(this)) {
            hideMainLayout();
            Toaster.errorToast(this, "Please check your internet connection");
        } else {
            hideProgressBar();
            getDataUsingNetwork();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toaster.successToast(this, "Permission Granted");
                getDataUsingNetwork();
            } else {
                Toaster.errorToast(this, "Permission Denied");
                finish();
            }
        }
    }

    private void checkUpdate() {
        AppUpdateManager appUpdateManager = AppUpdateManagerFactory.create(HomeActivity.this);
        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();
        appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                try {
                    appUpdateManager.startUpdateFlowForResult(appUpdateInfo, AppUpdateType.IMMEDIATE, HomeActivity.this, WEATHER_FORECAST_APP_UPDATE_REQ_CODE);
                } catch (IntentSender.SendIntentException exception) {
                    Toaster.errorToast(this, "Update Failed");
                }
            }
        });
    }

    private void loadSavedBackground() {
        SharedPreferences prefs = getPreferences(MODE_PRIVATE);
        currentBackgroundIndex = prefs.getInt("background_index", 0);
        setBackground(currentBackgroundIndex);
    }

    private void setBackground(int index) {
        if (isImage1Showing) {
            backgroundImage2.setImageResource(backgroundImages[index]);
        } else {
            backgroundImage1.setImageResource(backgroundImages[index]);
        }
        crossfadeBackground();
    }

    private void crossfadeBackground() {
        int shortAnimationDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);

        ImageView fadeOutView = isImage1Showing ? backgroundImage1 : backgroundImage2;
        ImageView fadeInView = isImage1Showing ? backgroundImage2 : backgroundImage1;

        fadeInView.setAlpha(0f);
        fadeInView.setVisibility(View.VISIBLE);

        fadeInView.animate()
                .alpha(1f)
                .setDuration(shortAnimationDuration)
                .setListener(null);

        fadeOutView.animate()
                .alpha(0f)
                .setDuration(shortAnimationDuration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        fadeOutView.setVisibility(View.GONE);
                    }
                });

        isImage1Showing = !isImage1Showing;
    }

    public void switchBackground(View view) {
        currentBackgroundIndex = (currentBackgroundIndex + 1) % backgroundImages.length;
        setBackground(currentBackgroundIndex);
        saveBackgroundPreference();
    }

    private void saveBackgroundPreference() {
        SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
        editor.putInt("background_index", currentBackgroundIndex);
        editor.apply();
    }

    private void toggleTheme() {
        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        boolean isNightMode = currentNightMode == Configuration.UI_MODE_NIGHT_YES;
        AppCompatDelegate.setDefaultNightMode(isNightMode ? AppCompatDelegate.MODE_NIGHT_NO : AppCompatDelegate.MODE_NIGHT_YES);
        recreate();
    }

    public void openGitHub(View view) {
        String url = "https://github.com/Tsumugii24/WeatherGlimpse";
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);
    }

    private void drawTrend(List<WeatherData> weatherData) {
        Intent intent = new Intent(this, TrendActivity.class);
        intent.putExtra("weatherData", new ArrayList<>(weatherData));
        startActivity(intent);
    }
}