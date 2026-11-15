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
import android.text.Html;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
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
    private ConstraintLayout map_constraint_layout;

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
    private final ArrayList<Placemark> placemarks = new ArrayList<>();
    private final ArrayList<PlacemarkMapObject> placemarkMapObjects = new ArrayList<>();
    private final ArrayList<MapObjectTapListener> placemarkTapListeners = new ArrayList<>();

    private static final String[] PLACEMARKS = {
            "60.938836|76.565343|Пункт приёма вторсырья Эко-Пункт|ул. Ленина, 16, Нижневартовск, Ханты-Мансийский автономный округ, 628606",
            "60.951186|76.559995|Пункт приёма одежды|ул. Мира, 11/2, Нижневартовск, Ханты-Мансийский автономный округ, 628606",
            "60.946178|76.578106|Пункт приёма вторсырья|ул. Мира, 29А, Нижневартовск, Ханты-Мансийский автономный округ, 628606",
            "60.935877|76.539806|Пункт приёма вторсырья|панель №20, Нижневартовск, Ханты-Мансийский автономный округ, 628606",
            "60.941541|76.557583|Контейнер для батареек и ламп|ул. Ленина, 7, корп. 1, Нижневартовск, Ханты-Мансийский автономный округ, 628606",
            "60.931442|76.551727|Контейнер для ПЭТ-бутылок ТрансСервис|ул. Менделеева, 2, Нижневартовск, Ханты-Мансийский автономный округ, 628606",
            "60.932713|76.549977|Контейнер для сбора макулатуры|ул. Менделеева, 7, Нижневартовск, Ханты-Мансийский автономный округ, 628606",
            "60.935330|76.585101|Пункт приёма одежды на благотворительность|ул. Ленина, 38А, Нижневартовск, Ханты-Мансийский автономный округ, 628606",
            "60.940700|76.594134|Контейнер для вторсырья|ул. Чапаева, 22, Нижневартовск, Ханты-Мансийский автономный округ, 628606",
            "60.937612|76.539843|Пункт приёма опасных отходов и шин|ул. Ленина, 4П, стр. 12, Нижневартовск, Ханты-Мансийский автономный округ, 628606",
            "60.941727|76.557812|Контейнер для ПЭТ-бутылок ТрансСервис|ул. Ленина, 7, корп. 1, Нижневартовск, Ханты-Мансийский автономный округ, 628606",
            "60.937904|76.551399|Контейнер для батареек|ул. Кузоваткина, 14, Нижневартовск, Ханты-Мансийский автономный округ, 628606",
            "60.937950|76.536092|Контейнер для ПЭТ-бутылок|ул. Ленина, 2П, стр. 12, Нижневартовск, Ханты-Мансийский автономный округ, 628606",
            "60.967223|76.534906|Пункт приёма шин и металлолома|Индустриальная ул., 54, Нижневартовск, Ханты-Мансийский автономный округ, 628606",
            "60.940138|76.591120|Контейнер для крышечек|ул. Мира, 43С1, Нижневартовск, Ханты-Мансийский автономный округ, 628606",
            "60.944286|76.565609|Контейнер для батареек и ламп|ул. Маршала Жукова, 8, Нижневартовск, Ханты-Мансийский автономный округ, 628606",
            "60.960698|76.535580|Пункт приёма ПЭТ-бутылок Нзум Юнипак|Индустриальная ул., 46, стр. 19, Нижневартовск, Ханты-Мансийский автономный округ, 628606",
            "60.957968|76.533081|Пункт приёма вторсырья Эко-Мир|панель № 15, Нижневартовск, Ханты-Мансийский автономный округ, 628606",
            "60.935681|76.617437|Контейнер для батареек и ламп|улица Романтиков, Нижневартовск, Ханты-Мансийский автономный округ, 628606",
            "60.946697|76.576438|Контейнер для вторсырья|улица Комсомольское Озеро, Нижневартовск, Ханты-Мансийский автономный округ, 628606",
            "60.952581|76.503444|СанСет|ул. Авиаторов, 2, Нижневартовск, Ханты-Мансийский автономный округ, 628606",
            "60.945330|76.607244|Контейнер для вторсырья|Интернациональная ул., 53, Нижневартовск, Ханты-Мансийский автономный округ, 628606",
            "60.940513|76.623643|Контейнер раздельного сбора отходов|Интернациональная ул., 38А, Нижневартовск, Ханты-Мансийский автономный округ, 628606",
            "60.989803|76.465773|Контейнер для пластиковых бутылок и алюминиевых банок РО Югра Экология|район НВ ГПЗ, Нижневартовск, Ханты-Мансийский автономный округ, 628606"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_main);
        setSettingsForMap();
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
                    .setTitle("Что ожидается в этом разделе")
                    .setMessage("В данном разделе планируется реализовать распознавание мусора на фото и его сортировку")
                    .setCancelable(true)
                    .setNegativeButton("`Закрыть`", new DialogInterface.OnClickListener() {
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
        introducing_text_view.setText(Html.fromHtml("<h1>Добро пожаловать в приложение для обучения сортировке мусора!</h1>\n" +
                "<p>Мы рады видеть вас здесь! Начните учиться правильной сортировке отходов, чтобы сделать наш мир чище и здоровее.</p>\n" +
                "<p>Следуйте нашим рекомендациям, разделяйте отходы на категории и помогайте сохранить окружающую среду.</p>\n" +
                "<p>Давайте вместе делать мир лучше!</p>"));

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

    private void setSettingsForMap() {
        if (BuildConfig.MAPKIT_API_KEY.isEmpty() || map_view != null) {
            return;
        }

        MapKitFactory.initialize(this);
        FrameLayout mapContainer = findViewById(R.id.map_container);
        map_view = (MapView) getLayoutInflater().inflate(R.layout.view_map, mapContainer, false);
        findViewById(R.id.map_unavailable_text).setVisibility(View.GONE);
        mapContainer.addView(map_view);

        Map map = map_view.getMapWindow().getMap();
        map.move(new CameraPosition(INITIAL_MAP_CENTER, 12f, 0f, 0f));
        map.addCameraListener(this);
        MapObjectCollection mapObjects = map.getMapObjects();
        ImageProvider pin = ImageProvider.fromResource(this, R.drawable.red_placemark);

        for (String rawPlacemark : PLACEMARKS) {
            String[] fields = rawPlacemark.split("\\|");
            Placemark model = new Placemark(Double.parseDouble(fields[0]),
                    Double.parseDouble(fields[1]), fields[2], fields[3], fields[2]);
            placemarks.add(model);

            Point point = new Point(model.getLatitude(), model.getLongitude());
            PlacemarkMapObject marker = mapObjects.addPlacemark(point);
            marker.setIcon(pin);
            placemarkMapObjects.add(marker);

            MapObjectTapListener listener = (object, position) -> {
                new AlertDialog.Builder(this)
                        .setTitle(model.getTitle())
                        .setMessage(getString(R.string.placemark_details, model.getContent()))
                        .setPositiveButton(R.string.close, null)
                        .show();
                return true;
            };
            placemarkTapListeners.add(listener);
            marker.addTapListener(listener);
        }

        userLocationLayer = MapKitFactory.getInstance().createUserLocationLayer(map_view.getMapWindow());
        userLocationLayer.setHeadingEnabled(true);
        userLocationLayer.setObjectListener(this);
        updateUserLocation();
    }

    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void updateUserLocation() {
        if (userLocationLayer != null) {
            userLocationLayer.setVisible(hasLocationPermission());
        }
    }

    private void setLessonsList(){
        lessons_list.add(new Lesson(R.drawable.ic_question_reasons,
                "5 причин сортировать отходы",
                "\t\t\t Еще сомневаетесь в необходимости сортировать отходы?  Есть 5 причин начть это делать!\n\n\n\n\n" +
                        "1️⃣ Сокращается рост свалок. Больше отходов отправляется на переработку," +
                        " что помогает сократить количество отходов на свалках на 50–60 %. \n\n\n" +
                        "" +
                        "2️⃣ Снижает угрозу окружающей среде и здоровью людей. Опасные отходы попадают на специализированные предприятия," +
                        " где уничтожаются, не нанося вред природе и здоровью. \n\n\n" +
                        "" +
                        "3️⃣ Возвращает в цикл производства уже использованные ресурсы." +
                        " Вторсырьё можно ещё раз использовать для производства новых товаров.\n\n\n" +
                        ""+
                        "4️⃣ Помогает экономике страны. Раздельный сбор в перспективе может принести ощутимую пользу экономике.\n\n\n" +
                        ""+
                        "5️⃣ Приносит выгоду населению. Отсортировав отходы дома, можно сдать макулатуру, металлолом или стеклотару в ближайший пункт приёма.",
                R.drawable.ic_content_5reasons));

        lessons_list.add(new Lesson(R.drawable.ic_litter,
                "Основные виды отходов",
                "\t\t Основные категории, на которые разделяется бытовой мусор, следующие:" +
                        " бумага и картон, стекло, пластик, текстиль, металл, пищевые отходы, опасные и не сортируемые отходы — то есть те," +
                        " которые не входят в основные категории и не могут быть переработаны вторично." +
                        " Такие отходы вывозятся на полигоны или мусоросжигательные заводы." +
                        " Все остальные категории можно переработать и дать им вторую жизнь.\n\n\n\n" +
                        "1️⃣ Бумага и картон: газеты, журналы, офисная бумага, карточные коробки и упаковка из картона.\n\n" +
                        "2️⃣ Пластик: пластиковые бутылки, контейнеры, упаковочные материалы, пленка, пластиковые пакеты.\n\n" +
                        "3️⃣ Стекло: бутылки, банки, стеклянные контейнеры.\n\n" +
                        "4️⃣ Металл: алюминиевые банки, жестяные банки, металлические крышки, металлическая упаковка.\n\n" +
                        "5️⃣ Органические отходы: пищевые остатки, овощи, фрукты, чайные мешки, кофейная гуща.\n\n" +
                        "6️⃣ Электроника и электроотходы: вышедшая из строя электроника, старые компьютеры, мобильные телефоны, батарейки.\n\n" +
                        "7️⃣ Опасные отходы: жидкие и твердые химические отходы, аккумуляторы, лампы накаливания, лекарства, краски.\n\n" +
                        "8️⃣ Текстиль: одежда, обувь, ткани, текстильные изделия.\n\n" +
                        "9️⃣ Строительные отходы: бетон, кирпичи, древесина, металлические конструкции.\n\n" +
                        "\uD83D\uDD1F Медицинские отходы: использованные шприцы, перевязочный материал, медицинские инструменты.\n\n\n" +
                        "\t\t Эти виды отходов являются основными категориями для сортировки и переработки в целях уменьшения негативного воздействия на окружающую среду и рационального использования ресурсов." +
                        " Правильная сортировка и утилизация отходов способствуют снижению количества отходов на свалке, а также поддерживают экологически чистую и устойчивую среду жизнедеятельности.",
                R.drawable.different_litter));

        lessons_list.add(new Lesson(R.drawable.ic_plastic,
                "Виды пластика для вторичной переработки",
                "\t\t Пластик является наиболее обширным мусором. " +
                        "Определить вид пластика можно, если найти треугольный значок с цифрой и название пластика на самом пластике, " +
                        "однако это не всегда удастся сделать, так как на некоторых объектах он не указан. " +
                        "Пластик имеет большое количество различных видов. Основные виды пластика и изготавливаемые из них изделия изображены на рисунке. " +
                        "Из данных видов пластика не подлежат переработке виды пластика под номерами 3 и 7.",
                R.drawable.different_plastics));

        lessons_list.add(new Lesson(R.drawable.ic_sort_trash,
                "Виды контейнеров для сортировки мусора",
                "\t\t Для удобства сортировки мусора в домашних условиях, в общественных местах" +
                        " и на предприятиях используются различные виды контейнеров. Вот некоторые из них:\n\n\n" +
                        "1️⃣ Контейнер для пластика - обычно имеет оранжевый цвет.\n\n" +
                        "2️⃣ Контейнер для бумаги - часто окрашен в синий цвет.\n\n" +
                        "3️⃣ Контейнер для картона - часто окрашен в желтый цвет.\n\n" +
                        "4️⃣ Контейнер для стекла - чаще всего зеленого цвета.\n\n" +
                        "5️⃣ Контейнер для органических отходов - может быть черного цвета.\n\n" +
                        "6️⃣ Контейнер для металла - обычно серебристого или серого цвета.\n\n" +
                        "7️⃣ Контейнер для перерабатываемых отходов - обычно имеет красный цвет.\n\n" +
                        "8️⃣ Контейнер для опасных отходов - может быть коричневого цвета.\n\n" +
                        "9️⃣ Универсальный контейнер - часто окрашен в серый или черный цвет.\n\n\n" +
                        "\t\t Иногда цвет контейнеров может отлмчаться, но тогда нал самом контейнере будет написано для какого мусора он предназначен.",
                R.drawable.different_containers));

        lessons_list.add(new Lesson(R.drawable.how_to_sort_trash,
                "Как правильно сортировать мусор",
                "\t\t Если вы собираетесь разделять отходы, то вам потребуется несколько емкостей для их хранения. " +
                        "Их можно разместить на балконе, в кладовой. В последнее время наблюдается тенденция размещения контейнеров в подъездах многоквартирных домов. \n\n\n" +
                        "Раздельная сортировка отходов может состоять из следующих этапов:\n\n" +
                        "1️⃣ Отобрать опасные и не перерабатываемые отходы. Для опасных отходов следует завести отдельное мусорное ведро.\n\n" +
                        "2️⃣ Очистить вторсырье в соответствии с правилами приема. Бутылки должны быть чистыми, а макулатура и другое объемное сырье должно быть упаковано, связано.\n\n" +
                        "3️⃣ Отобрать вторсырье: бумага, стекло, металл и некоторые виды пластика: HDPE, LDPE, PP, PETE, PS. В случае если вы собираетесь сдавать вторсырье в общий контейнер для последующей сортировки, тогда помещайте его в одном мусорное ведро. В противном случае вам понадобятся пакеты или ведра для разных видов вторсырья.\n\n" +
                        "4️⃣ Отнести отходы на пункт приема. \n\n\n" +
                        "\t\t Основные правила приема на пунктах: \n\n\n" +
                        "1️⃣ Разные виды отходов не должны быть смешанны.\n\n" +
                        "2️⃣ Мусор должен быть в сухом и чистом виде.",
                R.drawable.ic_how_to_sort));

        lessons_list.add(new Lesson(R.drawable.ic_man_think,
                "А вы знаете, что…",
                "✅70% объема мусорного ведра в нашей квартире занимает упаковка от продуктов и товаров. При этом большая часть её пригодна для переработки или вторичного использования.\n\n\n" +
                        "✅На производство 1 бутылки тратится такое же количество энергии, как на переработку 10 таких же изделий.\n\n" +
                        "✅60 кг макулатуры, переданной на переработку, позволяют сохранить 1 дерево.\n\n" +
                        "✅Около 500 лет потребуется для полного разложения в природе одноразовых подгузников, гигиенических прокладок, платков и других изделий из нетканых материалов в природе.\n\n" +
                        "✅5 миллиардов окурков люди выбрасывают каждый год. Полностью «бычок» от сигареты с синтетическим фильтром в окружающей среде разложится только через 10 лет.\n\n" +
                        "✅2 миллиарда тонн отходов генерирует население Земли ежегодно.\n\n" +
                        "✅7 миллионов тонн одежды выбрасывается на планете ежегодно. Только 12% вещей передаются на переработку.\n\n" +
                        "✅2000 тонн мусора собрали на околоземной орбите во время первого космического субботника на МКС в 2002 году.\n\n" +
                        "✅1,5 миллиона тонн метана и 21,5 миллион тонн углекислого газа выделяют ежегодно в атмосферу российские мусорные полигоны.\n\n" +
                        "✅32 кг золота, 3,5 тонны серебра и 2,5 тонны бронзы извлекли из 78985 тонн электронного мусора в Японии. Акция по сбору телефонов и других гаджетов была посвящена Олимпиаде 2020.\n\n",
                R.drawable.man_think));
    }

    private void setActionsForClGroups() {
        View[] tabs = {main_profile_button_constraint_layout, main_education_button_constraint_layout,
                main_map_button_constraint_layout, main_trash_detection_button_constraint_layout};
        for (View tab : tabs) {
            tab.setOnClickListener(view -> {
                showTab(view.getId());
                if (view == main_map_button_constraint_layout && map_view != null && !hasLocationPermission()
                        && !getPreferences(MODE_PRIVATE).getBoolean("asked_for_location", false)) {
                    getPreferences(MODE_PRIVATE).edit().putBoolean("asked_for_location", true).apply();
                    requestLocation.launch(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION});
                }
            });
        }
    }

    private void showTab(int tab) {
        selectedTab = tab;
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
