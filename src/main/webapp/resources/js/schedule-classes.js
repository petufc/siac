/**
 * Essa classe serve para gerênciar as datas e os
 * respectivos horários de consultas do profissional 
 * 
 */

var ScheduleTime = function(){
	var self = this;
	this.timeInit = moment();
	this.timeEnd = moment();
	var id = null;
	var state;
	var rating;
	var comment;
	var patient;
		
	self.__construct = function(date, hourInit, minuteInit, hourEnd, minuteEnd){
		this.timeInit = moment(date.getTime());
		this.timeEnd = moment(date.getTime());
		self.setTimeInit(date, hourInit, minuteInit);
		self.setTimeEnd(date, hourEnd, minuteEnd);
	}
	
	self.setPatient = function(patient){
		self.patient = patient;
	}
	
	self.getPatient = function(){
		return self.patient;
	}
	
	self.getComment = function(){
		return self.comment;
	}
	
	self.setComment = function(comment){
		self.comment = comment;
	}
	
	self.setRating = function(rating){
		self.rating = rating;
	}
	
	self.getRating = function(){
		return self.rating;
	}
	
	self.setState = function(state){
		self.state = state;
	}
	
	self.getState = function(){
		return self.state;
	}
	
	self.setId = function(id){
		self.id = id;
	}
	
	self.getId = function(){
		return self.id;
	}
	
	self.setTimeInit = function(date, hourInit, minuteInit){
		this.timeInit.hours(hourInit);
		this.timeInit.minutes(minuteInit);
	}
	
	self.getTimeInit = function(){
		return this.timeInit.format("HH:mm");
	}
	
	self.setTimeEnd = function(date, hourEnd, minuteEnd){
		this.timeEnd.hours(hourEnd);
		this.timeEnd.minutes(minuteEnd);
	}
	
	self.getTimeEnd = function(){
		return this.timeEnd.format("HH:mm");
	}
	
	self.getDateInit = function(){
		return this.timeInit.format("DD/MM/YYYY");
	}
	
	self.getDateEnd = function(){
		return this.timeEnd.format("DD/MM/YYYY");
	}
	
	self.toEmail = function(){
		return this.timeInit.format("DD/MM/YYYY")+" "+this.timeInit.format('HH:mm')+" às "+this.timeEnd.format('HH:mm');
	}
	
	ScheduleTime.prototype.toJSON = function(){
		return {"id":this.id, "timeInit":this.timeInit.format('HH:mm'), "timeEnd":this.timeEnd.format('HH:mm')};
	}
	
};

var ScheduleDay = function(){
	var self = this;
	var date = "";
	this.listSchedules = [];
	
	
	self.setDate = function(date){
		self.date = date;
	}
	
	self.getDate = function(){
		return self.date;
	}
	
	self.addSchedule = function(date, hourInit, minuteInit, hourEnd, minuteEnd, state, rating, comment, id, patient){
		var sch = new ScheduleTime();
		
		state = state ? state : "Sem Estado"; 
		
		rating = rating ? rating : "Sem Nota";
		
		comment = comment ? comment : "Sem comentário cadastrádo!";
	
		id = id ? id : null;
		
		sch.setRating(rating);
		sch.setState(state);
		sch.setComment(comment);
		sch.setId(id);
		sch.setPatient(patient);
		
		sch.__construct(date, hourInit, minuteInit, hourEnd, minuteEnd);
		this.listSchedules.push(sch);
	}
	
	/*
	 * Função utilizada para saber se um horário pode ser
	 * removido ou nao na tela de cadastrar horários.
	 * 
	 * Caso esse dia contenha algum horário com id != null
	 * Então esse horário está cadastrado no banco, e não pode 
	 * ser removido na tela de cadastro.
	 */
	self.isSheduleDayRegistered = function(){
		return this.listSchedules.some(function(element){
			return (element.getId() ? true : false)
		});
	}
	
	self.showListSchedules = function(){
		this.listSchedules.forEach(function(element, index, listSchedules){
			console.log(JSON.stringify(element));
		});
	}
	
	self.clearListSchedules = function(){
		this.listSchedules = [];
	}
	
	self.getListSchedules = function(){
		return this.listSchedules;
	}
	
	self.getScheduleTimeById = function(id){
		var result = null;
		this.listSchedules.forEach(function(scheduleTime){
			if(scheduleTime.getId() == id)
				result = scheduleTime;
		});
		return result;
	}
	
	self.removeScheduleTimeById = function(id){
		this.listSchedules = this.listSchedules.filter(function(element){
			return (!(element == id))
		});
	}
	
	ScheduleDay.prototype.toJSON = function(){
		return {"date": this.date, "schedules": this.listSchedules};
	}	
};


