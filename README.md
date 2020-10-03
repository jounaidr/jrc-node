# jrc-node

jrc aims to be a simple and secure cryptocurrency network implementing the Cryptonight proof of work 
algorithm, and developed using the Java Spring framework.

---

* [Installation](#installation)
* [Features](#features)
    - [Genesis](#genesis)
    - [Block Hashing](#block%20hashing)
    - [Proof Of Work](#proof%20of%20work)
* [License](#license)

---

## Installation

Installation will be done through RPM on RHEL/Centos servers (currently unimplemented)

## Features

### Genesis
The genesis block is currently generated based on the following parameters (which is hardcoded in [block.java](https://github.com/jounaidr/jrc-node/blob/master/src/main/java/com/jounaidr/jrc/node/blockchain/Block.java)):
```java
private static final String GENESIS_PREVIOUS_HASH = "dummyhash";
private static final String GENESIS_DATA = "dummydata";
private static final String GENESIS_TIME_STAMP = "1";
```
Note: this data will be changed upon deployment

### Block Hashing
Each block's hash is generated using the [Keccak-256](https://keccak.team/keccak_specs_summary.html) algorithm (similar to SHA3), for which the implementation can be found
in [KeccakHashHelper.java](https://github.com/jounaidr/jrc-node/blob/master/src/main/java/com/jounaidr/jrc/node/crypto/KeccakHashHelper.java). 
This implementation uses the [Bouncy Castle Api](https://www.bouncycastle.org/).

### Proof Of Work
jrc will implement the Cryptonight proof of work algorithm.

---

## License

[![License](http://img.shields.io/:license-mit-blue.svg?style=flat-square)](http://badges.mit-license.org)

- **[MIT license](http://opensource.org/licenses/mit-license.php)**
- Copyright 2020 © JounaidR.