# HTM-MoClu

> Hierarchical Temporal Memory Models Cluster implementation

## What is Htm-Moclu?

Short for **Hierarchical Temporal Memory Models Cluster**. [Numenta](http://numenta.com/) presented **[HtmEngine](https://github.com/numenta/numenta-apps/tree/master/htmengine)** which is a set of long-running services upon which a scalable, real-time, and production-ready scalar anomaly detection application may be built. Currently, **HtmEngine** can only be scaled vertically. **Htm-Moclu** provides a similar platform for [htm.java](https://github.com/numenta/htm.java) applications, and has the ability to scale horizontally using multiple servers.

You can have several Web servers and several servers just to handle model data all of them working as a Cluster.

## Architecture

- Models, anomaly detection, and prediction provided by [htm.java](https://github.com/numenta/htm.java)
- Cluster implemented using **Akka Cluster + Sharing + Persistence**
- Data store: Mongodb
- Rest API: Lift Framework (moclu-http)

## Installation ##

### Requirements ###
**SBT**

* http://www.scala-sbt.org/download.html 
* http://www.scala-sbt.org/0.13/tutorial/Manual-Installation.html

**MongoDB**

* https://www.mongodb.org/downloads#production
* Linux http://docs.mongodb.org/master/tutorial/install-mongodb-on-linux
* OSX http://docs.mongodb.org/master/tutorial/install-mongodb-on-os-x
* Windows http://docs.mongodb.org/master/tutorial/install-mongodb-on-windows

After installing MongoDB you will need to start mongod service and use default settings, otherwise you will need to configure application.conf

***Starting the cluster***
```sh
git clone https://github.com/antidata/htm-moclu.git
cd htm-moclu
sbt publishLocal
sbt console
```

***Starting the Web server***
```sh
cd htm-moclu/moclu-http
sbt ~container:start
```

### Using the REST API ###

####Create model - Endpoint ####

`/create/{model id}`

Example

`/create/24StreetSensor`

* Request Payload

```json
{ }
```

* Response

```json
{
  "status":200,
  "msg":"Htm Model 24StreetSensor created"
}
```

####Send data - Endpoint ####
`/event/{model id}`

Example

`/event/24StreetSensor`

* Request Payload

```json
{ 
  "value":12.2,
  "timestamp":"7/2/10 1:11" 
}
```

* Response

```json
{
  "status":200,
  "msg":"Applied event Htm Model 24StreetSensor",
  "data":{
    "id":"24StreetSensor",
    "anomalyScore":1.0,
    "prediction":"12.2"
  }
}
```

####Get submitted data - Endpoint ####
`/getData/{model id}`

Example

`/getData/24StreetSensor`

* Request Payload

```json
{ }
```

* Response

```json
{
  "status":200,
  "data":[{
    "value":12.2,
    "timestamp":1278051060000,
    "anomalyScore":1.0
  },{
    "value":13.2,
    "timestamp":1278051120000,
    "anomalyScore":1.0
  },{
    "value":14.2,
    "timestamp":1278051180000,
    "anomalyScore":1.0
  },{
    "value":12.2,
    "timestamp":1278051300000,
    "anomalyScore":1.0
  },{
    "value":13.2,
    "timestamp":1278051360000,
    "anomalyScore":0.0
  }]
}
```

### Few examples using **wget** ###
* Create

```sh
wget -O- --post-data='{}' --header=Content-Type:application/json "http://localhost:8080/create/24StreetSensor"
```

* Send event data

```sh
wget -O- --post-data='{"value":13.2,"timestamp":"7/2/10 1:16"}' --header=Content-Type:application/json "http://localhost:8080/event/24StreetSensor"
```

* Get events data

```sh
wget -O- --header=Content-Type:application/json "http://localhost:8080/getData/24StreetSensor"
```

### Setting up the Cluster ###

#### Configuring MongoDB ####

To set the IP address where you have MongoDB running just add the following parameter to SBT:

`-DMONGOHOST={IP}`

For example having Mongo running on 192.168.1.144 the config will look like

`-DMONGOHOST=192.168.1.144`

This configuration is the same for all nodes.

#### Cluster Seeds ####

You can have one or more seeds for your Cluster, you can start trying with just one.

add the following parameter to SBT:

`-DSEEDHOST={IP}`

In the case you have the Seed running on 192.168.1.122, after modifying:

`-DSEEDHOST=192.168.1.122`

If you want to add more seed nodes you will need to update the application.conf and add more parameters.

#### Local IP ####

Finally we need to specify the local IP with the following parameter

`-DHOST=192.168.1.123`

In case you are starting the node in 192.168.1.123

#### Putting all together ####

```sh
$ sbt -Djava.library.path=./sigar -DHOST=192.168.1.123 -DSEEDHOST=192.168.1.122 -DMONGOHOST=192.168.1.144 -Xmx8096M -Xss2M
```
