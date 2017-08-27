package net.chrigel.clustercode.api.rest;

import net.chrigel.clustercode.api.dto.ApiError;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import javax.ws.rs.core.Response;
import java.util.function.Supplier;

abstract class AbstractRestApi {

    private final XLogger log = XLoggerFactory.getXLogger(getClass());

    protected final Response createResponse(Supplier entitySupplier) {
        try {
            return Response.ok(entitySupplier.get())
                    .build();
        } catch (Exception ex) {
            log.catching(ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ApiError.builder()
                            .message(ex.getMessage()))
                    .build();
        }
    }

}
