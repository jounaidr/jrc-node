package com.jounaidr.jrc.server.api.implementation.util;

import com.jounaidr.jrc.server.api.generated.model.PeerModel;
import com.jounaidr.jrc.server.peers.peer.Peer;

public class PeerModelUtil {
    public static PeerModel getPeerAsModel(Peer peer){
        PeerModel peerModel = new PeerModel();

        peerModel.setPeerSocket(peer.getPeerSocket());
        peerModel.setPeerStatus(peer.getPeerStatus().toString());

        return peerModel;
    }
}
