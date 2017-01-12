package com.example.appdaddy.bizmi;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class ConversationsFragment extends BaseFragment {

    private OnFragmentInteractionListener mListener;

    public ConversationsFragment() {
        // Required empty public constructor
    }

    public static ConversationsFragment newInstance() {
        ConversationsFragment fragment = new ConversationsFragment();
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
