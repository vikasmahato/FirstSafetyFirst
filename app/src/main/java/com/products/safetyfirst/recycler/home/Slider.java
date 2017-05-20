package com.products.safetyfirst.recycler.home;

import android.support.v7.widget.RecyclerView;
import android.view.View;


import com.products.safetyfirst.R;

/**
 * Created by profileconnect on 12/04/17.
 */

public class Slider extends RecyclerView.ViewHolder {

    RecyclerView sliders;

    public Slider(View v) {
        super(v);
        sliders = (RecyclerView) v.findViewById(R.id.slider_holder);
    }

    public RecyclerView getSliders() {
        return sliders;
    }

    public void setSliders(RecyclerView sliders) {
        this.sliders = sliders;
    }
}