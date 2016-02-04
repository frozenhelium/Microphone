package com.fhx.microphone;

import android.os.Environment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.File;
import java.io.FilenameFilter;
import java.util.List;

/**
 * Created by fhx on 2/4/16.
 */
public class FileAdapter extends RecyclerView.Adapter<FileAdapter.FileViewHolder> {

    private String[] mFiles;

    public FileAdapter(){
        this.loadFileNames();
    }

    public void loadFileNames(){

        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            Log.e("Microphone", "Failed to detect External Storage");
        } else {
            File appDir = new File(Environment.getExternalStorageDirectory()+File.separator+"Microphone");
            if(appDir.exists()) {
                FilenameFilter wavFilter = new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String filename) {
                        return filename.endsWith(".wav");
                    }
                };
                mFiles = appDir.list(wavFilter).clone();
            }
        }
    }

    @Override
    public int getItemCount() {
        if(mFiles == null)
            return 0;
        return mFiles.length;
    }

    @Override
    public FileViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new FileViewHolder(LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.layout_file, parent, false));
    }

    @Override
    public void onBindViewHolder(FileViewHolder holder, int position) {
        String fileName = mFiles[position];
        holder.fileName.setText(fileName);
    }

    public class FileViewHolder extends RecyclerView.ViewHolder{
        protected TextView fileName;
        FileViewHolder(View v){
            super(v);
            fileName = (TextView)v.findViewById(R.id.file_name);
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();
                    Log.v("clicked", ""+pos);
                }
            });
        }
    }
}
