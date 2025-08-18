# caching-proxy
Roadmap.sh Spring Boot project: https://roadmap.sh/projects/caching-server

The project is built using Spring Boot in Java (Maven).  
Spring Boot version: 3.4.7  
Java version: 24  
*Spring Shell version: 3.4.0  

## Running the program

Git clone the project:
```sh
git clone https://github.com/mravluk/caching-proxy.git
```
Add a project to your Java IDE and run it

## Usage

CLI tool currently has only 2 commands: one for creating a proxy server and one for clearing the cache
1. Creating a proxy server: 
```sh
caching-proxy --port <port> --origin <url>
```
2. Clearing the cache:
```sh
caching-proxy --clear-cache
```