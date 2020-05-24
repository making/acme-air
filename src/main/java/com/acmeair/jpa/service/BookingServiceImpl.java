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
package com.acmeair.jpa.service;

import com.acmeair.entities.*;
import com.acmeair.service.BookingService;
import com.acmeair.service.CustomerService;
import com.acmeair.service.FlightService;
import com.acmeair.service.KeyGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.Date;
import java.util.List;

@Service
public class BookingServiceImpl implements BookingService {
    private final EntityManager em;
    private final FlightService flightService;
    private final CustomerService customerService;
    private final KeyGenerator keyGenerator;

    public BookingServiceImpl(EntityManager em, FlightService flightService, CustomerService customerService, KeyGenerator keyGenerator) {
        this.em = em;
        this.flightService = flightService;
        this.customerService = customerService;
        this.keyGenerator = keyGenerator;
    }

    @Transactional
    @Override
    public BookingPK bookFlight(String customerId, FlightPK flightId) {
        try {
            // We still delegate to the flight and customer service for the map
            // access than getting the map instance directly
            Flight f = flightService.getFlightByFlightKey(flightId);
            Customer c = customerService.getCustomerByUsername(customerId);

            Booking newBooking = new Booking(keyGenerator.generate().toString(), new Date(), c, f);
            BookingPK key = newBooking.getPkey();

            em.persist(newBooking);

            return key;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Booking getBooking(String user, String id) {
        BookingPK key = new BookingPK(user, id);
        return em.find(Booking.class, key);
    }

    @Override
    public List<Booking> getBookingsByUser(String user) {
        Query q = em.createQuery("SELECT obj FROM Booking obj where obj.customer.id=?1");
        q.setParameter(1, user);

        List<Booking> results = (List<Booking>) q.getResultList();

        return results;
    }

    @Transactional
    @Override
    public void cancelBooking(String user, String id) {
        Booking booking = getBooking(user, id);
        if (booking != null) {
            em.remove(booking);
        }
    }


}
