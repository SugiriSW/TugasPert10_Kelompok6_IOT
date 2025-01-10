#include <ESP8266WiFi.h>
#include <ESP8266HTTPClient.h>
#include <DHT.h>
#include <ArduinoJson.h>
#include <Wire.h>
#include <NTPClient.h>
#include <WiFiUdp.h>

// Pin Konfigurasi
#define DHTPIN D2        // Pin DHT
#define DHTTYPE DHT11    // Gunakan DHT11, ganti ke DHT22 jika diperlukan
#define GAS_ANALOG A0    // Pin analog untuk pembacaan gas level
#define GAS_DIGITAL D1   // Pin digital untuk pembacaan gas level

// Inisialisasi DHT
DHT dht(DHTPIN, DHTTYPE);

// Info jaringan Wi-Fi
const char* ssid = "POCO";
const char* password = "akusuge3";

// URL API
const String apiURL = "https://d6464rhl-42352.asse.devtunnels.ms/api/sensors";

// Setup NTP client
WiFiUDP udp;
NTPClient timeClient(udp, "pool.ntp.org", 0, 60000); // Menggunakan NTP server standar

// Fungsi untuk mengirimkan data ke API
void sendDataToAPI(float temperature, float humidity, int gas_level) {
  HTTPClient http;
  WiFiClientSecure client;

  // Mengonfigurasi SSL/TLS (Pastikan sertifikat sudah diinstal jika diperlukan)
  client.setInsecure();  // Mengabaikan sertifikat SSL untuk percakapan HTTPS

  // Membuka koneksi
  http.begin(client, apiURL);

  // Menyiapkan JSON untuk dikirim
  StaticJsonDocument<256> doc;

  // Menambahkan data ke JSON
  doc["temperature"] = temperature;
  doc["humidity"] = humidity;
  doc["gas_level"] = gas_level;

  String jsonStr;
  serializeJson(doc, jsonStr);

  // Menambahkan header
  http.addHeader("Content-Type", "application/json");

  // Mengirimkan request POST
  int httpResponseCode = http.POST(jsonStr);

  // Memeriksa status respons
  if (httpResponseCode > 0) {
    Serial.print("HTTP Response code: ");
    Serial.println(httpResponseCode);
  } else {
    Serial.print("Error sending POST request: ");
    Serial.println(httpResponseCode);
  }

  // Menutup koneksi
  http.end();
}

void setup() {
  // Mulai komunikasi serial
  Serial.begin(115200);

  // Menghubungkan ke Wi-Fi
  WiFi.begin(ssid, password);
  while (WiFi.status() != WL_CONNECTED) {
    delay(1000);
    Serial.println("Connecting to WiFi...");
  }
  Serial.println("Connected to WiFi!");

  // Menginisialisasi sensor DHT
  dht.begin();

  // Menginisialisasi NTP Client untuk mendapatkan waktu
  timeClient.begin();
  timeClient.update();  // Memperbarui waktu pertama kali
}

void loop() {
  // Memperbarui waktu setiap detik
  timeClient.update();

  // Membaca suhu dan kelembapan
  float temperature = dht.readTemperature();  // Mengambil suhu dalam Celcius
  float humidity = dht.readHumidity();        // Mengambil kelembapan
  
  // Membaca nilai gas (nilai analog)
  int gas_level = analogRead(GAS_ANALOG);
  
  // Cek jika pembacaan sensor berhasil
  if (isnan(temperature) || isnan(humidity)) {
    Serial.println("Failed to read from DHT sensor!");
    return;
  }

  // Tampilkan hasil pembacaan
  Serial.print("Temperature: ");
  Serial.print(temperature);
  Serial.print("°C, Humidity: ");
  Serial.print(humidity);
  Serial.print("%, Gas Level: ");
  Serial.println(gas_level);

  // Kirim data ke API
  sendDataToAPI(temperature, humidity, gas_level);
  
  // Tunggu selama 10 detik sebelum mengambil data lagi
  delay(10000);
}
