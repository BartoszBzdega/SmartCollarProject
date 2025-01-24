#include <Adafruit_GPS.h>
#include <Wire.h>

#define GPSSerial Serial1
Adafruit_GPS GPS(&GPSSerial);

#define GPSECHO false
#define slaveAddress 8

uint32_t timer = millis();
float gpspacket[5];
char* gpstransdata[24];  

void setup() {
  Serial.begin(115200);
  Serial.println("SmartObroza");

  GPS.begin(9600);
  GPS.sendCommand(PMTK_SET_NMEA_OUTPUT_RMCGGA);
  GPS.sendCommand(PMTK_SET_NMEA_UPDATE_1HZ);
  GPS.sendCommand(PGCMD_ANTENNA);

  delay(1000);
  GPSSerial.println(PMTK_Q_RELEASE);

  Wire.begin(); // Initialize I2C
}

void loop() {
  char c = GPS.read();
  if (GPSECHO && c) Serial.print(c);

  if (GPS.newNMEAreceived()) {
    if (!GPS.parse(GPS.lastNMEA())) return;
  }

  if (millis() - timer > 5000) {
    timer = millis();

    gpspacket[0] = GPS.latitudeDegrees;
    gpspacket[1] = GPS.longitudeDegrees;
    gpspacket[2] = (float)GPS.hour;
    gpspacket[3] = (float)GPS.minute;


    char* formattedStr = (char*)malloc(50 * sizeof(char));
    if (formattedStr) {
      snprintf(formattedStr, 50, "%.6f/%.6f/%02d/%02d",
               gpspacket[0], gpspacket[1], (int)gpspacket[2], (int)gpspacket[3]);


      gpstransdata[0] = formattedStr;

      Serial.println("##########");
      Serial.println("GPS Data: ");
      Serial.println(gpstransdata[0]);


      Wire.beginTransmission(slaveAddress);
      Wire.write(gpstransdata[0]);
      Wire.endTransmission();


      free(formattedStr);
    }

    delay(1000);
  }
}
