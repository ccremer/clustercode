package net.chrigel.clustercode.transcode.impl;

import net.chrigel.clustercode.process.ExternalProcess;
import net.chrigel.clustercode.transcode.impl.ffmpeg.FfmpegProgressCalculator;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.inject.Provider;

import static org.mockito.Mockito.when;

public class ProgressCalculatorTest {

    private ProgressCalculator subject;

    @Mock
    private Provider<ExternalProcess> externalProcessProvider;

    @Mock
    private ExternalProcess externalProcess;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(externalProcessProvider.get()).thenReturn(externalProcess);

    }

    @Ignore
    public void getProgress() throws Exception {

    }

}