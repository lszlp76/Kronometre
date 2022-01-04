package com.zlpls.kronometre;

import static com.google.android.material.math.MathUtils.floorMod;

import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.zlpls.kronometre.ui.main.PageViewModel;
import com.zlpls.kronometre.ui.main.SectionsPagerAdapter;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;


public class TimerFragment extends Fragment {
    TextView textView, textView2, maxvalue, minvalue,
            lapTotal, avevalue, minvalcmin, maxvalcmin, avevalcmin, lapnoMin, lapnoMax;
    Button button2, button1, button3, button4, button;
    RadioButton radioButton, minuteButton, cminuteButton;
    Runnable runnable;// bir işlemi belirli periyodda yapamya yarar
    Handler handler; // runnable ile çalışmaya yarar
    RadioGroup radioGroup;
    TableLayout tableLayout;
    boolean go; // uygulama çalışma izini. radiobutonlar seçili değilse başlamaz
    int number;
    Toolbar toolbar;
    String unit; //birim
    String
            second = null, minute = null, hour = null;
    String timer = "00:00:00";
    String departure = "---";
    int modul; // modul saniye ,cminute olacak değer. saniye için 60, cminute için 100 olmalı
    int milis; // saniye için 1000, cm,müte için 600 olmalı
    int m;// dakika
    int h;// saat
    String hh, mm, ss;
    int scm = 0;// chronos modu seçimi . Açılışta sıfır olmalı
    int lapsayisi = 0;
    ArrayList<String> laps = new ArrayList<String>(); // bu lapların olduğu dizi
    DecimalFormat dec = new DecimalFormat("#0.00");
    boolean Auth;
    ArrayList<Double> lapsval = new ArrayList<Double>(); // bu cycle dizisi
    String Timeunit;
    int lapnomax = 0, lapnomin = 0;
    double min, max, ave;
    PageViewModel pageViewModel;
    SectionsPagerAdapter viewPager;
    ExcelSave excelSave = new ExcelSave();
    boolean isNoPressed ; // resetleme kutusu çıktığında No'ya basıldı bilgisi MainActivity'e gitmeli.O nedenle bu var.
    /*** operasyonel fonksiyonlar***/

