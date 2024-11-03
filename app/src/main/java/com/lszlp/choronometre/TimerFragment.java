package com.lszlp.choronometre;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.text.InputFilter;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.lszlp.choronometre.databinding.FragmentTimerBinding;
import com.lszlp.choronometre.main.PageViewModel;
import com.lszlp.choronometre.main.SectionsPagerAdapter;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Stack;

/**
 * https://www.android-examples.com/android-create-stopwatch-example-tutorial-in-android-studio/
 */

public class TimerFragment extends Fragment {
    FragmentTimerBinding binding;
    long MillisecondTime, StopTime, StartTime, TimeBuff, UpdateTime = 0L;
    int Hours, Seconds, Minutes, MilliSeconds;

    TextView  /*textView2*/
            maxvalue, minvalue, totalObservationTime,
            lapTotal, avevalue, minvalcmin, maxvalcmin, avevalcmin, lapnoMin, lapnoMax, cycPerHour, cycPerMinute;
    Button button2, button1, button3, button4, button;
    //ListView lapList;
    RecyclerView lapList;
    String lapToWrite;
    // String[] ListElements = new String[] {  };
    Lap lapValue;
    ArrayList<Lap> lapsArray = new ArrayList<Lap>();
    ArrayList<Lap> ListElementsArrayList;
    Stack<String> StackList;
    LapListAdapter lapListAdapter;
    // RadioButton radioButton, minuteButton, cminuteButton;
    Runnable runnable;// bir işlemi belirli periyodda yapamya yarar
    Handler handler; // runnable ile çalışmaya yarar
    //RadioGroup radioGroup;
    TableLayout tableLayout;
    boolean go; // uygulama çalışma izini. radiobutonlar seçili değilse başlamaz
    int number, micro;
    Toolbar toolbar;
    String unit, diffTime; //birim
    String
            second = null, minute = null, hour = null;
    String timer = "00:00:00.000";
    String departure = "---";
    int modul; // modul saniye ,cminute olacak değer. saniye için 60, cminute için 100 olmalı
    int milis; // saniye için 1000, cm,müte için 600 olmalı
    int m;// dakika
    int h;// saat
    String hh, mm, ss, msec;
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
    List<String> saveValue;

    String currentDateandTimeStop;
    String currentDateandTimeStart;
    Date timeStop, timeStart;
    boolean isNoPressed; // resetleme kutusu çıktığında No'ya basıldı bilgisi MainActivity'e gitmeli.O nedenle bu var.
    //Typeface tf;
    private DateFormat df = new SimpleDateFormat("ddMMyy@HHmm");
    private DateFormat df2 = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss ", Locale.ENGLISH);
    private String m_Text = "";

