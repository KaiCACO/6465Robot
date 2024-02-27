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

var tx = 0.0;
var ty = 0.0;
var tz = 0.0;
var ta = 0.0;

socket.on('message_from_backend', (message) => {
    console.log('Received message from backend:', message);
    try {
        tx = message["tx"];
        ty = message["ty"];
        tz = message["tz"];
        ta = message["ta"];
    } catch (e) {
        console.log("Error parsing message from backend: ", e);
    }
    updateUI();
});

function updateUI() {
    var t = `translate(${tx + 50}vw, ${-tz}vw)`;
    console.log(t);
    document.getElementById("target").style.transform = t;
}