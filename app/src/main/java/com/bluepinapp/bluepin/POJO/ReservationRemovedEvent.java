package com.bluepinapp.bluepin.POJO;

import android.support.annotation.Nullable;

import com.bluepinapp.bluepin.model.Reservation;

/**
 * Created by Alex on 1/26/2017.
 */

public class ReservationRemovedEvent {

    private final String error;

    private final Reservation reservation;

    private final int index;


    public ReservationRemovedEvent(@Nullable String error, @Nullable Reservation reservation, int index){
        this.error = error;
        this.reservation = reservation;
        this.index = index;
    }

    public String getError() {
        return error;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public int getIndex() {
        return index;
    }

}
