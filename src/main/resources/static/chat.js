var stompClient = null;

function setConnected(connected) {
    $("#connect").prop("disabled", connected);
    $("#disconnect").prop("disabled", !connected);
    if (connected) {
        $("#conversation").show();
    }
    else {
        $("#conversation").hide();
    }
    $("#messages").html("");
}

function connect() {
    var socket = new SockJS('/websocket-poc');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        setConnected(true);
        console.log('Connected: ' + frame);

        stompClient.subscribe('/topic/chat', function (message) {
            showMessage(JSON.parse(message.body));
        });

        stompClient.subscribe('/user/queue/chat', function (message) {
            showMessage(JSON.parse(message.body));
        });

        stompClient.subscribe('/topic/activeUsers', function (users) {
            clearUsers();
            updateUsers(JSON.parse(users.body));
        });

        activeMe();
    });
}

function disconnect() {
    if (stompClient != null) {
        stompClient.disconnect();
    }
    setConnected(false);
    console.log("Disconnected");
}

function sendName() {
    stompClient.send("/app/message", {},
        JSON.stringify({
            'text': $("#text").val(),
            'recipient': $("#recipient").val()
        })
    );
    $("#text").val("");
    $("#text").focus();
}

function activeMe() {
    stompClient.send("/app/newConnection", {}, '');
}

function showMessage(message) {
    $("#messages").append("<tr><td><strong>" + message.sender +
        "</strong> says to <strong>" + message.recipient + "</strong>: " + message.text + "</td></tr>");
}

function clearUsers() {
    document.getElementById('recipient').innerHTML = "";
}

function updateUsers(users) {

    function addAllPeople() {
        var option = document.createElement('option');
        option.value = '';
        option.innerHTML = 'All people';
        document.getElementById("recipient").appendChild(option);
    }

    addAllPeople();
    users.forEach(function(item) {
        var option = document.createElement('option');
        option.value = item;
        option.innerHTML = item;
        document.getElementById("recipient").appendChild(option);
    });
}

$(function () {
    $("form").on('submit', function (e) {
        e.preventDefault();
    });
    $( "#connect" ).click(function() { connect(); });
    $( "#disconnect" ).click(function() { disconnect(); });
    $( "#send" ).click(function() { sendName(); });

    connect();
});
