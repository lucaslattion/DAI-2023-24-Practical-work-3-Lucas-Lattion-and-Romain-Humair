# DAI-2023-24-Practical-work-3 Java UDP programming

* Lucas Lattion
* Romain Humair

# GPS Tracker

## How to build the application

first, clone the repository.

Then, you have two possibilities to build the jar file:
1. IntelliJ
    2. select "Maven Package as JAR file" on top right of IntelliJ
    3. click "Run" button
2. command line in the project folder
    3. ```mvn dependency:resolve clean compile package```


JAR file will be generated in the /target/ folder

## How to use the CLI

minimum parameter is client or server mode

cmd
```
java -jar java-udp-programming-1.0-SNAPSHOT.jar
```
result
```
Please specify either --server or --client option.
```




## Help
cmd
```
java -jar java-udp-programming-1.0-SNAPSHOT.jar -h
```
result
```

```


## Examples

Server : ```java -jar java-udp-programming-1.0-SNAPSHOT.jar -s```
```

```

Client : ```java -jar java-udp-programming-1.0-SNAPSHOT.jar -c```
```

```








### Section 1 - Overview
This protocol outlines the communication for a GPS tracking system using UDP. It involves the exchange of GPS data between trackers, a server, and clients. The system is designed to efficiently transmit real-time and historical location data, including timestamps, latitude, longitude, and battery levels.

- GPS-emitter --> Server <--> Client

### Section 2 - Transport Protocol
The system uses the User Datagram Protocol (UDP) due to its low latency and efficiency. The following UDP ports are designated by default for communication:
- **Tracker to Server Port:** 12345 (used by GPS trackers to send data to the server)
- **Client to Server Port:** 23456 (used by clients to send requests to the server and by server to reply)

ports be changed. use the cmd `--help` for more information



### Section 3 - Messages
#### GPS-Tracker to Server
The tracker send message 
- **PROVIDE Message:**
   - **Format:** `PROVIDE:ID,timestamp,latitude,longitude,battery_level`
   - **Example:** `PROVIDE:1234,20231218T123000Z,37.7749,-122.4194,85`


#### Client to Server
- **GET-IDS Message:** list of all trackers ID stored on the server
    - **Format:** `GET-IDS:`

    - **REQUEST** 
    ```
    GET-IDS:
    ```
    - **RESPONSE** 
    ```
   IDS:
    245160350818314
    597968797216796
    673352503620987
    945983442423128
    ```
- **GET-LAST Message:** last data from a specific tracker (ID)
    - **Format:** `GET-LAST: <ID>`
    - **REQUEST** 
    ```
    GET-LAST:673352503620987 
    ```
    - **RESPONSE** 
    ```
   TrackerData{trackerId='673352503620987', timestamp=1702957396983, latitude=50.5579, longitude=-88.518, batteryLevel=17}
    ```
- **GET-HISTORY Message:** all data from a specific tracker (ID)
    - **Format:** `GET-HISTORY: <ID>`
    - **REQUEST** 
    ```
    GET-HISTORY:673352503620987
    ```
    - **RESPONSE** 
    ```
   HISTORY:
    TrackerData{trackerId='673352503620987', timestamp=1702957301992, latitude=52.6475, longitude=-90.7085, batteryLevel=23}
    TrackerData{trackerId='673352503620987', timestamp=1702957306974, latitude=44.985, longitude=-111.787, batteryLevel=43}
    TrackerData{trackerId='673352503620987', timestamp=1702957311974, latitude=55.3323, longitude=-90.7589, batteryLevel=14}
    TrackerData{trackerId='673352503620987', timestamp=1702957316977, latitude=49.6168, longitude=-100.9021, batteryLevel=44}
    TrackerData{trackerId='673352503620987', timestamp=1702957321979, latitude=43.1958, longitude=-102.6128, batteryLevel=30}
    TrackerData{trackerId='673352503620987', timestamp=1702957326976, latitude=53.781, longitude=-80.1177, batteryLevel=64}
    TrackerData{trackerId='673352503620987', timestamp=1702957331985, latitude=55.2012, longitude=-95.1729, batteryLevel=58}
    TrackerData{trackerId='673352503620987', timestamp=1702957336978, latitude=56.5947, longitude=-93.2001, batteryLevel=94}
    ```

