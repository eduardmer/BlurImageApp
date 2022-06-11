package com.blur_image_app.workers;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.blur_image_app.Constants;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SaveImageWorker extends Worker {

    private static final String TITLE = "image";
    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss z", Locale.getDefault());

    public SaveImageWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context applicationContext = getApplicationContext();

        ContentResolver resolver = applicationContext.getContentResolver();
        try {
            String resourceUri = getInputData().getString(Constants.KEY_IMAGE_URI);
            Bitmap bitmap = BitmapFactory.decodeStream(resolver.openInputStream(Uri.parse(resourceUri)));
            String outputUri = MediaStore.Images.Media.insertImage(resolver, bitmap, TITLE, DATE_FORMATTER.format(new Date()));
            if (TextUtils.isEmpty(outputUri)) {
                return Result.failure();
            }
            Data outputData = new Data.Builder()
                    .putString(Constants.KEY_IMAGE_URI, outputUri)
                    .build();
            return Result.success(outputData);
        } catch (Exception exception) {
            return Worker.Result.failure();
        }
    }
}
