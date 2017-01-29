package net.chrigel.clustercode.task.impl;

import net.chrigel.clustercode.task.CleanupService;
import net.chrigel.clustercode.task.Media;

import javax.inject.Inject;

class CleanupServiceImpl implements CleanupService {

    @Inject
    CleanupServiceImpl() {

    }

    @Override
    public void performCleanup(Media candidate, boolean successful) {

    }
}
