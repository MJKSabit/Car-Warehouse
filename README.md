# Car Warehouse - JavaFX Offline

---

## Problem Analysis

- Server will have a database connection
- TCP Networing
- Authentication of **Manufacturer**
- Unauthenticated access of **Viewer**

## Users

### Admin

Add, remove **Manufacturer**

### Manufacturer

Add, Edit, Delete and View all cars

### Viewer

Search by Reg#, Search by Make/Model, Buy



## Data

### Car

Registration No, Make, Model, Year, Price, Color[3], **Quantity** (can be greater than 1), ***Image***

### Login Details

Hashed Password, Username



## Current UI

![Card View](https://i.ibb.co/qN6sdHz/image.png)



Simple Sever-Client Program using:

### Server

- SQLite using JDBC
- SHA265 Hashing
- Log4J2
- UUID for saving image
- JUnit
- Maven

### Client

- JavaFX Frontend
- Material Theme using JFoenix

## Communication Protocol

`Server` will have multiple `Client`. Each of these `Client` will have a `Router` and `State` of its own to manage State.

`Router` will have `Interceptor` and `Listener` which will catch a request to a followed protocol. 

`Error` will have message with it and will be precise.