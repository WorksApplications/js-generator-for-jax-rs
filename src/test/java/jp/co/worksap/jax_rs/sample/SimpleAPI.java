package jp.co.worksap.jax_rs.sample;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

@Path("/sample")
public class SimpleAPI {
    @Path("/")
    @GET
    public Response load(@QueryParam("message") String message) {
        return Response.ok().build();
    }

    @Path("/cat")
    @POST
    public Response postCat(@FormParam("foo") String foo,
            @FormParam("bar") int bar) {
        return Response.ok().build();
    }

    @Path("/dog/{name}")
    @PUT
    public Response callDog(@PathParam("name") String name) {
        return Response.ok().build();
    }
}