    private RadioGroup.OnCheckedChangeListener onCheckedChangeListener = new RadioGroup.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(RadioGroup radioGroup, int i) {

            switch (i) {
                case R.id.cminuteButton:
                    // if (checked)
                    //if windows phone programming book is selected
                    //set the checked radio button's text style bold italic

                    cminuteButton.setTypeface(null, Typeface.BOLD_ITALIC);
                    //set the other two radio buttons text style to default
                    minuteButton.setTypeface(null, Typeface.NORMAL);
                    modul = 100;
                    milis = 600;
                    unit = "Cmin./cyc";
                    Timeunit = "Cmin. - Hunderedth Minute";
                    // Toast.makeText(getApplicationContext(),cminuteButton.getText(),Toast.LENGTH_SHORT).show();
                    go = true;
                    break;
                case R.id.minuteButton:
                    // if (checked)
                    minuteButton.setTypeface(null, Typeface.BOLD_ITALIC);
                    cminuteButton.setTypeface(null, Typeface.NORMAL);
                    milis = 1000;
                    modul = 60;
                    unit = "sec./cyc";
                    Timeunit = "Sec. - Second ";
                    // Toast.makeText(getApplicationContext(),minuteButton.getText(),Toast.LENGTH_SHORT).show();
                    go = true;
                    // go her iki durumda da true oluyor
                    break;
            }


        }
    };

    public static TimerFragment newInstance() {
        return new TimerFragment();
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


        return inflater.inflate(R.layout.fragment_timer, container, false);


    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        /**** ESAS HESAPLAYICI KOD *****/


        tableLayout = view.findViewById(R.id.tableLayout);
        textView = view.findViewById(R.id.textView);
        textView2 = view.findViewById(R.id.textView2);
        button2 = view.findViewById(R.id.button2);
        button1 = view.findViewById(R.id.button);
        button3 = view.findViewById(R.id.button3);
        button4 = view.findViewById(R.id.button4);
        button = view.findViewById(R.id.button);// save butonu
        maxvalue = view.findViewById(R.id.maxval);
        minvalue = view.findViewById(R.id.minval);
        avevalue = view.findViewById(R.id.aveval);
        minvalcmin = view.findViewById(R.id.minvalcmin);
        maxvalcmin = view.findViewById(R.id.maxvalcmin);
        avevalcmin = view.findViewById(R.id.avevalcmin);
        lapnoMax = view.findViewById(R.id.lapnomax);
        lapnoMin = view.findViewById(R.id.lapnomin);
        lapTotal = view.findViewById(R.id.laptotal);

        //minuteButton =view.findViewById(R.id.minuteButton);
        //cminuteButton=view.findViewById(R.id.cminuteButton);
        radioGroup = view.findViewById(R.id.radioGroup);
        radioGroup.setOnCheckedChangeListener(onCheckedChangeListener);
        //modul = 60; // bu kısım radio button olacak
        //milis = 1000;
        textView.setText(timer);

        button3.setEnabled(false);// lap tuşu kapanıyor

        button.setEnabled(false);// save butonu kapalı
        button4.setEnabled(false);// reset butonu kapalı v1.nci release hata verdi.

        Auth = true;// açılışta auth true olmalı
        // START STOP butonu yazılması --> button2 değişecek


        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (modul > 0) {
                    if (Auth == true) {
                        button2.setText("STOP");

                        start();
                        button.setEnabled(false);// save butonu kapalı
                        button4.setEnabled(false);// reset butonu kapalı
                    } else {
                        button2.setText("START");

                        stop();
                        button.setEnabled(true);// save butonu açık
                        button4.setEnabled(true);// reset butonu açık
                    }
                } else {
                    Toast.makeText(getContext(), "Choose time unit !", Toast.LENGTH_SHORT).show();
                }
            }
        });
        minuteButton = (RadioButton) view.findViewById(R.id.minuteButton);
        cminuteButton = (RadioButton) view.findViewById(R.id.cminuteButton);

        isNoPressed = false;// ilk olarak resetlemede kullanılacak.
// reset butonuna basılınca olanlar
        button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reset();
               /* handler.removeCallbacks(runnable);
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Delete All Datas");
                builder.setMessage("Are you sure to delete all datas ?");

                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    *//* RESET düğmesi çıktığı zaman No ya basınca
                    Start'a basılmış gibi olmalı
                     *//*
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (Auth == true) {
                            button2.setText("STOP");

                            //start();
                            //button.setEnabled(false);// save butonu kapalı
                            //button4.setEnabled(false);// reset butonu kapalı
                        } else {
                            button2.setText("START");

                            stop();
                            button.setEnabled(true);// save butonu açık
                            button4.setEnabled(true);// reset butonu açık
                        }
                    }
                });
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (go == true) {
                            reset();
                        } else {

                        }
                    }
                });
                builder.show();*/

            }
        });
