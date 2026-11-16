package com.rather.ecosort;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.rather.ecosort.adapters.CollectionPointsAdapter;
import com.rather.ecosort.adapters.LessonAdapter;
import com.rather.ecosort.adapters.RecyclerViewInterface;
import com.rather.ecosort.models.Lesson;
import com.rather.ecosort.models.Placemark;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.layers.ObjectEvent;
import com.yandex.mapkit.map.CameraListener;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.map.CameraUpdateReason;
import com.yandex.mapkit.map.CompositeIcon;
import com.yandex.mapkit.map.IconStyle;
import com.yandex.mapkit.map.Map;
import com.yandex.mapkit.map.MapObjectCollection;
import com.yandex.mapkit.map.MapObjectTapListener;
import com.yandex.mapkit.map.PlacemarkMapObject;
import com.yandex.mapkit.map.RotationType;
import com.yandex.mapkit.mapview.MapView;
import com.yandex.mapkit.user_location.UserLocationLayer;
import com.yandex.mapkit.user_location.UserLocationObjectListener;
import com.yandex.mapkit.user_location.UserLocationView;
import com.yandex.runtime.image.ImageProvider;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements RecyclerViewInterface, UserLocationObjectListener, CameraListener {

    private ConstraintLayout main_profile_button_constraint_layout;
    private ConstraintLayout main_education_button_constraint_layout;
    private ConstraintLayout main_map_button_constraint_layout;
    private ConstraintLayout main_trash_detection_button_constraint_layout;

    private ImageView main_profile_image_view;
    private ImageView main_education_image_view;
    private ImageView main_map_image_view;
    private ImageView main_trash_detection_image_view;

    private TextView main_profile_text_view;
    private TextView main_education_text_view;
    private TextView main_map_text_view;
    private TextView main_trash_detection_text_view;

    private ConstraintLayout introducing_constraint_layout;
    private ConstraintLayout profile_constraint_layout;
    private ConstraintLayout learn_constraint_layout;
    private ConstraintLayout detect_constraint_layout;
    private View map_constraint_layout;

    private TextView move_to_registration_text_view;
    private TextView move_to_login_text_view;
    private ConstraintLayout registration_layout;
    private ConstraintLayout login_layout;
    private MaterialButton registration_button;
    private MaterialButton login_button;

    private RecyclerView lessons_recycler_view;

    private ArrayList<Lesson> lessons_list;

    private static final String PENDING_PHOTO = "pending_photo";
    private static final String DISPLAYED_PHOTO = "displayed_photo";
    private static final String SELECTED_TAB = "selected_tab";
    private static final String ASKED_FOR_LOCATION = "asked_for_location";
    private static final String COLLECTION_POINTS_MAP_MODE = "collection_points_map_mode";
    private static final float PLACEMARK_LABEL_ZOOM = 14f;
    private static final Point INITIAL_MAP_CENTER = new Point(60.941068, 76.564929);

    private Uri pendingPhoto;
    private Uri displayedPhoto;
    private int selectedTab;
    private boolean labelsVisible;

    private final ActivityResultLauncher<Uri> takePicture = registerForActivityResult(
            new ActivityResultContracts.TakePicture(), this::onPhotoResult);
    private final ActivityResultLauncher<String[]> requestLocation = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(), result -> updateUserLocation());

    private MaterialButton detect_trash_material_button;
    private MaterialButton detect_info_material_button;
    private ImageView detected_image_view;

    private TextView introducing_text_view;

    private MapView map_view;
    private UserLocationLayer userLocationLayer;
    private List<Placemark> placemarks;
    private MaterialButtonToggleGroup pointModes;
    private CollectionPointsAdapter pointsAdapter;
    private RecyclerView pointList;
    private EditText pointQuery;
    private TextView pointSearchStatus;
    private FrameLayout mapContainer;
    private boolean showingPointsMap;
    private final ArrayList<PlacemarkMapObject> placemarkMapObjects = new ArrayList<>();
    private final ArrayList<MapObjectTapListener> placemarkTapListeners = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        setUpCollectionPoints(savedInstanceState);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        main_profile_button_constraint_layout = findViewById(R.id.main_profile_button_constraint_layout);
        main_education_button_constraint_layout = findViewById(R.id.main_education_button_constraint_layout);
        main_map_button_constraint_layout = findViewById(R.id.main_map_button_constraint_layout);
        main_trash_detection_button_constraint_layout = findViewById(R.id.main_trash_detection_button_constraint_layout);

        main_profile_image_view = findViewById(R.id.main_profile_image_view);
        main_education_image_view = findViewById(R.id.main_education_image_view);
        main_map_image_view = findViewById(R.id.main_map_image_view);
        main_trash_detection_image_view = findViewById(R.id.main_trash_detection_image_view);

        main_profile_text_view = findViewById(R.id.main_profile_text_view);
        main_education_text_view = findViewById(R.id.main_education_text_view);
        main_map_text_view = findViewById(R.id.main_map_text_view);
        main_trash_detection_text_view = findViewById(R.id.main_trash_detection_text_view);

        introducing_constraint_layout = findViewById(R.id.introducing_constraint_layout);
        profile_constraint_layout = findViewById(R.id.profile_constraint_layout);
        learn_constraint_layout = findViewById(R.id.learn_constraint_layout);
        map_constraint_layout = findViewById(R.id.map_constraint_layout);
        detect_constraint_layout = findViewById(R.id.detect_constraint_layout);

        move_to_registration_text_view = findViewById(R.id.move_to_registration_text_view);
        move_to_login_text_view = findViewById(R.id.move_to_login_text_view);
        registration_layout = findViewById(R.id.registration_layout);
        login_layout = findViewById(R.id.login_layout);
        registration_button = findViewById(R.id.registration_button);
        login_button = findViewById(R.id.login_button);

        setActionsForProfileItems();

        detected_image_view = findViewById(R.id.detected_image_view);
        detect_trash_material_button = findViewById(R.id.detect_trash_material_button);
        detect_info_material_button = findViewById(R.id.detect_info_material_button);

        detect_info_material_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this)
                    .setTitle(R.string.detection_info_title)
                    .setMessage(R.string.detection_info_message)
                    .setCancelable(true)
                    .setNegativeButton(R.string.detection_info_close, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        detect_trash_material_button.setOnClickListener(view -> openCamera());
        if (savedInstanceState != null) {
            pendingPhoto = savedInstanceState.getParcelable(PENDING_PHOTO);
            displayedPhoto = savedInstanceState.getParcelable(DISPLAYED_PHOTO);
            detected_image_view.setImageURI(displayedPhoto);
        }

        lessons_recycler_view = findViewById(R.id.lessons_recycler_view);

        lessons_list = new ArrayList<>();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        lessons_recycler_view.setLayoutManager(linearLayoutManager);

        setLessonsList();

        LessonAdapter lessonAdapter = new LessonAdapter(this, lessons_list, this);
        lessons_recycler_view.setAdapter(lessonAdapter);

        introducing_text_view = findViewById(R.id.introducing_text_view);
        introducing_text_view.setText(Html.fromHtml(getString(R.string.introducing_html)));

        main_profile_image_view.setBackgroundResource(R.drawable.ic_baseline_person_65);
        main_education_image_view.setBackgroundResource(R.drawable.ic_baseline_menu_book_24);
        main_map_image_view.setBackgroundResource(R.drawable.ic_baseline_map_24);
        main_trash_detection_image_view.setBackgroundResource(R.drawable.ic_baseline_search_24);

        setActionsForClGroups();
        if (savedInstanceState != null) {
            showTab(savedInstanceState.getInt(SELECTED_TAB));
        }
    }

    private void openCamera() {
        try {
            File directory = new File(getCacheDir(), "photos");
            if (!directory.isDirectory() && !directory.mkdirs()) {
                throw new IOException("Cannot create photo directory");
            }
            File photo = File.createTempFile("capture-", ".jpg", directory);
            pendingPhoto = FileProvider.getUriForFile(
                    this, getPackageName() + ".photos", photo);
            takePicture.launch(pendingPhoto);
        } catch (ActivityNotFoundException | IOException exception) {
            onPhotoResult(false);
            Toast.makeText(this, R.string.camera_unavailable, Toast.LENGTH_SHORT).show();
        }
    }

    private void onPhotoResult(boolean success) {
        if (pendingPhoto == null) {
            return;
        }
        if (success) {
            deletePhoto(displayedPhoto);
            displayedPhoto = pendingPhoto;
            detected_image_view.setImageURI(displayedPhoto);
        } else {
            deletePhoto(pendingPhoto);
        }
        pendingPhoto = null;
    }

    private void deletePhoto(Uri photo) {
        if (photo != null) {
            getContentResolver().delete(photo, null, null);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(PENDING_PHOTO, pendingPhoto);
        outState.putParcelable(DISPLAYED_PHOTO, displayedPhoto);
        outState.putInt(SELECTED_TAB, selectedTab);
        outState.putBoolean(COLLECTION_POINTS_MAP_MODE, showingPointsMap);
    }

    private void setAlertDialogProfileInfo() {
        new AlertDialog.Builder(this)
                .setMessage(R.string.profile_unavailable)
                .setPositiveButton(R.string.close, null)
                .show();
    }

    private void setActionsForProfileItems(){
        move_to_registration_text_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registration_layout.setVisibility(View.VISIBLE);
                login_layout.setVisibility(View.INVISIBLE);
            }
        });

        move_to_login_text_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login_layout.setVisibility(View.VISIBLE);
                registration_layout.setVisibility(View.INVISIBLE);
            }
        });

        registration_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setAlertDialogProfileInfo();
            }
        });

        login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setAlertDialogProfileInfo();
            }
        });
    }

    private void setUpCollectionPoints(Bundle savedInstanceState) {
        placemarks = CollectionPoints.fromRows(getResources().getStringArray(R.array.demo_collection_points));
        pointQuery = findViewById(R.id.collection_points_query);
        pointSearchStatus = findViewById(R.id.collection_points_status);
        pointList = findViewById(R.id.collection_points_list);
        pointModes = findViewById(R.id.collection_points_mode);
        mapContainer = findViewById(R.id.map_container);

        pointsAdapter = new CollectionPointsAdapter(point -> {
            dismissPointKeyboard();
            showPointDetails(point, true);
        });
        pointList.setLayoutManager(new LinearLayoutManager(this));
        pointList.setAdapter(pointsAdapter);
        pointQuery.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence text, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence text, int start, int before, int count) {
                searchPoints(text.toString());
            }

            @Override
            public void afterTextChanged(Editable text) {
            }
        });
        pointQuery.setOnEditorActionListener((view, action, event) -> {
            if (action != EditorInfo.IME_ACTION_SEARCH) {
                return false;
            }
            dismissPointKeyboard();
            return true;
        });

        setSettingsForMap();
        boolean restoreMap = map_view != null && savedInstanceState != null
                && savedInstanceState.getBoolean(COLLECTION_POINTS_MAP_MODE);
        if (restoreMap) {
            pointModes.check(R.id.collection_points_map_mode);
        }
        showPointResults(restoreMap);
        pointModes.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                dismissPointKeyboard();
                showPointResults(checkedId == R.id.collection_points_map_mode);
                if (showingPointsMap) {
                    requestMapLocation();
                }
            }
        });
        searchPoints(pointQuery.getText().toString());
    }

    private void dismissPointKeyboard() {
        InputMethodManager keyboard = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        keyboard.hideSoftInputFromWindow(pointQuery.getWindowToken(), 0);
        pointQuery.clearFocus();
    }

    private void searchPoints(String query) {
        List<Placemark> matches = CollectionPoints.search(placemarks, query);
        pointsAdapter.showPoints(matches);
        pointList.scrollToPosition(0);
        pointSearchStatus.setText(matches.isEmpty()
                ? getString(R.string.collection_points_empty)
                : getString(R.string.collection_points_found, matches.size()));

        if (!placemarkMapObjects.isEmpty()) {
            Set<Placemark> visible = new HashSet<>(matches);
            for (int i = 0; i < placemarks.size(); i++) {
                placemarkMapObjects.get(i).setVisible(visible.contains(placemarks.get(i)));
            }
        }
    }

    private void showPointResults(boolean showMap) {
        showingPointsMap = showMap;
        pointList.setVisibility(showMap ? View.GONE : View.VISIBLE);
        mapContainer.setVisibility(showMap ? View.VISIBLE : View.GONE);
        updateUserLocation();
    }

    private void showPointDetails(Placemark point, boolean fromList) {
        AlertDialog.Builder details = new AlertDialog.Builder(this)
                .setTitle(point.getTitle())
                .setMessage(getString(R.string.placemark_details, point.getContent()))
                .setPositiveButton(R.string.close, null);
        if (fromList && map_view != null) {
            details.setNeutralButton(R.string.collection_point_show_on_map, (dialog, which) -> {
                pointModes.check(R.id.collection_points_map_mode);
                map_view.getMapWindow().getMap().move(new CameraPosition(
                        new Point(point.getLatitude(), point.getLongitude()), 16f, 0f, 0f));
            });
        }
        details.show();
    }

    private void setSettingsForMap() {
        if (BuildConfig.MAPKIT_API_KEY.isEmpty()) {
            return;
        }

        MapKitFactory.initialize(this);
        map_view = (MapView) getLayoutInflater().inflate(R.layout.view_map, mapContainer, false);
        mapContainer.addView(map_view);
        pointModes.setVisibility(View.VISIBLE);

        Map map = map_view.getMapWindow().getMap();
        map.move(new CameraPosition(INITIAL_MAP_CENTER, 12f, 0f, 0f));
        map.addCameraListener(this);
        MapObjectCollection mapObjects = map.getMapObjects();
        ImageProvider pin = ImageProvider.fromResource(this, R.drawable.red_placemark);

        for (Placemark model : placemarks) {
            Point point = new Point(model.getLatitude(), model.getLongitude());
            PlacemarkMapObject marker = mapObjects.addPlacemark(point);
            marker.setIcon(pin);
            placemarkMapObjects.add(marker);

            MapObjectTapListener listener = (object, position) -> {
                showPointDetails(model, false);
                return true;
            };
            placemarkTapListeners.add(listener);
            marker.addTapListener(listener);
        }

        userLocationLayer = MapKitFactory.getInstance().createUserLocationLayer(map_view.getMapWindow());
        userLocationLayer.setHeadingEnabled(true);
        userLocationLayer.setObjectListener(this);
    }

    private void requestMapLocation() {
        if (!hasLocationPermission() && !getPreferences(MODE_PRIVATE)
                .getBoolean(ASKED_FOR_LOCATION, false)) {
            getPreferences(MODE_PRIVATE).edit().putBoolean(ASKED_FOR_LOCATION, true).apply();
            requestLocation.launch(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION});
        }
    }

    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void updateUserLocation() {
        if (userLocationLayer != null) {
            userLocationLayer.setVisible(showingPointsMap
                    && selectedTab == R.id.main_map_button_constraint_layout && hasLocationPermission());
        }
    }

    private void setLessonsList() {
        lessons_list.add(new Lesson(R.drawable.ic_question_reasons,
                getString(R.string.lesson_sorting_reasons_title),
                getString(R.string.lesson_sorting_reasons_body), R.drawable.ic_content_5reasons));

        lessons_list.add(new Lesson(R.drawable.ic_litter,
                getString(R.string.lesson_waste_categories_title),
                getString(R.string.lesson_waste_categories_body), R.drawable.different_litter));

        lessons_list.add(new Lesson(R.drawable.ic_plastic,
                getString(R.string.lesson_recyclable_plastics_title),
                getString(R.string.lesson_recyclable_plastics_body), R.drawable.different_plastics));

        lessons_list.add(new Lesson(R.drawable.ic_sort_trash,
                getString(R.string.lesson_sorting_containers_title),
                getString(R.string.lesson_sorting_containers_body), R.drawable.different_containers));

        lessons_list.add(new Lesson(R.drawable.how_to_sort_trash,
                getString(R.string.lesson_sorting_steps_title),
                getString(R.string.lesson_sorting_steps_body), R.drawable.ic_how_to_sort));

        lessons_list.add(new Lesson(R.drawable.ic_man_think,
                getString(R.string.lesson_recycling_facts_title),
                getString(R.string.lesson_recycling_facts_body), R.drawable.man_think));
    }

    private void setActionsForClGroups() {
        View[] tabs = {main_profile_button_constraint_layout, main_education_button_constraint_layout,
                main_map_button_constraint_layout, main_trash_detection_button_constraint_layout};
        for (View tab : tabs) {
            tab.setOnClickListener(view -> showTab(view.getId()));
        }
    }

    private void showTab(int tab) {
        if (selectedTab == R.id.main_map_button_constraint_layout && tab != selectedTab) {
            dismissPointKeyboard();
        }
        selectedTab = tab;
        updateUserLocation();
        introducing_constraint_layout.setVisibility(tab == 0 ? View.VISIBLE : View.INVISIBLE);
        updateTab(tab, main_profile_button_constraint_layout, main_profile_image_view,
                main_profile_text_view, profile_constraint_layout, R.drawable.ic_baseline_person_65,
                R.drawable.ic_baseline_person_65_second);
        updateTab(tab, main_education_button_constraint_layout, main_education_image_view,
                main_education_text_view, learn_constraint_layout, R.drawable.ic_baseline_menu_book_24,
                R.drawable.ic_baseline_menu_book_24_second);
        updateTab(tab, main_map_button_constraint_layout, main_map_image_view,
                main_map_text_view, map_constraint_layout, R.drawable.ic_baseline_map_24,
                R.drawable.ic_baseline_map_24_second);
        updateTab(tab, main_trash_detection_button_constraint_layout, main_trash_detection_image_view,
                main_trash_detection_text_view, detect_constraint_layout, R.drawable.ic_baseline_search_24,
                R.drawable.ic_baseline_search_24_second);
    }

    private void updateTab(int tab, View button, ImageView icon, TextView label, View panel,
                           int inactiveIcon, int activeIcon) {
        boolean active = tab == button.getId();
        icon.setBackgroundResource(active ? activeIcon : inactiveIcon);
        label.setTextColor(ContextCompat.getColor(this, active ? R.color.light_green : R.color.light_gray));
        panel.setVisibility(active ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (map_view != null) {
            MapKitFactory.getInstance().onStart();
            map_view.onStart();
            updateUserLocation();
        }
    }

    @Override
    protected void onStop() {
        if (map_view != null) {
            map_view.onStop();
            MapKitFactory.getInstance().onStop();
        }
        super.onStop();
    }

    @Override
    public void onItemClick(int position) {

    }

    @Override
    public void onObjectAdded(@NonNull UserLocationView userLocationView) {
        userLocationLayer.setAnchor(
                new PointF((float)(map_view.getWidth() * 0.5), (float)(map_view.getHeight() * 0.5)),
                new PointF((float)(map_view.getWidth() * 0.5), (float)(map_view.getHeight() * 0.83)));

        userLocationView.getArrow().setIcon(ImageProvider.fromResource(
                this, R.drawable.clear_sigh));

        CompositeIcon pinIcon = userLocationView.getPin().useCompositeIcon();

        pinIcon.setIcon(
                "icon",
                ImageProvider.fromResource(this, R.drawable.ic_baseline_map_24),
                new IconStyle().setAnchor(new PointF(0f, 0f))
                        .setRotationType(RotationType.ROTATE)
                        .setZIndex(0f)
                        .setScale(1f)
        );

        pinIcon.setIcon(
                "pin",
                ImageProvider.fromResource(this, R.drawable.ic_baseline_home_65),
                new IconStyle().setAnchor(new PointF(0.5f, 0.5f))
                        .setRotationType(RotationType.ROTATE)
                        .setZIndex(1f)
                        .setScale(0.5f)
        );

        userLocationView.getAccuracyCircle().setFillColor(0x990000ff);
    }

    @Override
    public void onObjectRemoved(@NonNull UserLocationView userLocationView) {

    }

    @Override
    public void onObjectUpdated(@NonNull UserLocationView userLocationView, @NonNull ObjectEvent objectEvent) {

    }

    @Override
    public void onCameraPositionChanged(@NonNull Map map, @NonNull CameraPosition cameraPosition,
                                        @NonNull CameraUpdateReason reason, boolean finished) {
        boolean showLabels = cameraPosition.getZoom() >= PLACEMARK_LABEL_ZOOM;
        if (!finished || labelsVisible == showLabels) {
            return;
        }
        labelsVisible = showLabels;
        for (int i = 0; i < placemarkMapObjects.size(); i++) {
            placemarkMapObjects.get(i).setText(showLabels ? placemarks.get(i).getName() : "");
        }
    }
}
