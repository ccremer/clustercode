package net.chrigel.clustercode.api.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.XSlf4j;
import net.chrigel.clustercode.api.dto.ApiError;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.InputStream;

@Path("/docs")
@Api(description = "the swagger documentation")
public class DocumentationApi {

    @Path("api.html")
    @GET
    @Produces({MediaType.TEXT_HTML})
    @ApiOperation(value = "Swagger API definition", notes = "Retrieves a html file that describes this API.", response
            = File.class, tags = {"api"})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = File.class),
            @ApiResponse(code = 500, message = "Unexpected error", response = ApiError.class)})
    public InputStream getApiHtml() {
        return getClass().getResourceAsStream("/swagger.html");
    }

    @Path("api.json")
    @GET
    @Produces({MediaType.APPLICATION_OCTET_STREAM})
    @ApiOperation(value = "Swagger API definition", notes = "Retrieves a json file that describes this API.", response
            = File.class, tags = {"api"})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = File.class),
            @ApiResponse(code = 500, message = "Unexpected error", response = ApiError.class)})
    public InputStream getApiJson() {
        return getClass().getResourceAsStream("/swagger.json");
    }
}

