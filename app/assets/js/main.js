/**
 * Created by Timi on 29.10.2014.
 */
var monopoly = angular.module("monopoly", []);

monopoly.controller('MainCtrl', [ '$scope', function($scope, $http) {
	$scope.players;
	$scope.lala;
	$scope.pic = {
		0 : "/assets/images/boger.jpg",
		1 : "/assets/images/maechtel.jpg",
		2 : "/assets/images/schoppa.jpg",
		3 : "/assets/images/eck.jpg",
		4 : "/assets/images/neuschwander.jpg",
		5 : "/assets/images/bittel.jpg"
	};

	angular.element(document).ready(function() {

		$scope.checkAnswer = function(select) {

			$.ajax({
				url : ('/answer/' + select),
				dataType : "html",
				success : updateMessage
			}).then(function() {
				updateNameOfPlayer();
				updateDice();
				updateButtons();
			});

		}

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
			'#possibleOptions' : '/possibleOptions'
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

		var update = function(data) {
			$.ajax({
				url : options[data],
				dataType : "html",
				success : updateMessage
			}).then(function() {
				updateNameOfPlayer();
				updateDice();
				updateButtons();
			});
		};

		var updateMessageAjax = function() {
			update('#rollDice');
		};

		var updateNameOfPlayer = function() {
			$.ajax({
				url : options['#currentPlayer'],
				dataType : "html",
				success : updateName
			})
		};

		$('#rollDice').on('click', updateMessageAjax);

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

		/** **************** TODO in eine Funktion ************************ */
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

		$('#prisonBuy').on('click', function() {
			update('#prisonBuy');
		});

		/** ************************************************************* */

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
				updateSinglePlayer(i, item);
				updatePlayerPosition(i, item.pos);
			})
		};

		var updateName = function(data) {
			var obj = $.parseJSON(data);
			$scope.lala = obj;
			$scope.$apply();
			// $('.whois').html("Spieler: "+ obj.name + " sie sind dran!");
		};

		var updateSinglePlayer = function(index, player) {
			$('#namePlayer_' + index).html(player.name);
			$('#budgetPlayer_' + index).html(player.budget);
			$('#ownershipPlayer_' + index).html(player.ownership);
			$('#positionPlayer_' + index).html(player.pos);
		};

		/**
		 * ********************** player position
		 * *******************************
		 */

		function init() {
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

		var updatePlayerPosition = function(i, position) {
			var currentPlayer = $(player[i]);
			currentPlayer.remove()
			$('.pos-' + position).append(currentPlayer);
		}

		/** ********************** websockets ******************************* */
		connect();

		function connect() {
			var host = location.origin.replace(/^http/, 'ws');
			host = host + "/socket";
			var socket = new WebSocket(host);

			message('Socket Status: ' + socket.readyState + ' (ready)');

			socket.onopen = function() {
				message('Socket Status: ' + socket.readyState + ' (open)');
			};

			socket.onmessage = function(msg) {
				var msgnew = msg.data;
				$scope.players = JSON.parse(msgnew);
				$scope.$apply();
				console.log(msgnew);
				updateAllPlayer(msgnew);
			};

			socket.onclose = function() {
				message('Socket Status: ' + socket.readyState + ' (Closed)');
			};

			function message(msg) {
				$('#wsLog').append('<p>' + msg + '</p>');
			}

		}// End connect

		init();
	});

} ]);
