package net.chrigel.clustercode.api.impl;

import com.google.inject.TypeLiteral;
import com.owlike.genson.ext.jaxrs.GensonJsonConverter;
import io.logz.guice.jersey.JerseyModule;
import io.logz.guice.jersey.configuration.JerseyConfiguration;
import net.chrigel.clustercode.api.ProgressReportAdapter;
import net.chrigel.clustercode.api.RestApiServices;
import net.chrigel.clustercode.api.StateMachineMonitor;
import net.chrigel.clustercode.api.dto.FfmpegProgressReport;
import net.chrigel.clustercode.api.dto.HandbrakeProgressReport;
import net.chrigel.clustercode.transcode.impl.TranscodeModule;
import net.chrigel.clustercode.transcode.impl.Transcoders;
import net.chrigel.clustercode.util.di.AbstractPropertiesModule;

import javax.inject.Singleton;
import java.util.Locale;
import java.util.Properties;

public class ApiModule extends AbstractPropertiesModule {

    public static final String REST_ENABLED_KEY = "CC_REST_API_ENABLED";
    public static final String REST_PORT_KEY = "CC_REST_API_PORT";

    private final Properties properties;

    public ApiModule(Properties properties) {
        this.properties = properties;
    }

    @Override
    protected void configure() {

        bind(StateMachineMonitor.class).to(StateMachineMonitorImpl.class).in(Singleton.class);

        String restEnabled = getEnvironmentVariableOrProperty(properties, REST_ENABLED_KEY);
        String restPort = getEnvironmentVariableOrProperty(properties, REST_PORT_KEY);
        if ("true".equalsIgnoreCase(restEnabled)) {
            installJersey(Integer.valueOf(restPort));
        } else {
            bind(RestApiServices.class).to(DisabledApiService.class);
        }

        bind(new TypeLiteral<ProgressReportAdapter<FfmpegProgressReport>>() {
        }).to(FfmpegProgressAdapter.class);

        bind(new TypeLiteral<ProgressReportAdapter<HandbrakeProgressReport>>() {
        }).to(HandbrakeProgressAdapter.class);
    }

    private void installJersey(int port) {

        JerseyConfiguration configuration = JerseyConfiguration.builder()
                .addPackage("net.chrigel.clustercode.api")
                .addPort(port)
                .withContextPath("/api/v1")
                .registerClasses(GensonJsonConverter.class)
                .build();


        install(new JerseyModule(configuration));
        bind(RestApiServices.class).to(RestApiServicesImpl.class).in(Singleton.class);
    }
}
