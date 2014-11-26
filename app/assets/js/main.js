/**
 * Created by Timi on 29.10.2014.
 */
var monopoly = angular.module("monopoly", []);

monopoly.controller('MainCtrl', [ '$scope', function($scope, $http) {
	$scope.player;

	$(document).ready(function() {

		var options = {
			'#update' : '/update',
			'#rollDice' : '/rollDice',
			'#endTurn' : '/endTurn',
			'#buy' : '/buy',
			'#start' : 'start/2'
		};

		var updatePlayerAjax = function() {
			$.ajax({
				url : options['#update'],
				dataType : "html",
				success : updateAllPlayer
			});
		};

		var updateMessageAjax = function() {
			$.ajax({
				url : options['#rollDice'],
				dataType : "html",
				success : updateMessage
			}).then(

			/*
			 * $.ajax({ url: options['#update'], dataType: "html", success:
			 * updateAllPlayer })
			 */
			)

		};

		$('#update').on('click', updatePlayerAjax);

		$('#rollDice').on('click', updateMessageAjax);

		// TODO in eine Funktion
		$('#endTurn').on('click', function() {
			$.ajax({
				url : options['#endTurn'],
				dataType : "html",
				success : updateMessage
			});
		});
		$('#buy').on('click', function() {
			$.ajax({
				url : options['#buy'],
				dataType : "html",
				success : updateMessage
			});
		});

		var updateMessage = function(data) {
			var obj = $.parseJSON(data);
			$("#msg").html(obj.msg);
		};

		var updateAllPlayer = function(data) {
			var obj = $.parseJSON(data);
			$.each(obj, function(i, item) {
				updateSinglePlayer(i, item);
			})
		};

		var updateSinglePlayer = function(index, player) {
			$('#namePlayer_' + index).html(player.name);
			$('#budgetPlayer_' + index).html(player.budget + 1);
			$('#ownershipPlayer_' + index).html(player.ownership);
			$('#positionPlayer_' + index).html(player.pos);
		};

		/** ********************** websockets ******************************* */
		connect();

		function connect() {
			var socket = new WebSocket("ws://localhost:9000/socket");

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
	});

} ]);
