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

import com.acmeair.entities.Customer;
import com.acmeair.entities.Customer.MemberShipStatus;
import com.acmeair.entities.Customer.PhoneType;
import com.acmeair.entities.CustomerAddress;
import com.acmeair.entities.CustomerSession;
import com.acmeair.service.CustomerService;
import com.acmeair.service.KeyGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.Calendar;
import java.util.Date;

@Service
public class CustomerServiceImpl implements CustomerService {

    private static final int DAYS_TO_ALLOW_SESSION = 1;

    private final EntityManager em;
    private final KeyGenerator keyGenerator;

    public CustomerServiceImpl(EntityManager em, KeyGenerator keyGenerator) {
        this.em = em;
        this.keyGenerator = keyGenerator;
    }

    @Transactional
    @Override
    public Customer createCustomer(String username, String password,
                                   MemberShipStatus status, int total_miles, int miles_ytd,
                                   String phoneNumber, PhoneType phoneNumberType,
                                   CustomerAddress address) {
        final Customer customer = new Customer(username, password, status,
                total_miles, miles_ytd, address, phoneNumber, phoneNumberType);
        try {
            em.persist(customer);
            return customer;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional
    @Override
    public Customer updateCustomer(Customer updatedCustomer) {
        try {
            final Customer customer = em.find(Customer.class, updatedCustomer.getUsername());

            final CustomerAddress customerAddress = customer.getAddress();
            customerAddress.setCity(updatedCustomer.getAddress().getCity());
            customerAddress.setCountry(updatedCustomer.getAddress().getCountry());
            customerAddress.setPostalCode(updatedCustomer.getAddress().getPostalCode());
            customerAddress.setStateProvince(updatedCustomer.getAddress().getStateProvince());
            customerAddress.setStreetAddress1(updatedCustomer.getAddress().getStreetAddress1());
            customerAddress.setStreetAddress2(updatedCustomer.getAddress().getStreetAddress2());

            customer.setMilesYtd(updatedCustomer.getMilesYtd());
            customer.setPassword(updatedCustomer.getPassword());
            customer.setPhoneNumber(updatedCustomer.getPhoneNumber());
            customer.setPhoneNumberType(updatedCustomer.getPhoneNumberType());
            customer.setStatus(updatedCustomer.getStatus());
            customer.setTotalMiles(updatedCustomer.getTotalMiles());

            em.persist(customer);
            return customer;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Customer getCustomerByUsername(String username) {
        return em.find(Customer.class, username);
    }

    @Override
    public boolean validateCustomer(String username, String password) {
        boolean validatedCustomer = false;
        final Customer customerToValidate = getCustomerByUsername(username);
        if (customerToValidate != null) {
            validatedCustomer = password.equals(customerToValidate.getPassword());
        }
        return validatedCustomer;
    }

    @Override
    public Customer getCustomerByUsernameAndPassword(String username, String password) {
        final Customer c = getCustomerByUsername(username);
        if (!c.getPassword().equals(password)) {
            return null;
        }
        return c;
    }

    @Transactional
    @Override
    public CustomerSession validateSession(String sessionid) {
        try {
            final CustomerSession cSession = em.find(CustomerSession.class, sessionid);
            if (cSession == null) {
                return null;
            }

            final Date now = new Date();

            if (cSession.getTimeoutTime().before(now)) {
                em.remove(cSession);
                return null;
            }
            return cSession;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional
    @Override
    public CustomerSession createSession(String customerId) {
        try {
            final String sessionId = keyGenerator.generate().toString();
            final Date now = new Date();
            final Calendar c = Calendar.getInstance();
            c.setTime(now);
            c.add(Calendar.DAY_OF_YEAR, DAYS_TO_ALLOW_SESSION);
            final Date expiration = c.getTime();
            final CustomerSession cSession = new CustomerSession(sessionId, customerId, now, expiration);
            em.persist(cSession);
            return cSession;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional
    @Override
    public void invalidateSession(String sessionid) {
        try {
            final CustomerSession cSession = em.find(CustomerSession.class, sessionid);
            if (cSession != null) {
                em.remove(cSession);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
