#include <Arduino.h>
#include <SPI.h>
#include <Adafruit_BLE.h>
#include <Adafruit_BluefruitLE_SPI.h>
#include <Adafruit_BluefruitLE_UART.h>
#include <Adafruit_NeoPixel.h>
#include <Wire.h>
#include <Adafruit_Sensor.h>
#include <Adafruit_LSM9DS0.h>
#include "Adafruit_SI1145.h"

#if SOFTWARE_SERIAL_AVAILABLE
  #include <SoftwareSerial.h>
#endif

#define Blue_rx                           0
#define Blue_tx                           1
#define Board_LED                         8
#define BLUEFRUIT_UART_MODE_PIN           -1 
#define BLUEFRUIT_UART_CTS_PIN            -1 
#define BLUEFRUIT_UART_RTS_PIN            -1 
#define BUFSIZE                           128  
#define VERBOSE_MODE                      true 
#define FACTORYRESET_ENABLE               1
#define MINIMUM_FIRMWARE_VERSION         "0.6.6"
#define MODE_LED_BEHAVIOUR               "MODE"
#define BLUEFRUIT_HWSERIAL_NAME          Serial1

Adafruit_BluefruitLE_UART ble(BLUEFRUIT_HWSERIAL_NAME, BLUEFRUIT_UART_MODE_PIN);
Adafruit_LSM9DS0 lsm = Adafruit_LSM9DS0(1000);
Adafruit_SI1145 uv = Adafruit_SI1145();

String gpsArray[4];
int x;
String GPSdata;
float GPSdatafloat[4];
float wek;
float wekfin;


void error(const __FlashStringHelper* err) {
  Serial.println(err);
  while (1);
}

void displaySensorDetails(void) {
  sensor_t accel, mag, gyro, temp;
  lsm.getSensor(&accel, &mag, &gyro, &temp);
  
  Serial.println(F("------------------------------------"));
  Serial.print(F("Sensor:       ")); Serial.println(accel.name);
  Serial.print(F("Driver Ver:   ")); Serial.println(accel.version);
  Serial.print(F("Unique ID:    ")); Serial.println(accel.sensor_id);
  Serial.print(F("Max Value:    ")); Serial.print(accel.max_value); Serial.println(F(" m/s^2"));
  Serial.print(F("Min Value:    ")); Serial.print(accel.min_value); Serial.println(F(" m/s^2"));
  Serial.print(F("Resolution:   ")); Serial.print(accel.resolution); Serial.println(F(" m/s^2"));  
  Serial.println(F("------------------------------------"));
  Serial.println(F(""));

  Serial.println(F("------------------------------------"));
  Serial.print(F("Sensor:       ")); Serial.println(mag.name);
  Serial.print(F("Driver Ver:   ")); Serial.println(mag.version);
  Serial.print(F("Unique ID:    ")); Serial.println(mag.sensor_id);
  Serial.print(F("Max Value:    ")); Serial.print(mag.max_value); Serial.println(F(" uT"));
  Serial.print(F("Min Value:    ")); Serial.print(mag.min_value); Serial.println(F(" uT"));
  Serial.print(F("Resolution:   ")); Serial.print(mag.resolution); Serial.println(F(" uT"));  
  Serial.println(F("------------------------------------"));
  Serial.println(F(""));

  Serial.println(F("------------------------------------"));
  Serial.print(F("Sensor:       ")); Serial.println(gyro.name);
  Serial.print(F("Driver Ver:   ")); Serial.println(gyro.version);
  Serial.print(F("Unique ID:    ")); Serial.println(gyro.sensor_id);
  Serial.print(F("Max Value:    ")); Serial.print(gyro.max_value); Serial.println(F(" rad/s"));
  Serial.print(F("Min Value:    ")); Serial.print(gyro.min_value); Serial.println(F(" rad/s"));
  Serial.print(F("Resolution:   ")); Serial.print(gyro.resolution); Serial.println(F(" rad/s"));  
  Serial.println(F("------------------------------------"));
  Serial.println(F(""));

  Serial.println(F("------------------------------------"));
  Serial.print(F("Sensor:       ")); Serial.println(temp.name);
  Serial.print(F("Driver Ver:   ")); Serial.println(temp.version);
  Serial.print(F("Unique ID:    ")); Serial.println(temp.sensor_id);
  Serial.print(F("Max Value:    ")); Serial.print(temp.max_value); Serial.println(F(" C"));
  Serial.print(F("Min Value:    ")); Serial.print(temp.min_value); Serial.println(F(" C"));
  Serial.print(F("Resolution:   ")); Serial.print(temp.resolution); Serial.println(F(" C"));  
  Serial.println(F("------------------------------------"));
  Serial.println(F(""));
  
  delay(500);
}

void  setupSensor(void) {
  // 1.) Set the accelerometer range
  //lsm.setupAccel(lsm.LSM9DS0_ACCELRANGE_2G);
  // lsm.setupAccel(lsm.LSM9DS0_ACCELRANGE_4G);
  // lsm.setupAccel(lsm.LSM9DS0_ACCELRANGE_6G);
  // lsm.setupAccel(lsm.LSM9DS0_ACCELRANGE_8G);
   lsm.setupAccel(lsm.LSM9DS0_ACCELRANGE_16G);
  
  // 2.) Set the magnetometer sensitivity
  lsm.setupMag(lsm.LSM9DS0_MAGGAIN_2GAUSS);
  // lsm.setupMag(lsm.LSM9DS0_MAGGAIN_4GAUSS);
  // lsm.setupMag(lsm.LSM9DS0_MAGGAIN_8GAUSS);
  // lsm.setupMag(lsm.LSM9DS0_MAGGAIN_12GAUSS);

  // 3.) Setup the gyroscope
  lsm.setupGyro(lsm.LSM9DS0_GYROSCALE_245DPS);
  // lsm.setupGyro(lsm.LSM9DS0_GYROSCALE_500DPS);
  // lsm.setupGyro(lsm.LSM9DS0_GYROSCALE_2000DPS);
}

