package com.mywork.bleapplication;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

public class TestResultsViewModel extends AndroidViewModel {
    BleRepository bleRepository;
    public TestResultsViewModel(@NonNull Application application) {
        super(application);
        bleRepository = BleRepository.getInstance();
    }
    private MutableLiveData<byte[]> values;

    public MutableLiveData<byte[]> getCurrentValue() {
        if (values == null) {
            values = new MutableLiveData<byte[]>();
        }
        return values;
    }
}
