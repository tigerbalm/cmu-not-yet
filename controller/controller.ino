#include <SPI.h>
#include <WiFi.h>
#include <PubSubClient.h>

#include <Servo.h> 

#define EntryGateServoPin 5
#define ExitGateServoPin 6
#define Open 90
#define Close 0

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

  initEntryExitLEDs();
  initEntryExitGates();

  EntryGateServo.attach(EntryGateServoPin);
  ExitGateServo.attach(ExitGateServoPin);
  
  Serial.println("Attempting to connect to WiFi network");
  WiFi.begin("reshout");
  while (WiFi.status() != WL_CONNECTED) delay(500);
  Serial.println("Connected to WiFi network");  

  Serial.println("Attempting to connect to MQTT broker");  
  if (client.connect("ClientID"))
  {
    Serial.println("Connected to MQTT broker");

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

void initEntryExitGates() {
  EntryGateServo.write(Close);
  ExitGateServo.write(Close);  
}

