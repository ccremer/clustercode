package net.chrigel.clustercode.cluster.impl;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.name.Names;
import net.chrigel.clustercode.GlobalModule;
import net.chrigel.clustercode.api.RestApiServices;
import net.chrigel.clustercode.api.impl.ApiModule;
import net.chrigel.clustercode.api.rest.TasksApi;
import net.chrigel.clustercode.cleanup.impl.CleanupModule;
import net.chrigel.clustercode.cluster.ClusterService;
import net.chrigel.clustercode.process.impl.ProcessModule;
import net.chrigel.clustercode.scan.MediaScanSettings;
import net.chrigel.clustercode.scan.impl.MediaScanSettingsImpl;
import net.chrigel.clustercode.scan.impl.ScanModule;
import net.chrigel.clustercode.transcode.impl.TranscodeModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

public class CancelTaskIT {

    private Injector injector;

    @Before
    public void setUp() throws Exception {
        Properties properties = getMinimalConfiguration();
        injector = Guice.createInjector(Arrays.asList(
            new ApiModule(properties),
            new ClusterModule(),
            new TranscodeModule(properties),
            new ProcessModule(),
            new GlobalModule(),
            new AbstractModule() {
                @Override
                protected void configure() {
                    bind(MediaScanSettings.class).to(MediaScanSettingsImpl.class);
                    Names.bindProperties(binder(), properties);
                }
            }
        ));

        injector.getInstance(ClusterService.class).joinCluster();
    }

    @After
    public void tearDown() throws Exception {
        injector.getInstance(ClusterService.class).leaveCluster();
    }

    @Test
    public void cancelTask_InCluster_ShouldReturn409_IfHostnameDoesNotExist() throws Exception {

        TasksApi api = injector.getInstance(TasksApi.class);

        assertThat(api.stopTask("test").getStatus()).isEqualTo(409);
    }

    @Test
    public void cancelTask_Locally_ShouldReturn409_IfHostnameDoesNotExist() throws Exception {

        TasksApi api = injector.getInstance(TasksApi.class);

        assertThat(api.stopTask("localhost").getStatus()).isEqualTo(200);
    }

    private Properties getMinimalConfiguration() {
        Properties config = new Properties();
        config.setProperty(CleanupModule.CLEANUP_STRATEGY_KEY, "CHOWN");
        config.setProperty(TranscodeModule.TRANSCODE_TYPE_KEY, "HANDBRAKE");
        config.setProperty(ApiModule.REST_PORT_KEY, "8080");
        config.setProperty(ScanModule.MEDIA_INPUT_DIR_KEY, "input");
        config.setProperty(ClusterModule.CLUSTER_JGROUPS_HOSTNAME_KEY, "localhost");
        return config;
    }
}
