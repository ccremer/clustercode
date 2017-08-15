package net.chrigel.clustercode.statemachine.actions;

import net.chrigel.clustercode.scan.Profile;
import net.chrigel.clustercode.scan.ProfileScanService;
import net.chrigel.clustercode.statemachine.StateContext;
import net.chrigel.clustercode.statemachine.Action;
import net.chrigel.clustercode.statemachine.states.State;
import net.chrigel.clustercode.statemachine.states.StateEvent;

import javax.inject.Inject;
import java.util.Optional;

public class SelectProfileAction extends Action {

    private final ProfileScanService profileScanService;

    @Inject
    SelectProfileAction(ProfileScanService profileScanService) {
        this.profileScanService = profileScanService;
    }

    @Override
    protected StateEvent doExecute(State from, State to, StateEvent event, StateContext context) {
        log.entry(from, to, event, context);
        Optional<Profile> result = profileScanService.selectProfile(context.getSelectedMedia());
        if (result.isPresent()) {
            log.info("Selected {}", result.get());
            context.setSelectedProfile(result.get());
            return StateEvent.RESULT;
        } else {
            return StateEvent.NO_RESULT;
        }
    }
}
