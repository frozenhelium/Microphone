package com.fhx.microphone;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by fhx on 2/4/16.
 */
public class RecordingsFragment extends Fragment {
    Context mContext = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_recordings, container, false);
        final RecyclerView recyclerView;
        final RecyclerView.LayoutManager layoutManager;
        recyclerView = (RecyclerView)rootView.findViewById(R.id.recycler_view_recordings);
        recyclerView.setHasFixedSize(true);
        recyclerView.setClickable(true);
        layoutManager = new LinearLayoutManager(rootView.getContext());
        recyclerView.setLayoutManager(layoutManager);
        final FileAdapter adapter = new FileAdapter(getActivity());
        recyclerView.setAdapter(adapter);

        final FloatingActionButton a = (FloatingActionButton) rootView.findViewById(R.id.fabBtn);
        a.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String A;
                if(adapter.returnSelectedNames().size()==1){
                    A = "the file " + adapter.returnSelectedNames().get(0)+"?";
                }
                else {
                    A = adapter.returnSelectedNames().size() + " files?";
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage("Are you sure you want to delete "+A);
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        adapter.onClickDelete();
                        adapter.notifyDataSetChanged();
                        adapter.setAllUnselected();
                        int a = adapter.getItemCount();
                        Log.d("Whatttttt??  ", "onClick: " + a);
                        for (int i = 0; i < a; i++) {
                            layoutManager.getChildAt(i).setSelected(false);
                        }
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Clear Selections
                        adapter.setAllUnselected();
                        for (int i = 0; i < adapter.getItemCount(); i++) {
                            layoutManager.getChildAt(i).setSelected(false);
                        }
                    }
                });
                builder.show();
            }
        });

        return rootView;
    }
}