    /*** operasyonel fonksiyonlar***/
/*
    private RadioGroup.OnCheckedChangeListener onCheckedChangeListener = new RadioGroup.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(RadioGroup radioGroup, int i) {

            switch (i) {
                case R.id.cminuteButton:
                    // if (checked)
                    //if windows phone programming book is selected
                    //set the checked radio button's text style bold italic
                  // cminuteButton.setTypeface(tf, Typeface.BOLD_ITALIC);
                    //set the other two radio buttons text style to default
                 //   minuteButton.setTypeface(tf, Typeface.NORMAL);
                    modul = 100;
                    milis = 600;
                    unit = "Cmin";
                    Timeunit = "Cmin. - Hunderedth Minute";
                    // Toast.makeText(getApplicationContext(),cminuteButton.getText(),Toast.LENGTH_SHORT).show();
                    go = true;
                    ;
                    break;
                case R.id.minuteButton:
                    // if (checked)

                 //   minuteButton.setTypeface( tf, Typeface.BOLD_ITALIC);
                  //  cminuteButton.setTypeface(tf, Typeface.NORMAL);
                    milis = 1000;
                    modul = 60;
                    unit = "Sec.";
                    Timeunit = "Sec. - Second ";
                    // Toast.makeText(getApplicationContext(),minuteButton.getText(),Toast.LENGTH_SHORT).show();
                    go = true;
                    // go her iki durumda da true oluyor
                    break;
            }

            binding.unitValue.setText(unit);
        }
    };
*/
    public static TimerFragment newInstance() {
        return new TimerFragment();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate(Bundle savedInstanceState) {

        // tf = getResources().getFont(R.font.digital7);

        pageViewModel = new ViewModelProvider(requireActivity()).get(PageViewModel.class);

        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        binding = FragmentTimerBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();

        return view;


    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        /**** ESAS HESAPLAYICI KOD *****/


        // tableLayout = view.findViewById(R.id.tableLayout);


        cycPerHour = binding.cycPerHour;
        cycPerMinute = binding.cycPerMinute;
        totalObservationTime = binding.totalObservationTime;
        button2 = binding.button2;
        button2.setVisibility(View.GONE);
        button1 = binding.button;
        button1.setVisibility(View.GONE);// bunlar gözükmesin .silersen kodu bozabilirsin
        button3 = binding.button3;
        button3.setVisibility(View.GONE);
        button4 = binding.button4;
        button4.setVisibility(View.GONE);
        button = binding.button;// save butonu
        button.setVisibility(View.GONE);
        maxvalue = binding.maxVal;
        minvalue = binding.minVal;
        avevalue = binding.aveVal;
        ;
//        radioGroup = view.findViewById(R.id.radioGroup);
//        radioGroup.setOnCheckedChangeListener(onCheckedChangeListener);


        SpannableString ssp = new SpannableString(timer);
        ssp.setSpan(new RelativeSizeSpan(0.5f), 9, ssp.length(), 0);
        binding.textView.setText(ssp);

        button3.setEnabled(false);// lap tuşu kapanıyor

        button.setEnabled(false);// save butonu kapalı
        button4.setEnabled(false);// reset butonu kapalı v1.nci release hata verdi.
        handler = new Handler();
        Auth = true;// açılışta auth true olmalı
        // START STOP butonu yazılması --> button2 değişecek


        ListElementsArrayList = new ArrayList<>();
        binding.lapList.setLayoutManager(new LinearLayoutManager(getContext()));
        lapListAdapter = new LapListAdapter(ListElementsArrayList);
        binding.lapList.setAdapter(lapListAdapter);

        lapListAdapter.notifyDataSetChanged();
        lapListAdapter.setOnItemClickListener(new LapListAdapter.OnItemClickListener() {
            @Override
            public void onAddMessage(int position) {

                // AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                 //View view = getLayoutInflater().inflate(R.layout.addnotedialogbox, null);

                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.AlertDialogCustom));

                builder.setTitle("Add notes for Lap "+ ListElementsArrayList.get(position).lapsayisi);

                final TextInputEditText input =  new TextInputEditText(getActivity());//view.findViewById(R.id.message);

                //builder.setView(view);

                input.setText(lapsArray.get(lapsayisi-position-1).message);
          ;
                LinearLayout layoutName = new LinearLayout(getActivity());
                layoutName.setOrientation(LinearLayout.VERTICAL);
                layoutName.addView(input); // displays the user input bar
                builder.setView(layoutName);

// Set up the buttons
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();

                        m_Text = input.getText().toString();
                       lapsArray.get(lapsayisi-position-1).message = m_Text;

                        }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        });
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
                    ((MainActivity) getActivity()).drawer.open();
                    // Toast.makeText(getContext(), "Choose time unit !", Toast.LENGTH_SHORT).show();
                }
            }
        });
        //minuteButton = (RadioButton) view.findViewById(R.id.minuteButton);
        //cminuteButton = (RadioButton) view.findViewById(R.id.cminuteButton);

        isNoPressed = false;// ilk olarak resetlemede kullanılacak.
// reset butonuna basılınca olanlar
        button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reset();


            }
        });
