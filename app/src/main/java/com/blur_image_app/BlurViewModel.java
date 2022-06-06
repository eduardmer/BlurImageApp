package com.blur_image_app;

import static com.blur_image_app.Constants.IMAGE_MANIPULATION_WORK_NAME;
import static com.blur_image_app.Constants.KEY_IMAGE_URI;
import android.app.Application;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkContinuation;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import com.blur_image_app.workers.BlurImageWorker;
import java.util.List;
import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class BlurViewModel extends ViewModel {

    private Uri mImageUri;
    private final WorkManager workManager;
    private final LiveData<List<WorkInfo>> workInfo;

    public BlurViewModel(Application application){
        this.workManager = WorkManager.getInstance(application);
        mImageUri = getImageUri(application.getApplicationContext());
        workInfo = workManager.getWorkInfosForUniqueWorkLiveData(IMAGE_MANIPULATION_WORK_NAME);
    }

    public void applyBlur(int blurLevel){
        OneTimeWorkRequest.Builder workRequest = new OneTimeWorkRequest.Builder(BlurImageWorker.class);
        workRequest.setInputData(createInputDataForUri());

        WorkContinuation workContinuation = workManager.beginUniqueWork(IMAGE_MANIPULATION_WORK_NAME, ExistingWorkPolicy.KEEP, workRequest.build());
        for (int i=1 ; i <= blurLevel ; i++) {
            OneTimeWorkRequest.Builder blurImageBuilder = new OneTimeWorkRequest.Builder(BlurImageWorker.class);

            workContinuation = workContinuation.then(blurImageBuilder.build());
        }

        workContinuation.enqueue();
    }

    private Data createInputDataForUri() {
        Data.Builder builder = new Data.Builder();
        if (mImageUri != null) {
            builder.putString(KEY_IMAGE_URI, mImageUri.toString());
        }
        return builder.build();
    }

    private Uri getImageUri(Context context) {
        Resources resources = context.getResources();

        Uri imageUri = new Uri.Builder()
                .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
                .authority(resources.getResourcePackageName(R.drawable.android_cupcake))
                .appendPath(resources.getResourceTypeName(R.drawable.android_cupcake))
                .appendPath(resources.getResourceEntryName(R.drawable.android_cupcake))
                .build();

        return imageUri;
    }

}
