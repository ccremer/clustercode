package net.chrigel.clustercode.constraint.impl;

import net.chrigel.clustercode.scan.Media;

class NoConstraint extends AbstractConstraint {

    @Override
    public boolean accept(Media candidate) {
        return logAndReturnResult(true, "{}", candidate);
    }
}
