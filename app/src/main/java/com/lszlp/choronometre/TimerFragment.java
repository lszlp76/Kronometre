package com.lszlp.choronometre;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.lszlp.choronometre.databinding.FragmentTimerBinding;
import com.lszlp.choronometre.main.PageViewModel;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TimerFragment extends Fragment {
    public String Timeunit;
    private FragmentTimerBinding _binding;
    private FragmentTimerBinding getBinding() {
        return _binding;
    }
    private BroadcastReceiver timeUpdateReceiver;
    SpannableString spannableString;
    TextView maxvalue, minvalue, totalObservationTime, cycPerHour, cycPerMinute;
    Button button2, button1, button3, button4, button;
    String lapToWrite;
    String timeString;
    Lap lapValue;
    ArrayList<Lap> lapsArray = new ArrayList<>();
    ArrayList<Lap> ListElementsArrayList;
    LapListAdapter lapListAdapter;



    public void setUnitDisplay(String unitValue) {
        FragmentTimerBinding currentBinding = getBinding();

        // Binding'in ve Fragment'ın hazır olduğunu kontrol et
        if (currentBinding == null || !isAdded()) {
            Log.w("TimerFragment", "Cannot set unit text, view is destroyed or fragment detached.");
            return;
        }

        // unitValue'yu unit değişkenine kaydet (İleride kullanmak için)
        this.unit = unitValue;

        // TextView'ı güncelle
        // Not: "unitValue" yerine "unitValue.setText" kullanılıyordu,
        // bu nedenle buradaki TextView adının "unitValue" olduğunu varsayıyoruz.
        currentBinding.unitValue.setText(unitValue);
    }
    /*Timer düzeltme için
    Runnable runnable;
    Handler handler;

 String hh, mm, ss, msec;
    long MillisecondTime, StopTime, StartTime, TimeBuff, UpdateTime = 0L;
     int Hours, Seconds, Minutes, MilliSeconds;

long MillisecondTime, StopTime, StartTime, TimeBuff, UpdateTime = 0L;

// String hh, mm, ss, msec;
// long elapsedTime = 0;
// boolean running = false;
// private long startTime = 0;
    private long startTime = 0;
     */
    int Hours, Seconds, Minutes, MilliSeconds;

    boolean go;
    int number, micro;
    String unit, diffTime;
    String second = null, minute = null, hour = null;
    String timer = "00:00:00.000";
    String departure = "---";
    int modul;
    int milis;
    int m;
    int h;

    int lapsayisi = 0;
    ArrayList<String> laps = new ArrayList<>();
    DecimalFormat dec = new DecimalFormat("#0.00");
    boolean Auth;
    ArrayList<Double> lapsval = new ArrayList<>();
    int lapnomax = 0, lapnomin = 0;
    double min, max, ave;
    PageViewModel pageViewModel;
    ExcelSave excelSave = new ExcelSave();
    List<String> saveValue;



    String currentDateandTimeStop;
    String currentDateandTimeStart;
    Date timeStop, timeStart;
    boolean isNoPressed;
    private DateFormat df = new SimpleDateFormat("ddMMyy@HHmm");
    private DateFormat df2 = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss ", Locale.ENGLISH);
    private String m_Text = "";
    private TextView avevalue;
    private long lastKnownElapsedTime = 0L; // Servisten gelen son zamanı burada tutacağız
    public static TimerFragment newInstance() {
        return new TimerFragment();
    }
    /*
    public void resumeFromRotation() {
        if (running && handler != null) {
            handler.removeCallbacks(runnable);
            startTime = SystemClock.elapsedRealtime() - elapsedTime;
            handler.postDelayed(runnable, 0);

        }
    }
    */

    // State'leri kaydet
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        // Auth=false (çalışıyor), Auth=true (durmuş)
        outState.putBoolean("Auth", Auth);

        // 'elapsedTime' yerine servisten gelen son zamanı kaydediyoruz
        outState.putLong("lastKnownElapsedTime", lastKnownElapsedTime);


    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {

            // 'running' yerine 'Auth' durumunu geri yükle
            // Varsayılan: true (durmuş)
            Auth = savedInstanceState.getBoolean("Auth", true);

            // 'elapsedTime' yerine 'lastKnownElapsedTime'ı geri yükle
            lastKnownElapsedTime = savedInstanceState.getLong("lastKnownElapsedTime", 0L);

            // Durumu UI'daki butonlara yansıt
            updateButtonStatesBasedOnAuth();

            // Eğer zaman 0'dan büyükse, ekranı son bilinen zamanla güncelle
            // (Bu, ekran döndüğünde "00:00:00" görmeyi engeller)
            if (lastKnownElapsedTime > 0) {
                updateTimeDisplay(lastKnownElapsedTime);
            }
        }

    }

    /**
     * Servisi başlatmadan/durdurmadan,
     * sadece 'Auth' değişkenine göre butonların görünümünü (START/STOP) günceller.
     * Ekran dönüşleri (rotation) için kullanılır.
     */
    private void updateButtonStatesBasedOnAuth() {
        if (getActivity() == null || _binding == null) return;

        if (Auth) { // Durum: Durmuş (Auth = true)
            button2.setText("START");
            button3.setEnabled(false);
            button.setEnabled(true);
            button4.setEnabled(true);
        } else { // Durum: Çalışıyor (Auth = false)
            button2.setText("STOP");
            button3.setEnabled(true);
            button.setEnabled(false);
            button4.setEnabled(false);
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pageViewModel = new ViewModelProvider(requireActivity()).get(PageViewModel.class);
        // UI Thread'e geçişi sağlamak için Handler tanımla
        final Handler uiHandler = new Handler(requireContext().getMainLooper());

        timeUpdateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                long elapsed = intent.getLongExtra(Constants.EXTRA_ELAPSED_TIME, 0);

                // Logcat'te sürekli verinin geldiğini görüyorsunuz
                Log.d("ChronoUpdate", "Elapsed Time Received: " + elapsed);

                lastKnownElapsedTime = elapsed;

                // UI Güncellemesini Handler ile zorlayın
                uiHandler.post(() -> {
                    // Logcat'e bu satırı ekleyin:
                    Log.d("ChronoUpdate", "UI Thread'de updateTimeDisplay çağrıldı.");
                    updateTimeDisplay(elapsed);
                });
            }
        };
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
                timeUpdateReceiver,
                new IntentFilter(Constants.ACTION_TIME_UPDATE)
        );
    }
    // BU METODU EKLEYİN (Binding'i temizlemek için KRİTİK!)
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        _binding = null; // View yok edildiğinde binding'i temizle
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (timeUpdateReceiver != null) {
            try {
                LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(timeUpdateReceiver);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private void updateTimeDisplay(long elapsedMillis) {
// 1. Binding null ise hemen çık
        FragmentTimerBinding currentBinding = getBinding();
        if (currentBinding == null || getActivity() == null || !isAdded()) return;

        int hours = (int) (elapsedMillis / 3600000);
        int minutes = (int) (elapsedMillis - hours * 3600000) / 60000;
        int seconds = (int) (elapsedMillis - hours * 3600000 - minutes * 60000) / 1000;
        int millis = (int) (elapsedMillis % 1000);

        // GEÇİCİ TEST İÇİN EKLEYİN
        // Bu Toast, TextView'da yazmasa bile, metodun çalıştığını doğrular.
//        if (elapsedMillis > 1000 && elapsedMillis < 2000) {
//            Toast.makeText(getContext(), "UI Update Fired!", Toast.LENGTH_SHORT).show();
//        }


        timeString = String.format("%02d:%02d:%02d.%03d", hours, minutes, seconds, millis);

        spannableString = new SpannableString(timeString);
        spannableString.setSpan(new RelativeSizeSpan(0.5f), 9, spannableString.length(), 0);
        // 2. Güncelleme için yeni binding objesini kullanın
        currentBinding.textView.setText(spannableString);
// DEBUG: Eğer hala çalışmıyorsa, bu log'u ekleyin
        Log.d("UpdateCheck", "TextView güncellendi! Değer: " + timeString);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        _binding = FragmentTimerBinding.inflate(getLayoutInflater()); // Atamayı buraya yapın
        return _binding.getRoot();

    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeViews();
        setupRecyclerView();
        setupClickListeners();
    }

    private void initializeViews() {
        cycPerHour = _binding.cycPerHour;
        cycPerMinute = _binding.cycPerMinute;
        totalObservationTime = _binding.totalObservationTime;
        button2 = _binding.button2;
        button1 = _binding.button;
        button3 = _binding.button3;
        button4 = _binding.button4;
        button = _binding.button;
        maxvalue = _binding.maxVal;
        minvalue = _binding.minVal;
        avevalue = _binding.aveVal;

        // Butonları görünür yap
        setWidgetsVisibility(false);
        /*
// GEÇİCİ TEST İÇİN EKLEYİN: Varsayılan saniye modunu zorla
        this.modul = 60;   // 60 saniye
        this.milis = 1000; // 1000 milisaniye (1 saniye)
        this.unit = "sec"; // "sec" birimi
*/
        SpannableString ssp = new SpannableString(timer);
        ssp.setSpan(new RelativeSizeSpan(0.5f), 9, ssp.length(), 0);
        _binding.textView.setText(ssp);

        button3.setEnabled(false);
        button.setEnabled(false);
        button4.setEnabled(false);
       // handler = new Handler();
        Auth = true;
    }

    private void setupRecyclerView() {
        ListElementsArrayList = new ArrayList<>();
        _binding.lapList.setLayoutManager(new LinearLayoutManager(getContext()));
        lapListAdapter = new LapListAdapter(ListElementsArrayList);
        _binding.lapList.setAdapter(lapListAdapter);

        lapListAdapter.setOnItemClickListener(new LapListAdapter.OnItemClickListener() {
            @Override
            public void onAddMessage(int position) {
                showNoteDialog(position);
            }
        });
    }

    private void setupClickListeners() {
        Log.d("ModulCheck", "Mevcut Modul Değeri: " + modul); // YENİ SATIR
        button2.setOnClickListener(v -> {
            if (modul > 0) {
                if (Auth) {
                    button2.setText("STOP");
                    start();
                    button.setEnabled(false);
                    button4.setEnabled(false);
                } else {
                    button2.setText("START");
                    stop();
                    button.setEnabled(true);
                    button4.setEnabled(true);
                }
            } else {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).drawer.open();
                }
            }
        });

        button4.setOnClickListener(v -> reset());
        button3.setOnClickListener(v -> takeLap());
    }

    // ================================================================
    // DEĞİŞEN METOT: showNoteDialog
    // ================================================================
    private void showNoteDialog(int position) {
        if (getActivity() == null) return;

        // RecyclerView listesinden doğru Lap objesini al
        // (ListElementsArrayList'in ters sıralı olduğunu varsayarak)
        Lap lapToEdit = ListElementsArrayList.get(position);
        int lapNumber = lapToEdit.lapsayisi;
        String currentNote = lapToEdit.message;

        // Eski AlertDialog.Builder kodunu silin ve bunu kullanın:
        CustomAlertDialogFragment dialog = CustomAlertDialogFragment.newInstanceForNote(
                position,    // RecyclerView'daki pozisyon
                lapNumber,   // Lap'ın gerçek numarası (örn: Lap 5)
                currentNote  // Mevcut not (varsa)
        );

        // Diyaloğu MainActivity'nin FragmentManager'ı üzerinden göster
        dialog.show(requireActivity().getSupportFragmentManager(), "ADD_NOTE_DIALOG_TAG");
    }
