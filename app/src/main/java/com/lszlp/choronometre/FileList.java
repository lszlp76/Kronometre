package com.lszlp.choronometre;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FileList#newInstance} factory method to
 * create an instance of fragment.
 */
public class FileList extends Fragment
        implements CustomAlertDialogFragment.CustomDialogListener { // 1. Listener Eklendi

    // Sınıf Değişkenleri (RecyclerView ve Listeler)
    RecyclerView fileList;
    ExcelSave excelSave = new ExcelSave();
    private ArrayList<String> pathArray = new ArrayList<>(); // 2. Sınıf Değişkeni yapıldı
    private FileListAdapter adapter; // 2. Sınıf Değişkeni yapıldı

    public FileList() {
        // Required empty public constructor
    }

    public static FileList newInstance() {
        return new FileList();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_file_list, container, false);
    }

    // onViewCreated metodunu kullanarak View binding'i daha güvenli hale getirelim.
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fileList = view.findViewById(R.id.fileList);

        // setUserVisibleHint içinde yapılan dosya yükleme mantığını buraya taşıyabiliriz
        // veya setUserVisibleHint'i kullanmaya devam edebiliriz.
        // Mevcut yapıda dosya yükleme setUserVisibleHint'te kalmıştır.
    }

    /**
     * bu metod özel (Fragment görünür olduğunda çalışır)
     **/
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && getView() != null) {

            loadFiles(this.getActivity()); // Dosya yükleme mantığını ayrı bir metoda taşıdık

            // ListView yerine RecyclerView kullanımına geçiş
            RecyclerView recyclerView = getView().findViewById(R.id.fileList);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

            // Eğer adapter zaten oluşturulmuşsa tekrar oluşturma
            if (adapter == null) {
                adapter = new FileListAdapter(pathArray);
            }
            recyclerView.setAdapter(adapter);
            adapter.notifyDataSetChanged();

            adapter.setmListener(new FileListAdapter.onItemClickListener() {
                @Override
                public void onShareFile(int position) {
                    String fileName = pathArray.get(position);
                    excelSave.share(getActivity(), fileName);
                    adapter.notifyItemChanged(position);
                }

                @Override
                public void onDeleteFile(int position) {
                    String fileName = pathArray.get(position);
                    showDeleteDialog(position, fileName);
                }
            });
            // =======================================================
            // KAYDIRMA (SWIPE) İŞLEMİNİ EKLEME/AKTİFLEŞTİRME
            // =======================================================
            ItemTouchHelper.SimpleCallback swipeCallback = new ItemTouchHelper.SimpleCallback(0,
                    ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

                @Override
                public boolean onMove(@NonNull RecyclerView recyclerView,
                                      @NonNull RecyclerView.ViewHolder viewHolder,
                                      @NonNull RecyclerView.ViewHolder target) {
                    return false;
                }

                @Override
                public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                    int position = viewHolder.getAdapterPosition();

                    // Position -1 olası hatası kontrolü
                    if (position == RecyclerView.NO_POSITION) {
                        adapter.notifyDataSetChanged(); // UI'ı yenile
                        return;
                    }

                    String fileName = pathArray.get(position);

                    if (direction == ItemTouchHelper.LEFT) {
                        // Sola kaydırma - Silme işlemi -> Custom Dialog çağrılır
                        showDeleteDialog(position, fileName);

                        // ÖNEMLİ: Diyalog gösterildiği için, kayan öğeyi geri döndürmeliyiz.
                        // Aksi takdirde öğe kaybolur ve diyaloğu iptal edersek sıkıntı olur.
                        adapter.notifyItemChanged(position);

                    } else if (direction == ItemTouchHelper.RIGHT) {
                        // Sağa kaydırma - Paylaşma işlemi (Direkt yapılır)
                        excelSave.share(getActivity(), fileName);
                        adapter.notifyItemChanged(position); // Kaydırma bittikten sonra öğeyi eski yerine getirir
                    }
                }

                // Kaydırma sırasında görsel dekorasyonu sağlar
                @Override
                public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                                        @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                        int actionState, boolean isCurrentlyActive) {

                    new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)

                            .addSwipeLeftBackgroundColor(Color.RED)
                            .addSwipeLeftActionIcon(R.drawable.ic_delete) // ic_delete mevcut olmalı
                            .setSwipeLeftLabelColor(Color.rgb(255, 255, 255))
                            .addSwipeLeftLabel("DELETE")

                            .addSwipeRightBackgroundColor(Color.rgb(0, 150, 136)) // Teal tonu
                            .addSwipeRightActionIcon(R.drawable.ic_send) // ic_send mevcut olmalı
                            .addSwipeRightLabel("SHARE")
                            .setSwipeRightLabelColor(Color.rgb(255, 255, 255))
                            .create()
                            .decorate();

                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                }
            };

            new ItemTouchHelper(swipeCallback).attachToRecyclerView(recyclerView);

            }
    }
    private void loadFiles(Context context) {
        pathArray.clear();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // ✅ Scoped storage: use MediaStore API
            ContentResolver resolver = context.getContentResolver();
            String selection = MediaStore.Downloads.RELATIVE_PATH + " LIKE ?";
            String[] selectionArgs = new String[]{"Download/IndustrialChronometer/%"};

            String[] projection = {
                    MediaStore.Downloads._ID,
                    MediaStore.Downloads.DISPLAY_NAME,
                    MediaStore.Downloads.DATE_MODIFIED
            };

            try (Cursor cursor = resolver.query(
                    MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                    projection,
                    selection,
                    selectionArgs,
                    MediaStore.Downloads.DATE_MODIFIED + " DESC"
            )) {
                if (cursor != null) {
                    int nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Downloads.DISPLAY_NAME);
                    while (cursor.moveToNext()) {
                        String name = cursor.getString(nameColumn);
                        if (name.toLowerCase(Locale.ROOT).endsWith(".xls")) {
                            pathArray.add(name);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            // ✅ Legacy Android (pre-Android 10)
            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File folder = new File(path, "IndustrialChronometer");

            if (folder.exists()) {
                File[] listFiles = folder.listFiles();
                if (listFiles != null) {
                    Arrays.sort(listFiles, (o1, o2) -> Long.compare(o2.lastModified(), o1.lastModified()));
                    for (File f : listFiles) {
                        if (f.isFile() && f.getName().toLowerCase(Locale.ROOT).endsWith(".xls")) {
                            pathArray.add(f.getName());
                        }
                    }
                }
            }
        }

        if (pathArray.isEmpty()) {
            Log.w("FileList", "No Excel files found in Downloads/IndustrialChronometer");
        } else {
            Log.d("FileList", "Loaded " + pathArray.size() + " Excel files");
        }
    }



    private void showDeleteDialog(int position, String fileName) {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).drawer.setAlpha(0.2f); // Drawer'ı soluklaştır
        }
        CustomAlertDialogFragment dialog = CustomAlertDialogFragment.newInstanceForDelete(
                position,
                fileName
        );

        // Düzeltme: getParentFragmentManager() yerine getChildFragmentManager() kullanıldı
        // veya getParentFragmentManager() kullanılıyorsa Düzeltme 1'deki mantık değişir.
        // En temiz ve garanti yöntem, Fragment içinden çağrılırken getChildFragmentManager() kullanmaktır.

        dialog.show(getChildFragmentManager(), "DELETE_DIALOG_TAG");
        // NOT: Eğer MainActivity'den çağrılıyorsa getParentFragmentManager() doğru olabilir,
        // ancak Fragment'ın kendi içinden çağrıldığı için getChildFragmentManager() listener'ı bulma şansını artırır.
        // Eğer ViewPager kullanıyorsanız, getParentFragmentManager() daha doğru olabilir.
        // En güvenlisi, Düzeltme 1'deki onAttach mantığını kullanıp, burada getFragmentManager() kullanmaktır.
    }

    // =================================================================
    // 4. CustomAlertDialogFragment.CustomDialogListener IMPLEMENTASYONU
    // =================================================================

    @Override
    public void onDeleteConfirmed(int position, String fileName) {
        // Silme işlemi onaylandığında burası çalışır

        if (adapter != null && position >= 0 && position < pathArray.size()) {

            // Bu satırın çalıştığından emin olmak için log veya Toast ekleyin
            // Toast.makeText(getContext(), "Silme Onaylandı: " + fileName, Toast.LENGTH_SHORT).show();

            // Dosyayı sistemden sil
            excelSave.delete(getActivity(), fileName);

            // Listeden kaldır ve UI'ı güncelle
            pathArray.remove(position);
            adapter.notifyItemRemoved(position);

            // Snackbar ile geri alma seçeneği (UNDO)
            Snackbar.make(fileList, "File deleted: " + fileName, Snackbar.LENGTH_LONG)
                    .setAction("Undo", v -> {
                        // Geri alma işlemi (varsayımsal geri alma fonksiyonu yoksa excelSave sınıfınızda oluşturulmalıdır)
                        // Şimdilik sadece listeye geri ekleyip UI'ı güncelliyoruz.
                        pathArray.add(position, fileName);
                        adapter.notifyItemInserted(position);
                    }).show();
        }
    }

    @Override
    public void onNoteSaved(int position, int lapNumber, String noteText) {

    }

    @Override
    public void onDeleteLap(int position, int lapNumber) {

    }

    // Bu Fragment'ta kullanılmayan metotlar boş bırakılabilir
    @Override
    public void onResetConfirmed() {
        // Timer Fragment'ta kullanılır.
    }

    @Override
    public void onSaveConfirmed(String fileName) {
        // Main Activity'de veya Timer Fragment'ta kullanılır.
    }

    @Override
    public void onCancelled() {
        // Kullanıcı iptal ettiğinde RecyclerView'ın yerini korumak için
        // adapter.notifyDataSetChanged() veya adapter.notifyItemRangeChanged() çağrılabilir.
    }




}

