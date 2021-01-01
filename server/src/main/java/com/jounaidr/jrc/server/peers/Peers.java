package com.jounaidr.jrc.server.peers;

import com.jounaidr.jrc.server.blockchain.Blockchain;
import com.jounaidr.jrc.server.peers.peer.Peer;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.regex.Pattern;

@Slf4j
public class Peers {
    private Blockchain blockchain;
    private int maxPeers;

    private int poolSize;
    private ScheduledThreadPoolExecutor executor;

    private ArrayList<Peer> peerList;

    public Peers(Blockchain blockchain, int maxPeers, String peerSockets) {
        this.blockchain = blockchain;
        this.maxPeers = maxPeers;

        if(maxPeers > Runtime.getRuntime().availableProcessors()){
            log.warn("It is recommended to set the maximum peers to less than {} for your system, performance may be impacted...", Runtime.getRuntime().availableProcessors());
        }
        executor = new ScheduledThreadPoolExecutor(poolSize);

        this.peerList = new ArrayList<>();
        this.initialisePeerList(peerSockets);
    }

    public void addPeer(String peerSocket){
        if(peerList.size() < maxPeers){
            if(isSocketValid(peerSocket)){
                if(!isPeerKnown(peerSocket)){
                    poolSize++;
                    executor.setCorePoolSize(poolSize);

                    peerList.add(new Peer(blockchain, executor, peerSocket));
                    peerList.get(peerList.size() - 1).startPolling();
                }
                else{
                    log.info("Unable to add new peer [{}] as its already known", peerSocket);
                }
            }
            else{
                log.error("Unable to add new peer [{}] as its socket is of invalid format", peerSocket);
            }
        }
        else{
            log.error("Unable to add new peer [{}] as max peer size of {} has been reached", peerSocket, maxPeers);
        }
    }

    private boolean isSocketValid(String peerSocket){
        String ipV4Pattern = "(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}):(\\d+)";
        String ipV6Pattern = "\\[([a-zA-Z0-9:]+)\\]:(\\d+)";

        Pattern validSocket = Pattern.compile( ipV4Pattern + "|" + ipV6Pattern);

        return validSocket.matcher(peerSocket).matches();
    }

    private boolean isPeerKnown(String peerSocket){
        for(Peer peer : peerList){
            if(peer.getPeerSocket().equals(peerSocket)){
                return true;
            }
        }
        return false;
    }

    private void initialisePeerList(String peerSockets){
        // Split the socket list and add a peer for each individual socket
        for(String peerSocket : peerSockets.split(",")){
            this.addPeer(peerSocket);
        }
    }
}
