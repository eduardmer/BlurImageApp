package com.blur_image_app;

import static com.blur_image_app.Constants.KEY_IMAGE_URI;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.work.WorkInfo;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import com.blur_image_app.databinding.ActivityMainBinding;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    BlurViewModel viewModel;
    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        viewModel = new ViewModelProvider(this).get(BlurViewModel.class);

        binding.goButton.setOnClickListener(v -> viewModel.applyBlur(getBlurLevel()));

        viewModel.workInfo.observe(this, workInfos -> {

            if (workInfos == null || workInfos.isEmpty())
                return;

            WorkInfo workInfo = workInfos.get(0);

            boolean isFinished = workInfo.getState().isFinished();

            if (isFinished){
                showWorkFinished();
                String outputUri = workInfo.getOutputData().getString(KEY_IMAGE_URI);
                if (!outputUri.isEmpty())
                    binding.imageView.setImageURI(Uri.parse(outputUri));

            }
            else
                showWorkInProgress();
        });
    }

    private void showWorkInProgress() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.cancelButton.setVisibility(View.VISIBLE);
        binding.goButton.setVisibility(View.GONE);
        binding.seeFileButton.setVisibility(View.GONE);
    }

    private void showWorkFinished() {
        binding.progressBar.setVisibility(View.GONE);
        binding.cancelButton.setVisibility(View.GONE);
        binding.goButton.setVisibility(View.VISIBLE);
    }

    private int getBlurLevel(){
        switch (binding.radioGroupBlur.getCheckedRadioButtonId()){
            case R.id.blur_lvl_1:
                return 1;
            case R.id.blur_lvl_2:
                return 2;
            case R.id.blur_lvl_3:
                return 3;
        }
        return 1;
    }
}