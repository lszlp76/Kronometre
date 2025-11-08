package com.lszlp.choronometre.main;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.lszlp.choronometre.Lap;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;

public class PageViewModel extends ViewModel {

    private MutableLiveData<Integer> mIndex = new MutableLiveData<>();//lapNumbers
    private MutableLiveData<Float> mTimeValue = new MutableLiveData<>();//lapValues
    private MutableLiveData<Float> mMaxTimeValue = new MutableLiveData<>();//maxcycletime
    private MutableLiveData<Float> mMinTimeValue = new MutableLiveData<>();//mincycletime
    private MutableLiveData<Float> mAvgTimeValue = new MutableLiveData<>();//avgcycletime
    private String mTimeUnit = new String();
    private int mPrecisionValue  ;
    private MutableLiveData<String> mTimerValue = new MutableLiveData<>();
    public void setAvgTimeValue (float timeValue){
        mAvgTimeValue.setValue(timeValue);
    }
    public LiveData<Float> getAvgTimeValue() {
        return mAvgTimeValue;
    }
   public String getmPrecisionValue  () {

        switch (mPrecisionValue) {
            case 0:
                return "0.0";
            case 1:
                return "0.00";
            case 2:
                return "0.000";
            default:
                return "#0.0";
        }
    }
    public void setmPrecisionValue (int mPrecisionValue){
        this.mPrecisionValue = mPrecisionValue;
    }
    public void setTimeUnit (String mTimeUnit){
        this.mTimeUnit = mTimeUnit;
    }
    public String getTimeUnit(){
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

    // Bu LiveData, bir tur silindiğinde ChartFragment'ı tetiklemek için kullanılacak.
// 'Boolean' değeri, basit bir "güncelleme oldu" sinyali göndermek için yeterlidir.
    private final MutableLiveData<Boolean> onLapDataChanged = new MutableLiveData<>();

    public LiveData<Boolean> getOnLapDataChanged() {
        return onLapDataChanged;
    }

    // TimerFragment, bir turu sildikten sonra bu metodu çağıracak.
    public void notifyLapDataChanged() {
        onLapDataChanged.setValue(true);
    }
    // Tüm tur listesini tutacak olan yeni LiveData
    private final MutableLiveData<ArrayList<Lap>> lapListLiveData = new MutableLiveData<>();
    // ChartFragment bu metodu kullanarak listeyi alacak
    public LiveData<ArrayList<Lap>> getLapList() {
        return lapListLiveData;
    }
    // TimerFragment bu metodu kullanarak güncel listeyi ViewModel'e yazacak
    public void setLapList(ArrayList<Lap> laps) {
        // Yeni bir ArrayList oluşturup kopyalamak, referans sorunlarını engeller.
        lapListLiveData.setValue(new ArrayList<>(laps));
    }

    // 1. Yeni bir LiveData ekleyin
    private final MutableLiveData<ArrayList<Lap>> lapsForChart = new MutableLiveData<>();

    // 2. Bu LiveData için bir getter oluşturun
    public LiveData<ArrayList<Lap>> getLapsForChart() {
        return lapsForChart;
    }

    // 3. Tur listesini güncelleyecek bir metod ekleyin
// Bu metod, TimerFragment'tan çağrılacak.
    public void updateLapsForChart(ArrayList<Lap> currentLaps) {
        if (currentLaps == null) {
            lapsForChart.setValue(new ArrayList<>());
            return;
        }
        // Grafik soldan sağa (1, 2, 3...) çizileceği için listenin ters çevrilmesi gerekir.
        // Çünkü lapsArray'de son tur en başta (indeks 0) yer alır.
        ArrayList<Lap> reversedList = new ArrayList<>(currentLaps);
        Collections.reverse(reversedList);
        lapsForChart.setValue(reversedList);
    }

}