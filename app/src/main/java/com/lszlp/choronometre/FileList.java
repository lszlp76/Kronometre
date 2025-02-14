package com.lszlp.choronometre;

import android.app.AlertDialog;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FileList#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FileList extends Fragment {

    RecyclerView fileList;
    ExcelSave excelSave = new ExcelSave();

    public FileList() {
        // Required empty public constructor
    }


    // TODO: Rename and change types and number of parameters
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
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_file_list, container, false);

    }

    public void getFiles() {

    }

    /**
     * bu metod özel
     **/
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {

            fileList = getView().findViewById(R.id.fileList);
            ArrayList<String> pathArray = new ArrayList<String>();
            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);// bulunduğu folder
            File folder = new File(path, "IndustrialChoronometer");
            if (!folder.exists()) {
                ;

            } else {
                File file = new File(String.valueOf(path + "/IndustrialChoronometer"));
                File[] listFiles = file.listFiles();
                Arrays.sort(listFiles, new Comparator() {
                    public int compare(Object o1, Object o2) {

                        if (((File) o1).lastModified() > ((File) o2).lastModified()) {
                            return -1;
                        } else if (((File) o1).lastModified() < ((File) o2).lastModified()) {
                            return +1;
                        } else {
                            return 0;
                        }
                    }

                });
                for (int i = 0; i < listFiles.length; i++) {

                    if (listFiles[i].isFile() && (listFiles[i].getName().endsWith(".xls"))) {
                        Date date = new Date(listFiles[i].getParentFile().lastModified());
                        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                        String formattedDate = formatter.format(date);

                        pathArray.add(listFiles[i].getName());

                    }

                }

            }

//           ArrayAdapter arrayAdapter = new ArrayAdapter(getActivity(),  R.layout.customfilelistrow,R.id.listviewrow, pathArray);
//            fileList.setAdapter(arrayAdapter);
//            arrayAdapter.notifyDataSetChanged();
//            ;
//
//            fileList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//                @Override
//                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                    String fileName = pathArray.get(i);
//                    excelSave.share(getActivity(), fileName);
//                }
//            });
//
//            fileList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
//                @Override
//                public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
//                    String fileName = pathArray.get(i);
//
//                    {
//
//                        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.AlertDialogCustom));
//                        builder.setTitle("Delete File");
//                        builder.setMessage("Are you sure to delete " + fileName + " permanently ?");
//
//                        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialogInterface, int p) {
//                                System.out.println("No ya basıldı");
//                            }
//                        });
//                        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialogInterface, int p) {
//                                pathArray.remove(i);
//                                excelSave.delete(getActivity(), fileName);
//                                arrayAdapter.notifyDataSetChanged();
//                            }
//                        });
//
//                        builder.show();
//                    }
//
//
//                    return true;
//                }
//            });


            // ListView yerine RecyclerView kullanımına geçiş
            RecyclerView recyclerView = getView().findViewById(R.id.fileList);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            FileListAdapter adapter = new FileListAdapter(pathArray);
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

                    AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.AlertDialogCustom));
                    builder.setTitle("Delete");
                    builder.setMessage("Are you sure to delete " + fileName + " permanently ?");

                    builder.setNegativeButton("No", (dialog, which) -> {
                        adapter.notifyItemChanged(position);
                    });

                    builder.setPositiveButton("Yes", (dialog, which) -> {
                        pathArray.remove(position);
                        excelSave.delete(getActivity(), fileName);
                        adapter.notifyItemRemoved(position);

                        Snackbar.make(recyclerView, "File deleted", Snackbar.LENGTH_LONG)
                                .setAction("Undo", v -> {
                                    pathArray.add(position, fileName);
                                    adapter.notifyItemInserted(position);
                                }).show();
                    });

                    builder.show();
                }
            });



            // Kaydırma işlemlerini yönetecek ItemTouchHelper
//            ItemTouchHelper.SimpleCallback swipeCallback = new ItemTouchHelper.SimpleCallback(0,
//                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
//
//                @Override
//                public boolean onMove(@NonNull RecyclerView recyclerView,
//                                    @NonNull RecyclerView.ViewHolder viewHolder,
//                                    @NonNull RecyclerView.ViewHolder target) {
//                    return false;
//                }
//
//                @Override
//                public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
//                    int position = viewHolder.getAdapterPosition();
//                    String fileName = pathArray.get(position);
//
//                    if (direction == ItemTouchHelper.LEFT) {
//                        // Sola kaydırma - Silme işlemi
//                        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.AlertDialogCustom));
//                        builder.setTitle("Delete");
//                        builder.setMessage("Are you sure to delete " + fileName + " permanently ?");
//
//                        builder.setNegativeButton("No", (dialog, which) -> {
//                            adapter.notifyItemChanged(position);
//                        });
//
//                        builder.setPositiveButton("Yes", (dialog, which) -> {
//                            pathArray.remove(position);
//                            excelSave.delete(getActivity(), fileName);
//                            adapter.notifyItemRemoved(position);
//
//                            Snackbar.make(recyclerView, "File deleted", Snackbar.LENGTH_LONG)
//                                .setAction("Undo", v -> {
//                                    pathArray.add(position, fileName);
//                                    adapter.notifyItemInserted(position);
//                                }).show();
//                        });
//
//                        builder.show();
//                    } else if (direction == ItemTouchHelper.RIGHT) {
//                        // Sağa kaydırma - Paylaşma işlemi
//                        excelSave.share(getActivity(), fileName);
//                        adapter.notifyItemChanged(position);
//                    }
//                }
//
//                @Override
//                public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
//                                      @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY,
//                                      int actionState, boolean isCurrentlyActive) {
//
//                    new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
//
//                            .addSwipeLeftBackgroundColor(Color.RED)
//                        .addSwipeLeftActionIcon(R.drawable.ic_delete)
//                            .setSwipeLeftLabelColor(Color.rgb(255, 255, 255))
//                            .addSwipeLeftLabel("Delete")
//
//                            .addSwipeRightBackgroundColor(Color.rgb(0, 150, 136))
//                        .addSwipeRightActionIcon(R.drawable.ic_send)
//                            .addSwipeRightLabel("Share")
//                            .setSwipeRightLabelColor(Color.rgb(255, 255, 255))
//                            .create()
//
//                        .decorate();
//
//                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
//                }
//            };
//
//            new ItemTouchHelper(swipeCallback).attachToRecyclerView(recyclerView);
       }

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

    }
}

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
        return
                files.size();
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
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mListener !=null){
                        int position = getBindingAdapterPosition();
                        if (position != RecyclerView.NO_POSITION){
                            mListener.onDeleteFile(position);
                        }
                    }
                }
            });
            share.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null){
                        int position = getBindingAdapterPosition();
                        if (position != RecyclerView.NO_POSITION){
                            mListener.onShareFile(position);

                    }
                }
            };
        });
    }
}}