//lap butonuna basılınca olanlar
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takeLap();
                //yeni fonksiyon olarak yazdım lap almayı
              /*  //System.out.println("Modul değeri-->"+modul);
                int deltah = 0;
                int deltam = 0;
                int i = 0;
                double delta1, delta0;
                double deltas = 0;
                double delta;

                double sum = 0;



                // laps dizisinde string olarak tutulan ifadelerin sayıya çevrilmiş olarak tutuldukları dizi
                String lap = hh + ":" + mm + ":" + ss;
                laps.add(lap);


                textView2.setMovementMethod(new ScrollingMovementMethod());
                if (lapsayisi > 0) {


                    second = laps.get(lapsayisi - 1).substring(6, 8);
                    minute = laps.get(lapsayisi - 1).substring(3, 5);
                    hour = laps.get(lapsayisi - 1).substring(0, 2);

                    delta1 = (Integer.parseInt(hh) / 60 + Integer.parseInt(mm) + Double.valueOf(Integer.parseInt(ss)) / Double.valueOf(modul));
                    delta0 = (Integer.parseInt(hour) / 60 + Integer.parseInt(minute) + Double.valueOf(Integer.parseInt(second)) / Double.valueOf(modul));

                    // deltah = Math.abs(Integer.parseInt(hh)/60 - Integer.parseInt(hour)/60);
                    // deltam = Math.abs(Integer.parseInt(mm) - Integer.parseInt(minute));
                    // deltas = Math.abs(Integer.parseInt(ss)/modul - Integer.parseInt(second)/modul);
                    delta = Math.abs(delta1 - delta0);
                    lapsval.add(delta);

                } else {
                    // ilk lap değeri
                    second = laps.get(lapsayisi).substring(6, 8);
                    minute = laps.get(lapsayisi).substring(3, 5);
                    hour = laps.get(lapsayisi).substring(0, 2);
                    delta0 = (Integer.parseInt(hour) / 60 + Integer.parseInt(minute) + Double.valueOf(Integer.parseInt(second)) / Double.valueOf(modul));
                    delta = delta0 ;
                    lapsval.add(delta0);

                }
                // laps değerlerini sayıya çevirme

                if (laps.size() > 0) {
                    while (i < lapsval.size()) {
                        sum = (lapsval.get(i) + sum);


                        //second = laps.get(i).substring(6, 8);
                        //minute = laps.get(i).substring(3, 5);
                        //hour = laps.get(i).substring(0, 2);
                        //lapsval[i] = (Integer.parseInt(hour) / 60 + Integer.parseInt(minute) + Double.valueOf(Integer.parseInt(second)) / Double.valueOf(modul));
                        i++;
                    }
                    // lapsval dizisindeki max ve min değerleri bulmak

                    // DoubleSummaryStatistics stat = Arrays.stream(lapsval).summaryStatistics();
             *//*
                double max =stat.getMax();
             double min =stat.getMin();
             double ave = stat.getAverage();
             double sum= stat.getSum();
*//*

                    min = Collections.min(lapsval);
                    lapnomin = lapsval.indexOf(min) + 1;// 0 yazmaması için
                    max = Collections.max(lapsval);
                    lapnomax = lapsval.indexOf(max) + 1;

                    ave = sum / lapsval.size();

                    // tablodaki yerine yazdırmak
                    maxvalue.setText(dec.format(max*60)); // saniye olarak yazıyor
                    maxvalcmin.setText(dec.format(max * 100));
                    minvalcmin.setText(dec.format(min * 100));
                    minvalue.setText(dec.format(min * 60));
                    avevalue.setText(dec.format(ave*60));
                    avevalcmin.setText(dec.format(ave * 100));
                    lapnoMin.setText(String.valueOf(lapnomin));
                    lapnoMax.setText(String.valueOf(lapnomax));
                    lapTotal.setText(String.valueOf(lapsval.size()));
                }
                // EN SON YAZILAN EN ÜSTE  version
                textView2.setText( "Lap " + (lapsayisi + 1) + ": " + lap + "  Cyc.Time: " + dec.format(delta* modul) +" " + unit + "\n" + textView2.getText().toString());
        *//* BU KISIM EN SON YAZILANI EN ALTA ATIYOR
        textView2.append("Lap " + (lapsayisi + 1) + ": " + lap + "  Cyc.Time: " + dec.format(delta) + " min/cyc ");
        textView2.append(System.getProperty("line.separator"));*//*
                //textView2.setSingleLine(true);
                lapsayisi++;



                    pageViewModel.setTimeValue((lapsval.get(lapsayisi - 1)).floatValue() * modul);
                    pageViewModel.setMaxTimeValue((float)(max*modul));
                    pageViewModel.setMinTimeValue((float) (min*modul));
                    pageViewModel.setAvgTimeValue((float)(ave*modul));


                pageViewModel.setIndex(lapsayisi);// lapsayıbilgisini gönderiyor

                //System.out.println("deneme değeri "+pageViewModel.getTimeValue().getValue());
               // System.out.println("deneme lapsayısı "+lapsval.get(lapsayisi-1));
            }*/
            }
        });

    }

    public void takeLap() {
        //System.out.println("Modul değeri-->"+modul);
        int deltah = 0;
        int deltam = 0;
        int i = 0;
        double delta1, delta0;
        double deltas = 0;
        double delta;

        double sum = 0;


        // laps dizisinde string olarak tutulan ifadelerin sayıya çevrilmiş olarak tutuldukları dizi
        String lap = hh + ":" + mm + ":" + ss;
        laps.add(lap);


        textView2.setMovementMethod(new ScrollingMovementMethod());
        if (lapsayisi > 0) {


            second = laps.get(lapsayisi - 1).substring(6, 8);
            minute = laps.get(lapsayisi - 1).substring(3, 5);
            hour = laps.get(lapsayisi - 1).substring(0, 2);

            delta1 = (Integer.parseInt(hh) / 60 + Integer.parseInt(mm) + Double.valueOf(Integer.parseInt(ss)) / Double.valueOf(modul));
            delta0 = (Integer.parseInt(hour) / 60 + Integer.parseInt(minute) + Double.valueOf(Integer.parseInt(second)) / Double.valueOf(modul));

            // deltah = Math.abs(Integer.parseInt(hh)/60 - Integer.parseInt(hour)/60);
            // deltam = Math.abs(Integer.parseInt(mm) - Integer.parseInt(minute));
            // deltas = Math.abs(Integer.parseInt(ss)/modul - Integer.parseInt(second)/modul);
            delta = Math.abs(delta1 - delta0);
            lapsval.add(delta);

        } else {
            // ilk lap değeri
            second = laps.get(lapsayisi).substring(6, 8);
            minute = laps.get(lapsayisi).substring(3, 5);
            hour = laps.get(lapsayisi).substring(0, 2);
            delta0 = (Integer.parseInt(hour) / 60 + Integer.parseInt(minute) + Double.valueOf(Integer.parseInt(second)) / Double.valueOf(modul));
            delta = delta0;
            lapsval.add(delta0);

        }
        // laps değerlerini sayıya çevirme

        if (laps.size() > 0) {
            while (i < lapsval.size()) {
                sum = (lapsval.get(i) + sum);


                //second = laps.get(i).substring(6, 8);
                //minute = laps.get(i).substring(3, 5);
                //hour = laps.get(i).substring(0, 2);
                //lapsval[i] = (Integer.parseInt(hour) / 60 + Integer.parseInt(minute) + Double.valueOf(Integer.parseInt(second)) / Double.valueOf(modul));
                i++;
            }
            // lapsval dizisindeki max ve min değerleri bulmak

            // DoubleSummaryStatistics stat = Arrays.stream(lapsval).summaryStatistics();
             /*
                double max =stat.getMax();
             double min =stat.getMin();
             double ave = stat.getAverage();
             double sum= stat.getSum();
*/

            min = Collections.min(lapsval);
            lapnomin = lapsval.indexOf(min) + 1;// 0 yazmaması için
            max = Collections.max(lapsval);
            lapnomax = lapsval.indexOf(max) + 1;

            ave = sum / lapsval.size();

            // tablodaki yerine yazdırmak
            maxvalue.setText(dec.format(max * 60)); // saniye olarak yazıyor
            maxvalcmin.setText(dec.format(max * 100));
            minvalcmin.setText(dec.format(min * 100));
            minvalue.setText(dec.format(min * 60));
            avevalue.setText(dec.format(ave * 60));
            avevalcmin.setText(dec.format(ave * 100));
            lapnoMin.setText(String.valueOf(lapnomin));
            lapnoMax.setText(String.valueOf(lapnomax));
            lapTotal.setText(String.valueOf(lapsval.size()));
        }
        // EN SON YAZILAN EN ÜSTE  version
        textView2.setText("Lap " + (lapsayisi + 1) + ": " + lap + "  Cyc.Time: " + dec.format(delta * modul) + " " + unit + "\n" + textView2.getText().toString());
        /* BU KISIM EN SON YAZILANI EN ALTA ATIYOR
        textView2.append("Lap " + (lapsayisi + 1) + ": " + lap + "  Cyc.Time: " + dec.format(delta) + " min/cyc ");
        textView2.append(System.getProperty("line.separator"));*/
        //textView2.setSingleLine(true);
        lapsayisi++;


        pageViewModel.setTimeValue((lapsval.get(lapsayisi - 1)).floatValue() * modul);
        pageViewModel.setMaxTimeValue((float) (max * modul));
        pageViewModel.setMinTimeValue((float) (min * modul));
        pageViewModel.setAvgTimeValue((float) (ave * modul));
        pageViewModel.setIndex(lapsayisi);// lapsayıbilgisini gönderiyor

        //System.out.println("deneme değeri "+pageViewModel.getTimeValue().getValue());
        // System.out.println("deneme lapsayısı "+lapsval.get(lapsayisi-1));
    }

    public void save (){
        // activity nin context i için getActivity kullan
      excelSave.save(getActivity(),unit,laps,lapsval,ave,modul);
        //excelSave.deneme(getActivity());
    }


    public void start() {
      Auth = false;
        // buraya number=XX yazrsan o saniyeden başlatabilirsin
       // mainden çalıştıracaksan if i bunu kaldır
       //if (button2.getText() == "STOP")

        {
            if (modul > 0) {
                cminuteButton.setEnabled(false);
                minuteButton.setEnabled(false);
                button3.setEnabled(true); // lap tuşu açılıyor
                handler = new Handler();
                runnable = new Runnable() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void run() {
                        // buradaki her şey belirtilen periyyotta olaaktır.
                        number++;
                        if (number > 0 && floorMod(number, modul) == 0) {
                            m++;
                            number = 0;
                        }
                        if (m >= 60) { //dakika 1e eşit ve büyükse ve 60 olursa
                            h++;
                            m = 0;

                        }
                        hh = h < 10 ? "0" + h : h + "";
                        mm = m < 10 ? "0" + m : m + "";
                        ss = number < 10 ? "0" + Math.floorMod(number, modul) : Math.floorMod(number, modul) + "";

                        textView.setText(hh + ":" + mm + ":" + ss);

                        handler.postDelayed(runnable, milis);

                    }
                };
                handler.post(runnable);
                //button2.setEnabled(false);
            } else {
                Toast.makeText(getContext(), "Choose time unit!", Toast.LENGTH_SHORT).show();
            }
        }
        ;


    }

   public void stop() {
        Auth = true;
        //button2.setEnabled(true);
        handler.removeCallbacks(runnable);
        ; // double serisi verirken içine kaç tane yazacağını belirt

    }
