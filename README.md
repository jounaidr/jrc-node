# jrc-node

JRC aims to be a simple and secure cryptocurrency network implementing the Cryptonight proof of work 
algorithm, and developed using the Java Spring framework.

---

* [Installation](#installation)
* [Features](#features)
    - [Genesis](#genesis)
    - [Block Hashing](#block-hashing)
    - [Proof Of Work](#proof-of-work)
    - [Block Validation](#block-validation)
    - [P2P Networking](#p2p-networking)
         - [API](#api)
         - [Interfacing Peers](#interfacing-peers)   
* [Testing](#testing)
    - [Unit Tests](#unit-tests)
    - [Minerate Integration Test](#minerate-integration-test)
* [DevOps](#devops)
    - [CI Testing](#ci-testing)
    - [Version Control](#version-control)
* [License](#license)

---

## Installation

Installation will be done through RPM on RHEL/Centos servers (currently unimplemented)

## Features

### Genesis
The genesis block is currently generated based on the following parameters (which is hardcoded in [Block.java](https://github.com/jounaidr/jrc-node/blob/master/src/main/java/com/jounaidr/jrc/node/blockchain/Block.java)):
```java
private static final String GENESIS_PREVIOUS_HASH = "dummyhash";
private static final String GENESIS_DATA = "dummydata";
private static final String GENESIS_TIME_STAMP = "2020-11-07T19:40:57.585581100Z";
private static final String GENESIS_NONCE = "dummydata";
private static final String GENESIS_DIFFICULTY = "3";
private static final String GENESIS_PROOF_OF_WORK = "1101011101110100010010011001011010101001000010001011100011111011000110111000010010111111000100000000000011100011110011000000001101011000011110011010111110001000101010111000000100001100010101001100101011001110011110010000011110001011001010000001010011011000";
```
Note: this data will be changed upon deployment

### Block Hashing
Each block's hash is generated using the [Keccak-256](https://keccak.team/keccak_specs_summary.html) algorithm, for which the implementation can be found
in [KeccakHashHelper.java](https://github.com/jounaidr/jrc-node/blob/master/src/main/java/com/jounaidr/jrc/node/crypto/KeccakHashHelper.java). 
This implementation uses the [Bouncy Castle Api](https://www.bouncycastle.org/).

### Proof Of Work
JRC currently implements the CryptoNight proof of work algorithm, for which the current component being used is a JNI wrapper around the C++ implementation of CryptoNightV2 provided in the [Monero sourcecode](https://github.com/monero-project/monero/commit/f3cd51a12b202875bd8191668aceb8a4f810ecd4).
Current component dependency:
```xml
<dependency>
    <groupId>com.jounaidr</groupId>
    <artifactId>CryptoNightJNI</artifactId>
    <version>1.0</version>
</dependency>
```
The component is integrated during the [.mineBlock() method in Block.java](https://github.com/jounaidr/jrc-node/blob/fceecdc550a1f10776949fda1bad1514cef27791/src/main/java/com/jounaidr/jrc/node/blockchain/Block.java#L104-L107). More information on this component used can be found in this repo: [CryptoNightJNI](https://github.com/jounaidr/CryptoNightJNI) 

I am also working on a java native implementation to potentially replace the JNI component, for which current progress can be found in these repos: [keccak-java-speedtest](https://github.com/jounaidr/keccak-java-speedtest), [JCryptoNight](https://github.com/jounaidr/JCryptoNight).

### Block validation
In order to validate blocks, [Block.java](https://github.com/jounaidr/jrc-node/blob/master/src/main/java/com/jounaidr/jrc/node/blockchain/Block.java)
uses the following method, that throws an `InvalidObjectException` on invalid blocks:
```java
// Validation checks against the supplied previous block
if(!previousBlock.getHash().equals(previousBlock.generateHash())){
    throw new InvalidObjectException(String.format("Block validation failed, supplied previous block has an invalid hash. Supplied previous block hash: %s, should be: %s...", previousBlock.getHash(), previousBlock.generateHash()));
}
if(!previousBlock.isProofOfWorkValid()){
    throw new InvalidObjectException("Block validation failed, supplied previous block has an invalid proof of work...");
}
// Validation checks for this block
if(!this.getPreviousHash().equals(previousBlock.getHash())){
    throw new InvalidObjectException(String.format("Block validation failed, this block doesn't reference the previous blocks hash correctly. Reference to previous hash: %s, supplied previous blocks hash: %s...", this.getPreviousHash(), previousBlock.getHash()));
}
if(!this.getHash().equals(this.generateHash())){
    throw new InvalidObjectException(String.format("Block validation failed, this block has an incorrect hash value. This blocks hash: %s, should be: %s...", this.getHash(), this.generateHash()));
}
if(!this.isProofOfWorkValid()){
    throw new InvalidObjectException("Block validation failed, this block has an incorrect proof of work...");
}
```

### P2P Networking
Each node contains an initial list of peers (which is set on node initialization through the `peers.sockets` property) for which communication occurs through HTTP RESTful endpoints. The peer list is then subsequently updated automatically when new peers are discovered in the cluster.
The maximum numbers of peers the node can communicate with is set using the `peers.max` property.

#### API
The server API is generated using the [OpenApi Generator](https://github.com/OpenAPITools/openapi-generator) with spring integration.
The API specification is located in the server-api-generator module, called [openapi.yaml](https://github.com/jounaidr/jrc-node/blob/develop/server-api-generator/src/main/resources/openapi.yaml).
New endpoints can be defined following the format outlined in the spec, for example the `/blockchain/size` endpoint is defined as follows:
```yaml
/blockchain/size:
  get:
    summary: Get the blockchain length
    operationId: getBlockchainSize
    responses:
      200:
        description: successful operation
        content:
          application/json:
            schema:
              type: integer
```
Running `mvn install` on the server-api-generator or root modules will generate the relevant API classes and Model classes to be used by spring.
All generated code will be placed in the following package: `com.jounaidr.jrc.server.api.generated...`

Within the generated classes, there will be an `...ApiDelegate` interface for each top-level endpoint defined, for example the /blockchain/... endpoints are implemented in the [BlockchainApiDelegateImpl.java](https://github.com/jounaidr/jrc-node/blob/develop/server/src/main/java/com/jounaidr/jrc/server/api/implementation/BlockchainApiDelegateImpl.java) class which implements the generated class. 
Each `...ApiDelegateImpl` must be defined as a bean within [JrcServerConfig.java](https://github.com/jounaidr/jrc-node/blob/develop/server/src/main/java/com/jounaidr/jrc/server/JrcServerConfig.java).

Click [here]()(Currently unimplemented) for the documentation on the currently implemented endpoints.

#### Interfacing Peers
Each node runs the service bean, [Peers.java](https://github.com/jounaidr/jrc-node/blob/develop/server/src/main/java/com/jounaidr/jrc/server/peers/Peers.java), which contains a list of [Peer.java](https://github.com/jounaidr/jrc-node/blob/develop/server/src/main/java/com/jounaidr/jrc/server/peers/peer/Peer.java) objects that are able to interface with the different endpoints for the associated peer.
The [OkHttp](https://square.github.io/okhttp/) client package is used to handle the peer requests, which is wrapped within the [PeerClient.java](https://github.com/jounaidr/jrc-node/blob/develop/server/src/main/java/com/jounaidr/jrc/server/peers/peer/PeerClient.java) class, that contains helper methods to handle and return the relevant data from the response objects.  

The [Peers](https://github.com/jounaidr/jrc-node/blob/develop/server/src/main/java/com/jounaidr/jrc/server/peers/Peers.java) service bean initialises with a [ScheduledThreadPoolExecutor](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ScheduledThreadPoolExecutor.html) that increases its thread pool size by one for each [Peer](https://github.com/jounaidr/jrc-node/blob/develop/server/src/main/java/com/jounaidr/jrc/server/peers/peer/Peer.java).
The executor is used to run Peer services, for which there are currently two, the [PeerBroadcastingService](https://github.com/jounaidr/jrc-node/blob/develop/server/src/main/java/com/jounaidr/jrc/server/peers/peer/services/PeerBroadcastingService.java), and the [PeerPollingService](https://github.com/jounaidr/jrc-node/blob/develop/server/src/main/java/com/jounaidr/jrc/server/peers/peer/services/PeerPollingService.java).
Using a pooled thread executor allows for peers to be polled, and to be broadcast to simultaneously, whilst also being efficient as threads are held onto and not constantly being created.
Since the `peers.max` can be set by the user, the executor's pool size could increase to an amount that might start to affect system performance, therefore the user is warned on initialisation as follows:
```java
if(maxPeers > Runtime.getRuntime().availableProcessors()){
    log.warn("It is recommended to set the max peers to less than {} for your system, performance may be impacted...", Runtime.getRuntime().availableProcessors());
}
```
The integer returned by `Runtime.getRuntime().availableProcessors()` details the number of logical processors available on the system, 
for which the optimal number of threads can be found using the following formula: `Number of threads = Number of Available Cores * Target CPU utilization * (1 + Wait time / Service time)`
(more detail on this [here](https://engineering.zalando.com/posts/2019/04/how-to-set-an-ideal-thread-pool-size.html)).
Until performance if fully tested to calculate the formulas variables, currently only a warning is displayed if the amount peers exceeds the amount of logical cores available.

The [PeerPollingService](https://github.com/jounaidr/jrc-node/blob/develop/server/src/main/java/com/jounaidr/jrc/server/peers/peer/services/PeerPollingService.java) run method script order is as follows:
* First the peer's health endpoint (/actuator/health) is requested, and the local status of the peer is updated accordingly in its [Peer.java](https://github.com/jounaidr/jrc-node/blob/develop/server/src/main/java/com/jounaidr/jrc/server/peers/peer/Peer.java) object.
* The peer's socketlist endpoint (/peers) is then checked to see if the peer has discovered any new peers itself, for which the node will then add to its own peer list
* The peer's blockchain size is then retrieved (/blockchain/size) and depending its difference with the node's blockchain size, the following happens:
    - If there is no difference, the nodes are insync (and do nothing)
    - If the difference is 1, the peer has the latest block, for which the node will request and validate it
    - If the difference is greater than 1, the nodes blockchain is potentially out of sync with the network and will attempt to re-synchronise
    - If the difference is less than 1, then the peer is out of sync with the network (and do nothing)

## Testing
### Unit Tests
Each class has an associated unit test class which can be found [here](https://github.com/jounaidr/jrc-node/tree/master/src/test/java/com/jounaidr/jrc/node).
* ~~100% line coverage as of 16/11/2020~~
* 98% line coverage as of 14/01/2021

### Minerate Integration Test
Inorder to test that the difficulty is adjusted correctly, a basic [mine rate integration test](https://github.com/jounaidr/jrc-node/blob/develop/src/test-integration/java/com/jounaidr/jrc/node/MinerateTest.java) was created.
The test simulates a blockchain object being ran in a new thread, for which the console logs can be monitored to validate correct behaviour.
The test is set to run for 30 mins, which gives the following results for a single node running with a [Intel Core i7 i7-10750H](https://ark.intel.com/content/www/us/en/ark/products/201837/intel-core-i7-10750h-processor-12m-cache-up-to-5-00-ghz.html):
```shell script
2020-11-16 21:50:28 [main] INFO  - Valid proof of work has been found with nonce: 2152, and timestamp 2020-11-16T21:50:28.741831300Z ! POW hash was found in: 34 seconds...
2020-11-16 21:50:28 [main] INFO  - The following block has been mined: Block{hash='bc830f8dd60b7792cff22fe3dae268ca0f4f085e6bb9e97c73107dc4dabcbaba', previousHash='0098f81819a2f4302ebb77c91e3c1910669830b56d0393279662d27324887b40', data='����K5', timeStamp='2020-11-16T21:50:28.741831300Z', nonce='2152', difficulty='11', proofOfWork='0000000000010100000111010011110001111101001111011111001100100010010000100010011011001011011111110100001001111011100001100011011001111011111101110101101110000110001111110001011000000010000010101000101010101011011111111001100101101111111111000101010011101011'} ...
Block took: 34 seconds to mine...
2020-11-16 21:52:29 [main] INFO  - Valid proof of work has been found with nonce: 7696, and timestamp 2020-11-16T21:52:29.340615500Z ! POW hash was found in: 121 seconds...
2020-11-16 21:52:29 [main] INFO  - The following block has been mined: Block{hash='8004f34c95b89f338e9fa691ef8130b8fb107c8fd0897fd86859537ce645128f', previousHash='bc830f8dd60b7792cff22fe3dae268ca0f4f085e6bb9e97c73107dc4dabcbaba', data='X�SG(t', timeStamp='2020-11-16T21:52:29.340615500Z', nonce='7696', difficulty='10', proofOfWork='0000000000110100100100001001010100100101100011000111100101011101101011110001001000011011111000000110000101110011100011011101110101100100100111011100111100100101100010001010100000000010101010111110101111101001100100010100111111110100001111100100010001010011'} ...
Block took: 121 seconds to mine...
2020-11-16 21:54:06 [main] INFO  - Valid proof of work has been found with nonce: 6221, and timestamp 2020-11-16T21:54:06.965242200Z ! POW hash was found in: 97 seconds...
2020-11-16 21:54:06 [main] INFO  - The following block has been mined: Block{hash='1a61f1d7ad9455733bf17b5dd4b9bf16ad478632e19e4527e74cc25e06ad80b3', previousHash='8004f34c95b89f338e9fa691ef8130b8fb107c8fd0897fd86859537ce645128f', data='�Ӽ0�&', timeStamp='2020-11-16T21:54:06.965242200Z', nonce='6221', difficulty='11', proofOfWork='0000000000011011010001001001010110000101010010001010000011111011111010100010011100000010110010101001100110100001011011011001010001110101110001101111011110110001011101001111101111000011111110111011111100011000001100000110000001101111010101100001001101101111'} ...
Block took: 97 seconds to mine...
2020-11-16 21:54:39 [main] INFO  - Valid proof of work has been found with nonce: 2070, and timestamp 2020-11-16T21:54:39.383180300Z ! POW hash was found in: 33 seconds...
2020-11-16 21:54:39 [main] INFO  - The following block has been mined: Block{hash='63b66e1242ce29ab71385f0308398f2eb899b75b231a11da94690555c23f6238', previousHash='1a61f1d7ad9455733bf17b5dd4b9bf16ad478632e19e4527e74cc25e06ad80b3', data='�����', timeStamp='2020-11-16T21:54:39.383180300Z', nonce='2070', difficulty='12', proofOfWork='0000000000001101100101101001110001101111011000001110010111010111011101110011011110101010101010110110100110001011011010001011001000000010101111000000100101001000010001011110111000100011001110010010110100111000010011111110100111100110110100001111111011010100'} ...
Block took: 33 seconds to mine...
2020-11-16 21:55:46 [main] INFO  - Valid proof of work has been found with nonce: 4200, and timestamp 2020-11-16T21:55:45.990708800Z ! POW hash was found in: 66 seconds...
2020-11-16 21:55:46 [main] INFO  - The following block has been mined: Block{hash='5109e8918bc332039d078f288a931db9e27da3c4b04a91dc1523be9f2eaf016c', previousHash='63b66e1242ce29ab71385f0308398f2eb899b75b231a11da94690555c23f6238', data='�ⰨU�', timeStamp='2020-11-16T21:55:45.990708800Z', nonce='4200', difficulty='13', proofOfWork='0000000000000110010000110000001110100000011010000111110000110100101111010101111011100110010001001110010011011111111111000101001111100001010011111101010010110100100110101011001100010110101000111101101010010011110111001010000100111011111100001100111000111001'} ...
Block took: 66 seconds to mine...
2020-11-16 21:57:50 [main] INFO  - Valid proof of work has been found with nonce: 7854, and timestamp 2020-11-16T21:57:50.640631300Z ! POW hash was found in: 125 seconds...
2020-11-16 21:57:50 [main] INFO  - The following block has been mined: Block{hash='0573b651163edf09bcd8c560d96df72ab47fef54995082af06f433e941a4b36e', previousHash='5109e8918bc332039d078f288a931db9e27da3c4b04a91dc1523be9f2eaf016c', data='du����', timeStamp='2020-11-16T21:57:50.640631300Z', nonce='7854', difficulty='12', proofOfWork='0000000000001111110110001110110010011100001000111110100000001110000010101111111011111010011010110111110111111111111001101110010010111101100010101101110101000101000100100111000111010010000110101111110110001111000101001011110111000010000011100001000111001111'} ...
Block took: 125 seconds to mine...
2020-11-16 22:00:22 [main] INFO  - Valid proof of work has been found with nonce: 9883, and timestamp 2020-11-16T22:00:22.182340500Z ! POW hash was found in: 152 seconds...
2020-11-16 22:00:22 [main] INFO  - The following block has been mined: Block{hash='d613db05f6f6f3f9681ba2a281ffd4e9d6c43baf6b5b4300bfeb350dc373370a', previousHash='0573b651163edf09bcd8c560d96df72ab47fef54995082af06f433e941a4b36e', data='n���O-', timeStamp='2020-11-16T22:00:22.182340500Z', nonce='9883', difficulty='11', proofOfWork='0000000000010011111011001010011111110110110101011010100110000011011010011101011101110011001001100001100101011101010101110011000110001101010111010110110111011100010111100000101011001000010010011010111101010100111101010101011001101101010001101010001011011001'} ...
Block took: 152 seconds to mine...
```
The test proves that difficulty adjustment during mining works correctly with a single node, and suggests the difficulty will range from 10 to 13 in these circumstances.

## DevOps
### CI Testing
For my CI pipeline I am currently using [Circleci](https://circleci.com/) to execute the [unit test suite](https://github.com/jounaidr/jrc-node/tree/master/src/test/java/com/jounaidr/jrc/node) on each commit.
I might switch to using a Jenkins based pipeline later on in development depending on resources.

### Version Control
The default project branch is set to [develop](https://github.com/jounaidr/jrc-node), for which I will commit to during development.
The [master](https://github.com/jounaidr/jrc-node/tree/master) branch is currently locked and will only be unlocked at commit to after each
major slice of the project is complete. 

### Project/Issue Tracking
To track the tickets I am currently working on I am using [GitHub projects](https://github.com/users/jounaidr/projects/1).

---

## License

[![License](http://img.shields.io/:license-mit-blue.svg?style=flat-square)](http://badges.mit-license.org)

- **[MIT license](http://opensource.org/licenses/mit-license.php)**
- Copyright 2020 © JounaidR.
