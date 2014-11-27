/**
 * Created by Timi on 29.10.2014.
 */

$(document).ready(function () {

    var options = {
        '#update': '/update',
        '#rollDice': '/rollDice',
        '#endTurn': '/endTurn',
        '#buy': '/buy',
        '#start': 'start/2'
    };

    var player = {
        '0': '#player-boger',
        '1': '#player-maechtel',
        '2': '#player-schoppa',
        '3': '#player-eck',
        '4': '#player-neuschwander',
        '5': '#player-bittel'
    };

    var updatePlayerAjax = function () {
        $.ajax({
            url: options['#update'],
            dataType: "html",
            success: updateAllPlayer
        });
    };


    var updateMessageAjax = function () {
        $.ajax({
            url: options['#rollDice'],
            dataType: "html",
            success: updateMessage
        });

    };


    $('#update').on('click', updatePlayerAjax);

    $('#rollDice').on('click', updateMessageAjax);

    // TODO in eine Funktion
    $('#endTurn').on('click', function () {
        $.ajax({
            url: options['#endTurn'],
            dataType: "html",
            success: updateMessage
        });
    });
    $('#buy').on('click', function () {
        $.ajax({
            url: options['#buy'],
            dataType: "html",
            success: updateMessage
        });
    });


    var updateMessage = function (data) {
        var obj = $.parseJSON(data);
        $("#msg").html(obj.msg);
    };


    var updateAllPlayer = function (data) {
        var obj = $.parseJSON(data);
        $.each(obj, function (i, item) {
            updateSinglePlayer(i, item);
            updatePlayerPosition(i, item.pos);
        })
    };

    var updateSinglePlayer = function (index, player) {
        $('#namePlayer_' + index).html(player.name);
        $('#budgetPlayer_' + index).html(player.budget + 1);
        $('#ownershipPlayer_' + index).html(player.ownership);
        $('#positionPlayer_' + index).html(player.pos);
    };

    /************************ player position ********************************/

    var updatePlayerPosition = function (i, position) {
        var currentPlayer = $(player[i]);
        currentPlayer.remove()
        $('.pos-' + position).append(currentPlayer);
    }


    /************************ websockets ********************************/
    connect();

    function connect() {
        var socket = new WebSocket("ws://localhost:9000/socket");

        message('Socket Status: ' + socket.readyState + ' (ready)');

        socket.onopen = function () {
            message('Socket Status: ' + socket.readyState + ' (open)');
        };

        socket.onmessage = function (msg) {
            var msgnew = $(msg)[0].data;
            console.log(msgnew);
            updateAllPlayer(msgnew);
        };

        socket.onclose = function () {
            message('Socket Status: ' + socket.readyState + ' (Closed)');
        };


        function message(msg) {
            $('#wsLog').append('<p>' + msg + '</p>');
        }


    }//End connect
});
