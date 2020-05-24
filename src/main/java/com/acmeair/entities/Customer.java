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

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;

@Entity
public class Customer implements Serializable {

    public enum MemberShipStatus {NONE, SILVER, GOLD, PLATINUM, EXEC_PLATINUM, GRAPHITE}

    ;

    public enum PhoneType {UNKNOWN, HOME, BUSINESS, MOBILE}

    ;

    private static final long serialVersionUID = 1L;

    @Id
    @Column(columnDefinition = "VARCHAR")
    private String id;
    private String password;
    private MemberShipStatus status;
    private int totalMiles;
    private int milesYtd;

    @Embedded
    private CustomerAddress address;
    private String phoneNumber;
    private PhoneType phoneNumberType;

    public Customer() {
    }

    public Customer(String username, String password, MemberShipStatus status, int totalMiles, int milesYtd, CustomerAddress address, String phoneNumber, PhoneType phoneNumberType) {
        this.id = username;
        this.password = password;
        this.status = status;
        this.totalMiles = totalMiles;
        this.milesYtd = milesYtd;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.phoneNumberType = phoneNumberType;
    }

    public String getUsername() {
        return id;
    }

    public void setUsername(String username) {
        this.id = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public MemberShipStatus getStatus() {
        return status;
    }

    public void setStatus(MemberShipStatus status) {
        this.status = status;
    }

    public int getTotalMiles() {
        return totalMiles;
    }

    public void setTotalMiles(int total_miles) {
        this.totalMiles = total_miles;
    }

    public int getMilesYtd() {
        return milesYtd;
    }

    public void setMilesYtd(int miles_ytd) {
        this.milesYtd = miles_ytd;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public PhoneType getPhoneNumberType() {
        return phoneNumberType;
    }

    public void setPhoneNumberType(PhoneType phoneNumberType) {
        this.phoneNumberType = phoneNumberType;
    }

    public CustomerAddress getAddress() {
        return address;
    }

    public void setAddress(CustomerAddress address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return "Customer [id=" + id + ", password=" + password + ", status="
                + status + ", totalMiles=" + totalMiles + ", milesYtd="
                + milesYtd + ", address=" + address + ", phoneNumber="
                + phoneNumber + ", phoneNumberType=" + phoneNumberType + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Customer other = (Customer) obj;
        if (address == null) {
            if (other.address != null)
                return false;
        } else if (!address.equals(other.address))
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (milesYtd != other.milesYtd)
            return false;
        if (password == null) {
            if (other.password != null)
                return false;
        } else if (!password.equals(other.password))
            return false;
        if (phoneNumber == null) {
            if (other.phoneNumber != null)
                return false;
        } else if (!phoneNumber.equals(other.phoneNumber))
            return false;
        if (phoneNumberType != other.phoneNumberType)
            return false;
        if (status != other.status)
            return false;
        if (totalMiles != other.totalMiles)
            return false;
        return true;
    }

}
