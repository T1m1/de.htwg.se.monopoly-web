/**
 * Created by Timi on 29.10.2014.
 */
var monopoly = angular.module("monopoly", ['ngCookies']);

monopoly.controller('MainCtrl', function($scope, $http, $cookies) {
	$scope.players;
	$scope.currentplayer;
	$scope.prisonQuestion;
	$scope.game;
	$scope.pic = {
		0 : "/assets/images/boger.jpg",
		1 : "/assets/images/maechtel.jpg",
		2 : "/assets/images/schoppa.jpg",
		3 : "/assets/images/eck.jpg",
		4 : "/assets/images/neuschwander.jpg",
		5 : "/assets/images/bittel.jpg"
	};

	$scope.getGameInfo  = function() {
		var rawCookie = $cookies['PLAY_SESSION'];
		var rawData = rawCookie.substring(rawCookie.indexOf('=') + 1, rawCookie.length-1);
		var myObject = new Object();
		myObject.info = rawData;
		$scope.game = myObject;
		$scope.$apply();
		$('#gameinfo').modal('show');
	};

	$scope.updateQuestion = function() {
		$http.get('/question').then(function(res) {
			$scope.prisonQuestion = res.data;
			$('#myModal').modal('show');
		});
	};
	
	$scope.updateNameOfPlayer = function() {
		$http.get('/currentPlayer').then(function(res) {
			$scope.currentplayer = res.data;
		});
	};

	angular.element(document).ready(function() {

		var options = {
			'#update' : '/update',
			'#rollDice' : '/rollDice',
			'#endTurn' : '/endTurn',
			'#buy' : '/buy',
			'#prisonCard' : '/prisonCard',
			'#prisonBuy' : '/prisonBuy',
			'#prisonRoll' : '/prisonRoll',
			'#start' : '/start/2',
			'#diceResult' : '/diceResult',
			'#currentPlayer' : '/currentPlayer',
			'#end' : '/end',
			'#possibleOptions' : '/possibleOptions',
			'#message': '/message'
		};

		var player = {
			'0' : '#player-boger',
			'1' : '#player-maechtel',
			'2' : '#player-schoppa',
			'3' : '#player-eck',
			'4' : '#player-neuschwander',
			'5' : '#player-bittel'
		};

		var dice = {
			'1' : '/assets/images/1.gif',
			'2' : '/assets/images/2.gif',
			'3' : '/assets/images/3.gif',
			'4' : '/assets/images/4.gif',
			'5' : '/assets/images/5.gif',
			'6' : '/assets/images/6.gif'
		};

		var actions = {
			'END_TURN' : '#endTurn',
			'ROLL_DICE' : '#rollDice',
			'BUY_STREET' : '#buy ',
			'START_TURN' : '#rollDice',
			'SURRENDER' : '#end',
			'REDEEM_WITH_MONEY' : '#prison',
			'REDEEM_WITH_CARD' : '#prison',
			'REDEEM_WITH_DICE' : '#prison'
		};

		var pictures = new Array();
		
		$('#rollDice').on('click', function() {
			update('#rollDice');
		});
		
		$('#endTurn').on('click', function() {
            update('#endTurn');
		});

		$('#buy').on('click', function() {
			update('#buy');
		});

		$('#prisonCard').on('click', function() {
			update('#prisonCard');
		});

		$('#prisonBuy').on('click', function() {
			update('#prisonBuy');
		});

		$('#prisonRoll').on('click', function() {
			update('#prisonRoll');
		});

		var updateInformation = function() {
			updateDice();
			updateButtons();
		};

		var update = function(data) {
			$.ajax({
				url : options[data],
				dataType : "html",
				success : updateMessage
			}).then(function() {
				updateInformation();
			});
		};

		var updateButtons = function() {
			$.ajax({
				url : options['#possibleOptions'],
				dataType : "html",
				success : updateAllButtons
			})
		};

		var updateAllButtons = function(data) {
			var obj = $.parseJSON(data);
			$.each(actions, function(key, value) {
				$(value).attr('disabled', true);
			});

			$.each(obj, function(key, value) {
				$(actions[value]).attr('disabled', false);
			});
		};


		var updateDice = function() {
			$.ajax({
				url : options['#diceResult'],
				dataType : "html",
				success : function(data) {
					var obj = $.parseJSON(data);
					$('#dice1').attr('src', pictures[obj.dice1].src);
					$('#dice2').attr('src', pictures[obj.dice2].src);
				}
			});
		};


		var updateMessage = function(data) {
			var obj = $.parseJSON(data);
			$("#msg").html(obj.msg);
		};

		var updateAllPlayer = function(data) {
			var obj = $.parseJSON(data);
			$.each(obj, function(i, item) {
				updatePlayerPosition(i, item.pos);
			})
		};
		
		// function for checking the right answer
		$scope.checkAnswer = function(select) {

			$.ajax({
				url : ('/answer/' + select),
				dataType : "html",
				success : updateMessage
			}).then(function() {
				updateButtons();
			});
		}

		var updatePlayerPosition = function(i, position) {
			var currentPlayer = $(player[i]);
			currentPlayer.remove()
			$('.pos-' + position).append(currentPlayer);
			/* init dice pictures */
			var i;
			for (i = 1; i <= 6; i++) {
				pictures[i] = new Image();
				pictures[i].src = dice[i];
			}
		};

		var updateCurrentMessage = function () {
			$.ajax({
				url : options['#message'],
				dataType : "html",
				success : updateMessage
			});
		};
		
		function init() {
			
			$scope.updateNameOfPlayer();
			$.each(player, function(key, value) {
				$(value).hide()
			});
			$.ajax({
				url : options['#update'],
				dataType : "html",
				success : function(data) {
					var playeinfor = JSON.parse(data);
					$scope.players = playeinfor;
					$scope.$apply();
					for (i = 0; i < Object.keys(playeinfor).length; i++) {
						$(player[i]).show();
					}

				}
			});

			// Test
			var i;
			for (i = 1; i <= 6; i++) {
				pictures[i] = new Image();
				pictures[i].src = dice[i];
			}
		}

		/** ********************** websockets ******************************* */

		connect();

		function connect() {
			var host = location.origin.replace(/^http/, 'ws');

			// read Play session cookie
			var rawCookie = $cookies['PLAY_SESSION'];
			var rawData = rawCookie.substring(rawCookie.indexOf('=') + 1, rawCookie.length-1);
			host = host + "/socket/" +rawData;

			var socket = new WebSocket(host);

			message('Socket Status: ' + socket.readyState + ' (ready)');

			socket.onopen = function() {
				message('Socket Status: ' + socket.readyState + ' (open)');
			};

			socket.onmessage = function(msg) {
				var msgnew = msg.data;
				$scope.players = JSON.parse(msgnew);
				$scope.$apply();
				updateAllPlayer(msgnew);
				updateInformation();
				updateCurrentMessage();
			};

			socket.onclose = function() {
				message('Socket Status: ' + socket.readyState + ' (Closed)');
			};

			function message(msg) {
				$('#wsLog').append('<p>' + msg + '</p>');
			}

		}// End connect

		// initialize
		init();
	});

});
