package com.example.filemanager.Fragments;

import android.Manifest;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.PermissionRequest;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.filemanager.FileAdapter;
import com.example.filemanager.FileOpener;
import com.example.filemanager.OnFileSelectedListener;
import com.example.filemanager.R;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class InternalFragment extends Fragment implements OnFileSelectedListener {

    private RecyclerView recyclerView;
    private FileAdapter fileAdapter;
    private List<File> fileList;
    private ImageView img_back;
    private TextView tv_pathHolder;
    File storage;
    View view;
    String data;

    String[] items = {"Details", "Rename", "Delete", "Share"};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_internal, container, false);
        tv_pathHolder = view.findViewById(R.id.tv_pathholder);
        img_back = view.findViewById(R.id.img_back);

        String internalStorage = System.getenv("EXTERNAL_STORAGE");
        storage = new File(internalStorage);

        try {
            data = getArguments().getString("path");
            File file = new File(data);
            storage = file;
        } catch (Exception e) {
            e.printStackTrace();
        }

        tv_pathHolder.setText(storage.getAbsolutePath());
        runtimePermission();

        return view;
    }

    private void runtimePermission(){
        Dexter.withContext(getContext()).withPermissions(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ).withListener(new MultiplePermissionsListener() {

            @Override
            public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                displayFiles();
            }

            @Override
            public void onPermissionRationaleShouldBeShown(List<com.karumi.dexter.listener.PermissionRequest> list, PermissionToken permissionToken) {
                permissionToken.continuePermissionRequest();
            }

        }).check();
    }

    public ArrayList<File> findFiles(File file) {
        ArrayList<File> arrayList = new ArrayList<>();
        File[] files = file.listFiles();

        for (File singleFile : files) {
            if(singleFile.isDirectory() && !singleFile.isHidden()){
                arrayList.add(singleFile);
            }
        }
        for(File singleFile : files) {
            if(singleFile.getName().toLowerCase().endsWith(".jpeg") || singleFile.getName().toLowerCase().endsWith(".jpg") || singleFile.getName().toLowerCase().endsWith(".png")
                    || singleFile.getName().toLowerCase().endsWith(".mp3") || singleFile.getName().toLowerCase().endsWith(".wav") || singleFile.getName().toLowerCase().endsWith(".mp4")
                    || singleFile.getName().toLowerCase().endsWith(".pdf") || singleFile.getName().toLowerCase().endsWith(".apk") || singleFile.getName().toLowerCase().endsWith(".doc")) {
                arrayList.add(singleFile);
            }
        }
        return arrayList;
    }

    private void displayFiles() {
        recyclerView = view.findViewById(R.id.recycler_internal);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        fileList = new ArrayList<>();
        fileList.addAll(findFiles(storage));
        fileAdapter = new FileAdapter(getContext(), fileList, this);
        recyclerView.setAdapter(fileAdapter);
    }

    @Override
    public void onFileClicked(File file) {
        if(file.isDirectory()){
            Bundle bundle = new Bundle();
            bundle.putString("path", file.getAbsolutePath());
            InternalFragment internalFragment = new InternalFragment();
            internalFragment.setArguments(bundle);
            getFragmentManager().beginTransaction().replace(R.id.fragment_container, internalFragment).addToBackStack(null).commit();
        }
        else {
            try {
                FileOpener.openFile(getContext(), file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onFileLongClicked(File file) {
        final Dialog optionDialog = new Dialog(getContext());
        optionDialog.setContentView(R.layout.option_dialogue);
        optionDialog.setTitle("Select Options.");
        ListView options = (ListView) optionDialog.findViewById(R.id.List);
        CustomAdapter customAdapter = new CustomAdapter();
        options.setAdapter(customAdapter);
        optionDialog.show();
    }

    class CustomAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return items.length;
        }

        @Override
        public Object getItem(int position) {
            return items[position];
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View myView = getLayoutInflater().inflate(R.layout.option_layout, null);
            TextView txtOptions = myView.findViewById(R.id.txtOption);
            ImageView imgOptions = myView.findViewById(R.id.imgOption);
            txtOptions.setText(items[position]);
            if(items[position].equals("Details")){
                imgOptions.setImageResource(R.drawable.baseline_info_24);
            }
            else if(items[position].equals("Rename")){
                imgOptions.setImageResource(R.drawable.baseline_drive_file_rename_outline_24);
            }
            else if(items[position].equals("Delete")){
                imgOptions.setImageResource(R.drawable.baseline_delete_24);
            }
            else if(items[position].equals("Share")){
                imgOptions.setImageResource(R.drawable.baseline_ios_share_24);
            }
            return myView;
        }
    }

}
