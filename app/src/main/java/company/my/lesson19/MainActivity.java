package company.my.lesson19;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.VideoView;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    // Объявить переменные для хранения ссылок к виджетам макета
    Button photoCamera;
    ImageView image;
    Button videoCamera;
    // VideoView это новый виджет который не проходили, но который служит
    // для воспроизведения видео, также поддерживается создание кнопок контроля
    VideoView video;

    // Коды используемые для отправки запроса на получение разрешений
    // и получения результата снятых на камеру данных
    // RCODE для фото, VCODE для видео
    public static final int RCODE = 0;
    public static final int VCODE = 1;

    // Переменная для сохранения сгенерированного пути для фото и видео
    // в этой переменной храниться путь для последнего созданного файла
    String mediaPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        photoCamera = findViewById(R.id.openCamera);
        image = findViewById(R.id.image);

        // Установить обработчик события клик на кнопке снятия фото
        photoCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Проверить разрешение для доступа к камере
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    // Если не дано разрешение на использование камеры, спросить разрешение и отправить код запроса
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                            Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, RCODE);

                } else {
                    // Если получено разрешение открыть камеру для снятия фото
                    showCamera();
                }
            }
        });

        // Привязать кнопку открытия камеры для снятия видео к кнопке
        videoCamera = findViewById(R.id.openVideo);
        // Привязать виджет VideoView к переменной
        video = findViewById(R.id.video);
        // Класс MediaController служит для показа кнопок управления воспроизведением видео в интерфейсе
        MediaController mc = new MediaController(this);
        mc.setAnchorView(video);
        // Подключить MediaController к VideoView
        video.setMediaController(mc);

        videoCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Спросить разрешение на использование камеры
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    // Если нет разрешение, запросить и отправить код запроса видео
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                            Manifest.permission.CAMERA}, VCODE);

                } else {
                    // Если есть разрешение открыть камеру для снятия видео
                    openVideo();
                }
            }
        });
    }

    // Функция для запуска камеры для снятия фотографий
    void showCamera() {
        // Инициализировать экземпляр камеры для снятия фото
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Объявить и инициализировать файл со значением null
        File photoFile = null;
        try {
            // Попытаться присвоить созданному файлу значение возвращаемое функцией createImageFile
            photoFile = createImageFile();

            // Перехват исключений связанных с созданием нового файла
        } catch (Exception e) {
            // Вывод трассировки стека вызовов функций в Logcat
            e.printStackTrace();
        }

        // В случае создания временного файла значение photoFile меняется с null на значение созданного файла
        // и данная проверка возвращает результат true
        if (photoFile != null) {
            // Получить URI для файла, который служит путём для сохранения снятой фотографии
            Uri photoURI = FileProvider.getUriForFile(this, "com.example.android.fileprovider", photoFile);
            // Добавить значение пути сохранения фото через extra передаваемое Intent-у камеры
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            // Запустить Activity и ждать до получения результата (пока Activity камеры не будет закрыто)
            // и отправить код запроса в Activity
            startActivityForResult(cameraIntent, RCODE);
        }
    }

    // Функция для создания временного фото
    File createImageFile() throws IOException {
        // Получить значение текущей даты отформатированного по формату yyyyMMdd_HHmmss:
        // y - год, M - месяц, d - день, H - часы, m - минуты, s - секунды
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        // Строка для хранения названия файла имеет формат CAM_yyyyMMdd_HHmmss_
        String fileName = "CAM_" + timestamp + "_";
        // Получить путь к папке для хранения фотографии
        // в данном случае выбрана стандартная папка, где хранятся фотографии
        // затем внутри папки фотографий создаётся папка My Lesson Camera и внутри неё создаётся папка Photos
        // это необходимо, чтобы отделить снятые видео от снятых фотографий и в общем снятые через это приложение
        // файлы от других файлов в памяти устройства
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "My Lesson Camera/Photos");

        // Проверить удалось ли создать указанные выше папки, если они не существуют
        if (!storageDir.exists() && !storageDir.mkdirs()) {
            // Вывыести ошибку в консоль Logcat если не получилось создать папки
            Log.e("TAG", "Problems creating directory");
        }

        // Создать новый временный файл, который затем планируется заменить фото полученным с камеры
        File image = File.createTempFile(fileName, ".jpg", storageDir);

        // Присвоить путь файла переменой mediaPath для дальнейшего оповещения
        // галереи о новом файле, для незамедлителього появления данного файла в галерее
        mediaPath = image.getAbsolutePath();

        // Верунть созденный временный файл вызвавшей данную функцию строке
        return image;
    }

    // Функция openVideo работает так же как openCamera
    void openVideo() {
        // Инициализация экземпляра камеры для снятия видео
        Intent videoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        File videoFile = null;

        try {
            videoFile = createVideoFile();
        } catch (Exception e)
        {
            e.printStackTrace();
        }

        if(videoFile != null)
        {
            Uri videoUri = FileProvider.getUriForFile(this,"com.example.android.fileprovider", videoFile);
            videoIntent.putExtra(MediaStore.EXTRA_OUTPUT, videoUri);
            // Запустить Activity и передать код запроса видео
            startActivityForResult(videoIntent, VCODE);
        }
    }

    // Данная функция работает аналогично функции createImageFile
    // отличается лишь путь сохранения видео и расширение видео
    File createVideoFile() throws IOException {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fileName = "VCAM_" + timestamp + "_";
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "My Lesson Camera/Videos");
        if (!storageDir.exists() && !storageDir.mkdirs()) {
            Log.e("TAG", "Problems creating directory");
        }
