package clustercode.api.rest.v1.rest;

import clustercode.api.rest.v1.dto.ApiError;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import javax.ws.rs.core.Response;
import java.util.function.Supplier;

abstract class AbstractRestApi {

    protected final XLogger log = XLoggerFactory.getXLogger(getClass());

    final Response createResponse(Supplier entitySupplier) {
        try {
            return Response.ok(entitySupplier.get())
                           .build();
        } catch (Exception ex) {
            log.catching(ex);
            return Response
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ApiError
                            .builder()
                            .message(ex.getMessage())
                            .build())
                    .build();
        }
    }

    final Response clientError(String message) {
        return Response
                .status(Response.Status.BAD_REQUEST)
                .entity(ApiError
                        .builder()
                        .message(message)
                        .build())
                .build();
    }

    final Response serverError(Throwable ex) {
        return Response
                .serverError()
                .entity(ApiError
                        .builder()
                        .message(ex.getMessage())
                        .build())
                .build();
    }

    final Response serverError(String message) {
        return Response
                .serverError()
                .entity(ApiError
                        .builder()
                        .message(message)
                        .build())
                .build();
    }
}