- **GET-ALL Message:** last position of all trackers
    - **Format:** `GET-ALL:`
    - **REQUEST** 
    ```
    GET-ALL: 
    ```
    - **RESPONSE** 
    ```
   ALL:
    245160350818314: TrackerData{trackerId='245160350818314', timestamp=1702957300192, latitude=43.807, longitude=-82.2927, batteryLevel=26}
    597968797216796: TrackerData{trackerId='597968797216796', timestamp=1702957298460, latitude=51.1479, longitude=-107.5987, batteryLevel=72}
    673352503620987: TrackerData{trackerId='673352503620987', timestamp=1702957501974, latitude=59.7718, longitude=-116.4574, batteryLevel=25}
    945983442423128: TrackerData{trackerId='945983442423128', timestamp=1702957295154, latitude=44.0893, longitude=-106.4811, batteryLevel=61}
    ```


### Section 4 - Examples
#### Example 1: Tracker Updating Server
- **Scenario:** A GPS tracker sends its current location and battery status to the server.
- **Message:** `PROVIDE:1234,20231218T123000Z,37.7749,-122.4194,85`
- **UDP Port:** 12345

#### Example 2: Client Requesting Last Known Position
- **Scenario:** A client requests the last known position of tracker ID 1234.
- **Message:** `GET-LAST:1234`
- **UDP Port:** 23456


## Section 5 - Protocol Diagrams

### Normal working diagram case

In this sequence diagram:
- The **GPS Tracker** sends 'PROVIDE' messages to the Server using UDP port 5050.
- The **Client** sends various 'GET-*' requests (like 'GET-IDS', 'GET-LAST', and 'GET-HISTORY') to the Server using UDP port 5051.
- The **Server** responds to the Client with 'LIST' messages using UDP port 5052.

This diagram provides a clear visual representation of the message flow between the GPS trackers, server, and clients in your system.

```mermaid
sequenceDiagram
    participant Tracker as GPS Tracker
    participant Server
    participant Client

    %% Messages from Tracker to Server
    Tracker->>Server: PROVIDE (5050)
    Note right of Server: Receives location data<br>and battery level

    %% Messages from Client to Server
    Client->>Server: GET-IDS (5051)
    Note right of Server: Responds with list<br>of tracker IDs

    Client->>Server: GET-LAST <id> (5051)
    Note right of Server: Sends last known<br>position of ID

    Client->>Server: GET-HISTORY <id> (5051)
    Note right of Server: Sends historical data<br>for ID

    %% Message from Server to Client
    Server->>Client: LIST (5052)
    Note right of Client: Receives list of<br>current tracker data
```

### Timeout diagram case

In this diagram:
- The **Client** initially sends a 'GET-LAST' request to the **Server** using UDP port 5051.
- If the packet is lost, the client waits for 2 seconds (timeout) and then retries the request.
- This retry mechanism occurs up to 5 times.
- Upon successful receipt of the request, the Server responds with the requested data using UDP port 5052.

This sequence effectively demonstrates how the system handles packet loss with a timeout and retry strategy, ensuring reliability in the data communication process.

```mermaid
sequenceDiagram
    participant Client
    participant Server

    %% Initial Request from Client to Server
    Client->>Server: GET-LAST <id> (5051)
    Note right of Server: Receives request for<br>last position of ID

    %% Handling Timeout and Retries
    alt Packet Lost - Retry Mechanism
        loop Retry up to 5 times
            Note over Client: Wait for 2 seconds<br>Timeout
            Client->>Server: GET-LAST <id> (5051)
            Note right of Server: Attempt to resend<br>last position of ID
        end
    end

    %% Successful Response from Server
    Server->>Client: Response with Data (5052)
    Note left of Client: Receives last known<br>position of ID
```

### Malformed Message Format

There is no respond to malformed messages. They are just ignored/dropped.

## Edge Cases

Edge cases could include network interruptions, client disconnections, and malformed message formats. Each case should be handled gracefully, with the server providing an appropriate error code and message to the client, or by timing out the connection after a certain period of inactivity.

### Network Interruption

In case of network inturruption, a lost packet is not important for the trackers.

For the client-server communication, in case a packet is lost, there is a timeout of 2 seconds and a trial of 5 times.



## Tool used
- Maven
- Java 17
- Intellij IDEA Ultimate
- GitHub
- Markdown
- ChatGpt