//lap butonuna basılınca olanlar
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takeLap();

            }
        });
        super.onViewCreated(view, savedInstanceState);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void takeLap() {
        //binding.lapList.setAdapter(adapter);
        //System.out.println("Modul değeri-->"+modul);
        if (go) {
            System.out.println("go var");
        } else {
            System.out.println("go yok");
        }

        int deltah = 0;
        int deltam = 0;
        int i = 0;
        double delta1, delta0;
        double deltas = 0;
        double delta;
        double sum = 0;


        // laps dizisinde string olarak tutulan ifadelerin sayıya çevrilmiş olarak tutuldukları dizi
        String lap = hh + ":" + mm + ":" + ss; //+ "." + msec;
        laps.add(lap);


        // textView2.setMovementMethod(new ScrollingMovementMethod());
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
            maxvalue.setText(dec.format(max * modul)); // saniye olarak yazıyor
            //   maxvalcmin.setText(dec.format(max * 100));
            //  minvalcmin.setText(dec.format(min * 100));
            minvalue.setText(dec.format(min * modul));
            avevalue.setText(dec.format(ave * modul));
            //  avevalcmin.setText(dec.format(ave * 100));
            //  lapnoMin.setText(String.valueOf(lapnomin));
            //  lapnoMax.setText(String.valueOf(lapnomax));
            //  lapTotal.setText(String.valueOf(lapsval.size()));
        }
        // EN SON YAZILAN EN ÜSTE  version


        //textView2.setText("Lap " + (lapsayisi + 1) + ": " + lap + "  Cyc.Time: " + dec.format(delta * modul) + " " + unit + "\n\n" + textView2.getText().toString());
        m_Text = "";// her seferinde 0 laman lazım yoksa hepsine aynı m_text'i yazarsin
        lapToWrite = (lapsayisi + 1) + "      " + lap + "     " + dec.format(delta * modul) + " " + unit;
        //her zaman ilk değer olarak ekler,

        lapValue = new Lap(dec.format(delta * modul) + " " + unit, lap, lapsayisi + 1,m_Text);

        lapsArray.add(lapsayisi,lapValue);
        ListElementsArrayList.add(0, lapValue);//her zaman üste eklemek için
        System.out.println("Dizi---> " + ListElementsArrayList.size());
        cycPerMinute.setText(String.valueOf(dec.format(calculateCycPerMinute(ave))));
        cycPerHour.setText(String.valueOf(dec.format(calculateCycPerHour(ave))));
        /* BU KISIM EN SON YAZILANI EN ALTA ATIYOR
        textView2.append("Lap " + (lapsayisi + 1) + ": " + lap, + "  Cyc.Time: " + dec.format(delta) + " min/cyc ");
        textView2.append(System.getProperty("line.separator"));*/
        //textView2.setSingleLine(true);
        lapsayisi++;


        pageViewModel.setTimeValue((lapsval.get(lapsayisi - 1)).floatValue() * modul);
        pageViewModel.setMaxTimeValue((float) (max * modul));
        pageViewModel.setMinTimeValue((float) (min * modul));
        pageViewModel.setAvgTimeValue((float) (ave * modul));
        pageViewModel.setIndex(lapsayisi);// lapsayıbilgisini gönderiyor
        pageViewModel.setTimeUnit(unit);

        lapListAdapter.notifyDataSetChanged();

      //  System.out.println("deneme değeri "+pageViewModel.getTimeValue().getValue());
      //  System.out.println("deneme lapsayısı "+lapsval.get(lapsayisi-1));
    }

    public void share() {

        // excelSave.share(getActivity());

    }

    public void save() {
        // activity nin context i için getActivity kullan
        if (Auth)
            excelSave.save(getActivity(), unit, laps, lapsval, ave, modul, diffTime, calculateCycPerHour(ave), calculateCycPerMinute(ave),lapsArray);
        else {
            Toast.makeText(getContext(), "Chrono is running !", Toast.LENGTH_SHORT).show();
        }

        //excelSave.deneme(getActivity());
    }

    public void start() {
        Auth = false;
        ((MainActivity) getActivity()).isResetDone = false ;
        // buraya number=XX yazrsan o saniyeden başlatabilirsin
        // mainden çalıştıracaksan if i bunu kaldır
        //if (button2.getText() == "STOP")

        {
            if (modul > 0) {
                getStartTime();
                //cminuteButton.setEnabled(false);
                //minuteButton.setEnabled(false);
                button3.setEnabled(true); // lap tuşu açılıyor
                //  handler = new Handler();

                runnable = new Runnable() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void run() {
                        // buradaki her şey belirtilen periyyotta olaaktır.

                        MillisecondTime = SystemClock.uptimeMillis() - StartTime;

                        UpdateTime = TimeBuff + MillisecondTime;

                        Seconds = (int) (UpdateTime / milis);

                        Minutes = Seconds / modul;

                        Seconds = Seconds % modul;

                        Hours = Minutes / 60;


                        MilliSeconds = (int) (UpdateTime % 1000);

                        hh = Hours < 10 ? "0" + Hours : Hours + "";
                        mm = Minutes < 10 ? "0" + Minutes : Minutes + "";
                        ss = Seconds < 10 ? "0" + Math.floorMod(Seconds, modul) : Math.floorMod(Seconds, modul) + "";
                        msec = String.valueOf(MilliSeconds);
                        String string = String.format("%02d", Hours)
                                + ":" + String.format("%02d", Minutes)
                                + ":"
                                + String.format("%02d", Seconds)
                                + "."
                                + String.format("%03d", MilliSeconds);

                        SpannableString spannableString = new SpannableString(string);
                        spannableString.setSpan(new RelativeSizeSpan(0.5f), 9, spannableString.length(), 0);

                        binding.textView.setText(spannableString);


                        /**
                         micro ++ ;
                         if (micro == 100) {
                         number++;
                         micro = 0;

                         }
                         if (number == modul) {
                         m++;

                         number = 0;
                         }
                         if (m == 60) { //dakika 1e eşit ve büyükse ve 60 olursa
                         h++;
                         m = 0;

                         }


                         hh = h < 10 ? "0" + h : h + "";
                         mm = m < 10 ? "0" + m : m + "";
                         ss = number < 10 ? "0" + Math.floorMod(number, modul) : Math.floorMod(number, modul) + "";

                         textView.setText(hh + ":" + mm + ":" + ss + "." + micro);
                         */
                        pageViewModel.setTimerValue(binding.textView.getText().toString()); //diğer fragmanda
                        //görünmesi için
                        handler.postDelayed(runnable, 0);
                        // handler.postDelayed(runnable, delaymilissecond)


                    }

                };
                handler.post(runnable);

                //button2.setEnabled(false);
            } else {
                ((MainActivity) getActivity()).drawer.open();
                //  Toast.makeText(getContext(), "Choose time unit!", Toast.LENGTH_SHORT).show();
            }
        }
        ;


    }

    private void getStartTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        currentDateandTimeStart = sdf.format(new Date());
        timeStart = new Date();
        StartTime = SystemClock.uptimeMillis();


        handler.postDelayed(runnable, 0);
        System.out.println("start time : >> " + StartTime);

    }

    public void stop() {
        Auth = true;
        //button2.setEnabled(true);
//        totalObservationTime.setText(String.valueOf(calculateTotalObservationTime()));
        getStopTime();
        TimeBuff += MillisecondTime;

        handler.removeCallbacks(runnable);


        ; // double serisi verirken içine kaç tane yazacağını belirt

    }

    private void getStopTime() {

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        currentDateandTimeStop = sdf.format(new Date());
        StopTime = SystemClock.uptimeMillis();
        Date date1 = null;
        try {
            date1 = sdf.parse(currentDateandTimeStart);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Date date2 = null;
        try {
            date2 = sdf.parse(currentDateandTimeStop);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        long diff = date2.getTime() - date1.getTime();

        int timeInSeconds = (int) (diff / 1000);
        int hours, minutes, seconds;
        hours = timeInSeconds / 3600;
        timeInSeconds = timeInSeconds - (hours * 3600);
        minutes = timeInSeconds / 60;
        timeInSeconds = timeInSeconds - (minutes * 60);
        seconds = timeInSeconds;
        diffTime = (hours < 10 ? "0" + hours : hours) + ":" + (minutes < 10 ? "0" + minutes : minutes) + ":" + (seconds < 10 ? "0" + seconds : seconds);
        System.out.println("difftime : "+diffTime);
        totalObservationTime.setText(diffTime);
        System.out.println("stop time : >> " + StopTime);

    }
    public void getTotalObservationTime
            (){
        long diff = StopTime-StartTime;


    }

    public void reset() {
       ((MainActivity) getActivity()).drawerSwitchCmin.setEnabled(true);
       ((MainActivity) getActivity()).drawerSwitchSec.setEnabled(true);
        handler.removeCallbacks(runnable);
        Auth = true;
        button4.setEnabled(false); //dataları sildikten sonra butonu kapat v1.nci releasedeki hatadan dolayı
        button2.setText("START");
        m = 0;
        h = 0;
        number = 0;

        MillisecondTime = 0L;
        StartTime = 0L;
        StopTime =0L;
        TimeBuff = 0L;
        UpdateTime = 0L;
        Seconds = 0;
        Minutes = 0;
        MilliSeconds = 0;

        SpannableString ssp = new SpannableString(timer);
        ssp.setSpan(new RelativeSizeSpan(0.5f), 9, ssp.length(), 0);
        binding.textView.setText(ssp);
        cycPerHour.setText("");
        cycPerMinute.setText("");
        totalObservationTime.setText("");
        button2.setEnabled(true);// start tuşu açılıyor
        button3.setEnabled(false);// lap tuşu kapanıyor
        button.setEnabled(false);
        lapsayisi = 0;
        avevalue.setText(departure);
        //binding.lapList.setAdapter(null);
        ListElementsArrayList.clear();

        laps.clear(); // lapları siliyor
        lapsval.clear();// aralık değerlerini siliyor
        maxvalue.setText(departure);
        minvalue.setText(departure);

        lapListAdapter.notifyDataSetChanged();
    }

    private double calculateCycPerMinute(double ave) {
        double cycPerMinute = 0;
        switch (modul) {//modul saniye ,cminute olacak değer. saniye için 60, cminute için 100 olmalı
            case 60:
                cycPerMinute = (60 / ave) / 100;
            case 100:
                cycPerMinute = (100 / ave) / 100;
        }
        return cycPerMinute;
    }


    private double calculateCycPerHour(double ave) {
        double cycPerHour = 0;
        switch (modul) {//modul saniye ,cminute olacak değer. saniye için 60, cminute için 100 olmalı
            case 60:
                cycPerHour = (3600 / ave) / 100;
            case 100:
                cycPerHour = (6000 / ave) / 100;
        }
        return cycPerHour;
    }
}