public void reset() {
    handler.removeCallbacks(runnable);
    if (go == true) {

        Auth = true;
        button4.setEnabled(false); //dataları sildikten sonra butonu kapat v1.nci releasedeki hatadan dolayı
        button2.setText("START");


        textView2.setText("");

        m = 0;
        h = 0;
        number = 0;

        //handler.removeCallbacks(runnable);
        textView.setText(timer);
        cminuteButton.setEnabled(true);
        minuteButton.setEnabled(true);
        button2.setEnabled(true);// start tuşu açılıyor
        button3.setEnabled(false);// lap tuşu kapanıyor
        button.setEnabled(false);
        lapsayisi = 0;
        laps.clear(); // lapları siliyor
        lapsval.clear();// aralık değerlerini siliyor
        maxvalue.setText(departure);
        maxvalcmin.setText(departure);
        minvalcmin.setText(departure);
        minvalue.setText(departure);
        avevalue.setText(departure);
        avevalcmin.setText(departure);
        lapnoMin.setText(departure);
        lapnoMax.setText(departure);
        lapTotal.setText(departure);
        ;


    } else {

    }

}
    public void reset2() {

        handler.removeCallbacks(runnable);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Delete All Datas");
        builder.setMessage("Are you sure to delete all datas ?");

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            /* RESET düğmesi çıktığı zaman No ya basınca
            Start'a basılmış gibi olmalı
             */
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (Auth == true) {
                    button2.setText("STOP");

                    // No ya basıldı.

                    //start();
                    //button.setEnabled(false);// save butonu kapalı
                    //button4.setEnabled(false);// reset butonu kapalı
                } else {
                    button2.setText("START");

                   // No ya basıldı.
                    stop();
                    button.setEnabled(true);// save butonu açık
                    button4.setEnabled(true);// reset butonu açık
                }
            }
        });
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (go == true) {

                    Auth = true;
                    button4.setEnabled(false); //dataları sildikten sonra butonu kapat v1.nci releasedeki hatadan dolayı
                    button2.setText("START");


                    textView2.setText("");

                    m = 0;
                    h = 0;
                    number = 0;

                    //handler.removeCallbacks(runnable);
                    textView.setText(timer);
                    cminuteButton.setEnabled(true);
                    minuteButton.setEnabled(true);
                    button2.setEnabled(true);// start tuşu açılıyor
                    button3.setEnabled(false);// lap tuşu kapanıyor
                    button.setEnabled(false);
                    lapsayisi = 0;
                    laps.clear(); // lapları siliyor
                    lapsval.clear();// aralık değerlerini siliyor
                    maxvalue.setText(departure);
                    maxvalcmin.setText(departure);
                    minvalcmin.setText(departure);
                    minvalue.setText(departure);
                    avevalue.setText(departure);
                    avevalcmin.setText(departure);
                    lapnoMin.setText(departure);
                    lapnoMax.setText(departure);
                    lapTotal.setText(departure);;



                } else {

                }
            }
        });
        builder.show();


    }


}
/*
 public void start() {
            Auth = false;
            // buraya number=XX yazrsan o saniyeden başlatabilirsin
            if (button2.getText() == "STOP") {
                if (modul > 0) {
                    cminuteButton.setEnabled(false);
                    minuteButton.setEnabled(false);
                    button3.setEnabled(true); // lap tuşu açılıyor
                    handler = new Handler();
                    runnable = new Runnable() {
                        @RequiresApi(api = Build.VERSION_CODES.N)
                        @Override
                        public void run() {
                            // buradaki her şey belirtilen periyyotta olaaktır.
                            number++;
                            if (number > 0 && floorMod(number, modul) == 0) {
                                m++;
                                number = 0;
                            }
                            if (m >= 60) { //dakika 1e eşit ve büyükse ve 60 olursa
                                h++;
                                m = 0;

                            }
                            hh = h < 10 ? "0" + h : h + "";
                            mm = m < 10 ? "0" + m : m + "";
                            ss = number < 10 ? "0" + Math.floorMod(number, modul) : Math.floorMod(number, modul) + "";

                            textView.setText(hh + ":" + mm + ":" + ss);

                            handler.postDelayed(runnable, milis);

                        }
                    };
                    handler.post(runnable);
                    //button2.setEnabled(false);
                } else {
                    Toast.makeText(getContext(), "Choose your chrono type!", Toast.LENGTH_SHORT).show();
                }
            }
            ;


        }

        public void reset() {
            Auth = true;
            button4.setEnabled(false); //dataları sildikten sonra butonu kapat v1.nci releasedeki hatadan dolayı
            button2.setText("START");


            textView2.setText("");

            m = 0;
            h = 0;
            number = 0;

            //handler.removeCallbacks(runnable);
            textView.setText(timer);
            cminuteButton.setEnabled(true);
            minuteButton.setEnabled(true);
            button2.setEnabled(true);// start tuşu açılıyor
            button3.setEnabled(false);// lap tuşu kapanıyor
            button.setEnabled(false);
            lapsayisi = 0;
            laps.clear(); // lapları siliyor
            lapsval.clear();// aralık değerlerini siliyor
            maxvalue.setText(departure);
            maxvalcmin.setText(departure);
            minvalcmin.setText(departure);
            minvalue.setText(departure);
            avevalue.setText(departure);
            avevalcmin.setText(departure);
            lapnoMin.setText(departure);
            lapnoMax.setText(departure);
            lapTotal.setText(departure);

        }


        public void stop() {
            Auth = true;
            //button2.setEnabled(true);
            handler.removeCallbacks(runnable);
            ; // double serisi verirken içine kaç tane yazacağını belirt


        }
 */