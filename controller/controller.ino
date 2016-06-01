#include <SPI.h>
#include <WiFi.h>
#include <PubSubClient.h>
#include <Servo.h>

#define EntryGateServoPin 5
#define ExitGateServoPin 6
#define Open 90
#define Close 0
#define SlotCount 4

char ssid[] = "reshout";

struct slotStatus {
  boolean parked;
  int flipCount;
};

const int slotPins[SlotCount + 1] = {-1, 30, 31, 32, 33};
const int slotThresholds[SlotCount + 1] = {-1, 80, 118, 131, 83};
const char* const slotTopics[] = { NULL, "/facilities/1/slots/1", "/facilities/1/slots/2", "/facilities/1/slots/3", "/facilities/1/slots/4" };
struct slotStatus slotStatus[SlotCount];

Servo EntryGateServo;
Servo ExitGateServo;

void callback(char* topic, byte* payload, unsigned int length)
{
  Serial.println(topic);
  Serial.println((char *) payload);
  if (payload != NULL && payload[0] == '1') EntryGateServo.write(Open);
  else if (payload != NULL && payload[0] == '0') EntryGateServo.write(Close);
}

WiFiClient wifiClient;
PubSubClient client("192.168.43.24", 1883, callback, wifiClient);

void setup()
{
  Serial.begin(9600);

  EntryGateServo.attach(EntryGateServoPin);
  ExitGateServo.attach(ExitGateServoPin);

  initEntryExitLEDs();
  initEntryExitGates();

  Serial.println("Attempting to connect to WiFi network");
  WiFi.begin(ssid);
  while (WiFi.status() != WL_CONNECTED) delay(500);
  Serial.println("Connected to WiFi network");

  Serial.println("Attempting to connect to MQTT broker");
  if (client.connect("ClientID"))
  {
    Serial.println("Connected to MQTT broker");
    initSlotStatus();

    if (client.subscribe("/facilities/1/gates/+")) Serial.println("Subscribe ok");
    else Serial.println("Subscribe failed");
  }
  else
  {
    Serial.println("MQTT connect failed");
    abort();
  }
}

void loop()
{
  checkSlotStatus();
  client.loop();
  delay(1000);
}

void initEntryExitLEDs()
{
  int i;
  for (i = 26; i <= 29; i++)
  {
    pinMode(i, OUTPUT);
    digitalWrite(i, HIGH);
  }
}

void initEntryExitGates()
{
  EntryGateServo.write(Close);
  ExitGateServo.write(Close);
}

void initSlotStatus() {
  int i;

  for (i = 1; i <= SlotCount; i++) {
    slotStatus[i].parked = isParked(i);
    slotStatus[i].flipCount = 0;

    client.publish(slotTopics[i], slotStatus[i].parked ? "1" : "0");
  }
}

void checkSlotStatus() {
  int i;
  boolean parked;

  for (i = 1; i <= SlotCount; i++) {
    parked = isParked(i);
    if (parked != slotStatus[i].parked)
    {
      if (++slotStatus[i].flipCount > 1)
      {
        client.publish(slotTopics[i], parked ? "1" : "0");
        slotStatus[i].parked = parked;
        slotStatus[i].flipCount = 0;
      }
    } else {
      slotStatus[i].flipCount = 0;
    }
  }
}

boolean isParked(int slotId)
{
  int pin = slotPins[slotId];
  int threshold = slotThresholds[slotId];
  int proximityVal = getProximityVal(pin);

  if (threshold >= proximityVal) return true;
  else return false;
}

long getProximityVal(int Pin)
{
    long duration = 0;
    pinMode(Pin, OUTPUT);         // Sets pin as OUTPUT
    digitalWrite(Pin, HIGH);      // Pin HIGH
    delay(1);                     // Wait for the capacitor to stabilize

    pinMode(Pin, INPUT);          // Sets pin as INPUT
    digitalWrite(Pin, LOW);       // Pin LOW
    while (digitalRead(Pin))       // Count until the pin goes
    {                             // LOW (cap discharges)
       duration++;
    }
    return duration;              // Returns the duration of the pulse
}
