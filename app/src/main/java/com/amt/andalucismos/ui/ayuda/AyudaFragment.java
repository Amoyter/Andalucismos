package com.amt.andalucismos.ui.ayuda;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.amt.andalucismos.R;

public class AyudaFragment extends Fragment {

    private WebView webViewAyuda;

    public AyudaFragment() {

    } // constructor

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    } // onCreate()

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ayuda, container, false);
        webViewAyuda = view.findViewById(R.id.webViewAyuda);
        webViewAyuda.loadUrl("file:///android_asset/Ayuda_Andalucismos.html");
        return view;
    } // onCreateView()
}