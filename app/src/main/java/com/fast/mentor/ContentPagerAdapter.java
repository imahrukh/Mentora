package com.fast.mentor;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import org.jspecify.annotations.NonNull;

public class ContentPagerAdapter extends FragmentStateAdapter {

    private final ModuleItem item;

    public ContentPagerAdapter(FragmentActivity fa, ModuleItem item) {
        super(fa);
        this.item = item;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return position == 0 ?
                VideoFragment.newInstance(item.getVideoId()) :
                PdfFragment.newInstance(item.getPdfUrl());
    }

    @Override
    public int getItemCount() {
        return 2; // Video + PDF tabs
    }
}