package com.example.auctionhouse.ui.about;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.auctionhouse.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class AboutFragment extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_about, container, false);
        root.setBackgroundColor(getResources().getColor(R.color.colorFragments));

        final TextView textView = root.findViewById(R.id.text);
        textView.setText(R.string.about_page);

        final TextView textView1 = root.findViewById(R.id.text1);
        textView1.setText(R.string.about_page1);

        FloatingActionButton fab = getActivity().findViewById(R.id.fab);
        fab.hide();

        return root;
    }
}
