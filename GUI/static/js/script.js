function submitForm(id) {
    vals = {}
    document.getElementById(id).querySelectorAll("select").forEach((e) => {vals[e.id] = e.value})
    console.log(vals);
    sendMessage(vals);
}
var socket = io.connect('http://' + document.domain + ':' + location.port);

socket.on('connect', () => {
    console.log('Connected to the backend via WebSocket');
});

function sendMessage(message) {
    socket.emit('message_from_frontend', message);
}

socket.on('message_from_backend', (message) => {
    console.log('Received message from backend:', message);
});