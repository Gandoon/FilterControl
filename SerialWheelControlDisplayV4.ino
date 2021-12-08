#include <LiquidCrystal.h>

/*
  FilterControl
  
  A simple filter wheel selector and control routine to be run on an Arduino.
  This code outputs an on/off continous or PM signal to be used via power 
  electronics to actuate a filter wheel that can produce index pulses on 
  sensorPin when a filter is in position.
  Useful for bespoke microscope setups etc.
  Commands are sent by serial interface to the Arduino. The wheel position should 
  be calibrated after startup unless an encoder is implemented. This code takes 
  as input pulses on sensorPin to determine that a new filter is in position.

  By Erik Hedlund (c) 2018

  Released under a GPL 2.0 license

*/

// initialize the LCD library with the numbers of the interface pins
LiquidCrystal lcd(12, 13, 2, A1, 4, 5, 6, 7, 8, 9); //

int wheelPin  = 10;         // the PWM pin the motor controller is attached to
int dispBrp   = 3;          // PWM pin for display backlight control
int hiBr      = 127;
int loBr      = 31;
int turnSpeed = 255;        // how fast should the motor spin
int sensorPin = A0;         // determines when filter is in position
int steps     = 1;          // how many steps to go (pulses from sensorPin)
int maxSteps  = 5;          // max number of filters - 1 (never any need to go a full lap or more)
int currPos   = 0;          // Current position (0-5)
int gotoPos   = 0;          // Position to go to (0-5)
int sense     = 0;
int prevSense = 0;
int hiloDelay = 2500;
unsigned long to = 0;       // Timeout in ms for Display blanking, if set to 0 always on.
unsigned long toRetain = 0;
unsigned long ct = 0;       // Current time, updated periodically for timeout calculation
unsigned long st = 0;       // Start time for a timeout
boolean dispOn = false;     // Is the display on? Starts out being off...
boolean moving = false;
boolean reading = true;     // Start up listening for commands
boolean freemoving = false;

//String clrStr = "                    "; // 20 blank spaces for clearing a row of the display
String NDs[] = {"0.04", "0.5", "1.0", "1.5", "2.0", "2.5"};

// the setup function runs once when you press reset or power the board
void setup() {
  // initialize digital pin LED_BUILTIN as an output.
  // pinMode(LED_BUILTIN, OUTPUT);
  pinMode(sensorPin, INPUT_PULLUP);
  pinMode(dispBrp, OUTPUT);
  Serial.begin(9600);
  lcd.begin(20, 2);
  delay(10);
  lcd.clear();
  analogWrite(dispBrp, 255); // Turn the screen on
  //dispOn = true;
  //inputString.reserve(200);
  // digitalWrite(sensorPin, HIGH); // Pullup transistors applied
  sense = digitalRead(sensorPin);
  //analogWrite(wheelPin, turnSpeed);
  //digitalWrite(LED_BUILTIN, sense);
  lcd.setCursor(0, 0);
  lcd.write("Filters initialised!");
  lcd.setCursor(0, 1);
  lcd.write("Current position: 0?");
  prevSense = sense;
  //delay(10);
  Serial.println("Setup finished, please choose position to go to");
  delay(hiloDelay);
  analogWrite(dispBrp, loBr);
  dispOn = true;
}

