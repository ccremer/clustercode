package clustercode.impl.transcode;

import clustercode.api.domain.TranscodeTask;
import clustercode.api.event.RxEventBus;
import clustercode.api.event.messages.ProfileSelectedMessage;
import clustercode.api.transcode.TranscodingService;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;

@Slf4j
public class TranscodingMessageHandler {

    private final TranscodingService transcodingService;
    private final RxEventBus eventBus;

    @Inject
    TranscodingMessageHandler(TranscodingService transcodingService,
                              RxEventBus eventBus) {

        this.transcodingService = transcodingService;
        this.eventBus = eventBus;
    }


    public void onProfileSelected(ProfileSelectedMessage msg) {
        TranscodeTask task = TranscodeTask
                .builder()
                .profile(msg.getProfile())
                .media(msg.getMedia())
                .build();
        transcodingService.transcode(task);
    }
}
