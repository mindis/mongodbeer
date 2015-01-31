## Couchbase to MongoDB

As a followup to one *fun* tweet I have migrated the Beer Sample application from Couchbase on a MongoDB instance.

<blockquote class="twitter-tweet" lang="en"><p>Moving my Java from Couchbase to MongoDB <a href="http://t.co/Wnn3pXfMGi">pic.twitter.com/Wnn3pXfMGi</a></p>&mdash; Tugdual Grall (@tgrall) <a href="https://twitter.com/tgrall/status/559664540041117696">January 26, 2015</a></blockquote>
<script async src="//platform.twitter.com/widgets.js" charset="utf-8"></script>

I have only changed few lines of code, connections, query, JSON manipulation *(just for fun)*


### Quick Start

##### Couchbase

Install Couchbase 2.x or 3.x, and be sure to import the beer-sample data


##### MongoDB

Install MongoDB


##### Import the from using MongoDB CB Data Replicator

Configure Couchbase XDCR to send the data into your MongoDB database.

See:


##### Install and run the application

Follow the steps documented here:


Clone this repo:

```
  $ git clone git://github.com/tgrall/mongodbeer.git
  $ cd mongodbeer
```

Run the application using jetty container.

```
  $ mvn  jetty:run
```

Navigate to http://localhost:8080/welcome