void setup() {
  while (!Serial);  // required for Flora & Micro
  delay(500);

  Serial.begin(9600);

  Serial.println("OK!");
  // // ACCELEROMETER INITIALISATION
  if(!lsm.begin()) {
    Serial.print(F("Ooops, no LSM9DS0 detected ... Check your wiring or I2C ADDR!"));
    while(1);
  }
  Serial.println(F("Found LSM9DS0 9DOF"));
  
  /* Display some basic information on this sensor */
  displaySensorDetails();
  
  /* Setup the sensor gain and integration time */
  setupSensor();
  
  /* We're ready to go! */
  Serial.println("");

  ////// BLUETOOTH INIT
  Serial.println(F("Adafruit Bluefruit Command Mode Example"));
  Serial.println(F("---------------------------------------"));
  
  /* Initialise the module */
  Serial.print(F("Initialising the Bluefruit LE module: "));
  // Change device name to make it more friendly using AT command
  if (!ble.begin(VERBOSE_MODE)) {
    error(F("Couldn't find Bluefruit, make sure it's in CoMmanD mode & check wiring?"));
  }
  Serial.println( F("OK!") );

  if (FACTORYRESET_ENABLE) {
    /* Perform a factory reset to make sure everything is in a known state */
    Serial.println(F("Performing a factory reset: "));
    if (!ble.factoryReset()) {
      error(F("Couldn't factory reset"));
    }
  }

  /* Disable command echo from Bluefruit */
  ble.echo(false);

  // Send AT command to change the Bluetooth device name
  ble.atcommand(F("AT+GAPDEVNAME=SmartObroza"));

  Serial.println("Requesting Bluefruit info:");
  /* Print Bluefruit information */
  ble.info();

  Serial.println(F("Please use Adafruit Bluefruit LE app to connect in UART mode"));
  Serial.println();

  ble.verbose(false);  // debug info is a little annoying after this point!

  // /* Wait for connection */
  // while (!ble.isConnected()) {
  //     delay(500);
  // }
  Wire.begin(8);                // join I2C bus with address #8
  Wire.onReceive(receiveEvent); // register event
}
void loop() {

int prevIndex = 0;
int slashIndex;
int index = 0;
 for (int i = 0; i < 5; i++) {
    GPSdatafloat[i] = 0.0;
  }

  // Rozdzielanie danych na tablicę float
  while ((slashIndex = GPSdata.indexOf('/', prevIndex)) != -1) {
    // Pobranie liczby pomiędzy separatorami
    String part = GPSdata.substring(prevIndex, slashIndex);
    GPSdatafloat[index] = part.toFloat();

    // Aktualizacja pozycji do następnego separatora
    prevIndex = slashIndex + 1;
    index++;
  }

  // Pobranie ostatniej liczby po ostatnim '/'
  String part = GPSdata.substring(prevIndex);
  GPSdatafloat[index] = part.toFloat();
Serial.println("Updated GPS Data:");
  for (int i = 0; i < 4; i++) {
    Serial.print("Value ");
    Serial.print(i + 1);
    Serial.print(": ");
    Serial.println(GPSdatafloat[i]);
  }
 GPSdata = "";

  lsm.read();
  Serial.print("Accel X: "); Serial.print((int)lsm.accelData.x); Serial.print(" ");
  Serial.print("Y: "); Serial.print((int)lsm.accelData.y);       Serial.print(" ");
  Serial.print("Z: "); Serial.println((int)lsm.accelData.z);     Serial.print(" ");
  float x = lsm.accelData.x/1000;
  float y = lsm.accelData.y/1000;
  float z = lsm.accelData.z/1000;
  wek = sqrt(x * x + y * y + z * z)-1.3;
  wekfin = wek * 9.81;
  Serial.println(wekfin);
  delay(500);
  for(int i = 0; i < 5; i++) {
  while(Wire.available())
  {
    char receivedChar = Wire.read();
    GPSdata += receivedChar;
  }
  delay(1000);
  }


  char str1[10], str2[10], str3[10];
  dtostrf(GPSdatafloat[0], 6, 3, str1);  // 6 to minimalna szerokość, 2 to liczba miejsc po przecinku
  dtostrf(GPSdatafloat[0], 6, 3, str2);
  dtostrf(wekfin, 6, 3, str3);  // Ustalamy 1 miejsce po przecinku dla wartości 0.3

  Serial.println("GPS Send:");
  Serial.println(GPSdatafloat[0]);
  Serial.println(GPSdatafloat[1]);
  Serial.println(wekfin);
  // String str1 = String(GPSdatafloat[0]);
  // String str2 = String(GPSdatafloat[1]);
  // String str3 = String(wekfin);
  String  comb = String(str1) + ", " + String(str2) + ", " + String(str3);
  Serial.println(comb);
  ble.print(comb);

}
// }
void receiveEvent() {
//   for(int i = 0; i < 5; i++) {
//     GPSdataArray[i] = Wire.read();
//     Serial.println(GPSdataArray[i]);
//   }
//   // x = Wire.read();
//   // Serial.println(x);
}
