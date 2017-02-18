package com.example.appdaddy.bizmi.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.appdaddy.bizmi.R;


public class ViewCustomersFragment extends Fragment {
    private BaseFragment.OnFragmentInteractionListener mListener;

    public ViewCustomersFragment() {
        // Required empty public constructor
    }

    public static ViewCustomersFragment newInstance() {
        ViewCustomersFragment fragment = new ViewCustomersFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_conversations, container, false);
    }
}
