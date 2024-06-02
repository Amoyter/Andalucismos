package com.amt.andalucismos.utils;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelStore;
import androidx.lifecycle.ViewModelStoreOwner;

public class ViewModelStoreOwnerSingleton implements ViewModelStoreOwner {
    private static ViewModelStoreOwnerSingleton instance;
    private ViewModelStore viewModelStore = new ViewModelStore();

    private ViewModelStoreOwnerSingleton() {}

    public static synchronized ViewModelStoreOwnerSingleton getInstance() {
        if (instance == null) {
            instance = new ViewModelStoreOwnerSingleton();
        }
        return instance;
    }

    @NonNull
    @Override
    public ViewModelStore getViewModelStore() {
        return viewModelStore;
    }
}
