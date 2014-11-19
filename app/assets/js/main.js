/**
 * Created by Timi on 29.10.2014.
 */

$(document).ready(function () {

    var options = {
        '#update':'/update',
        '#rollDice': '/rollDice',
        '#endTurn': '/endTurn',
        '#buy': '/buy',
        '#start': 'start/2'
    };

    var updatePlayerAjax = function() {
        $.ajax({
            url: options['#update'],
            dataType: "html",
            success: updateAllPlayer
        });
    };


    var updateMessageAjax = function() {
        $.ajax({
            url: options['#rollDice'],
            dataType: "html",
            success: updateMessage
        }).then($.ajax({
            url: options['#update'],
            dataType: "html",
            success: updateAllPlayer
        })  )

    };


    $('#update').on('click', updatePlayerAjax);

    $('#rollDice').on('click', updateMessageAjax);

    // TODO in eine Funktion
    $('#endTurn').on('click', function() {
        $.ajax({
            url: options['#endTurn'],
            dataType: "html",
            success: updateMessage
        });
        $.ajax({
            url: options['#update'],
            dataType: "html",
            success: updateAllPlayer
        });

    });
    $('#buy').on('click', function() {
        $.ajax({
            url: options['#buy'],
            dataType: "html",
            success: updateMessage
        });
        $.ajax({
            url: options['#update'],
            dataType: "html",
            success: updateAllPlayer
        });

    });


    var updateMessage = function(data) {
        var obj = $.parseJSON(data);
        $("#msg").html(obj.msg);
    };


    var updateAllPlayer = function(data) {
        var obj = $.parseJSON(data);
        $.each(obj, function(i, item){
            updateSinglePlayer(i, item);
        })
    };

    var updateSinglePlayer = function(index, player) {
      $('#namePlayer_' + index).html(player.name);
      $('#budgetPlayer_' + index).html(player.budget+1);
      $('#ownershipPlayer_' + index).html(player.ownership);
      $('#positionPlayer_' + index).html(player.pos);
    };


});
