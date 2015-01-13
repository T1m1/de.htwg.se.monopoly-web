/**
 * Created by Timi on 29.10.2014.
 */
var monopoly = angular.module("monopoly", ['ngCookies']);

monopoly.controller('MainCtrl', function ($scope, $http, $location, $cookies) {
    $scope.players;
    $scope.currentplayer;
    $scope.prisonQuestion;
    $scope.game;
    $scope.session;
    $scope.multiGame = false;
    $scope.currentInstancePlayer;

    $scope.pic = {
        0: "/assets/images/boger.jpg",
        1: "/assets/images/maechtel.jpg",
        2: "/assets/images/schoppa.jpg",
        3: "/assets/images/eck.jpg",
        4: "/assets/images/neuschwander.jpg",
        5: "/assets/images/bittel.jpg"
    };

    var endTurn = false;

    $scope.getGameInfo = function () {
        var id = location.href;
        id = id.substr(id.lastIndexOf('/') + 1, id.length)
        var myObject = new Object();
        var url = $location.absUrl()
        myObject.info = url + "/" + id;
        $scope.game = myObject;
        $('#gameinfo').modal('show');
    };


    $scope.updateQuestion = function () {
        $http.get('/question', {params: {session: $scope.session}}).then(function (res) {
            $scope.prisonQuestion = res.data;
            $('#myModal').modal('show');
        });
    };

    $scope.drawCard = function () {
        $http.get('/drawCard', {params: {session: $scope.session}}).then(function (res) {
            $scope.prisonQuestion = res.data;
            $('#myModal').modal('show');
        });
    };

    angular.element(document).ready(function () {

        var options = {
            '#update': '/update',
            '#rollDice': '/rollDice',
            '#endTurn': '/endTurn',
            '#buy': '/buy',
            '#prisonCard': '/prisonCard',
            '#prisonBuy': '/prisonBuy',
            '#prisonRoll': '/prisonRoll',
            '#start': '/start/2',
            '#diceResult': '/diceResult',
            '#currentPlayer': '/currentPlayer',
            '#end': '/end',
            '#possibleOptions': '/possibleOptions',
            '#message': '/message',
            '#drawCard': '/drawCard'
        };

        var player = {
            'boger': '#player-boger',
            'maechtel': '#player-maechtel',
            'schoppa': '#player-schoppa',
            'eck': '#player-eck',
            'neuschwander': '#player-neuschwander',
            'bittel': '#player-bittel'
        };

        var dice = {
            '1': '/assets/images/1.gif',
            '2': '/assets/images/2.gif',
            '3': '/assets/images/3.gif',
            '4': '/assets/images/4.gif',
            '5': '/assets/images/5.gif',
            '6': '/assets/images/6.gif'
        };

        var actions = {
            'END_TURN': '#endTurn',
            'ROLL_DICE': '#rollDice',
            'BUY_STREET': '#buy ',
            'START_TURN': '#rollDice',
            'SURRENDER': '#end',
            'REDEEM_WITH_MONEY': '#prison',
            'REDEEM_WITH_CARD': '#prison',
            'REDEEM_WITH_DICE': '#prison',
            'DRAW_CARD': '#drawCard'
        };

        var pictures = new Array();

        $('#drawCard').on('click', function () {
            update('#drawCard');
        });


        $('#rollDice').on('click', function () {
            update('#rollDice');
        });

        $('#endTurn').on('click', function () {
            update('#endTurn');
        });

        $('#buy').on('click', function () {
            update('#buy');
        });

        $('#prisonCard').on('click', function () {
            update('#prisonCard');
        });

        $('#prisonBuy').on('click', function () {
            update('#prisonBuy');
        });

        $('#prisonRoll').on('click', function () {
            update('#prisonRoll');
        });

        $('#leapmotion').on('click', function () {
            var scriptPath = location.origin + '/assets/js/leap.js'
            loadScript(scriptPath);
        });

        $('#end').on('click', function () {
            $.ajax({
                url: options['/end'],
                data: {'session': $scope.session},
                dataType: "html",
                success: end
            })
        });

        var end = function () {
            location.href = location.origin;
        };

        var update = function (data) {
            $.ajax({
                url: options[data],
                data: {'session': $scope.session},
                dataType: "html"
            });
        };

        var updateAllButtons = function (data) {
            var obj = $.parseJSON(data);
            $.each(actions, function (key, value) {
                $(value).attr('disabled', true);
                $(value).removeClass('btn-success');
            });

            $.each(obj, function (key, value) {
                $(actions[value]).attr('disabled', false);
                $(actions[value]).addClass('btn-success');
            });
        };


        var updateDice = function (data) {
            $('#dice1').attr('src', pictures[data.dice1].src);
            $('#dice2').attr('src', pictures[data.dice2].src);
        };


        var updateMessage = function (msg) {
            if (!(msg === "" || msg === null)) {
                $("#msg").html(msg);
            }
        };

        var updateAllPlayer = function (data) {
            var obj = $.parseJSON(data);
            $.each(obj, function (i, item) {
                updatePlayerPosition(item.pic, item.pos);
            })
        };

        var loadScript = function (scriptname) {
            var snode = document.createElement('script');
            snode.setAttribute('type', 'text/javascript');
            snode.setAttribute('src', scriptname);
            document.getElementsByTagName('head')[0].appendChild(snode);
        }

        // function for checking the right answer
        $scope.checkAnswer = function (select) {
            $.ajax({
                url: ('/answer/' + select),
                data: {'session': $scope.session},
                dataType: "html",
                success: updateMessage
            }).then(function () {
                //updateButtons();
            });
        };

        var updatePlayerPosition = function (pic, position) {
            var currentPlayer = $(player[pic]);
            currentPlayer.remove();
            $('.pos-' + position).append(currentPlayer);
            /* init dice pictures */
            var i;
            for (i = 1; i <= 6; i++) {
                pictures[i] = new Image();
                pictures[i].src = dice[i];
            }
        };


        function init() {
            $.each(player, function (key, value) {
                $(value).hide()
            });
            $.ajax({
                url: options['#update'],
                data: {'session': $scope.session},
                dataType: "html",
                success: function (data) {
                    var playeinfor = JSON.parse(data);
                    $scope.players = playeinfor;
                    $scope.$apply();
                    $.each(playeinfor, function (key, value) {
                        $(player[value.pic]).show();
                    })
                }
            });

            var i;
            for (i = 1; i <= 6; i++) {
                pictures[i] = new Image();
                pictures[i].src = dice[i];
            }
        };

        /** ********************** websockets ******************************* */

        connect();


        var disableAllButtons = function () {
            $.each(actions, function (key, value) {
                $(value).attr('disabled', true);
                $(value).removeClass('btn-success');
            });
        };

        function connect() {
            var host = location.origin.replace(/^http/, 'ws');
            var id = location.href;

            id = id.substr(id.lastIndexOf('/') + 1, id.length);
            $scope.session = id;
            host = host + "/socket/" + id;

            var socket = new WebSocket(host);

            socket.onopen = function () {
                // cookie auslesen um den namen für den aktuellen Spieler zu erhalten
                message('Socket Status: ' + socket.readyState + ' (Open)');
            };

            socket.onmessage = function (msg) {
                var data = $.parseJSON(msg.data);

                if (!$scope.multiGame) {
                    $scope.multiGame = true;
                    // TODO refactoring of regex to get player name of cookie without exact game name
                    var rawCookie = $cookies['PLAY_SESSION'];
                    var test = rawCookie.substr(rawCookie.indexOf(data.gameName + "="), rawCookie.length);
                    var uuu = test.substr(0, test.indexOf("&"));
                    $scope.currentInstancePlayer = uuu.substr(uuu.indexOf("=") + 1, uuu.length)
                }

                $scope.players = JSON.parse(data.players);
                $scope.currentplayer = data.currentPlayer;
                $scope.$apply();
                updateAllPlayer(data.players);
                updateDice(data.dices);

                if(data.currentPlayer.name !== $scope.currentInstancePlayer) {
                    disableAllButtons();
                } else {
                    updateAllButtons(data.buttons);
                }
                updateMessage(data.msg);
            };

            socket.onclose = function () {
                message('Socket Status: ' + socket.readyState + ' (Closed)');
            };

            function message(msg) {
                $('#wsLog').append('<p>' + msg + '</p>');
            };

        }// End connect

        // initialize
        init();
    });
})
;
