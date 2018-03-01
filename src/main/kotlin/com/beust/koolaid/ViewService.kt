package com.beust.koolaid

import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

@Path("/v0/views")
class ViewService {
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    fun retrieveViewCount(): ViewCount = ViewCount(1)
}
