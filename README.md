# RaspberryPi IoT

This project consists of three sub projects, an Angular project and two Spring Boot projects. The idea for this project came when I needed a way for my solar heated swimming pool to be toggled on and off based on the temperature the solar panels reach when the sun hits them. I decided to build a Spring Boot backend project that will allow a user to set configuration parameters to control the behaviour of the second Spring Boot project that will be run on a RaspberryPi zero. Users can set a turn off temperature threshold at which value the pump will autonomously switch off, they can also specify whether they want the pump to be overriden, **OFF** to have the pump on for how ever long the user decides, **ON** to have the pump switched on or **NONE** to have the system function autonomously. The backend Spring Boot project will be run on a RaspberryPi4 4GB model. The ***Angular project*** serves as a front end to the ***Control Hub Project***. The ***RaspberryPi Pump Controller Project*** interfaces with the ***Control Hub Project*** to get its task of controlling the pool pump done.

## Installation
This project makes use of the RabbitMQ message broker and as such will require you to have an instance running in a docker container. The instance that was used was the 3.6.9-management instance but there should not be a reason for it not to work on the latest version, provided the instance also includes the management interface. The command to run is 

```docker run -d --name name_of_choice -p 15672:15672 -p 5672:5672 rabbitmq:3.6.9-management```. 

You can view the message broker and its channels and exchanges by visiting ```localhost:16572```, the default username and password is *guest*.

The project will also require you to have node installed, follow [this](https://nodejs.org/en/download/) link to install it for your operating system. 

With node installed we can also now install the needed Angular CLI. To install it globally run ```npm install -g @angular/cli```.

## Control Hub Project
To start the backend with which the user interacts via the Angular project we simply run the main application in your IDE. This has to be done before the UI project is started as it depends on this backend project backend.

## Angular Project
To start the Angular project navigate to the directory containing your *package.json* file and run ```npm install```, this will install all needed node modules on your local machine needed to run the Anular app. Next we serve the project by running ```ng serve```. This will host the UI on ```localhost:4200```.

## RaspberryPi Pump Controller Project
To start this project, same as before, navigate to the main application class and run the program in your IDE.