// ================================================================
    // YENİ METOT: updateNoteForLap (MainActivity tarafından çağrılır)
    // ================================================================
    /**
     * CustomAlertDialogFragment'tan gelen veriyi işler ve notu günceller.
     * @param position RecyclerView'daki öğenin pozisyonu.
     * @param newNoteText Kullanıcının girdiği yeni not.
     */
    public void updateNoteForLap(int position, String newNoteText) {
        if (ListElementsArrayList != null && ListElementsArrayList.size() > position) {

            // 1. Ana UI listesini güncelle (ListElementsArrayList)
            Lap lapInUI = ListElementsArrayList.get(position);
            lapInUI.message = newNoteText;

            // 2. Ana veri kaynağı listesini güncelle (lapsArray)
            // lapsArray'in nasıl sıralandığına bağlı olarak doğru indeksi bulmalıyız.
            // Eski kodunuz (lapsayisi - position - 1) kullanıyordu,
            // ama bu ListElementsArrayList'e bağlıydı.
            // En güvenli yol, Lap numarasını kullanarak asıl objeyi bulmaktır.
            int lapNumberToFind = lapInUI.lapsayisi;

            for (Lap originalLap : lapsArray) {
                if (originalLap.lapsayisi == lapNumberToFind) {
                    originalLap.message = newNoteText;
                    break;
                }
            }

            // 3. RecyclerView'ı uyar
            if (lapListAdapter != null) {
                lapListAdapter.notifyItemChanged(position);
            }

            Toast.makeText(getContext(), "Note for Lap " + lapNumberToFind + " saved.", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void takeLap() {
        if (getActivity() == null) return;
// 1. Servisten gelen güncel zamanı al
        long currentTimeMillis = lastKnownElapsedTime;
        int currentHours = (int) (currentTimeMillis / 3600000);
        int currentMinutes = (int) (currentTimeMillis - currentHours * 3600000) / 60000;
        int currentSeconds = (int) (currentTimeMillis - currentHours * 3600000 - currentMinutes * 60000) / 1000;
        // 2. Zamanı string'e çevir
        String hh_lap = (currentHours < 10) ? "0" + currentHours : String.valueOf(currentHours);
        String mm_lap = (currentMinutes < 10) ? "0" + currentMinutes : String.valueOf(currentMinutes);
        String ss_lap = (currentSeconds < 10) ? "0" + currentSeconds : String.valueOf(currentSeconds);

        String lap = hh_lap + ":" + mm_lap + ":" + ss_lap;
        laps.add(lap);


        double delta;
        if (lapsayisi > 0) {
            // Önceki lap zamanını listeden al
            String previousLapString = laps.get(lapsayisi - 1);
            String[] parts = previousLapString.split(":");
            int prevHour = Integer.parseInt(parts[0]);
            int prevMin = Integer.parseInt(parts[1]);
            int prevSec = Integer.parseInt(parts[2]);
// Eski kodunuzdaki 'modul'ü kullanarak dakika cinsinden hesaplama
            double delta1 = (currentHours / 60.0 + currentMinutes + (double)currentSeconds / modul);
            double delta0 = (prevHour / 60.0 + prevMin + (double)prevSec / modul);
            delta = Math.abs(delta1 - delta0);
        } else {
            // İlk lap
            delta = (currentHours / 60.0 + currentMinutes + (double)currentSeconds / modul);
        }
//            double delta1 = (Integer.parseInt(hh) / 60.0 + Integer.parseInt(mm) + Double.parseDouble(ss) / modul);
//            double delta0 = (Integer.parseInt(hour) / 60.0 + Integer.parseInt(minute) + Double.parseDouble(second) / modul);
//            delta = Math.abs(delta1 - delta0);
//        } else {
//            second = laps.get(lapsayisi).substring(6, 8);
//            minute = laps.get(lapsayisi).substring(3, 5);
//            hour = laps.get(lapsayisi).substring(0, 2);
//            delta = (Integer.parseInt(hour) / 60.0 + Integer.parseInt(minute) + Double.parseDouble(second) / modul);
//        }

        lapsval.add(delta);
        updateStatistics();

        m_Text = "";
        lapValue = new Lap(dec.format(delta * modul) + " ", lap, lapsayisi + 1, m_Text);
        lapsArray.add(lapsayisi, lapValue);
        ListElementsArrayList.add(0, lapValue);

        updateDisplay();
        lapsayisi++;

        if (pageViewModel != null) {
            pageViewModel.setTimeValue((float) (lapsval.get(lapsayisi - 1) * modul));
            pageViewModel.setMaxTimeValue((float) (max * modul));
            pageViewModel.setMinTimeValue((float) (min * modul));
            pageViewModel.setAvgTimeValue((float) (ave * modul));
            pageViewModel.setIndex(lapsayisi);
            pageViewModel.setTimeUnit(unit);
        }

        lapListAdapter.notifyDataSetChanged();
    }

    private void updateStatistics() {
        if (lapsval.isEmpty()) return;

        double sum = 0;
        for (Double val : lapsval) {
            sum += val;
        }

        min = Collections.min(lapsval);
        lapnomin = lapsval.indexOf(min) + 1;
        max = Collections.max(lapsval);
        lapnomax = lapsval.indexOf(max) + 1;
        ave = sum / lapsval.size();
    }

    private void updateDisplay() {
        maxvalue.setText(dec.format(max * modul));
        minvalue.setText(dec.format(min * modul));
        avevalue.setText(dec.format(ave * modul));
        cycPerMinute.setText(dec.format(calculateCycPerMinute(ave)));
        cycPerHour.setText(dec.format(calculateCycPerHour(ave)));
    }

    public void save(String fileName) {
        if (getActivity() == null) return;

        if (Auth) {
            excelSave.save(getActivity(), unit, laps, lapsval, ave, modul, diffTime,
                    calculateCycPerHour(ave), calculateCycPerMinute(ave), lapsArray,fileName);
        } else {
            Toast.makeText(getContext(), "Chrono is running!", Toast.LENGTH_SHORT).show();
        }
    }

    public void start() {
        if (getActivity() == null) return;

        Log.d("TimerFragment", "Start method called");
        //debugTimeUnitInfo(); // Debug info

        Auth = false;
        button2.setText("STOP");
        button3.setEnabled(true);
        button.setEnabled(false);
        button4.setEnabled(false);

        startForegroundService();
        getStartTime();
        //startLocalTimer();
    }


    private void getStartTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        currentDateandTimeStart = sdf.format(new Date());
        timeStart = new Date();
        //StartTime = SystemClock.uptimeMillis();
    }

    public void stop() {
        Auth = true;

        // Buton durumlarını güncelle
        button2.setText("START");
        button3.setEnabled(false);
        button.setEnabled(true);
        button4.setEnabled(true);

        // Servisi durdur
        stopForegroundService();

        // Lokal timer'ı durdur
       // stopLocalTimer();
        getStopTime();
    }
/*
    private void startLocalTimer() {
        if (runnable != null) {
            handler.removeCallbacks(runnable);
        }

        runnable = new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void run() {
                if (!Auth && getActivity() != null) { // Sadece çalışıyorsa ve activity varsa
                    MillisecondTime = SystemClock.uptimeMillis() - StartTime;
                    UpdateTime = TimeBuff + MillisecondTime;

                    Seconds = (int) (UpdateTime / milis);
                    Minutes = Seconds / modul;
                    Seconds = Seconds % modul;
                    Hours = Minutes / 60;
                    Minutes = Minutes % 60;
                    Hours = Hours % 24;
                    MilliSeconds = (int) (UpdateTime % 1000);

                    hh = Hours < 10 ? "0" + Hours : String.valueOf(Hours);
                    mm = Minutes < 10 ? "0" + Minutes : String.valueOf(Minutes);
                    ss = Seconds < 10 ? "0" + Math.floorMod(Seconds, modul) : String.valueOf(Math.floorMod(Seconds, modul));

                    String string = String.format("%02d:%02d:%02d.%03d", Hours, Minutes, Seconds, MilliSeconds);

                    SpannableString spannableString = new SpannableString(string);
                    spannableString.setSpan(new RelativeSizeSpan(0.5f), 9, spannableString.length(), 0);
                    binding.textView.setText(spannableString);

                    if (pageViewModel != null) {
                        pageViewModel.setTimerValue(binding.textView.getText().toString());
                    }

                    handler.postDelayed(this, 10); // 10ms'de bir güncelle
                }
            }
        };
        handler.post(runnable);
    }

    private void stopLocalTimer() {
        if (handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
        }
        TimeBuff += MillisecondTime;
    }
*/
    private void getStopTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        currentDateandTimeStop = sdf.format(new Date());
        //StopTime = SystemClock.uptimeMillis();

        try {
            Date date1 = sdf.parse(currentDateandTimeStart);
            Date date2 = sdf.parse(currentDateandTimeStop);

            if (date1 != null && date2 != null) {
                long diff = date2.getTime() - date1.getTime();
                int timeInSeconds = (int) (diff / 1000);
                int hours = timeInSeconds / 3600;
                timeInSeconds = timeInSeconds - (hours * 3600);
                int minutes = timeInSeconds / 60;
                timeInSeconds = timeInSeconds - (minutes * 60);
                int seconds = timeInSeconds;

                diffTime = String.format("%02d:%02d:%02d", hours, minutes, seconds);
                totalObservationTime.setText(diffTime);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void reset() {
        // Hem lokal timer'ı hem servisi resetle
        // stopLocalTimer();
        stopForegroundService();

        Auth = true;
        button4.setEnabled(false);
        button2.setText("START");

        // Tüm değişkenleri sıfırla
        m = 0;
        h = 0;
        number = 0;
        // Sıfırlanan değişkenleri temizle (handler/runnable ile ilgili olanlar zaten silindi)
        Seconds = 0;
        Minutes = 0;
        MilliSeconds = 0;
        lastKnownElapsedTime = 0L; // Yeni eklenen değişkeni sıfırla

        // UI'ı sıfırla
        SpannableString ssp = new SpannableString(timer);
        ssp.setSpan(new RelativeSizeSpan(0.5f), 9, ssp.length(), 0);
        _binding.textView.setText(ssp);

        cycPerHour.setText("");
        cycPerMinute.setText("");
        totalObservationTime.setText("");
        button2.setEnabled(true);
        button3.setEnabled(false);
        button.setEnabled(false);
        lapsayisi = 0;
        avevalue.setText(departure);

        // Listeleri temizle
        ListElementsArrayList.clear();
        laps.clear();
        lapsval.clear();
        maxvalue.setText(departure);
        minvalue.setText(departure);

        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).drawerSwitchCmin.setEnabled(true);
            ((MainActivity) getActivity()).drawerSwitchSec.setEnabled(true);
            ((MainActivity) getActivity()).drawerSwitchDmin.setEnabled(true);
        }

        lapListAdapter.notifyDataSetChanged();
    }

    private double calculateCycPerMinute(double ave) {
        return (1 / ave);
    }

    private double calculateCycPerHour(double ave) {
        return (60 / ave);
    }

    private void setWidgetsVisibility(boolean visible) {
        int visibility = visible ? View.VISIBLE : View.GONE;
        if (button2 != null) button2.setVisibility(visibility);
        if (button3 != null) button3.setVisibility(visibility);
        if (button4 != null) button4.setVisibility(visibility);
        if (button != null) button.setVisibility(visibility);

     }

    private void startForegroundService() {
        Intent serviceIntent = new Intent(getContext(), ChronometerService_.class);
        serviceIntent.setAction(Constants.ACTION_START);

        // Zaman birimi bilgilerini servise gönder
        serviceIntent.putExtra(Constants.EXTRA_TIME_UNIT, unit);
        serviceIntent.putExtra(Constants.EXTRA_MODUL, modul);
        serviceIntent.putExtra(Constants.EXTRA_MILIS, milis);


        Log.d("TimerFragment", "🔥 Sending to service - Unit: " + unit + ", Modul: " + modul + ", Milis: " + milis);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            requireContext().startForegroundService(serviceIntent);
            Log.d("ServiceStart", "startForegroundService (Oreo+) çağrıldı."); // YENİ LOG
        } else {
            requireContext().startService(serviceIntent);
            Log.d("ServiceStart", "startService (Pre-Oreo) çağrıldı."); // YENİ LOG
        }
    }

    // Debug metodu
    private void debugTimeUnitInfo() {
        Log.d("TimeUnitDebug", "=== TIME UNIT INFO ===");
        Log.d("TimeUnitDebug", "Unit: " + unit);
        Log.d("TimeUnitDebug", "Modul: " + modul);
        Log.d("TimeUnitDebug", "Milis: " + milis);
        Log.d("TimeUnitDebug", "======================");

        if (getActivity() != null) {
            String info = "Sending to service:\n" +
                    "Unit: " + unit + "\n" +
                    "Modul: " + modul + "\n" +
                    "Milis: " + milis;
            Toast.makeText(getActivity(), info, Toast.LENGTH_LONG).show();
        }
    }

    private void stopForegroundService() {
        Intent serviceIntent = new Intent(getContext(), ChronometerService_.class);
        serviceIntent.setAction(Constants.ACTION_STOP);
        requireContext().startService(serviceIntent);
    }

    // DND Permission methods
    private void checkAndRequestDndPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            NotificationManager notificationManager = (NotificationManager) requireContext().getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null && !notificationManager.isNotificationPolicyAccessGranted()) {
                showDndPermissionDialog();
            }
        }
    }

    private void showDndPermissionDialog() {
        if (getActivity() == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Rahatsız Etmeyin Erişimi Gerekli");
        builder.setMessage("Kilit ekranında kronometreyi gösterebilmek için 'Rahatsız Etmeyin' ayarlarına erişim izni vermeniz gerekiyor.");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            builder.setPositiveButton("Ayarlara Git", (dialog, which) -> requestDndPermission());
        }
        builder.setNegativeButton("İptal", (dialog, which) -> {
            Toast.makeText(requireContext(), "Kilit ekranı özelliği devre dışı", Toast.LENGTH_SHORT).show();
        });

        builder.setCancelable(false);
        builder.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestDndPermission() {
        try {
            Intent intent = new Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
            startActivityForResult(intent, Constants.REQUEST_DND_ACCESS);
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Ayarlara manuel olarak gidin", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.REQUEST_DND_ACCESS) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                NotificationManager notificationManager = (NotificationManager) requireContext().getSystemService(Context.NOTIFICATION_SERVICE);
                if (notificationManager != null && notificationManager.isNotificationPolicyAccessGranted()) {
                    Toast.makeText(requireContext(), "İzin verildi! Kilit ekranı özelliği aktif.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "İzin verilmedi. Kilit ekranı özelliği devre dışı.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    // TimerFragment.java'ya bu metodları ekleyin
    public void debugNotificationSettings() {
        if (getActivity() == null) return;

        NotificationManager notificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);

        StringBuilder debugInfo = new StringBuilder();
        debugInfo.append("=== NOTIFICATION DEBUG INFO ===\n\n");

        if (notificationManager != null) {
            // Notification channel bilgisi
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = notificationManager.getNotificationChannel(ChronometerService_.CHANNEL_ID);
                if (channel != null) {
                    debugInfo.append("Channel: ").append(channel.getId()).append("\n");
                    debugInfo.append("Name: ").append(channel.getName()).append("\n");
                    debugInfo.append("Importance: ").append(channel.getImportance()).append("\n");
                    debugInfo.append("Lockscreen Visibility: ").append(channel.getLockscreenVisibility()).append("\n");
                    debugInfo.append("Can Bypass DND: ").append(channel.canBypassDnd()).append("\n");
                    debugInfo.append("Can Show Badge: ").append(channel.canShowBadge()).append("\n");
                } else {
                    debugInfo.append("Channel NOT FOUND: ").append(ChronometerService_.CHANNEL_ID).append("\n");
                }
            }

            // Notification policy erişimi
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                debugInfo.append("DND Access: ").append(notificationManager.isNotificationPolicyAccessGranted()).append("\n");
            }

            // Notificationlar etkin mi
            debugInfo.append("Notifications Enabled: ").append(notificationManager.areNotificationsEnabled()).append("\n");
        } else {
            debugInfo.append("NotificationManager is NULL\n");
        }

        debugInfo.append("\n=== DEVICE INFO ===\n");
        debugInfo.append("Android Version: ").append(Build.VERSION.SDK_INT).append("\n");
        debugInfo.append("Model: ").append(Build.MODEL).append("\n");
        debugInfo.append("Brand: ").append(Build.BRAND).append("\n");

        Log.d("NotificationDebug", debugInfo.toString());

        // Debug bilgisini Toast olarak göster
        if (getActivity() != null) {
            new AlertDialog.Builder(getActivity())
                    .setTitle("Notification Debug Info")
                    .setMessage(debugInfo.toString())
                    .setPositiveButton("OK", null)
                    .show();
        }
    }

    // TimerFragment.java'de servisten gelen durum değişikliklerini dinle
    private BroadcastReceiver serviceStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ChronometerService_.ACTION_TIME_UPDATE.equals(intent.getAction())) {
                long elapsed = intent.getLongExtra("elapsed", 0);
                // UI güncelleme işlemleri
                updateButtonStates();
                if (isServicePaused){
                    onPause();
                }else{
                    onResume();
                }

            }
        }
    };
    // Fragment'ta pause/resume durumunu takip etmek için
    private boolean isServicePaused = false;

    // Buton metnini güncelle
    private void updateButtonStates() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (isServicePaused) {
                        button2.setText("RESUME");
                        button3.setEnabled(false); // Lap butonunu devre dışı bırak
                    } else {
                        button2.setText("STOP");
                        button3.setEnabled(true); // Lap butonunu aktif et
                    }
                }
            });

        }
    }
}
