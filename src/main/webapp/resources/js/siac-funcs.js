/**
 *	Todas as funcionalidades javascript do siac. 
 */

const MY_CALENDAR = "0";

//Essa variável é utilizada para saber qual o serviço que o usuário clicou.
var serviceId = "0";

$("document").ready(function(){

	onClickModalConfig();
	initCalendarPatient();
	onServiceClick();

});

function postAjaxCall(url, params){
	$.post(url, params).done(function(data, textStatus){
		return data;
	}).fail(function(textStatus, errorThrown){
		alert("Não foi possível carregar os horários: "+errorThrown);
	});
}


function openScheduleModal(schedules){
	
	$.each(schedules, function(schedule, status){
		var newRow = $("<tr></tr>");
		var tableData = "";
		tableData += '<td>'+schedule+'</td>';
		tableData += '<td>'+status+'</td>';
		newRow.append(tableData);
		$("#table-schedule").append(newRow);
	});
	
	$("#modal-schedules").modal('show'); 
	$("#modal-title-schedule").html("Horários dia <strong>"+date.format()+"</strong>");
	
}


function onServiceClick(){
	$(".link-service").click(function(){
		$(".service").removeClass("active");
		$(this).parent().addClass("active");

		serviceId = $(this).attr('id');
		
		var params = new Object();
		var url;
		
		if(serviceId == MY_CALENDAR){
			$("#my-calendar").text("Meu Calendário");
			params["userId"] = serviceId;
			url = "/siac/getUserAgenda"
		}else{
			$("#my-calendar").text("Calendário "+$(this).text());
			params["serviceId"] = serviceId;
			url = "/siac/getServiceAgenda";
		}
		$.getJSON(url, params, function(json){
			setCalendarSchedules(json);
		});
	});
}


function onClickModalConfig(){
	//Quando o usuário clicar na imagem o modal aparece...
	$("#avatar-img").click(function(){
		$("#modal-config").modal('show');
	});
}

//Função que inicia o calendário.
function initCalendarPatient(){
	$("#calendar_patient").fullCalendar({
		header: {
			left: 'prev',
			center: 'title',
			right: 'next'
		},
				 businessHours: true,
		         editable: false,
		         dayClick: clickFunction
	});
	
}


/*Essa função pega todos os dados vindos da requisição
  AJAX e preche o calendário com elas.*/
function setCalendarSchedules(json){
	if(json.consultations.length == 0){
		alert("Não existe nenhum evento cadastrado!");
		return;
	}
	
	$.each(json.consultations, function(key, obj){
		var serviceName = obj.service.name;
		$.each(obj, function(name, value){
			if(name == "schedule"){
				dateInit = new Date(value.dateInit);
				dateEnd = new Date(value.dateEnd);
				_dateInit = formatDate(dateInit);
				_dateEnd = formatDate(dateEnd);
				alert(_dateEnd);
				
				renderCalendarEvent(serviceName, _dateInit, _dateEnd);
			}
		});
	});
	
}

function formatDate(date){
	var dd = date.getDate();
    var mm = date.getMonth()+1; //January is 0!

    var yyyy = date.getFullYear();
    if(dd<10){
        dd='0'+dd
    } 
    if(mm<10){
        mm='0'+mm
    } 
    res = yyyy+'-'+mm+'-'+dd;
    return res;
}

//Essa função é responsável por adicionar um evento no calendário.
function renderCalendarEvent(serviceName, dayStart, dayEnd){
	$("#calendar_patient").fullCalendar('renderEvent',{
		title: serviceName,
		start: dayStart,
		end: dayEnd
	},'stick');
}

function clickFunction(date, jsEvent, view){
	
}
