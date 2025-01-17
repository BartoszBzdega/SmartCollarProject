#include <Adafruit_GPS.h>
#include <Wire.h>

#define GPSSerial Serial1
Adafruit_GPS GPS(&GPSSerial);

#define GPSECHO false
#define slaveAddress 8

uint32_t timer = millis();
float gpspacket[5];
char lon[20];
char lat[20];
char minute[3];
char hr[3];

void setup() {
  Serial.begin(115200);
  Serial.println("SmartObroza");

  GPS.begin(9600);
  GPS.sendCommand(PMTK_SET_NMEA_OUTPUT_RMCGGA);
  GPS.sendCommand(PMTK_SET_NMEA_UPDATE_1HZ);
  GPS.sendCommand(PGCMD_ANTENNA);

  delay(1000);
  GPSSerial.println(PMTK_Q_RELEASE);

  Wire.begin(); // Inicjalizacja I2C
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
    gpspacket[2] = GPS.minute;
    gpspacket[3] = GPS.hour;

    sprintf(lat, "%.6f", gpspacket[0]);
    sprintf(lon, "%.6f", gpspacket[1]);
    sprintf(minute, "%02d", (int)gpspacket[2]);
    sprintf(hr, "%02d", (int)gpspacket[3]);

    Serial.println("##########");
    Serial.println("Latitude:");
    Serial.println(lat);
    Serial.println("Longitude:");
    Serial.println(lon);
    Serial.println("Minutes: ");
    Serial.println(minute);
    Serial.println("Hours: ");
    Serial.println(hr);


    Wire.beginTransmission(slaveAddress);
    for (int i = 0; i < 4; i++) { 
      Wire.write((byte*)&gpspacket[i], sizeof(gpspacket[i])); // float
    }
    Wire.endTransmission();

    delay(1000);
  }
}
