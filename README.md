# FilterControl
A simple user interface for a motorised Arduino controlled filter wheel.
Useful for bespoke microscope setups etc.
SerialWheelControlDisplayV4 is a simple filter wheel selector and control routine to be run on an Arduino.
The code has provisions to output some useful feedback on a 2x20 element LCD display.
This code outputs an on/off continous or PM signal to be used via power electronics to actuate a motor, turning a filter wheel that can produce index pulses on sensorPin when a filter is in position.  
Commands are sent by serial interface to the Arduino. The wheel position should be calibrated after startup unless an encoder is implemented. This code takes as input pulses on sensorPin to determine that a new filter is in position.
