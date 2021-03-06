package com.lszlp.choronometre.main;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class PageViewModel extends ViewModel {

    private MutableLiveData<Integer> mIndex = new MutableLiveData<>();//lapNumbers
    private MutableLiveData<Float> mTimeValue = new MutableLiveData<>();//lapValues
    private MutableLiveData<Float> mMaxTimeValue = new MutableLiveData<>();//maxcycletime
    private MutableLiveData<Float> mMinTimeValue = new MutableLiveData<>();//mincycletime
    private MutableLiveData<Float> mAvgTimeValue = new MutableLiveData<>();//avgcycletime
    private String mTimeUnit = new String();
    private MutableLiveData<String> mTimerValue = new MutableLiveData<>();
    public void setAvgTimeValue (float timeValue){
        mAvgTimeValue.setValue(timeValue);
    }
    public LiveData<Float> getAvgTimeValue() {
        return mAvgTimeValue;
    }

    public void setTimeUnit (String mTimeUnit){
        this.mTimeUnit = mTimeUnit;
    }
    public String getmTimeUnit(){
        return mTimeUnit;
    }
    public void setMaxTimeValue (float timeValue){
        mMaxTimeValue.setValue(timeValue);
    }
    public LiveData<Float> getMaxTimeValue() {
        return mMaxTimeValue;
    }
     public void setTimerValue(String timerValue){
         mTimerValue.setValue(timerValue);
     }
     public LiveData<String> getTimerValue(){
          return mTimerValue;
     }
    public void setMinTimeValue (float timeValue){
        mMinTimeValue.setValue(timeValue);
    }
    public LiveData<Float> getMinTimeValue() {
        return mMinTimeValue;
    }
    /*
    private LiveData<String> mText = Transformations.map(mIndex, new Function<Integer, String>() {
        @Override
        public String apply(Integer input) {
            return "Hello world from section: " + input;
        }
    });

    */
    public void setTimeValue (float timeValue){
        mTimeValue.setValue(timeValue);
    }
    public LiveData<Float> getTimeValue() {
        return mTimeValue;
    }
    public void setIndex(int index) {
        mIndex.setValue(index);
    }

    public LiveData<Integer> getIndex() {
        return mIndex;
    }

}