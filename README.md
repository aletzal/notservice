
Notification Service

It's a test standalone console app that works like alarm, sends notification message through

email or http request. After start it opens ServerSocket connection and listens port, specified by start params.

To add new task you need to send JSONObject to specified port with params:

[external_id: (String), message: (String), time (LocalDateTime), notification_type (String)]

notification_type can be as one of patterns like: 'url=http://...' or 'email=email@email.ru'.

Time to run task must be between 50ms < time < 1 week from now (UTC+3).



Usage: java NotifyServer <port number> <IP address>(may be blank default IP address '127.0.0.1')



Maven goals:

1. To start server - 'mvn clean package exec:java "-Dexec.args=8090 127.0.0.1"'

2. To make executable jar - 'mvn clean compile assembly:single'
