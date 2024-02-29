from flask import Flask, render_template
from flask_socketio import SocketIO
from nwtez import TableTest as Table
import asyncio
import math
import mathy_utils
from mathy_utils import shorten


LIMELIGHT_HEIGHT = 0.23
DEGREES_TO_RAD = math.pi / 180

x_poses = []
y_poses = []
z_poses = []
current_x = 0
current_y = 0
current_z = 0

def valueChanged(table, key, value, isNew):
    if app.debug:
        print("valueChanged: key: '%s'; value: '%s'; isNew: '%s'" % (key, value, isNew))

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
        await asyncio.sleep(.1)
        emit("message_from_backend", update())

def update():
    global x_poses
    global y_poses
    global z_poses
    global current_x
    global current_y
    global current_z
    tx = robotTable.get("LimelightX")
    ty = robotTable.get("LimelightY")
    ta = robotTable.get("LimelightArea")
    tz = mathy_utils.y_to_z(ty, LIMELIGHT_HEIGHT)
    x_poses.append(tx)
    y_poses.append(ty)
    z_poses.append(tz)
    x_poses = shorten(x_poses, 10)
    y_poses = shorten(y_poses, 10)
    z_poses = shorten(z_poses, 10)

    current_x = mathy_utils.smooth_motion(x_poses, current_x)
    current_y = mathy_utils.smooth_motion(y_poses, current_y)
    current_z = mathy_utils.smooth_motion(z_poses, current_z)

    smooth_mode = True

    if smooth_mode:
        return {"tx": current_x, "ty": current_y, "tz": current_z, "ta": ta}
    else:
        return {"tx": tx, "ty": ty, "tz": tz, "ta": ta}

def run():
    socketio.run(app, debug=False)

if __name__ == '__main__':
    socketio.run(app, debug=True)