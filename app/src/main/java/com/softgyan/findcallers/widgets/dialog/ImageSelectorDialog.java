package com.softgyan.findcallers.widgets.dialog;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.softgyan.findcallers.R;

public class ImageSelectorDialog implements View.OnClickListener {
    private final BottomSheetBehavior<View> mBottomSheetBehavior;
    private final ImageSelectorCallback callback;
    private final View view;

    public ImageSelectorDialog(View view, ImageSelectorCallback callback) {
        mBottomSheetBehavior = BottomSheetBehavior.from(view);
        this.view = view;
        this.callback = callback;
        initViewComponent();
        Log.d("ImageSelectorDialog", "ImageSelectorDialog: called");
    }


    public BottomSheetBehavior<View> getBottomSheetBehavior() {
        return mBottomSheetBehavior;
    }

    private void initViewComponent() {
        ImageView ivCamera, ivGallery, ivRemove;
        ivCamera = view.findViewById(R.id.ivCamera);
        ivGallery = view.findViewById(R.id.ivGallery);
        ivRemove = view.findViewById(R.id.ivRemove);
        TextView tvClose = view.findViewById(R.id.tvClose);
        ivCamera.setOnClickListener(this);
        ivGallery.setOnClickListener(this);
        ivRemove.setOnClickListener(this);
        tvClose.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        if (id == R.id.ivCamera) {
            callback.onImageSelectResult(mBottomSheetBehavior, ImageSelectorCallback.CAMERA_CODE);
        } else if (id == R.id.ivGallery) {
            callback.onImageSelectResult(mBottomSheetBehavior, ImageSelectorCallback.GALLERY_CODE);
        } else if (id == R.id.ivRemove) {
            callback.onImageSelectResult(mBottomSheetBehavior, ImageSelectorCallback.REMOVE_CODE);
        } else if (id == R.id.tvClose) {
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        }
    }


    public interface ImageSelectorCallback {
        int CAMERA_CODE = 1;
        int GALLERY_CODE = 2;
        int REMOVE_CODE = 3;
        void onImageSelectResult(BottomSheetBehavior<View> sheetBehavior, int operationCode);

    }
}
