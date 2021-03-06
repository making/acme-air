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
package com.acmeair.service;

import com.acmeair.entities.AirportCodeMapping;
import com.acmeair.entities.Flight;
import com.acmeair.entities.FlightPK;
import com.acmeair.entities.FlightSegment;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public interface FlightService {

    Flight getFlightByFlightKey(FlightPK key);

    List<Flight> getFlightByAirportsAndDepartureDate(String fromAirport, String toAirport, Date deptDate);

    List<Flight> getFlightByAirports(String fromAirport, String toAirport);

    void storeAirportMapping(AirportCodeMapping mapping);

    Flight createNewFlight(String flightSegmentId,
                           Date scheduledDepartureTime, Date scheduledArrivalTime,
                           BigDecimal firstClassBaseCost, BigDecimal economyClassBaseCost,
                           int numFirstClassSeats, int numEconomyClassSeats,
                           String airplaneTypeId);

    void storeFlightSegment(FlightSegment flightSeg);
}