var ScheduleManager = function(){
	var self = this;
	var mapScheduleDay = new Map();
	
	self.addScheduleDay = function(date, scheduleDay){
		mapScheduleDay.set(date, scheduleDay);
	}
	
	/*
	 * Adiciona um novo horário na data passada por parametro.
	 * Se a data não estive no mapa um novo ScheduleDay é criado.
	 */ 
	self.addNewScheduleTime = function(date, hourInit, minuteInit, hourEnd, minuteEnd, state, rating, comment, id, patient){
		var	dateFormatted = moment(date).format("DD/MM/YYYY");
		var scheduleDay = mapScheduleDay.get(dateFormatted);
		if(scheduleDay){
			scheduleDay.addSchedule(date, hourInit, minuteInit, hourEnd, minuteEnd, state, rating, comment, id, patient);
		}else{
			var sch = new ScheduleDay();
			sch.setDate(dateFormatted);
			sch.addSchedule(date, hourInit, minuteInit, hourEnd, minuteEnd, state, rating, comment, id, patient);
			self.addScheduleDay(dateFormatted, sch);
		}
	}
	
	self.removeScheduleDay = function(date){
		mapScheduleDay.delete(date);
	}
	
	self.clearSchedules = function(){
		mapScheduleDay.clear();
	}
	
	self.getSchedulesMap = function(){
		return mapScheduleDay;
	}
	
	/*
	 * Essa função remove todos os horários que não estão
	 * na lista de horários atualizados. 
	 */
	self.updateSchedules = function(mapUpdated){
		var updatedMapSchedule = new Map();
		
		//Pegando todas as keys(datas) do mapa.
		var keys = mapUpdated.keys();
		for(var i = 0; i < mapUpdated.size; i++){
			
			date = keys.next().value;
			
			var schDay = mapScheduleDay.get(date);
			
			if(mapScheduleDay.has(date)){
				updatedMapSchedule.set(date, mapScheduleDay.get(date));
			}
		}
		mapScheduleDay.forEach(function(sch, key){
			if(sch.isSheduleDayRegistered()){
				updatedMapSchedule.set(key, sch);
			}
		});
		
		mapScheduleDay = updatedMapSchedule;
	}
	
	/*
	 * Retorna true se existir horários cadastrados 
	 * na data passada por parâmetro e false caso contrário.
	 */
	self.isScheduleRegistered = function(date){
		var schedule = mapScheduleDay.get(date);
		if(schedule){
			return schedule.listSchedules.length > 0 ? true : false;
		}
		return false;
	}
	
	self.getScheduleDay = function(date){
		return mapScheduleDay.get(date);
	}
	
	self.getScheduleDayAsJSON = function(date){
		var list = [mapScheduleDay.get(date)];
		return {"data":list};
	}
	
	self.getScheduleTimeById = function(idSchedule){
		var schedule = {patient: null};
		mapScheduleDay.forEach(function(scheduleDay){
			var schTemp = scheduleDay.getScheduleTimeById(idSchedule);
			if(schTemp)
				schedule = schTemp;	
		});
		return schedule;
	}
	
	ScheduleManager.prototype.toJSON = function(){
		var list = [];
		mapScheduleDay.forEach(function(value, key, mapScheduleDay){
			list.push(value);
		});
		return {"data":list};
	}
}