//        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File video = File.createTempFile(fileName, ".mp4", storageDir);

        mediaPath = video.getAbsolutePath();
        return video;
    }

    // Оповещение галереи о добавлении нового файла
    void galleryAddMedia() {
        // Получить ссылку на Intent проводящий сканирование медиафайлов в файловой системе
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        // Создать на основе пути созданного медиафайла
        // в функции createImageFile или createVideoFile
        File f = new File(mediaPath);
        // Получить уникальный идентификатор ресурса на основе файла
        Uri contentUri = Uri.fromFile(f);
        // Передать путь интенту для сканирования и добавления данного файла в галерею
        mediaScanIntent.setData(contentUri);
        // Отправить широковещательное сообщение с параметрами интента, которое затем
        // перехватывается необходимым системным процессом для обработки
        this.sendBroadcast(mediaScanIntent);
    }

    // Функция вызывается при возвращении значений с других Activity,
    // которые были запущены через функцию startActivityForResult
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Если возвращенный код равен коду запроса на снятие фото и код результата имеет статус RESULT_OK
        if (requestCode == RCODE && resultCode == RESULT_OK) {
            // Инициализировать переменную класса Bitmap используя полученные с камеры данные
            // в данном случае показывается миниатюра снятой фотографии
//            Bitmap photo = (Bitmap) data.getExtras().get("data");
            Drawable photo = Drawable.createFromPath(mediaPath);
            image.setImageDrawable(photo);
            // Указать полученное снятое фото в качестве источника ImageView
//            image.setImageBitmap(photo);
            galleryAddMedia();
        }
        // Если возвращенный код равен коду запроса на снятие видео и код результата имеет статус RESULT_OK
        if (requestCode == VCODE && resultCode == RESULT_OK) {
            // Инициализировать объект Uri (путь к видеофайлу) из полученных данных
            Uri videoUri = data.getData();
            // Установить в качестве источника VideoView полученный Uri (путь к видеофайлу)
            video.setVideoURI(videoUri);

            galleryAddMedia();

            // Запустить воспроизведение снятого видео
            video.start();
        }
    }

    // Функция вызывается после запроса на разрешение использования системных ресурсов
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // Если код запроса равен коду запроса на снятие фото или видео
        if (requestCode == RCODE || requestCode == VCODE) {
            // Если в списке результатов ответа на запрос разрешения есть элементы, значит разрешение дано
            if (grantResults.length > 0) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    // Если код полученного разрешения равен коду запроса открытия камеры на съёмку фото
                    // открыть камеру в режиме записи видео
                    if (requestCode == RCODE) {
                        showCamera();
                    }
                    // Если код полученного разрешения равен коду запроса открытия камеры на съёмку видео
                    // открыть камеру в режиме записи видео
                    if (requestCode == VCODE) {
                        openVideo();
                    }
                }
            }
        }
    }
}