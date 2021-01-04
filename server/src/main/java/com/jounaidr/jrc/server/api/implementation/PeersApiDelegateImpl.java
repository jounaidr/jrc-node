package com.jounaidr.jrc.server.api.implementation;

import com.jounaidr.jrc.server.api.generated.PeersApiDelegate;
import com.jounaidr.jrc.server.api.generated.model.PeerModel;
import com.jounaidr.jrc.server.api.implementation.util.PeerModelUtil;
import com.jounaidr.jrc.server.peers.Peers;
import com.jounaidr.jrc.server.peers.peer.Peer;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

public class PeersApiDelegateImpl implements PeersApiDelegate {
    private final Peers peers;

    public PeersApiDelegateImpl(Peers peers) {
        this.peers = peers;
    }

    @Override
    public ResponseEntity<List<PeerModel>> getSocketsList() {
        ArrayList<PeerModel> response = new ArrayList<>();

        for(Peer peer : peers.getPeerList()) {
            response.add(PeerModelUtil.getPeerAsModel(peer));
        }

        return ResponseEntity.ok(response);
    }
}
