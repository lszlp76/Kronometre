package com.lszlp.choronometre;

import static androidx.core.content.ContentProviderCompat.requireContext;

import static java.security.AccessController.getContext;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.text.InputFilter;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.core.content.FileProvider;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
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
    String timeUnit;
    private DecimalFormat dec = new DecimalFormat("#0.00");
    private EditText txt;
    private DecimalFormat decthree = new DecimalFormat("#0.000");
    private Date myDate = new Date();
    private DateFormat df = new SimpleDateFormat("ddMMyy@HHmm");
    private DateFormat df2 = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss ", Locale.ENGLISH);
    private String myString = df.format(myDate);
    private String xlString = df2.format(myDate);

    private String fileName = "";
    private  DecimalFormat currentDecimalFormat = new DecimalFormat();

    public String getTimeUnit() {
        return timeUnit;
    }
    public String getDecimalFormatPattern(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        int decimalCount = prefs.getInt(Constants.PREF_DECIMAL_PLACES, Constants.DEFAULT_DECIMAL_PLACES);
        Log.d("ExcelSave", "Shared Prefdeki değer: " + decimalCount);

        switch (decimalCount) {
            case 0:
                return "#0";
            case 1:
                return "#0.0";
            case 2:
                return "#0.00";
            case 3:
                return "#0.000";
            default:
                return "#0.0";
        }
    }




    public void setTimeUnit(String timeUnit) {
        this.timeUnit = timeUnit;
    }

    private void collectInput(Context context) {
        // convert edit text to string
        String getInput = txt.getText().toString();

        // ensure that user input bar is not empty
        if (getInput == null || getInput.trim().equals("")) {
            Toast.makeText(context, "Please enter your file name!", Toast.LENGTH_LONG).show();
        }

        // add input into an data collection arraylist
        else {
            //arrayListCollection.add(getInput);
            fileName = getInput;
            //  adapter.notifyDataSetChanged();
        }
    }
    public void share(Context context,String fileName){


        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);// bulunduğu folder
        File folder = new File(path, "IndustrialChronometer");
        if (!folder.exists()) {
           Toast.makeText(context,"No files to share !",Toast.LENGTH_LONG).show();

        }else{
            //Toast.makeText(context,"Files to share !",Toast.LENGTH_LONG).show();
            Uri fileToShareURI = null;

            File file = new File(folder, fileName);
            if (!file.exists()) {
                Toast.makeText(context, "File not found!", Toast.LENGTH_LONG).show();
                return;
            }
            file.setReadable(true, true);

           // fileToShareURI = Uri.parse(file.toString());
            fileToShareURI = FileProvider.getUriForFile(context,"com.lszlp.choronometre.fileprovider",file);
            Intent intent = new Intent(android.content.Intent.ACTION_SEND);
            intent.setType("application/vnd.ms-excel");
            intent.putExtra(Intent.EXTRA_STREAM, fileToShareURI); //Uri.fromFile(file));
            intent.putExtra(Intent.EXTRA_SUBJECT, "Industrial Chronometer App.send "+fileName);
            intent.putExtra(Intent.EXTRA_TEXT, "This file sent by Industrial Chronometer app. \nHave a nice day!");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            context.startActivity((Intent.createChooser(intent, "Share File : ")));



        }
        ;
    }
    public void delete(Context context, String fileName){
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);// bulunduğu folder
        System.out.println("Path : "+path.toString());
        File folder = new File(path, "IndustrialChronometer");
        File file = new File(folder, fileName);
        file.delete();
    }
    public void save(Context context, String timeUnit, ArrayList<String> laps, ArrayList<Double> lapsval,
                     Double ave, int modul, String totalStudyTime, double cycPerHour, double cycPerMinute,
                     ArrayList<Lap> lapsArray, String fileName) {

        String pattern = getDecimalFormatPattern(context);
        currentDecimalFormat = new DecimalFormat(pattern);

        if (laps == null || laps.isEmpty()) {
            Toast.makeText(context, "No laps to save!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (fileName == null || fileName.trim().isEmpty()) {
            Toast.makeText(context, "Please enter a file name!", Toast.LENGTH_SHORT).show();
            return;
        }

        String FILE_NAME = fileName + ".xls";
        ByteArrayOutputStream outputStreamBuffer = new ByteArrayOutputStream();

        try {
            WorkbookSettings wbSettings = new WorkbookSettings();
            wbSettings.setLocale(new Locale("en", "EN"));

            WritableWorkbook workbook = Workbook.createWorkbook(outputStreamBuffer, wbSettings);
            WritableSheet sheet = workbook.createSheet(fileName + " Time Study", 0);

            // Headers
            Label lbl = new Label(0, 0, fileName + " TIME STUDY DATA REPORT");
            Label lbl1 = new Label(0, 1, "Date");
            Label lbl2 = new Label(0, 2, "Time Unit");
            Label lbl3 = new Label(0, 3, "Total Study Time :");
            Label lbl4 = new Label(0, 4, "Maximum Cycle Time : ");
            Label lbl5 = new Label(0, 5, "Lap number of Max.Cyc.Time : ");
            Label lbl6 = new Label(0, 6, "Minimum Cycle Time : ");
            Label lbl7 = new Label(0, 7, "Lap number of Min.Cyc.Time : ");
            Label lbl8 = new Label(0, 8, "Average Cycle Time : ");
            Label lblCycPerHour = new Label(0, 9, "Cyc.per Hour :");
            Label lblCycPerMinute = new Label(0, 10, "Cyc.per Minute :");
            Label no = new Label(0, 11, "Lap No :");
            Label lbl9 = new Label(1, 11, "Laps Value:");
            Label lbl10 = new Label(2, 11, "Cycle Time [" + timeUnit + "] :");
            Label lbl10_ = new Label(3, 11, "Notes:");

            DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.ENGLISH);
            String xlString = df.format(new Date());

            // Header values
            Label lbl11 = new Label(1, 1, xlString);
            Label lbl12 = new Label(1, 2, timeUnit);
            Label lbl13 = new Label(1, 3, totalStudyTime);
            Label lbl14 = new Label(1, 4, currentDecimalFormat.format(Collections.max(lapsval) * modul) + " " + timeUnit);
            Label lbl15 = new Label(1, 5, String.valueOf(lapsval.indexOf(Collections.max(lapsval))));
            Label lbl16 = new Label(1, 6, currentDecimalFormat.format(Collections.min(lapsval) * modul) + " " + timeUnit);
            Label lbl17 = new Label(1, 7, String.valueOf(lapsval.indexOf(Collections.min(lapsval))));
            Label lbl18 = new Label(1, 8, currentDecimalFormat.format(ave * modul) + " " + timeUnit);
            Label lbl19 = new Label(1, 9, currentDecimalFormat.format(cycPerHour) + " cyc/hour");
            Label lbl20 = new Label(1, 10, currentDecimalFormat.format(cycPerMinute) + " cyc/minute");

            // Write headers
            sheet.addCell(lbl);
            sheet.addCell(lbl1);
            sheet.addCell(lbl2);
            sheet.addCell(lbl3);
            sheet.addCell(lbl4);
            sheet.addCell(lbl5);
            sheet.addCell(lbl6);
            sheet.addCell(lbl7);
            sheet.addCell(lbl8);
            sheet.addCell(lblCycPerHour);
            sheet.addCell(lblCycPerMinute);
            sheet.addCell(lbl9);
            sheet.addCell(lbl10);
            sheet.addCell(lbl10_);
            sheet.addCell(lbl11);
            sheet.addCell(lbl12);
            sheet.addCell(lbl13);
            sheet.addCell(lbl14);
            sheet.addCell(lbl15);
            sheet.addCell(lbl16);
            sheet.addCell(lbl17);
            sheet.addCell(lbl18);
            sheet.addCell(lbl19);
            sheet.addCell(lbl20);

            // Lap data
            for (int i = 0; i < laps.size(); i++) {
                Label lbls0 = new Label(0, 12 + i, String.valueOf(i + 1));
                Label lbls = new Label(1, 12 + i, laps.get(i));
                Label lbls2 = new Label(2, 12 + i, currentDecimalFormat.format((lapsval.get(i) * modul)));
                Label lbls3 = new Label(3, 12 + i, lapsArray.get(i).message);
                sheet.addCell(lbls0);
                sheet.addCell(lbls);
                sheet.addCell(lbls2);
                sheet.addCell(lbls3);
            }

            workbook.write();
            workbook.close();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Excel creation failed!", Toast.LENGTH_SHORT).show();
            return;
        }

        // ✅ Save into Downloads/IndustrialChoronometer using MediaStore
        try {
            ContentResolver resolver = context.getContentResolver();
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.Downloads.DISPLAY_NAME, FILE_NAME);
            contentValues.put(MediaStore.Downloads.MIME_TYPE, "application/vnd.ms-excel");
            contentValues.put(MediaStore.Downloads.RELATIVE_PATH,
                    Environment.DIRECTORY_DOWNLOADS + "/IndustrialChronometer");

            Uri fileUri = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/IndustrialChronometer");
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 1);
                fileUri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues);

                if (fileUri != null) {
                    try (OutputStream out = resolver.openOutputStream(fileUri)) {
                        outputStreamBuffer.writeTo(out);
                    }
                    contentValues.clear();
                    contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0);
                    resolver.update(fileUri, contentValues, null, null);
                }
            }
            if (fileUri != null) {
                try (OutputStream out = resolver.openOutputStream(fileUri)) {
                    outputStreamBuffer.writeTo(out);
                }

                Toast.makeText(context,
                        "Saved in Downloads/IndustrialChronometer as " + FILE_NAME,
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(context, "Failed to create file in Downloads", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Save failed!", Toast.LENGTH_SHORT).show();
        }
    }

}

