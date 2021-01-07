package com.jounaidr.jrc.server.api.implementation;

import com.jounaidr.jrc.server.api.generated.PeersApiDelegate;
import com.jounaidr.jrc.server.api.generated.model.PeerModel;
import com.jounaidr.jrc.server.api.implementation.util.PeerModelUtil;
import com.jounaidr.jrc.server.peers.Peers;
import com.jounaidr.jrc.server.peers.peer.Peer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

public class PeersApiDelegateImpl implements PeersApiDelegate {
    @Autowired
    private Peers peers; //The peers bean instance for this node, injected through spring

    /**
     * For each peer object in the peers peerList, convert it into
     * a PeerModel data object and store in temp arraylist to be returned
     *
     * @return the PeerModel list as a response entity with status code 200
     */
    @Override
    public ResponseEntity<List<PeerModel>> getSocketsList() {
        ArrayList<PeerModel> response = new ArrayList<>();

        for(Peer peer : peers.getPeerList()) {
            response.add(PeerModelUtil.getPeerAsModel(peer));
        }

        return ResponseEntity.ok(response);
    }
}
