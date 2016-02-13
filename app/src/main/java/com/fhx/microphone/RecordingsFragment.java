package com.fhx.microphone;

import android.app.Fragment;
import android.content.Context;
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
        RecyclerView recyclerView;
        RecyclerView.LayoutManager layoutManager;
        recyclerView = (RecyclerView)rootView.findViewById(R.id.recycler_view_recordings);
        recyclerView.setHasFixedSize(true);
        recyclerView.setClickable(true);
        layoutManager = new LinearLayoutManager(rootView.getContext());
        recyclerView.setLayoutManager(layoutManager);
        final FileAdapter adapter = new FileAdapter(getActivity());
        recyclerView.setAdapter(adapter);

        FloatingActionButton a = (FloatingActionButton) rootView.findViewById(R.id.fabBtn);
        a.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.onClickDelete();
            }
        });

        return rootView;
    }
}
