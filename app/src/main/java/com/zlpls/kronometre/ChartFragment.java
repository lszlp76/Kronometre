package com.zlpls.kronometre;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.DefaultAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.zlpls.kronometre.ui.main.PageViewModel;

import java.util.ArrayList;
import java.util.List;


public class ChartFragment extends Fragment {

    LineChart lineChart;
    ArrayList<Entry> lineEntry,lapValue,tMaxValue,tMinValue,tAvgValue;

    PageViewModel pageViewModel;

    public static ChartFragment newInstance() {
        return new ChartFragment();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pageViewModel = new ViewModelProvider(requireActivity()).get(PageViewModel.class);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_chart, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        lineChart = view.findViewById(R.id.chart);
        //izlenecek değerler
        lineEntry = new ArrayList<>();
        tAvgValue = new ArrayList<>();
        tMinValue = new ArrayList<>();
        tMaxValue = new ArrayList<>();
        lapValue = new ArrayList<>();

        pageViewModel.getIndex().observe(requireActivity(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                /*
                ama her seferinde lap tuşuna basınca lineENTRY dizisine eklemesi lazım
                */
                DrawChart();
            }
        });

        //grafik için değişkenlerin oluşturulması
        //1-Grafik tipini tanıt
        //lineChart.invalidate();
        //2- *** X değerleri***/
        //3 ***Y değerleri ***/

        //4
        //lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);

        //5
        //6


    }
    public void ClearChart(){

                lineEntry.clear();
                tAvgValue.clear();
                tMinValue.clear();
                tMaxValue.clear();
                lapValue.clear();
                lineChart.invalidate();
                lineChart.clear();


    }
    private void DrawChart() {
        ;
/*
https://stackoverflow.com/questions/40999699/i-am-trying-to-make-values-of-x-axis-show-on-the-bottom-of-the-graph-not-the-top

 */

        Integer i = pageViewModel.getIndex().getValue();
        Float j = pageViewModel.getTimeValue().getValue();
        Float tmax =pageViewModel.getMaxTimeValue().getValue();
        Float tmin =pageViewModel.getMinTimeValue().getValue();
        Float tave =pageViewModel.getAvgTimeValue().getValue();

        System.out.println("zaman değeri -->" + pageViewModel.getTimeValue().getValue());
        System.out.println("lap değeri-->" + pageViewModel.getIndex().getValue());

        //lineEntry.add(new Entry(i, j));// burası aslında drawchart içinde olmalı.
        lapValue.add(new Entry(i,j)); //cycle time value

        tMaxValue.add(new Entry(i, tmax));
//sabit değer göstermek iiçin
        for (int y=0;y<i;y++) {
            tMaxValue.get(y).setY(tmax);

        };

        tMinValue.add(new Entry(i,tmin));
        for (int y=0;y<i;y++) {
            tMinValue.get(y).setY(tmin);

        };

        tAvgValue.add(new Entry(i,tave));
        for (int y=0;y<i;y++) {
            tAvgValue.get(y).setY(tave);

        };

        /**T avg***/
        LineDataSet tAvgValueDataSet = new LineDataSet(tAvgValue, "Avg.Cyc.Time Value");
        tAvgValueDataSet.setColor(Color.CYAN);
        tAvgValueDataSet.setDrawCircles(false);//daireleri çizme
        tAvgValueDataSet.setDrawValues(false);//değerleri gösterme
        tAvgValueDataSet.setValueTextSize(12);

       /** LAPS ***/
        LineDataSet lapValueDataSet = new LineDataSet(lapValue, "Cyc.Time Value");
        lapValueDataSet.setColor(Color.BLUE);
        lapValueDataSet.setValueTextSize(12);
        lapValueDataSet.setCircleColor(Color.GREEN);
        lapValueDataSet.setCircleRadius(5);

        lapValueDataSet.enableDashedLine(3,3,3);
        lapValueDataSet.isDashedLineEnabled();
        /*T max **/
        LineDataSet tMaxValueDataSet = new LineDataSet(tMaxValue, "Max.Cyc.Time Value");
        tMaxValueDataSet.setColor(Color.GREEN);
        tMaxValueDataSet.setDrawCircles(false);//daireleri çizme
        tMaxValueDataSet.setDrawValues(false);//değerleri gösterme
        tMaxValueDataSet.setValueTextSize(12);
        /**T MİN **/

        LineDataSet tMinValueDataSet = new LineDataSet(tMinValue, "Min.Cyc.Time Value");
        tMinValueDataSet.setColor(Color.RED);
        tMinValueDataSet.setDrawCircles(false);//daireleri çizme
        tMinValueDataSet.setDrawValues(false);//değerleri gösterme
tMinValueDataSet.setValueTextSize(12);


        /*final YAxis yAxis = lineChart.getAxisLeft();
        yAxis.setLabelCount(4,true);
        yAxis.setMaxWidth(3);
        yAxis.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
        yAxis.setValueFormatter(new DefaultAxisValueFormatter(2));
        yAxis.setTextColor(Color.WHITE);
        yAxis.setTextSize(15f); //not in your original but added
        yAxis.setGridColor(Color.argb(102,255,255,255));
        yAxis.setAxisLineColor(Color.TRANSPARENT);
        yAxis.setAxisMinimum(0); //not in your original but added

*/
        List<ILineDataSet> iLinedata = new ArrayList<ILineDataSet>();

        iLinedata.add(tAvgValueDataSet);
        iLinedata.add(lapValueDataSet);
        iLinedata.add(tMaxValueDataSet);
        iLinedata.add(tMinValueDataSet);

        WindowManager wm = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        System.out.println("X ölçüsü-->"+ width);


        LineData linedata = new LineData(iLinedata);
        lineChart.setData(linedata);
        lineChart.invalidate(); //refreshing the line chart

        lineChart.setBackgroundColor(getResources().getColor(R.color.colorAccent));
        //lineChart.animateXY(500, 500);
        Description des = lineChart.getDescription();
        des.setText("Cycle Time Chart ");
        des.setPosition(width/2,100);//yukarıdan width alıyor
        des.setTextSize(15);
        ;




    }
}
