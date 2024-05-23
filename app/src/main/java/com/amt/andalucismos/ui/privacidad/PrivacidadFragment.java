package com.amt.andalucismos.ui.privacidad;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.amt.andalucismos.R;

public class PrivacidadFragment extends Fragment {

    private WebView webView;

    public PrivacidadFragment() {

    } // constructor

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    } // onCreate()

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_privacidad, container, false);
        webView = view.findViewById(R.id.webView);
        webView.loadUrl("file:///android_asset/Politica_de_Privacidad_Andalucismos.html");
        return view;
    } // onCreateView()
}