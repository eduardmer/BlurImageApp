package com.blur_image_app.workers;

import android.content.Context;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.blur_image_app.Constants;
import java.io.File;

public class CleanTemporaryFilesWorker extends Worker {


    public CleanTemporaryFilesWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context applicationContext = getApplicationContext();

        try {
            File outputDirectory = new File(applicationContext.getFilesDir(), Constants.OUTPUT_PATH);
            if (outputDirectory.exists()) {
                File[] entries = outputDirectory.listFiles();
                if (entries != null && entries.length > 0) {
                    for (File entry : entries) {
                        String name = entry.getName();
                        if (!TextUtils.isEmpty(name) && name.endsWith(".png")) {
                            entry.delete();
                        }
                    }
                }
            }

            return Result.success();
        } catch (Exception exception) {
            return Result.failure();
        }
    }

}
