from flask import Flask, jsonify, request
from flask_socketio import SocketIO, emit
from datetime import datetime
from pymongo.mongo_client import MongoClient
from pymongo.server_api import ServerApi
from flask_cors import CORS

# Inisialisasi Flask
app = Flask(__name__)

# Konfigurasi MongoDB Atlas URI
uri = "mongodb+srv://kelompok3:kelompok3@cluster0.lq950.mongodb.net/tugasgas?retryWrites=true&w=majority"

# Membuat client MongoDB dan menghubungkan ke server MongoDB Atlas
client = MongoClient(uri, server_api=ServerApi('1'))

# Pilih database dan koleksi yang sesuai
db = client.tugasgas  # Database 'tugasgas' yang digunakan
collection = db.sensor  # Koleksi 'sensor' yang digunakan

# Inisialisasi CORS dan Socket.IO
CORS(app)
socketio = SocketIO(app, cors_allowed_origins="*")

# Fungsi untuk mengonversi datetime menjadi string
def serialize_datetime(data):
    for item in data:
        if "timestamp" in item:
            item["timestamp"] = item["timestamp"].isoformat()  # Convert datetime to ISO format
    return data

# Fungsi untuk mengirim semua data melalui Socket.IO
def emit_all_data():
    try:
        # Ambil semua data dari koleksi MongoDB
        data = list(collection.find({}, {"_id": 0}).sort("timestamp", -1))
        
        # Serialisasi datetime
        serialized_data = serialize_datetime(data)
        
        # Emit data ke semua klien
        socketio.emit('all_data', serialized_data)
    except Exception as e:
        print(f"Error fetching data: {e}")

# Fungsi untuk mengirim 5 data terbaru melalui Socket.IO
def emit_latest_three_data():
    try:
        # Ambil 5 data terbaru dari MongoDB
        data = list(collection.find({}, {"_id": 0}).sort("timestamp", -1).limit(5))
        # Serialisasi datetime sebelum mengirim
        serialized_data = serialize_datetime(data)
        # Emit data ke semua klien
        socketio.emit('latest_three_data', serialized_data)
    except Exception as e:
        print(f"Error fetching data: {e}")

# Fungsi untuk mengirim rata-rata data per jam melalui Socket.IO
def emit_average_per_hour():
    try:
        # Ambil semua data dan urutkan berdasarkan timestamp secara ascending
        data = list(collection.find({}, {"_id": 0}).sort("timestamp", 1))
        
        # Kelompokkan data berdasarkan jam
        grouped_data = {}
        for entry in data:
            hour = entry["timestamp"].hour
            
            if hour not in grouped_data:
                grouped_data[hour] = {"temperature": [], "humidity": [], "gas_level": []}
            
            grouped_data[hour]["temperature"].append(entry["temperature"])
            grouped_data[hour]["humidity"].append(entry["humidity"])
            grouped_data[hour]["gas_level"].append(entry["gas_level"])

        # Hitung rata-rata untuk setiap jam
        average_data = []
        for hour, values in grouped_data.items():
            avg_temperature = sum(values["temperature"]) / len(values["temperature"]) if values["temperature"] else 0
            avg_humidity = sum(values["humidity"]) / len(values["humidity"]) if values["humidity"] else 0
            avg_gas_level = sum(values["gas_level"]) / len(values["gas_level"]) if values["gas_level"] else 0

            average_data.append({
                "hour": hour,
                "avg_temperature": avg_temperature,
                "avg_humidity": avg_humidity,
                "avg_gas_level": avg_gas_level
            })

        # Emit data rata-rata per jam ke semua klien
        socketio.emit('average_per_hour', average_data)
    except Exception as e:
        print(f"Error emitting average per hour: {e}")

# Endpoint untuk menerima data dari sensor
@app.route('/api/sensors', methods=['POST'])
def receive_sensor_data():
    try:
        data = request.json
        if not data or not all(k in data for k in ("temperature", "humidity", "gas_level")):
            return jsonify({"error": "Invalid data format"}), 400

        # Tambahkan timestamp
        sensor_data = {
            "temperature": data["temperature"],
            "humidity": data["humidity"],
            "gas_level": data["gas_level"],
            "timestamp": datetime.utcnow()
        }

        # Simpan ke MongoDB
        collection.insert_one(sensor_data)

        emit_latest_three_data()
        emit_all_data()
        emit_average_per_hour()
        
        return jsonify({"message": "Data successfully received"}), 201
    except Exception as e:
        return jsonify({"error": str(e)}), 500
    
@app.route('/data', methods=['GET'])
def get_three_latest_sensor_data():
    try:
        # Ambil 3 data terbaru dari MongoDB
        data = list(collection.find({}, {"_id": 0}).sort("timestamp", -1).limit(5))
        # Serialisasi datetime
        serialized_data = serialize_datetime(data)
        # Emit data ke klien (real-time)
        emit_latest_three_data()
        # Kembalikan data dalam respons JSON juga
        return jsonify(serialized_data), 200
    except Exception as e:
        return jsonify({"error": str(e)}), 500

# Endpoint untuk menampilkan semua data dari MongoDB
@app.route('/alldata', methods=['GET'])
def get_all_data():
    try:
        # Ambil semua data dari koleksi MongoDB
        data = list(collection.find({}, {"_id": 0}).sort("timestamp", -1))
        
        # Serialisasi datetime
        serialized_data = serialize_datetime(data)
        
        # Emit data ke klien (real-time)
        emit_all_data()
        
        # Kembalikan data dalam format JSON
        return jsonify(serialized_data), 200
    except Exception as e:
        return jsonify({"error": str(e)}), 500
    

@app.route('/jam', methods=['GET'])
def get_average_per_hour_v2():
    try:
        # Ambil semua data dan urutkan berdasarkan timestamp secara ascending
        data = list(collection.find({}, {"_id": 0}).sort("timestamp", 1))
        
        # Kelompokkan data berdasarkan jam
        grouped_data = {}
        for entry in data:
            hour = entry["timestamp"].hour
            
            if hour not in grouped_data:
                grouped_data[hour] = {"temperature": [], "humidity": [], "gas_level": []}
            
            grouped_data[hour]["temperature"].append(entry["temperature"])
            grouped_data[hour]["humidity"].append(entry["humidity"])
            grouped_data[hour]["gas_level"].append(entry["gas_level"])

        # Hitung rata-rata untuk setiap jam
        average_data = []
        for hour, values in grouped_data.items():
            avg_temperature = sum(values["temperature"]) / len(values["temperature"]) if values["temperature"] else 0
            avg_humidity = sum(values["humidity"]) / len(values["humidity"]) if values["humidity"] else 0
            avg_gas_level = sum(values["gas_level"]) / len(values["gas_level"]) if values["gas_level"] else 0

            average_data.append({
                "hour": hour,
                "avg_temperature": avg_temperature,
                "avg_humidity": avg_humidity,
                "avg_gas_level": avg_gas_level
            })

        emit_all_data()
        # Emit data rata-rata per jam ke semua klien
        emit_average_per_hour()

        return jsonify(average_data), 200
    except Exception as e:
        return jsonify({"error": str(e)}), 500

# Fungsi untuk menangani koneksi klien melalui Socket.IO
@socketio.on('connect')
def handle_connect():
    print("Client connected")
    emit_latest_three_data()

# Fungsi untuk menangani pemutusan koneksi klien
@socketio.on('disconnect')
def handle_disconnect():
    print("Client disconnected")

# Jalankan aplikasi
if __name__ == '__main__':
    socketio.run(app, debug=True, host='0.0.0.0', port=42352)
