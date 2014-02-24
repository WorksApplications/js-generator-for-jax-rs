package jp.co.worksap.jax_rs.sample;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("/foo")
public interface SimpleInterface {
    @GET
    @Path("/resource")
    Response getResource();
}