// the loop function runs over and over again forever
int i = 0;
String text = "";
void loop() {
  while (reading) {
    if (Serial.available() > 0) {
      char cmd = Serial.read();
      delay(1);
      if ( cmd == 's') {
        Serial.println("Set current position to: ");
        currPos = Serial.parseInt();

        if (dispOn) {
          lcd.clear();
          analogWrite(dispBrp, hiBr);
          lcd.setCursor(0, 0);
          lcd.print("Calibrating position");
          lcd.setCursor(0, 1);
          //text = "Set position to: "+currPos;
          //Serial.println(text);
          lcd.print("Set position to: ");
          lcd.print(currPos);
          delay(hiloDelay);

          analogWrite(dispBrp, loBr);
          lcd.clear();
          lcd.setCursor(0, 0);
          lcd.write("     ND filter     ");
          lcd.setCursor(0, 1);
          //text = NDs[currPos];
          lcd.print("Current filter: " + NDs[currPos]);
        }
        gotoPos = currPos;
        Serial.println(currPos);
        if (to > 0) {
          st = millis();
        } else {
          st = 0;
        }
      } else if (cmd == 'g') {
        Serial.println("Go to position: ");
        gotoPos = Serial.parseInt();
        Serial.println(gotoPos);
        Serial.println(gotoPos);
        Serial.println("from position:");
        Serial.println(currPos);
        if (Serial.read() == '\n') {
          if (gotoPos != currPos) {
            reading = false;
          } else {
            //Serial.println(currPos);
            Serial.println("gotoPos == currPos");
            Serial.println("No action");
            if (dispOn) {
              lcd.clear();
              if (to > 0) {
                analogWrite(dispBrp, loBr);
              }
              lcd.setCursor(0, 0);
              lcd.write("     ND filter     ");
              lcd.setCursor(0, 1);
              //text = NDs[currPos];
              lcd.print("Current filter: " + NDs[currPos]);
              //delay(hiloDelay); // Is this one strictly necessary??
            }
            Serial.println(NDs[currPos]);
          }
        }
        if (to > 0) {
          st = millis();
        } else {
          st = 0;
        }
      } else if ( cmd == 'l') {
        loBr = Serial.parseInt();
        delay(1);
        Serial.print("Set low brightness to: ");
        Serial.println(loBr);
        //lcd.clear();
        analogWrite(dispBrp, loBr);
        if (to > 0) {
          st = millis();
        } else {
          st = 0;
        }
      } else if ( cmd == 'h') {
        hiBr = Serial.parseInt();
        delay(1);
        Serial.print("Set high brightness to: ");
        Serial.println(hiBr);
        //lcd.clear();
        analogWrite(dispBrp, hiBr);
        delay(1500);
        analogWrite(dispBrp, loBr);
        if (to > 0) {
          st = millis();
        } else {
          st = 0;
        }
      } else if ( cmd == 'd') {
        int dTogg = Serial.parseInt();
        delay(1);
        if (dTogg == 0) {
          Serial.println("Turn display illumination off");
          lcd.clear();
          analogWrite(dispBrp, 0);
          dispOn = false;
          toRetain = to;
          Serial.print("Timeout to retain: ");
          Serial.println(toRetain);
          to = 0;
          st = 0;
          //delay(1500);
        } else if (dTogg == 1) {
          Serial.println("Turn display illumination on");
          analogWrite(dispBrp, loBr);
          dispOn = true;
          lcd.setCursor(0, 0);
          lcd.write("     ND filter     ");
          lcd.setCursor(0, 1);
          //to = 0;
          //text = NDs[currPos];
          lcd.print("Current filter: " + NDs[currPos]);
          to = toRetain;
          if (to > 0) {
            st = millis();
          } else {
            st = 0;
          }
          Serial.print("Revert to timeout: ");
          Serial.println(to);
        }
        //        if (to > 0) {
        //          st = millis();
        //        } else {
        //          st = 0;
        //        }
      } else if ( cmd == 't') {
        to = Serial.parseInt();
        delay(1);
        Serial.print("Set display timeout to: ");
        Serial.println(to);
        if (to > 0) {
          st = millis();
        } else {
          st = 0;
        }
        analogWrite(dispBrp, loBr);
        dispOn = true;
        lcd.setCursor(0, 0);
        lcd.write("     ND filter     ");
        lcd.setCursor(0, 1);
        //text = NDs[currPos];
        lcd.print("Current filter: " + NDs[currPos]);

      }  else if ( cmd == 'a') { //Show the about text
        //to = Serial.parseInt();
        delay(1);
        Serial.println("* About *");
        Serial.println("Serial Wheel Control - v 1.4");
        Serial.println("(c) 2018, Erik Hedlund");
        if (to > 0) {
          st = millis();
        } else {
          st = 0;
        }
        if (dispOn) {
          analogWrite(dispBrp, hiBr);
          //dispOn = true;
          lcd.setCursor(0, 0);
          lcd.print("Filter Wheel - v 1.4");
          lcd.setCursor(0, 1);
          lcd.print("(c) 2018 E G Hedlund");          
//          delay(hiloDelay*2);
//          analogWrite(dispBrp, loBr);
//          lcd.clear();
//          lcd.setCursor(0, 0);
//          lcd.write("     ND filter     ");
//          lcd.setCursor(0, 1);
//          //text = NDs[currPos];
//          lcd.print("Current filter: " + NDs[currPos]);
        }
      } else if ( cmd == 'b') { //Remove the about text
        //to = Serial.parseInt();
        delay(1);
        if (to > 0) {
          st = millis();
        } else {
          st = 0;
        }
        if (dispOn) {                    
          //delay(hiloDelay);
          analogWrite(dispBrp, loBr);
          lcd.clear();
          lcd.setCursor(0, 0);
          lcd.write("     ND filter     ");
          lcd.setCursor(0, 1);
          //text = NDs[currPos];
          lcd.print("Current filter: " + NDs[currPos]);
        }
      }

      if (gotoPos > maxSteps && !moving) {
        // Advance wheel one step without taking currPos into account
        steps = 1;
        gotoPos = (currPos + 1) % 6 ;
        //Serial.println("Received goto position: " + gotoPos);
        Serial.print("Moving one step... ");
        //Serial.println(gotoPos+")...");
        //delay(1);

        if (dispOn) {
          lcd.clear();
          analogWrite(dispBrp, hiBr);
          lcd.setCursor(0, 0);
          lcd.print("Calibrating position");
          lcd.setCursor(0, 1);
          //text = "Set position to: "+currPos;
          //Serial.println(text);
          lcd.print(" Moving one step... ");
          //lcd.print(currPos);
        }
        analogWrite(wheelPin, turnSpeed);
        moving  = true;
        freemoving = true;
        delay(1);
        if (to > 0) {
          st = millis();
        } else {
          st = 0;
        }
      } else if ((gotoPos != currPos) && !moving) {
        steps = (gotoPos - currPos) % 6;
        if (steps < 0) {
          steps += 6;
        }
        //      Serial.println("currPos + gotoPos:");
        //      Serial.println(currPos+gotoPos);
        //      Serial.println("(currPos+gotoPos)%6");
        //      Serial.println((currPos + gotoPos)%6);

        Serial.println("Steps to go:");
        Serial.println(steps);
        if (dispOn) {
          lcd.clear();
          analogWrite(dispBrp, hiBr);
          lcd.setCursor(0, 0);
          //lcd.autoscroll();
          lcd.print("Filters moving... *");
          lcd.print(currPos);
          lcd.setCursor(0, 1);
          lcd.print("Steps to go: ");
          lcd.print(steps);
        }
        //digitalWrite(LED_BUILTIN, 1);
        analogWrite(wheelPin, turnSpeed);
        moving  = true;
        delay(1);
        if (to > 0) {
          st = millis();
        } else {
          st = 0;
        }
      }
    }

      while (moving) {
        //Serial.println("Start of moving loop");
        //lcd.setCursor(18, 0);
        //lcd.print('*');
        //lcd.print(currPos);
        sense = digitalRead(sensorPin);
        delay(1);
        if (sense != prevSense) {
          prevSense = sense;
          Serial.println("Sense state change:");
          Serial.println(sense);
          lcd.setCursor(18, 0);
          if (!freemoving && dispOn) {
            lcd.print(' ');
          }

          i++;
          //currPos++;
          if (i % 2 == 0) {
            currPos = (currPos + 1) % 6;
            lcd.setCursor(18, 0);
            if (!freemoving) {
              if (dispOn) {
                lcd.print('*');
              }
            } else {
              freemoving = false;
            }
            //lcd.print('*');
            if (dispOn) {
              lcd.print(currPos);
              lcd.setCursor(13, 1);
              lcd.print(steps - i / 2);
            }
          }
          Serial.println("Position:");
          Serial.println(currPos);

          //Serial.println(i);
          if (i >= 2 * steps) {
            analogWrite(wheelPin, 0);
            //digitalWrite(LED_BUILTIN, sense);
            //n=0;
            delay(100);
            if (dispOn) {
              lcd.clear();
              lcd.setCursor(0, 0);
              lcd.write("  Finished moving   ");
              lcd.setCursor(0, 1);
              lcd.print("Current position: ");
              lcd.print(currPos);
              delay(hiloDelay);
              lcd.clear();
              analogWrite(dispBrp, loBr);
              lcd.setCursor(0, 0);
              lcd.print("     ND filter     ");
              lcd.setCursor(0, 1);
              text = "Current filter: ";
              lcd.print(text + NDs[currPos]);
            }
            //lcd.noAutoscroll();
            Serial.println("Finished moving");
            //Serial.println(currPos);
            Serial.println("Current Position:");
            Serial.println(currPos);
            i = 0;
            moving = false;
            //freemoving = false;
            reading = true; // Re-enable serial reading
          }
        }
        //Serial.println("End of moving loop");
        if (to > 0) {
          st = millis();
        } else {
          st = 0;
        }
      }
      ct = millis();
      // Check if the timeout has been reached...
      if (to > 0 && ct > st + to && dispOn) {
        lcd.clear();
        analogWrite(dispBrp, 0);
        //dispOn = false;
      }
      //    else {
      //      //st = 0;
      //    }
      // End of reading loop
    
  }
}
