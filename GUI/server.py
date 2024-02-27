from flask import Flask, render_template
from flask_socketio import SocketIO
from nwtez import Table
import asyncio
import math


LIMELIGHT_HEIGHT = 23
DEGREES_TO_RAD = math.pi / 180

def valueChanged(table, key, value, isNew):
    "penis"
    # print("valueChanged: key: '%s'; value: '%s'; isNew: '%s'" % (key, value, isNew))

robotTable = Table(valueChanged)

app = Flask(__name__)
socketio = SocketIO(app)

@app.route('/')
def page():
    return render_template('index.html')

@socketio.on('message_from_frontend')
def handle_message(message):
    if message == "handshake":
        asyncio.run(nwtUpdater(socketio.emit))
    else:
        try:
            for key, value in message.items():
                robotTable.put(f"py_{key}", value)
        except TypeError:
            print(f"TypeError! Message is not dict. Message: {message}")

async def nwtUpdater(emit):
    while True:
        await asyncio.sleep(0.2)
        emit("message_from_backend", update())

def update():
    tx = robotTable.get("LimelightX")
    ty = robotTable.get("LimelightY")
    try:
        tz = float(500 * 0.23 * math.tan(float(ty) * DEGREES_TO_RAD))
    except:
        tz = "0"
    ta = robotTable.get("LimelightArea")
    return {"tx": tx, "ty": ty, "tz": tz, "ta": ta}

def run():
    socketio.run(app, debug=False)

if __name__ == '__main__':
    socketio.run(app, debug=True)