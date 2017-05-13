package com.bluepinapp.bluepin.POJO;

import android.support.annotation.Nullable;

import com.bluepinapp.bluepin.model.Reservation;

/**
 * Created by Alex on 1/26/2017.
 */

public class ReservationEvent {

    private final String error;

    private final Reservation reservation;

    public ReservationEvent(@Nullable String error, @Nullable Reservation reservation){
        this.error = error;
        this.reservation = reservation;
    }

    public String getError() {
        return error;
    }

    public Reservation getReservation() {
        return reservation;
    }
}
