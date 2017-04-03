/*
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.appdaddy.bizmi.reservationAdapter;

import android.util.Log;

import com.example.appdaddy.bizmi.model.Reservation;
import com.example.appdaddy.bizmi.util.L;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

class ReservationFirebaseIndexArray extends ReservationFirebaseArray {

    private Query mQuery;
    private Query mThirdQuery;
    private ChangeEventListener mListener;
    private Map<Query, ValueEventListener> mRefs = new HashMap<>();
    private List<DataSnapshot> mDataSnapshots = new ArrayList<>();
    private Map<String, DataSnapshot> mThirdDataSnapshots = new HashMap<>();
    private int mIndex;

    public ReservationFirebaseIndexArray(Query keyRef, Query dataRef, Query thirdQueryRef) {
        super(keyRef);
        mQuery = dataRef;
        mThirdQuery = thirdQueryRef;
    }

    @Override
    public void cleanup() {
        super.cleanup();
        Set<Query> refs = new HashSet<>(mRefs.keySet());
        for (Query ref : refs) {
            ref.removeEventListener(mRefs.remove(ref));
        }
    }

    @Override
    public int getCount() {
        return mDataSnapshots.size();
    }

    @Override
    public DataSnapshot getItem(int index) {
        return mDataSnapshots.get(index);
    }

    @Override
    public DataSnapshot getUserItem(String userID) {
        return mThirdDataSnapshots.get(userID);
    }

    private int getIndexForKey(String key) {
        L.m(getCount() + " getCount() -- key: " + key);
        int dataCount = getCount();
        int index = 0;
        for (int keyIndex = 0; index < dataCount; keyIndex++) {
            String superKey = super.getItem(keyIndex).getKey();
            if (key.equals(superKey)) {
                L.m(superKey + " key.equals");
                break;
            } else if (mDataSnapshots.get(index).getKey().equals(superKey)) {
                L.m("increaseIndex " + index + " mDataSnapshots.get(index).getKey()");
                index++;
            }
        }
        return index;
    }

    private boolean isKeyAtIndex(String key, int index) {
        return index >= 0 && index < getCount() && mDataSnapshots.get(index).getKey().equals(key);
    }

    private int getSecondIndexForKey(String key) {
        int dataCount = mThirdDataSnapshots.size();
        int index = 0;
        for (int keyIndex = 0; index < dataCount; keyIndex++) {
            String superKey = super.getItem(keyIndex).getKey();
            if (key.equals(superKey)) {
                break;
            } else if (mDataSnapshots.get(index).getKey().equals(superKey)) {
                index++;
            }
        }
        return index;
    }

    private boolean isSecondKeyAtIndex(String key, int index) {
        return index >= 0 && index < getCount() && mDataSnapshots.get(index).getKey().equals(key);
    }
    @Override
    public void onChildAdded(DataSnapshot keySnapshot, String previousChildKey) {
        super.setOnChangedListener(null);
        super.onChildAdded(keySnapshot, previousChildKey);
        super.setOnChangedListener(mListener);
        Query ref = mQuery.getRef().child(keySnapshot.getKey());
        mRefs.put(ref, ref.addValueEventListener(new DataRefListener()));
    }

    @Override
    public void onChildChanged(DataSnapshot snapshot, String previousChildKey) {
        super.setOnChangedListener(null);
        super.onChildChanged(snapshot, previousChildKey);
        super.setOnChangedListener(mListener);
    }

    @Override
    public void onChildRemoved(DataSnapshot keySnapshot) {
        String key = keySnapshot.getKey();
        int index = getIndexForKey(key);
        mQuery.getRef().child(key).removeEventListener(mRefs.remove(mQuery.getRef().child(key)));

        super.setOnChangedListener(null);
        super.onChildRemoved(keySnapshot);
        super.setOnChangedListener(mListener);

        if (isKeyAtIndex(key, index)) {
            mDataSnapshots.remove(index);
            notifyChangedListeners(ChangeEventListener.EventType.REMOVED, index);
        }
    }

    @Override
    public void onChildMoved(DataSnapshot keySnapshot, String previousChildKey) {
        String key = keySnapshot.getKey();
        int oldIndex = getIndexForKey(key);

        super.setOnChangedListener(null);
        super.onChildMoved(keySnapshot, previousChildKey);
        super.setOnChangedListener(mListener);

        if (isKeyAtIndex(key, oldIndex)) {
            DataSnapshot snapshot = mDataSnapshots.remove(oldIndex);
            int newIndex = getIndexForKey(key);
            mDataSnapshots.add(newIndex, snapshot);
            notifyChangedListeners(ChangeEventListener.EventType.MOVED, newIndex, oldIndex);
        }
    }

    @Override
    public void onCancelled(DatabaseError error) {
        Log.e("TAG", "A fatal error occurred retrieving the necessary keys to populate your adapter.");
        super.onCancelled(error);
    }

    @Override
    public void setOnChangedListener(ChangeEventListener listener) {
        super.setOnChangedListener(listener);
        mListener = listener;
    }

    private class DataRefListener implements ValueEventListener {

        @Override
        public void onDataChange(DataSnapshot snapshot) {
            String key = snapshot.getKey();
            int index = getIndexForKey(key);

            if (snapshot.getValue() != null) {

                if (!isKeyAtIndex(key, index)) {

                    mDataSnapshots.add(index, snapshot);
                    L.m(index + " : index");
//                    Query usersRef = mThirdQuery.getRef().child(snapshot.getValue(Reservation.class).getLeaderID());
//                    usersRef.addListenerForSingleValueEvent(new UserDataRefListener());

                } else {
                    mDataSnapshots.set(index, snapshot);
                    notifyChangedListeners(ChangeEventListener.EventType.CHANGED, index);
                }

            } else {
                if (isKeyAtIndex(key, index)) {
                    mDataSnapshots.remove(index);
                    notifyChangedListeners(ChangeEventListener.EventType.REMOVED, index);
                } else {
                    Log.w("TAG", "Key not found at ref: " + snapshot.getRef());
                }
            }
        }

        @Override
        public void onCancelled(DatabaseError error) {
            notifyCancelledListeners(error);
        }
    }

    private class UserDataRefListener implements ValueEventListener {

        @Override
        public void onDataChange(DataSnapshot snapshot) {


            if (snapshot.getValue() != null) {

                L.m(snapshot.getKey());
                if(!mThirdDataSnapshots.containsKey(snapshot.getKey())){
                    mThirdDataSnapshots.put(snapshot.getKey(), snapshot);
                    L.m("Didn't contain " + snapshot.getKey());
                }
                notifyChangedListeners(ChangeEventListener.EventType.ADDED, 1);

            }
        }

        @Override
        public void onCancelled(DatabaseError error) {
            notifyCancelledListeners(error);
        }

    }
}
