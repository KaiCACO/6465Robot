from flask import Flask, render_template
from flask_socketio import SocketIO
from nwtez import Table
import asyncio
import logging

def valueChanged(table, key, value, isNew):
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
        print("handshake recieved!")
        asyncio.run(nwtUpdater(socketio.emit))
    try:
        print(f"Received message from frontend: {message}")
        for key, value in message.items():
            robotTable.put(key, value)
    except TypeError:
        print(f"TypeError! Message is not dict. Message: {message}")

async def nwtUpdater(emit):
    while True:
        await asyncio.sleep(1)
        emit("message_from_backend", "Update")

def run():
    socketio.run(app, debug=False)

if __name__ == '__main__':
    socketio.run(app, debug=True)