#include <Adafruit_GPS.h>

// Define the hardware serial port for GPS communication
#define GPSSerial Serial1

// Connect to the GPS on the hardware port
Adafruit_GPS GPS(&GPSSerial);

// Set GPSECHO to 'false' to disable raw GPS data echo for debugging
#define GPSECHO false

uint32_t timer = millis();
float gpspacket[2];
char lon[20]; // For formatted longitude
char lat[20]; // For formatted latitude

void setup() {
  // Initialize serial for debugging
  Serial.begin(115200);
  Serial.println("Adafruit GPS library basic parsing test!");

  // Initialize GPS at 9600 baud (default for Adafruit MTK GPS)
  GPS.begin(9600);

  // Configure GPS output and update rate
  GPS.sendCommand(PMTK_SET_NMEA_OUTPUT_RMCGGA); // RMC and GGA sentences
  GPS.sendCommand(PMTK_SET_NMEA_UPDATE_1HZ);    // 1 Hz update rate
  GPS.sendCommand(PGCMD_ANTENNA);               // Request antenna status

  delay(1000);
  GPSSerial.println(PMTK_Q_RELEASE); // Request firmware version
}

void loop() {
  char c = GPS.read();
  if (GPSECHO && c) Serial.print(c);

  // Check for and parse new NMEA sentences
  if (GPS.newNMEAreceived()) {
    if (!GPS.parse(GPS.lastNMEA())) return;
  }

  // Print GPS data every 5 seconds
  if (millis() - timer > 5000) {
    timer = millis();

    //if (GPS.fix) {
      gpspacket[0] = GPS.latitude;
      gpspacket[1] = GPS.longitude;

      dtostrf(gpspacket[0], 1, 14, lat);
      dtostrf(gpspacket[1], 1, 14, lon);

      Serial.println("##########");
      Serial.println("Latitude:");
      Serial.println(lat);
      Serial.println("Longitude:");
      Serial.println(lon);
    //} else {
      Serial.println("Waiting for GPS fix...");
    }
  }
//}

