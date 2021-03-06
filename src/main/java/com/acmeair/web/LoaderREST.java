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
package com.acmeair.web;

import com.acmeair.entities.AirportCodeMapping;
import com.acmeair.entities.Customer;
import com.acmeair.entities.Customer.PhoneType;
import com.acmeair.entities.CustomerAddress;
import com.acmeair.entities.FlightSegment;
import com.acmeair.service.CustomerService;
import com.acmeair.service.FlightService;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.math.BigDecimal;
import java.util.*;

@Component
@Path("/loader")
public class LoaderREST {
    private final CustomerService customerService;
    private final FlightService flightService;

    private static Object lock = new Object();

    public LoaderREST(CustomerService customerService, FlightService flightService) {
        this.customerService = customerService;
        this.flightService = flightService;
    }

    @GET
    @Path("/load")
    @Produces("text/plain")
    public String load() {
        return loadData(10, 30);
    }

    @GET
    @Path("/loadSmall")
    @Produces("text/plain")
    public String loadSmall() {
        return loadData(5, 5);
    }

    @GET
    @Path("/loadTiny")
    @Produces("text/plain")
    public String loadTiny() {
        return loadData(2, 2);
    }

    private String loadData(long numCustomers, int segments) {
        synchronized (lock) {
            try {
                loadCustomers(numCustomers);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                loadFlights(segments);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "Sample data loaded.";
        }
    }

    public void loadCustomers(long numCustomers) {
        System.out.println("Loading customer data...");
        final CustomerAddress address = new CustomerAddress("123 Main St.", null, "Anytown", "NC", "USA", "27617");
        for (long ii = 0; ii < numCustomers; ii++) {
            final String id = "uid" + ii + "@email.com";
            final Customer customer = customerService.getCustomerByUsername(id);
            if (customer == null) {
                customerService.createCustomer(id, "password", Customer.MemberShipStatus.GOLD, 1000000, 1000, "919-123-4567", PhoneType.BUSINESS, address);
            }
        }
        System.out.println("Done loading customer data.");
    }

    public void loadFlights(int segments) throws Exception {
        System.out.println("Loading flight data...");
        final InputStream csvInputStream = new ClassPathResource("mileage.csv").getInputStream();
        final LineNumberReader lnr = new LineNumberReader(new InputStreamReader(csvInputStream));
        final String line1 = lnr.readLine();
        StringTokenizer st = new StringTokenizer(line1, ",");
        final List<AirportCodeMapping> airports = new ArrayList<>();

        // read the first line which are airport names
        while (st.hasMoreTokens()) {
            AirportCodeMapping acm = new AirportCodeMapping();
            acm.setAirportName(st.nextToken());
            airports.add(acm);
        }
        // read the second line which contains matching airport codes for the
        // first line
        final String line2 = lnr.readLine();
        st = new StringTokenizer(line2, ",");
        int ii = 0;
        while (st.hasMoreTokens()) {
            final String airportCode = st.nextToken();
            airports.get(ii).setAirportCode(airportCode);
            ii++;
        }
        // read the other lines which are of format:
        // airport name, aiport code, distance from this airport to whatever
        // airport is in the column from lines one and two
        String line;
        int flightNumber = 0;
        while (true) {
            line = lnr.readLine();
            if (line == null || line.trim().equals("")) {
                break;
            }
            st = new StringTokenizer(line, ",");
            final String airportName = st.nextToken();
            final String airportCode = st.nextToken();
            if (!alreadyInCollection(airportCode, airports)) {
                final AirportCodeMapping acm = new AirportCodeMapping();
                acm.setAirportName(airportName);
                acm.setAirportCode(airportCode);
                airports.add(acm);
            }
            int indexIntoTopLine = 0;
            while (st.hasMoreTokens()) {
                final String milesString = st.nextToken();
                if (milesString.equals("NA")) {
                    indexIntoTopLine++;
                    continue;
                }
                final int miles = Integer.parseInt(milesString);
                final String toAirport = airports.get(indexIntoTopLine).getAirportCode();
                if (!flightService.getFlightByAirports(airportCode, toAirport).isEmpty()) {
                    // already there
                    continue;
                }
                final String flightId = "AA" + flightNumber;
                final FlightSegment flightSeg = new FlightSegment(flightId, airportCode, toAirport, miles);
                flightService.storeFlightSegment(flightSeg);
                final Date now = new Date();
                for (int daysFromNow = 0; daysFromNow < segments; daysFromNow++) {
                    final Calendar c = Calendar.getInstance();
                    c.setTime(now);
                    c.set(Calendar.HOUR_OF_DAY, 0);
                    c.set(Calendar.MINUTE, 0);
                    c.set(Calendar.SECOND, 0);
                    c.set(Calendar.MILLISECOND, 0);
                    c.add(Calendar.DATE, daysFromNow);
                    final Date departureTime = c.getTime();
                    final Date arrivalTime = getArrivalTime(departureTime, miles);
                    flightService.createNewFlight(flightId, departureTime, arrivalTime, new BigDecimal(500),
                            new BigDecimal(200), 10, 200, "B747");

                }
                flightNumber++;
                indexIntoTopLine++;
            }
        }

        for (int jj = 0; jj < airports.size(); jj++) {
            flightService.storeAirportMapping(airports.get(jj));
        }
        lnr.close();
        System.out.println("Done loading flight data.");
    }

    private static Date getArrivalTime(Date departureTime, int mileage) {
        final double averageSpeed = 600.0; // 600 miles/hours
        final double hours = (double) mileage / averageSpeed; // miles / miles/hour =
        // hours
        final double partsOfHour = hours % 1.0;
        final int minutes = (int) (60.0 * partsOfHour);
        final Calendar c = Calendar.getInstance();
        c.setTime(departureTime);
        c.add(Calendar.HOUR, (int) hours);
        c.add(Calendar.MINUTE, minutes);
        return c.getTime();
    }

    static private boolean alreadyInCollection(String airportCode, List<AirportCodeMapping> airports) {
        for (int ii = 0; ii < airports.size(); ii++) {
            if (airports.get(ii).getAirportCode().equals(airportCode)) {
                return true;
            }
        }
        return false;
    }
}
