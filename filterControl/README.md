This is a simple control interface written in modern Java. It communicates via serial interface (such as e.g. USB) with an Arduino.
The code will require libraries not included with this project. jSerialComm v1.3.11 or later is known to work. Other serial communications libraries
may or may not work, adjust code as necessary for your system.
The GUI will in its published form alow the user to select from one of six filters and the assumption is that the filters rotate in unidirectionally,
i.e. to switch from e.g. filter position 6 to 5, the wheel will have to perform a 5*60° = 320° rotation to arrive at the selected filter.
Note that the progress bar does not have any feedback from the system in the presented code. It is essentially a timer that will have to be calibrated
to the rotational speed of your own motor for the users convenience.
Filter position and serial interface settings will be stored (at a location of the users choosing) in XML format as long as shutdown is via the applications 
'quit' command (in menu or by pressing ctrl-q). It will NOT save settings and position if the application is closed by directly closing the window.

This code has been tested to work on MacOS, Windows 10, Debian, and Raspbian distributions.
