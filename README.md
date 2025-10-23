# DeviceNet

Run server:

`$ ./gradlew bootRun`


Run client code:

`./gradlew runClientMain`


Run unit tests:

`./gradlew test`



## Tech Stack

- Java 21
- Gradle 8.7
- REST API
- Spring Boot (simple server app setup)


## Design Overview

For the sake of example, data is stored in memory. We are building a tree (actually forest) structure on addDevice and store it directly in the tree form, leading to very efficient topology retrieval. Device nodes are stored in MacAddress &rarr; Node map that can be naturally transferred to a database, with MacAddress being used as a primary key.


The crux of the design is inverting the uplink edges on insert for a more efficient child-node retrieval. If they were stored in the provided child &rarr; parent format, we would have to browse all devices to collect a root device children runtime on (recursive) topology retrieval.


Since the network topology is large, shallow, but broad (large out-degree for top-level nodes), using a graph db (e.g. Neo4J) would make more sense than a relational db (parent &rarr; child is a (very) many-to-one map, hence storing children for each parent row isn't robust and update friendly, the issues can be addressed by maintaining an index on composed keys parent-mac &rarr; child-mac, that allows for efficient range lookup).  


### Add device

Endpoint `"/devices"`

Method: POST

Curl:

```
curl -X POST http://localhost:8080/api/devices \
-H "Content-Type: application/json" \
-d '{"macAddress": "CC:DD:EE:FF:AA:BB", "deviceType": "Access Point", "uplinkMacAddress": "BB:CC:DD:EE:FF:AA"
}'
```

Accepts json.

On insert, a node is created to represent to device and linked with both its parent (if non-empty and exists among registered devices), and already registered children. We allow to register a device with uplink MAC address not yet registered. If the parent is added later, the existing child is subsequently linked to it. In topology retrieval, nodes with set uplink MAC but non-registered parent are treated as root nodes.

The input is validated (MAC and deviceType must be set, uplink must not be self-reference), and we also do the global no-cycle check (a cycle may be created if nodes with ghost parents previously existed and the parent is newly registered). The check is cheap and does not require the full toposort/Tarjan algorithm, as each node has at most one parent. We can browse nodes via child &rarr; parent edges started from the one being added, which takes O(graph-depth) time (graph-depth is low and constant for a network graph, although in general we may need to browse all nodes if a graph forms a single cycle).

We may avoid doing the tree-check if only registered parents are allowed to be referenced via uplink, as each newly added graph node would be a leaf.

InvalidArgumentException is returned for an invalid input.



### List devices

Endpoint: `"/devices"`

Method: GET

Curl:

```
curl -X GET http://localhost:8080/api/devices
```

Devices are sorted runtime. For better performance, we could maintain a sorted index by deviceType in memory (e.g. as a linked list for easy insertion) or in a db.


### Get device

Endpoint: `"/devices/{macAddress}"`

Method: GET

Curl:

```
curl -X GET http://localhost:8080/api/devices/AA:BB:CC:DD:EE:FF
```

### Network

Endpoints:
`"/network"`
`"/network/{rootMacAddress}"`

Method: GET

Curl:

```
curl -X GET http://localhost:8080/api/network

curl -X GET http://localhost:8080/api/network/AA:BB:CC:DD:EE:FF
```

The network topology is maintained on device insertion. The specified root node (or all top-level nodes) is returned.
