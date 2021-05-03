# Raspberry Pi IoT
The raspberry-pi-iot project consists of a central system called the *Control Hub* that orchestrates user interaction with a number of IoT devices on a private network.

## Control Hub
The Control Hub runs on a RaspberryPi 2+, it facilitates messaging between the Control Hub and IoT devices. In addition to this it serves a frontend coded in Angular and a backend coded in Java - the frontend and corresponding backend allow end-users to manage their IoT devices within their private networks.
### System Requirements
* Arm7+ CPU architecture (RaspberryPi2, RaspberryPi3, RaspberryPi4)
* Internet capabilities
* An SD card at least 4 GB in size

### Installing the Control Hub
As stated previously, the Control Hub comprises the Angular frontend and the Java/Spring Boot backend, as such 2 components need to be deployed to a RaspberryPi. Ansible was used to define tasks for the 2 deployables mentioned.
To execute the tasks Ansible needs to be installed, follow the guide for your operating system [Installing Ansible on specific operating systems
](https://docs.ansible.com/ansible/latest/installation_guide/intro_installation.html#installing-ansible-on-specific-operating-systems) (Windows is not supported). Once Ansible has been installed, disable host key checking by adding the value *host_key_checking = False* to your ansible.cfg file (for Ubuntu this file is located at */etc/ansible*), this allows a password only SSH connection with remote hosts. Next follow the set up instructions below to deploy the Control Hub onto your RaspberryPi.
1. Install the *RaspberyPi OS Lite (32 bit)* operating system on an SD card using the [Raspberry Pi Imager software](https://www.raspberrypi.org/software/).
   * Select the *RaspberyPi OS Lite (32 bit)* image.
   * Insert the RaspberryPi SD card into you local machine and select it from the *CHOOSE STORAGE* list.
   * Open the *Advanced Options* pane by simulaeously hitting *Ctrl+Shift+X*.
     * Select *Enable SSH* and *Use password authentication* and enter a password you feel comfortable with for SSH-ing into the RaspberryPi later on.
     * In the case that your RaspberryPi has built in Wifi (RaspberryPi3+) and is the preferred means to connect to your private network, select *Configure wifi* and provide your Wifi specific SSID and password.
   * Click on *WRITE* (note that this will format the SD card and install the RaspberryPi OS onto it).
   
   For a video demonstration of how to perform the tasks mentioned follow the link [How to use Raspberry Pi Imager | Install Raspberry Pi OS to your Raspberry Pi (Raspbian)](https://www.youtube.com/watch?v=ntaXWS8Lk34).
2. Insert the SD card into your RaspberryPi and power it on.
3. Give the RaspberryPi time to connect to your private network, then find the IP address of your RaspberryPi by either logging into your router and opening the *Devices* tab or a similar option (depending on your router's make and model). Alternatively use a network utility like [netstat](https://linux.die.net/man/8/netstat) to determine the IP address of your RaspberryPi.   
4. Clone this project and navigate to the *Ansible* directory to open the *hosts.ini* file. Replace the IP address of the *[control_hub]* host with the IP address obtained in step 3.
5. Open the *deploy_and_start_control_hub_playbook.yml* file located in the *Ansible/Control Hub* directory and edit the value for *interface* to be either *wireless* or *ethernet*, based on what interface you will be using for the RaspberryPi to connect to your private network.
6. Next open a terminal on your local machine and *cd* to the *Ansible/Control Hub* directory, execute *ansible-playbook deploy_and_start_control_hub.yml -i ../hosts.ini*. This will run a number of Ansible tasks that:
   * Update the RaspberryPi apt repositories and upgrade the OS. 
   * Install needed packages and tools to run the control hub code. This includes the installation of Java 8, the Tomcat server and docker.
   * Create systemd unit files to manage the control hub backend, frontend and docker container hosting the RabbitMQ message-broker
   * Setup the previously mentioned unit files. 
   * Create a static IP address for the RasperryPi on your private network.
   * Restart your RaspberryPi and host the frontend to manage your IoT enabled RaspberryPi devices.
7. Visit the url *http://{your-ip-address-from-step-3}/control-hub-frontend* to interact with the Control Hub.

## IoT Devices
### Pump Controller
The pump controller uses a temperature sensor to determine whether a swimming pool heating system must be turned on or off, saving you money on electricity bills by keeping your pool pump off when weather conditions aren't adequate for heating. For more on the DIY to build this project see my Medium post - [Summer Time Project](here). For a demonstration of how this device works check out the YouTube video [Summer Time Project - Autonomous Pool Pump Toggling Based on Temperature](https://www.youtube.com/watch?v=kK2aJ3MEvgA)

### System Requirements
* SPI communication on RaspberryPi enabled
* Internet capabilities
* An SD card at least 4 GB in size

### Installing the Pump Controller
Once you followed the DIY guide - [Summer Time Project](here), follow the below steps to install the software needed to manage the Pump Controller on a RaspberryPi.
1. Again Ansible will be needed before moving on to the next step. Follow the guide on the Ansible documentation in order to install it - [Installing Ansible on specific operating systems](https://docs.ansible.com/ansible/latest/installation_guide/intro_installation.html#installing-ansible-on-specific-operating-systems) (Windows is not supported). Also edit the *ansible.cfg* file to disable host key checking as mentioned previously.
2. The *RaspberryPi OS Lite (32 bit)* operating system will need to be installed for the Pump Controller - follow steps 1, 2 and 3 of **Installing the Control Hub**.
3. In addition to the OS configurations in step 2, you will need to enable SPI communication:
   * [Open an SSH session](https://www.raspberrypi.org/documentation/remote-access/ssh/unix.md) with the RaspberryPi.
   * Execute the *sudo raspi-config* command to open an interaction screen of the RaspberyPi configuration options.
   * Navigate *Interfacing Options>SPI*, here an option to enable SPI communication is presented, select yes and finish.
     
   See [raspi-config](https://www.raspberrypi.org/documentation/configuration/raspi-config.md) for more.
4. Repeat step 4 of **Installing the Control Hub**, replacing the IP address of the *[pump_controller host]* host with your RaspberryPi IP on which the Pump Controller will run.
5. Open the *deploy_and_start_pump_controller_playbook.yml* file located in the *Ansible/Pump Controller* directory and edit the value for *interface* to be either *wireless* or *ethernet*, based on what interface you will be using for the RaspberryPi to connect to your private network.
6. In the same file edit the value for *control_hub_host_ip_address* to be the IP address of your Control Hub.
7. Open a terminal on you local machine and *cd* to the *Ansible/Pump Controller* directory and execute *ansible-playbook deploy_and_start_pump_controller.yml -i ../hosts.ini*. This will run a number of Ansible tasks that:
   * Update the RaspberryPi apt repositories and upgrade the OS
   * Install needed packages and tools to run the pump controller code. This includes the installation Java 8, the wiringpi package as well as the python-gpiozero python package.
   * Create a systemd unit files to manage the Pump Controller and its connection to the Control Hub
   * Setup the previously mentioned unit file
   * Create a static IP address for the RasperryPi on your private network.
   * Restart your RaspberryPi and run the Pump Controller program to manage your swimming pool heating system.
   
# Contact Details
For any queries please feel free to contact me:
* email address: piandarduinoguy@gmail.com