// =================================================================
// FileListAdapter Sınıfı (Değişiklik yapılmadı)
// =================================================================

/**
 * Özel RecyclerView Adapter sınıfı
 */
class FileListAdapter extends RecyclerView.Adapter<FileListAdapter.ViewHolder> {
    private ArrayList<String> files;
    private onItemClickListener mListener;

    public FileListAdapter(ArrayList<String> files) {
        this.files = files;
    }

    public void setmListener(onItemClickListener mListener) {
        this.mListener = mListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.customfilelistrow, parent, false);
        return new ViewHolder(view,mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.textView.setText(files.get(position));
    }

    @Override
    public int getItemCount() {
        return files.size();
    }

    public interface onItemClickListener{
        void onShareFile (int position);
        void onDeleteFile (int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        ImageView share,delete;

        ViewHolder(View view, onItemClickListener mListener) {
            super(view);
            textView = view.findViewById(R.id.listviewrow);
            share = view.findViewById(R.id.share);
            delete = view.findViewById(R.id.delete);

            delete.setOnClickListener(v -> {
                if(mListener !=null){
                    int position = getBindingAdapterPosition();
                    if (position != RecyclerView.NO_POSITION){
                        mListener.onDeleteFile(position);
                    }
                }
            });

            share.setOnClickListener(v -> {
                if (mListener != null){
                    int position = getBindingAdapterPosition();
                    if (position != RecyclerView.NO_POSITION){
                        mListener.onShareFile(position);
                    }
                }
            });
        }
    }
}