from flask import Flask, render_template
from flask_socketio import SocketIO
import nwtez

app = Flask(__name__)
socketio = SocketIO(app)

@app.route('/')
def page():
    return render_template('index.html')

@socketio.on('message_from_frontend')
def handle_message(message):
    print(f"Received message from frontend: {message}")

def run():
    socketio.run(app, debug=False)

if __name__ == '__main__':
    socketio.run(app, debug=True)