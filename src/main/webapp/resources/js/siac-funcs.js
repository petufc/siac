
/**
 *	Todas as principais funcionalidades javascript do siac. 
 */

var RESPONSE_ERROR = 500;
var RESPONSE_SUCCESS = 200;

//O terceiro parâmetro é uma função de callback, ela é chamada quando a requisição é retornada.
//Função que fax uma chamada ajax contendo a url e os parametros devidos.

function ajaxCall(_url, params, funcSucc, funcErr, method){
	method : method ? method : "GET";
	console.log(params)
	var ajax = $.ajax({
		method : method,
		contentType: "application/x-www-form-urlencoded; charset=ISO-8859-1",
		dataType: "json",
		url: _url,
		encoding:"UTF-8",
		data: params
	});
	

	ajax.done(funcSucc);
	
	if(funcErr)
		ajax.error(funcErr);
	else{
		ajax.error(function(textStatus, error){
			alertMessage("Ops! Aconteceu algo de errado.");
			console.log(JSON.stringify(textStatus)+" - "+error);
		});
	}

}

function ajaxCallNoJSON(_url, params, func, fail){
	$.ajax({
			method: "GET",
			encoding:"UTF-8",	
			contentType: "application/json; charset=ISO-8859-1",
			url: _url,
			data: params
		}
	).done(func).fail(fail);
}

function getFormatedDate(stringDate){
	//Formatando a data para YYYY-DD-MM
	//sch.getDate() retorna a data em DD/MM/YYYY
	//Logo é feito um split que é usado para criar um objeto date no formato abaixo.
	var from = stringDate.split("/");
	return new Date(from[2], from[1] - 1, from[0]);
}


const ALERT_SUCCESS = "alert-success";
const ALERT_ERROR = "alert-warning";
//Função que mostra a mensagem de alerta em cima do calendário.
//Type: SUCCESS ou ERROR
//Time: tempo para que a mensagem desapareça
function alertMessage(message, time, type, idAlert){
	//Se o tempo para esconder a mensagem não for passado por paramentro
	//o valor do tempo será 5 segundos.
	time = !time ? 5000 : time;
	var alertMessage = idAlert ? $("#"+idAlert) : $(".alert-message");
	
	alertMessage.removeClass(ALERT_SUCCESS);
	alertMessage.removeClass(ALERT_ERROR);
	
	var icon = "glyphicon glyphicon-exclamation-sign";

	if( type == ALERT_SUCCESS ){
		icon = "glyphicon glyphicon-ok-sign";
	}else{
		type = ALERT_ERROR;
	}
	
	
	
	alertMessage.addClass(type);
	
	alertMessage.find("#alert-icon").addClass(icon);
	
	alertMessage.find("#alert-text").text(message);
	alertMessage.removeClass("hidden");
	alertMessage.show();
	hideElement(alertMessage, time);
}

function hideElement(element, time){
	setTimeout(
			function(){
				$(element).slideUp(1500);
			}, time
	);
}

function stringToDate(string){
	var d = string.split("/");
    return new Date(d[2], d[1]-1, d[0]);
}

//returns 0 if the dates are equals, a negative number 
//if the first date is less than second, and a positive number otherwise
function compareDate(date1, date2){
	return (date1.getTime()-date2.getTime()); 
}