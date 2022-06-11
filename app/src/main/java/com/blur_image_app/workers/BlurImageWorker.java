package com.blur_image_app.workers;

import static com.blur_image_app.Constants.KEY_IMAGE_URI;
import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class BlurImageWorker extends Worker {


    public BlurImageWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        try{
            Context applicationContext = getApplicationContext();

            String resourceUri = getInputData().getString(KEY_IMAGE_URI);

            if (TextUtils.isEmpty(resourceUri)) {
                throw new IllegalArgumentException("Invalid input uri");
            }

            ContentResolver resolver = applicationContext.getContentResolver();
            Bitmap picture = BitmapFactory.decodeStream(resolver.openInputStream(Uri.parse(resourceUri)));
            Bitmap output = WorkerUtils.blurBitmap(picture, applicationContext);
            Uri outputUri = WorkerUtils.writeBitmapToFile(applicationContext, output);
            Data outputData = new Data.Builder()
                    .putString(KEY_IMAGE_URI, outputUri.toString())
                    .build();

            return Result.success(outputData);
        }catch (Throwable throwable){
            return Result.failure();
        }
    }

}
