package com.lszlp.choronometre;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FileList#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FileList extends Fragment {

    ListView fileList;
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
        System.out.println("onCreat calıştı");


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        System.out.println("onCreateView çalıştı");
        return inflater.inflate(R.layout.fragment_file_list, container, false);

    }
public void getFiles(){

}
/** bu metod özel**/
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser){
            System.out.println("set user  çalıştı");
            fileList =getView().findViewById(R.id.fileList);
            ArrayList<String> pathArray = new ArrayList<String>();
            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);// bulunduğu folder
            File folder = new File(path, "IndustrialChoronometer");
            if (!folder.exists()) {
                ;

            }
            else {
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
                        pathArray.add(listFiles[i].getName());

                    }

                }

            }
            ArrayAdapter arrayAdapter = new ArrayAdapter(getActivity(),android.R.layout.simple_list_item_1,pathArray);
            fileList.setAdapter(arrayAdapter);
            arrayAdapter.notifyDataSetChanged();
            ;

            fileList.setOnItemClickListener(new AdapterView.OnItemClickListener(){
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    String fileName = pathArray.get(i);
                    excelSave.share(getActivity(),fileName);
                }
            });

            fileList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                    String fileName = pathArray.get(i);

                    {

                        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.AlertDialogCustom));
                        builder.setTitle("Delete File");
                        builder.setMessage("Are you sure to delete "+fileName+" permanently ?");

                        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int p) {
                                System.out.println("No ya basıldı");
                            }
                        });
                        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int p) {
                                pathArray.remove(i);
                                excelSave.delete(getActivity(),fileName);
                                arrayAdapter.notifyDataSetChanged();
                            }
                        });

                        builder.show();
                    }


                    return false;
                }
            });
        }
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        System.out.println("onViewCreated çalıştı");
        fileList =view.findViewById(R.id.fileList);
        ArrayList<String> pathArray = new ArrayList<String>();
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);// bulunduğu folder
        File folder = new File(path, "IndustrialChoronometer");
        if (!folder.exists()) {
            ;

        }
        else {
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
                    pathArray.add(listFiles[i].getName());

                }

            }

        }
        ArrayAdapter arrayAdapter = new ArrayAdapter(getActivity(),android.R.layout.simple_list_item_1,pathArray);
        fileList.setAdapter(arrayAdapter);
        fileList.isLongClickable();
        arrayAdapter.notifyDataSetChanged();
        ;

        fileList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                String fileName = pathArray.get(i);

                {

                    AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.AlertDialogCustom));
                    builder.setTitle("Delete File");
                    builder.setMessage("Are you sure to delete " + fileName + " permanently ?");

                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int p) {
                            System.out.println("No ya basıldı");
                        }
                    });
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int p) {
                            pathArray.remove(i);
                            excelSave.delete(getActivity(), fileName);
                            arrayAdapter.notifyDataSetChanged();
                        }
                    });

                    builder.show();
                }

                return true;//bundan sonra başka clcik olmasın diye true
            }
        });
        fileList.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String fileName = pathArray.get(i);
                excelSave.share(getActivity(),fileName);
            }
        });
}
}