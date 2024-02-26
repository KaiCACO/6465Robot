function submitForm(id) {
    vals = {}
    document.getElementById(id).querySelectorAll("select").forEach((e) => {vals[e.id] = e.value})
    console.log(vals);
    sendMessage(vals);
}

var address = 'http://' + document.domain + ':' + location.port;
var socket = io.connect(address);

socket.on('connect', () => {
    console.log(`Connected to the backend via ${address}`);
    sendMessage("handshake")
});

function sendMessage(message) {
    socket.emit('message_from_frontend', message);
}

socket.on('message_from_backend', (message) => {
    console.log('Received message from backend:', message);
});