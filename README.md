
Notification Service Test Client

It's a test standalone console app, tries to make Socket connection to port and IP address 

specified by params on start. After establishing connection it sends two JSONObjects

["external_id", "1", "message", "New message 1", "time", LocalDateTime.now().plusSeconds(5L),"notification_type", "email=comexample@yandex.ru"]

["external_id", "2", "message", "New message 2", "time", LocalDateTime.now().plusSeconds(10L),"notification_type", "url=https://www.ya.ru"]

Usage: java NotifyClient <port number> <IP address> (port 1-65535)



Maven goals:

1. To start client - 'mvn clean package exec:java "-Dexec.args=8090 127.0.0.1"'

2. To make executable jar - 'mvn clean compile assembly:single'
