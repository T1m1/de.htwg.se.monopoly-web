/**
 * Created by Timi on 29.10.2014.
 */
var monopoly = angular.module("monopoly", []);

monopoly.controller('MainCtrl', [ '$scope', function ($scope, $http) {
    $scope.player;
    $scope.lala;

    var options = {
        '#update': '/update',
        '#rollDice': '/rollDice',
        '#endTurn': '/endTurn',
        '#buy': '/buy',
        '#prisonCard': '/prisonCard',
        '#prisonBuy': '/prisonBuy',
        '#start': '/start/2',
        '#currentPlayer': '/currentPlayer'
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
        }).then(
            updateNameOfPlayer
        );

    };

    var updateNameOfPlayer = function () {
        $.ajax({
            url: options['#currentPlayer'],
            dataType: "html",
            success: updateName
        })
    }

    $('#update').on('click', updatePlayerAjax);

    $('#rollDice').on('click', updateMessageAjax);

    /****************** TODO in eine Funktion *************************/
    $('#endTurn').on('click', function () {
        $.ajax({
            url: options['#endTurn'],
            dataType: "html",
            success: updateMessage
        }).then(
            updateNameOfPlayer
        );
    });


    $('#buy').on('click', function () {
        $.ajax({
            url: options['#buy'],
            dataType: "html",
            success: updateMessage
        });
    });

    $('#prisonCard').on('click', function () {
        $.ajax({
            url: options['#prisonCard'],
            dataType: "html",
            success: updateMessage
        });
    });

    $('#prisonBuy').on('click', function () {
        $.ajax({
            url: options['#prisonBuy'],
            dataType: "html",
            success: updateMessage
        }).then(
            updatePlayerAjax()
        )
    });

    /****************************************************************/


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

    var updateName = function (data) {
        var obj = $.parseJSON(data);
        $scope.lala = obj;
        $scope.$apply();
        //$('.whois').html("Spieler: "+ obj.name + " sie sind dran!");
    };

    var updateSinglePlayer = function (index, player) {
        $('#namePlayer_' + index).html(player.name);
        $('#budgetPlayer_' + index).html(player.budget);
        $('#ownershipPlayer_' + index).html(player.ownership);
        $('#positionPlayer_' + index).html(player.pos);
    };

    /************************ player position ********************************/


    function init() {
        $.each(player, function (key, value) {
            $(value).hide()
        });
        $.ajax({
            url: options['#update'],
            dataType: "html",
            success: function (data) {
                var playeinfor = JSON.parse(data);
                $scope.players = playeinfor;
                $scope.$apply();
                for (i = 0; i < Object.keys(playeinfor).length; i++) {
                    $(player[i]).show();
                }

            }
        });
    }

    angular.element(document).ready(function () {
        init();
    });

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
            var msgnew = msg.data;
            $scope.players = JSON.parse(msgnew);
            $scope.$apply();
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

} ]);
