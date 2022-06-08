package com.blur_image_app;

import static com.blur_image_app.Constants.IMAGE_MANIPULATION_WORK_NAME;
import static com.blur_image_app.Constants.KEY_IMAGE_URI;
import static com.blur_image_app.Constants.TAG_OUTPUT;
import android.app.Application;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import androidx.databinding.ObservableField;
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
import javax.inject.Inject;
import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class BlurViewModel extends ViewModel {

    private final WorkManager workManager;
    public final LiveData<List<WorkInfo>> workInfo;
    public final ObservableField<Uri> selectedImage;

    @Inject
    public BlurViewModel(Application application){
        this.workManager = WorkManager.getInstance(application);
        workInfo = workManager.getWorkInfosByTagLiveData(TAG_OUTPUT);
        selectedImage = new ObservableField<>();
    }

    public void applyBlur(int blurLevel){
        if (selectedImage.get() == null)
            return;
        OneTimeWorkRequest.Builder workRequest = new OneTimeWorkRequest.Builder(BlurImageWorker.class).setInputData(createInputDataForUri());
        if (blurLevel == 1)
            workRequest.addTag(TAG_OUTPUT);

        WorkContinuation workContinuation = workManager.beginUniqueWork(IMAGE_MANIPULATION_WORK_NAME, ExistingWorkPolicy.KEEP, workRequest.build());
        for (int i=2 ; i <= blurLevel ; i++) {
            OneTimeWorkRequest.Builder blurImageBuilder = new OneTimeWorkRequest.Builder(BlurImageWorker.class);
            if (i == blurLevel)
                blurImageBuilder.addTag(TAG_OUTPUT);
            workContinuation = workContinuation.then(blurImageBuilder.build());
        }

        workContinuation.enqueue();
    }

    private Data createInputDataForUri() {
        Data.Builder builder = new Data.Builder();
        if (selectedImage.get() != null)
            builder.putString(KEY_IMAGE_URI, selectedImage.get().toString());
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

    public void showSelectedImage(Intent data){
        try{
            selectedImage.set(data.getData());
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

}
