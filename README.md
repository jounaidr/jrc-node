# jrc-node

jrc aims to be a simple and secure cryptocurrency network implementing the Cryptonight proof of work 
algorithm, and developed using the Java Spring framework.

---

* [Installation](#installation)
* [Features](#features)
    - [Genesis](#genesis)
    - [Block Hashing](#block-hashing)
    - [Proof Of Work](#proof-of-work)
    - [Chain Validation/Replacement](#chain-validationreplacement)
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
private static final String GENESIS_TIME_STAMP = "1";
```
Note: this data will be changed upon deployment

### Block Hashing
Each block's hash is generated using the [Keccak-256](https://keccak.team/keccak_specs_summary.html) algorithm, for which the implementation can be found
in [KeccakHashHelper.java](https://github.com/jounaidr/jrc-node/blob/master/src/main/java/com/jounaidr/jrc/node/crypto/KeccakHashHelper.java). 
This implementation uses the [Bouncy Castle Api](https://www.bouncycastle.org/).

### Proof Of Work
jrc will implement the Cryptonight proof of work algorithm, for which I am currently researching and developing a Java implementation.
My current progress can be found in [this](https://github.com/jounaidr/keccak-java-speedtest) repo.

### Chain Validation/Replacement
In order to validate incoming chains, [Blockchain.java](https://github.com/jounaidr/jrc-node/blob/master/src/main/java/com/jounaidr/jrc/node/blockchain/Blockchain.java)
uses the following method:
```java
public boolean isChainValid(){
    if(this.chain.get(0).toString() != new Block().genesis().toString()){
        return false; //Verify first block in chain is genesis block
    }

    for(int i=1; i < this.chain.size(); i++){
        if(this.chain.get(i).getPreviousHash() != this.chain.get(i-1).getHash()){
            return false; //Verify each block in the chain references previous hash value correctly
        }
    }
    return true;
}
```

Chain replacement implements [ReentrantReadWriteLock](https://docs.oracle.com/javase/7/docs/api/java/util/concurrent/locks/ReentrantReadWriteLock.html) 
for concurrency:
```java
writeLock.lock();
try {
    this.chain = newChain;
} finally {
    writeLock.unlock();
}
```

## DevOps
### CI Testing
For my CI pipeline I am currently using [Circleci](https://circleci.com/) to execute my [test suite](https://github.com/jounaidr/jrc-node/tree/master/src/test/java/com/jounaidr/jrc/node) on each commit.
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
