/*******************************************************************************
 * Copyright (c) 2013 IBM Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.acmeair.entities;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.io.Serializable;
import java.util.Date;

@Entity
public class Booking implements Serializable {

    private static final long serialVersionUID = 1L;

    @EmbeddedId
    private BookingPK pkey;

    private Date dateOfBooking;

    @ManyToOne
    @JoinColumn(insertable = false, updatable = false)
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "flight_id")
    @JoinColumn(name = "flight_segment_id")
    private Flight flight;

    public Booking() {
    }

    public Booking(String id, Date dateOfFlight, Customer customer, Flight flight) {
        this.pkey = new BookingPK(customer.getUsername(), id);
        this.dateOfBooking = dateOfFlight;
        this.customer = customer;
        this.flight = flight;
    }

    public BookingPK getPkey() {
        return pkey;
    }

    // adding the method for index calculation
    public String getCustomerId() {
        return pkey.getCustomerId();
    }

    public void setPkey(BookingPK pkey) {
        this.pkey = pkey;
    }

    public void setFlight(Flight flight) {
        this.flight = flight;
    }

    public Date getDateOfBooking() {
        return dateOfBooking;
    }

    public void setDateOfBooking(Date dateOfBooking) {
        this.dateOfBooking = dateOfBooking;
    }

    public Customer getCustomer() {
        return customer;
    }

    public Flight getFlight() {
        return flight;
    }


    @Override
    public String toString() {
        return "Booking [key=" + pkey + ", dateOfBooking=" + dateOfBooking + ", customer=" + customer
                + ", flight=" + flight + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Booking other = (Booking) obj;
        if (customer == null) {
            if (other.customer != null)
                return false;
        } else if (!customer.equals(other.customer))
            return false;
        if (dateOfBooking == null) {
            if (other.dateOfBooking != null)
                return false;
        } else if (!dateOfBooking.equals(other.dateOfBooking))
            return false;
        if (flight == null) {
            if (other.flight != null)
                return false;
        } else if (!flight.equals(other.flight))
            return false;
        if (pkey == null) {
            if (other.pkey != null)
                return false;
        } else if (!pkey.equals(other.pkey))
            return false;
        return true;
    }

}
