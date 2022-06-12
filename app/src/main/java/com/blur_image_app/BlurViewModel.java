package com.blur_image_app;

import static com.blur_image_app.Constants.IMAGE_MANIPULATION_WORK_NAME;
import static com.blur_image_app.Constants.KEY_IMAGE_URI;
import static com.blur_image_app.Constants.TAG_OUTPUT;
import android.app.Application;
import android.net.Uri;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkContinuation;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import com.blur_image_app.workers.BlurImageWorker;
import com.blur_image_app.workers.CleanTemporaryFilesWorker;
import com.blur_image_app.workers.SaveImageWorker;
import java.util.List;

public class BlurViewModel extends AndroidViewModel {

    private final WorkManager workManager;
    public final LiveData<List<WorkInfo>> workInfo;
    private final MutableLiveData<Uri> selectedImage;
    private boolean canObserveWork;
    private Uri imageUri;

    public BlurViewModel(Application application){
        super(application);
        this.workManager = WorkManager.getInstance(application);
        workInfo = workManager.getWorkInfosByTagLiveData(TAG_OUTPUT);
        selectedImage = new MutableLiveData<>();
        canObserveWork = false;
    }

    public void applyBlur(int blurLevel){
        if (imageUri == null)
            return;

        canObserveWork = true;
        WorkContinuation workContinuation = workManager.beginUniqueWork(IMAGE_MANIPULATION_WORK_NAME, ExistingWorkPolicy.KEEP, OneTimeWorkRequest.from(CleanTemporaryFilesWorker.class));

        for (int i=1 ; i <= blurLevel ; i++) {
            OneTimeWorkRequest.Builder blurImageBuilder = new OneTimeWorkRequest.Builder(BlurImageWorker.class);
            if (i == 1)
                blurImageBuilder.setInputData(createInputDataForUri());
            workContinuation = workContinuation.then(blurImageBuilder.build());
        }

        workContinuation = workContinuation.then(new OneTimeWorkRequest.Builder(SaveImageWorker.class).addTag(TAG_OUTPUT).build());
        workContinuation.enqueue();
    }

    public void cancelWork(){
        workManager.cancelUniqueWork(IMAGE_MANIPULATION_WORK_NAME);
    }

    public boolean canObserveWork(){
        return canObserveWork;
    }

    public LiveData<Uri> getSelectedImage(){
        return selectedImage;
    }

    public void setSelectedImage(Uri uri){
        imageUri = uri;
        selectedImage.setValue(uri);
    }

    private Data createInputDataForUri() {
        Data.Builder builder = new Data.Builder();
        if (imageUri != null)
            builder.putString(KEY_IMAGE_URI, imageUri.toString());
        return builder.build();
    }

}
