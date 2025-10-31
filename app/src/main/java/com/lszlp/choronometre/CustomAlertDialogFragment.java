package com.lszlp.choronometre;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class CustomAlertDialogFragment extends DialogFragment {

    // Argüman Anahtarları
    private static final String ARG_ACTION = "action";
    private static final String ARG_FILENAME = "fileName";
    private static final String ARG_POSITION = "position";
    private static final String ARG_LAP_NUMBER = "lapNumber"; // Bunu ekleyin
    private static final String ARG_NOTE_TEXT = "noteText";   // Bunu ekleyin
    // Sınıf Değişkenleri
    private CustomDialogListener listener;
    private ChronometerAction currentAction;
    private EditText fileNameInput;
    private EditText noteInput;

    // Diyalog Tiplerini tanımlıyoruz
    public enum ChronometerAction {
        RESET,
        SAVE,
        DELETE,
        ADD_NOTE
    }

    // Geri çağırma (Callback) arayüzü
    public interface CustomDialogListener {
        void onResetConfirmed();
        void onSaveConfirmed(String fileName);
        void onCancelled();
        void onDeleteConfirmed(int position, String fileName);
        void onNoteSaved(int position, int lapNumber, String noteText); // Bunu ekleyin
        // Not: onPositiveClick(String tag) ve onNegativeClick(String tag) metotları
        // daha spesifik metotlar (onResetConfirmed, onCancelled vb.) olduğu için
        // temizlik amacıyla kaldırılabilir. Eğer başka yerlerde kullanılıyorsa geri ekleyin.
    }

    // =================================================================
    // FABRİKA METOTLARI (newInstance)
    // =================================================================

    // RESET Eylemi için fabrika metodu (Parametresiz)
    public static CustomAlertDialogFragment newInstanceForReset() {
        CustomAlertDialogFragment fragment = new CustomAlertDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_ACTION, ChronometerAction.RESET);
        fragment.setArguments(args);
        return fragment;
    }

    // SAVE Eylemi için fabrika metodu (Parametresiz)
    public static CustomAlertDialogFragment newInstanceForSave() {
        CustomAlertDialogFragment fragment = new CustomAlertDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_ACTION, ChronometerAction.SAVE);
        fragment.setArguments(args);
        return fragment;
    }

    // DELETE Eylemi için fabrika metodu (Pozisyon ve Dosya Adı ile)
    public static CustomAlertDialogFragment newInstanceForDelete(int position, String fileName) {
        CustomAlertDialogFragment fragment = new CustomAlertDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_ACTION, ChronometerAction.DELETE);
        args.putInt(ARG_POSITION, position);
        args.putString(ARG_FILENAME, fileName);
        fragment.setArguments(args);
        return fragment;
    }
    // NOTE EKLEMLEK İÇİN
    // CustomAlertDialogFragment.java içine bu yeni metodu ekleyin
    public static CustomAlertDialogFragment newInstanceForNote(int position, int lapNumber, String currentNote) {
        CustomAlertDialogFragment fragment = new CustomAlertDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_ACTION, ChronometerAction.ADD_NOTE);
        args.putInt(ARG_POSITION, position);
        args.putInt(ARG_LAP_NUMBER, lapNumber);
        args.putString(ARG_NOTE_TEXT, currentNote);
        fragment.setArguments(args);
        return fragment;
    }
    // =================================================================
    // YAŞAM DÖNGÜSÜ METOTLARI
    // =================================================================

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).drawer.setAlpha(1.f); // Drawer'ı tekrar görünür yapar
        }
        Log.d("DialogFragment", "onDestroy() called");
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            // 1. Önce, çağıran Fragment'a bağlanmayı dene (getParentFragment() kullanarak)
            if (getParentFragment() != null && getParentFragment() instanceof CustomDialogListener) {
                listener = (CustomDialogListener) getParentFragment();
            }
            // 2. Eğer Fragment yoksa veya uygun değilse, Activity'ye bağlanmayı dene
            else if (context instanceof CustomDialogListener) {
                listener = (CustomDialogListener) context;
            }
            // 3. Hiçbir yerde bulamazsak hata fırlat
            else {
                throw new ClassCastException(context.toString() + " must implement CustomDialogListener");
            }
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement CustomDialogListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Özel Layout'u şişir
        return inflater.inflate(R.layout.custom_alert_dialog, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Argümanları al
        currentAction = (ChronometerAction) getArguments().getSerializable(ARG_ACTION);
        final int position = getArguments().getInt(ARG_POSITION, -1);
        final String fileName = getArguments().getString(ARG_FILENAME);

        // Görünüm bileşenlerini bağla
        TextView titleView = view.findViewById(R.id.dialogTitle);
        TextView messageView = view.findViewById(R.id.dialogMessage);
        fileNameInput = view.findViewById(R.id.dialogFileNameInput);
        noteInput = view.findViewById(R.id.dialogNoteInput);
        Button btnPositive = view.findViewById(R.id.btnPositive);
        Button btnNegative = view.findViewById(R.id.btnNegative);

        // ===============================================
        // AŞAMA 1: Eyleme Göre Diyalog İçeriğini Ayarlama
        // ===============================================

        if (currentAction == ChronometerAction.SAVE) {
            titleView.setText("SAVE FILE");
            messageView.setText("Please enter your file name:");
            btnPositive.setText("SAVE");
            fileNameInput.setVisibility(View.VISIBLE);

        } else if (currentAction == ChronometerAction.RESET) {
            titleView.setText("RESET ALL DATA");
            messageView.setText("You'll will lose all data. Are you sure?");
            btnPositive.setText("RESET");
            fileNameInput.setVisibility(View.GONE);

        } else if (currentAction == ChronometerAction.DELETE) {
            titleView.setText("DELETE FILE");
            messageView.setText(fileName + " file will be deleted permenantly. Are you sure?");
            btnPositive.setText("DELETE");
            fileNameInput.setVisibility(View.GONE);
        } else if (currentAction == ChronometerAction.ADD_NOTE) {
            int lapNumber = getArguments().getInt(ARG_LAP_NUMBER);
            String currentNote = getArguments().getString(ARG_NOTE_TEXT, "");

            titleView.setText("Add notes for Lap " + lapNumber);
            messageView.setVisibility(View.GONE); // Mesaj alanını gizle

            fileNameInput.setHint("Add notes here..."); // EditText'in hint'ini değiştir
            fileNameInput.setText(currentNote); // Mevcut notu EditText'e ata
            fileNameInput.setVisibility(View.VISIBLE); // EditText'i göster
            btnPositive.setText("SAVE NOTE");
        }

        // ==================================================
        // AŞAMA 2: Tıklama Olaylarını Yönetme
        // ==================================================

        // Pozitif butona tıklama olayı
        btnPositive.setOnClickListener(v -> {
            if (currentAction == ChronometerAction.SAVE) {
                String dosyaAdi = fileNameInput.getText().toString().trim();

                if (dosyaAdi.isEmpty()) {
                    fileNameInput.setError("File name cannot be empty!");
                    return;
                }
                listener.onSaveConfirmed(dosyaAdi);

            } else if (currentAction == ChronometerAction.RESET) {
                listener.onResetConfirmed();

            } else if (currentAction == ChronometerAction.DELETE) {
                listener.onDeleteConfirmed(position, fileName);
            } else if (currentAction == ChronometerAction.ADD_NOTE) {
                int pos = getArguments().getInt(ARG_POSITION);
                int lapNum = getArguments().getInt(ARG_LAP_NUMBER);
                String noteText = fileNameInput.getText().toString();
                listener.onNoteSaved(pos, lapNum, noteText);
            }
            // Tüm başarılı işlemlerden sonra diyaloğu kapat
            dismiss();
        });

        // Negatif butona tıklama olayı (Tüm eylemler için aynı: İptal)
        btnNegative.setOnClickListener(v -> {
            listener.onCancelled();
            dismiss();
        });
    }

    // Başlık çubuğunu kaldırmak ve pencere stilini ayarlamak için
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        if (dialog.getWindow() != null) {
            // Başlık çubuğunu kaldırır
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);

            // Diyalog penceresinin varsayılan arka planını şeffaf yapar
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        return dialog;


    }
}