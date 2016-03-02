package com.fhx.microphone;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.opengl.Visibility;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

/**
 * Created by fhx on 2/4/16.
 */
public class FileAdapter extends RecyclerView.Adapter<FileAdapter.FileViewHolder> {

    private String[] mFiles;
    private Context mContext;
    public ArrayList<Boolean> isDuringSelection = new ArrayList<>();

    public FileAdapter(Context context){
        mContext = context;
        this.loadFileNames();

        for(int i=0;i<=getItemCount();i++){
            isDuringSelection.add(false);
        }
    }

    //Returns if any item is currently selected or not
    public boolean isAnySelected(){
        for(int i=0;i<=getItemCount();i++){
            if(isDuringSelection.get(i)) return true;
        }
        return false;
    }

    //Sets the visibility of FAB button
    public void setButtonVisibility(View v, int id,int visibility){
        FloatingActionButton deleteButton = (FloatingActionButton) v.findViewById(id);
        if(visibility == View.GONE){
            deleteButton.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.abc_slide_out_bottom));
        }
        else {
            deleteButton.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.abc_slide_in_bottom));
        }
        deleteButton.setVisibility(visibility);
    }

    //Returns path of all currently selected items
    public ArrayList<String> returnSelectedNames(){
        ArrayList<String> strings = new ArrayList<>();
        for(int i=0;i<=getItemCount();i++){
            if(isDuringSelection.get(i)){
                String a = Environment.getExternalStorageDirectory() + File.separator
                        + "Microphone" + File.separator + mFiles[i];
                strings.add(a);
            }
        }
        return strings;
    }

    //Action on pressing the delete FAB
    public void onClickDelete(){
        ArrayList<String> strings = returnSelectedNames();
        for(int i = 0; i<strings.size(); i++){
            File a = new File(strings.get(i));
            boolean delete = a.delete();
            Log.d("This : ", strings.get(i) + delete);
        }
        loadFileNames();
    }

    public void setAllUnselected(){
        for(int i = 0;i<=getItemCount();i++){
            isDuringSelection.set(i,false);
        }

    }

    public void loadFileNames(){

        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            Log.e("Microphone", "Failed to detect External Storage");
        }
        else {
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
                    Intent intent = new Intent();

                    if (isAnySelected()) {
                        if (v.isSelected()) {
                            v.setSelected(false);
                            isDuringSelection.set(pos, false);
                            if (!isAnySelected())
                                setButtonVisibility(v.getRootView(), R.id.fabBtn, View.GONE);
                        } else {
                            v.setSelected(true);
                            isDuringSelection.set(pos, true);
                        }
                    } else {
                        setButtonVisibility(v.getRootView(), R.id.fabBtn, View.GONE);
                        intent.setAction(android.content.Intent.ACTION_VIEW);
                        File file = new File(Environment.getExternalStorageDirectory() + File.separator
                                + "Microphone" + File.separator + mFiles[pos]);
                        intent.setDataAndType(Uri.fromFile(file), "audio/wav");
                        mContext.startActivity(intent);
                    }

                }
            });
            v.setOnLongClickListener(new View.OnLongClickListener() {

                public boolean onLongClick(View v) {
                    int pos = getAdapterPosition();
                    v.setSelected(true);
                    isDuringSelection.set(pos, true);
                    setButtonVisibility(v.getRootView(), R.id.fabBtn, View.VISIBLE);
                    return true;
                }
            });
        }
    }
}
