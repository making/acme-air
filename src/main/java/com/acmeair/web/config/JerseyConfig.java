package com.acmeair.web.config;

import com.acmeair.web.*;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.stereotype.Component;

import javax.ws.rs.ApplicationPath;

@Component
@ApplicationPath("/rest/api/*")
public class JerseyConfig extends ResourceConfig {
    public JerseyConfig() {
        register(BookingsREST.class);
        register(CustomerREST.class);
        register(FlightsREST.class);
        register(LoaderREST.class);
        register(LoginREST.class);
    }
}
