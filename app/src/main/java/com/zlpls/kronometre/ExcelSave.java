package com.zlpls.kronometre;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

public class ExcelSave {
    public int getLapsize() {
        return lapsize;
    }

    public void setLapsize(int lapsize) {
        this.lapsize = lapsize;
    }

    int lapsize;
    DecimalFormat dec = new DecimalFormat("#0.00");
    public void save(Context context, String timeUnit, ArrayList<String> laps, ArrayList<Double>lapsval, Double ave, int modul){
        if (laps.size() > 0) {
            DecimalFormat decthree = new DecimalFormat("#0.000");
            Date myDate = new Date();
            DateFormat df = new SimpleDateFormat("ddMMyy@HHmm");
            DateFormat df2 = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            String myString = df.format(myDate);
            String xlString = df2.format(myDate);
            // String myString = DateFormat.getDateInstance().format(myDate);
            String FILE_NAME = myString + "lapsdata" + ".xls";

// Önce kullanıcının yazma izni olup olmadığını kontrol ediyoruz
        /*
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

        } else {*/
            // Eğer izin varsa aşağıdaki kodu uygular
            // File sdCard = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
            //File path = getApplicationContext().getFilesDir();
            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);// bulunduğu folder
            File file = new File(path, FILE_NAME);


            WorkbookSettings wbSettings = new WorkbookSettings();
            wbSettings.setLocale(new Locale("en", "EN"));

            WritableWorkbook workbook;
            try {

                workbook = Workbook.createWorkbook(file, wbSettings);
                WritableSheet sheet = workbook.createSheet("Chronos Time Study", 0);
                int i = 0;
                // XL dosya içi başlıklar
                Label lbl = new Label(0, 0, "CHRONOS TIME STUDY DATA REPORT");
                Label lbl1 = new Label(0, 1, String.valueOf("date"));
                Label lbl2 = new Label(0, 2, "Time Unit");
                Label lbl3 = new Label(0, 3, "Total Study Time :");
                Label lbl4 = new Label(0, 4, "Maximum Cycle Time : ");
                Label lbl5 = new Label(0, 5, "Lap number of Max.Cyc.Time : ");
                Label lbl6 = new Label(0, 6, "Minimum Cycle Time : ");
                Label lbl7 = new Label(0, 7, "Lap number of Min.Cyc.Time : ");
                Label lbl8 = new Label(0, 8, "Average Cycle Time : ");

                Label no = new Label(0, 9, "Lap No :");
                Label lbl9 = new Label(1, 9, "Laps Value:");
                Label lbl10 = new Label(2, 9, "Cycle Time ["+timeUnit+"] :");

// başlık değerleri
                Label lbl11 = new Label(1, 1, xlString);

                Label lbl12 = new Label(1, 2, timeUnit);
                Label lbl13 = new Label(1, 3, laps.get(laps.size() - 1));
                Label lbl14 = new Label(1, 4, decthree.format(Collections.max(lapsval)*modul) + " "+ timeUnit);
                Label lbl15 = new Label(1, 5, String.valueOf(lapsval.indexOf(Collections.max(lapsval))));
                Label lbl16 = new Label(1, 6, decthree.format(Collections.min(lapsval)*modul) + " "+ timeUnit);
                Label lbl17 = new Label(1, 7, String.valueOf(lapsval.indexOf(Collections.min(lapsval))));
                Label lbl18 = new Label(1, 8, decthree.format(ave*modul) + " "+ timeUnit);


                while (i < laps.size()) {// laplar
                    Label lbls0 = new Label(0, 10 + i, String.valueOf(i + 1));
                    Label lbls = new Label(1, 10 + i, laps.get(i));// lap yazma
                    Label lbls2 = new Label(2, 10 + i, String.valueOf(dec.format(lapsval.get(i)*modul))); // cycle time yazma
                    sheet.addCell(lbls);
                    sheet.addCell(lbls0);
                    sheet.addCell(lbls2);
                    i++;
                }
                try {
                    sheet.addCell(lbl);
                    sheet.addCell(lbl1);
                    sheet.addCell(lbl2);
                    sheet.addCell(lbl3);
                    sheet.addCell(lbl4);
                    sheet.addCell(lbl5);
                    sheet.addCell(lbl6);
                    sheet.addCell(lbl7);
                    sheet.addCell(lbl8);
                    sheet.addCell(lbl9);
                    sheet.addCell(lbl10);
                    sheet.addCell(lbl11);
                    sheet.addCell(lbl12);
                    sheet.addCell(lbl13);
                    sheet.addCell(lbl14);
                    sheet.addCell(lbl15);
                    sheet.addCell(lbl16);
                    sheet.addCell(lbl17);
                    sheet.addCell(lbl18);


                } catch (RowsExceededException e) {
                    e.printStackTrace();
                } catch (WriteException e) {
                    e.printStackTrace();

                }
                workbook.write();

                try {
                    workbook.close();
                    Toast.makeText(context, "Your datas stored in DownLoad folder with name " + FILE_NAME, Toast.LENGTH_LONG).show();
                } catch (WriteException e) {

                    e.printStackTrace();
                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (RowsExceededException e) {
                e.printStackTrace();
            } catch (WriteException e) {
                e.printStackTrace();
            }
        }else{ Toast.makeText(context, "No lap to store ! ", Toast.LENGTH_SHORT).show(); }
    }
}

