package com.example.appdaddy.bizmi.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.appdaddy.bizmi.R;


public class ViewReservationsFragment extends Fragment {
    private BaseFragment.OnFragmentInteractionListener mListener;

    public ViewReservationsFragment() {
        // Required empty public constructor
    }

    public static ViewReservationsFragment newInstance() {
        ViewReservationsFragment fragment = new ViewReservationsFragment();
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
