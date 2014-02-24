package jp.co.worksap.jax_rs.sample;

import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("/simple")
public class SimpleImplementation implements SimpleInterface {

    @Override
    public Response getResource() {
        return null;
    }

}
