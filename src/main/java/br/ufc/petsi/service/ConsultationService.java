package br.ufc.petsi.service;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.swing.JOptionPane;

import org.eclipse.jdt.internal.compiler.impl.Constant;

import br.ufc.petsi.constants.Constants;
import br.ufc.petsi.dao.ConsultationDAO;
import br.ufc.petsi.dao.ReserveDAO;
import br.ufc.petsi.dao.UserDAO;
import br.ufc.petsi.dao.hibernate.HBUserDAO;
import br.ufc.petsi.enums.ConsultationState;
import br.ufc.petsi.event.Event;
import br.ufc.petsi.model.Consultation;
import br.ufc.petsi.model.Patient;
import br.ufc.petsi.model.Professional;
import br.ufc.petsi.model.Rating;
import br.ufc.petsi.model.Reserve;
import br.ufc.petsi.model.Scheduler;
import br.ufc.petsi.model.SocialService;
import br.ufc.petsi.util.ConsultationExclusionStrategy;
import br.ufc.petsi.util.Response;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


@Named
public class ConsultationService {

	@Inject
	private EmailService emailService;


	public String saveConsultation(Professional proTemp, String json, ConsultationDAO consDAO, ConsultationState state){
		Gson gson = new Gson();
		ObjectMapper mapper = new ObjectMapper();
		Response response = new Response();
		
		try{
			
			Scheduler scheduler = mapper.readValue(json, Scheduler.class);
			
			for (Consultation consultation : scheduler.getSchedule()) {
				consultation.setProfessional(proTemp);
				consultation.setService(proTemp.getSocialService());
				consultation.setState(state);
				
				if(consultation.getDateInit().after(consultation.getDateEnd())){
					response.setCode(Response.ERROR);
					response.setMessage("Ops, existe uma consulta com horário de inicio superior ao de fim");
					return gson.toJson(response);
				}
				
				consDAO.save(consultation);
			}
			
		}catch(Exception e){
			e.printStackTrace();
			response.setCode(Response.ERROR);
			response.setMessage("Ops, não foi possível cadastrar a(s) consulta(s)");
			return gson.toJson(response);
		}

		response.setCode(Response.SUCCESS);
		response.setMessage("Consulta(s) cadastrada(s) com sucesso");
		return gson.toJson(response);

	}
	
	public String saveConsultationNow(Professional proTemp, String json, ConsultationDAO consDAO, UserDAO userDAO){
		Gson gson = new Gson();
		Response response = new Response();
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		
		Consultation con = gson.fromJson(json, Consultation.class);
		con.setProfessional(proTemp);
		con.setService(proTemp.getSocialService());
		con.setState(ConsultationState.NO);
		
		if(con.getDateInit().after(con.getDateEnd())){
			response.setCode(Response.ERROR);
			response.setMessage("Ops, existe uma consulta com horário de inicio superior ao de fim");
			return gson.toJson(response);
		}
		
		try{
			List<Consultation> cons = consDAO.getConsultationByProfessional(proTemp);
			for(Consultation c: cons){
				
				if(formatter.format(c.getDateInit()).equals(formatter.format(con.getDateInit())) && formatter.format(c.getDateEnd()).equals(formatter.format(con.getDateEnd())) && (c.getState().equals(ConsultationState.SC) || c.getState().equals(ConsultationState.RV))){
					response.setCode(Response.ERROR);
					response.setMessage("Ops, não foi possível agendar a consulta, pois a mesma já está reservada/agendada");
					return gson.toJson(response);
				}
			}
			consDAO.save(con);
		}catch (Exception e) {
			e.printStackTrace();
			response.setCode(Response.ERROR);
			response.setMessage("Ops, não foi possível cadastrar a(s) consulta(s)");
			return gson.toJson(response);
		}
		
		response.setCode(Response.SUCCESS);
		response.setMessage("Consulta(s) cadastrada(s) com sucesso");
		return gson.toJson(response);
	}
	
	public String registerConsultation(Consultation con, ConsultationDAO consDAO){
		Consultation oldConsultation = consDAO.getConsultationById(con.getId());
		Gson gson = new Gson();
		Response res = new Response();
		if(oldConsultation.getPatient() == null){
			res.setCode(Response.ERROR);
			res.setMessage("Ops, não é possível registrar uma consulta quando a mesma não possui nenhum paciente!");
			return gson.toJson(res);
		}
		Date today = new Date();
		if(today.before(oldConsultation.getDateEnd())){
			res.setCode(Response.ERROR);
			res.setMessage("Ops, não é possível registrar uma consulta que ainda não aconteceu!");
			return gson.toJson(res);
		}
		consDAO.registerConsultation(con);
		res.setCode(Response.SUCCESS);
		res.setMessage("Consulta registrada com sucesso!");
		return gson.toJson(res);
	}
	
	public String getConsultationsByPatient(Patient patient, ConsultationDAO consDAO, ReserveDAO reserveDAO){
		String json;
		Gson gson = new Gson();

		List<Consultation> consultations = consDAO.getConsultationsByPatient(patient);

		List<Reserve> reserves = reserveDAO.getActiveReservesByPatient(patient);

		List<Event> events = new ArrayList<Event>();

		if(consultations != null){
			if(consultations.size() > 0){
				for(Consultation c : consultations){
					Event event = new Event(patient, c);
					events.add(event);
				}
			}
		}

		for(Reserve reserve: reserves){

			Consultation consultation = reserve.getConsultation();
			Event event = new Event(consultation.getPatient(), consultation);
			event.setState("Reservado");
			event.setColor("#D9D919");
			event.setTextColor("white");
			event.setIdReserve(reserve.getId());
			events.add(event);
		}

		json = gson.toJson(events);

		return json;
	}

	public String getConsultationsBySocialService(Patient patient, SocialService socialService, ConsultationDAO consDAO){
		String json = "";
		Gson gson = new Gson();

		List<Consultation> consultations = consDAO.getConsultationsBySocialService(socialService);

		List<Event> events = new ArrayList<Event>();

		for(Consultation c : consultations){
			if(c.getState().name() == ConsultationState.RD.name()){
				if(c.getPatient().getCpf().equals(patient.getCpf())){
					Event event = new Event(patient, c);
					events.add(event);
				}				

			}else{
				Event event = new Event(patient, c);
				events.add(event);
			}
		}

		json = gson.toJson(events);
		return json;
	}

	public String getConsultationsByProfessionalJSON(Professional professional, ConsultationDAO consDAO){
		List<Consultation> cons = consDAO.getConsultationByProfessional(professional);
		String json = "";
		try{
			Gson gson = new GsonBuilder().setExclusionStrategies(new ConsultationExclusionStrategy()).serializeNulls().create();
			json = gson.toJson(cons);

		}catch(Exception e){
			System.out.println("Error at getConsultationByProfessional Service: ");
		}
		return json;
	}

	public List<Consultation> getConsultationsByProfessional(Professional professional, ConsultationDAO consDAO){
		List<Consultation> cons = consDAO.getConsultationByProfessional(professional);
		return cons;
	}

	public String getConsultationsById(Patient patient, long id, ConsultationDAO consDAO){
		String json = "";
		Gson gson = new Gson();

		Consultation c = consDAO.getConsultationById(id);		
		
		Event event = new Event(patient, c);

		json = gson.toJson(event);
		return json;
	}

	public Consultation getConsultationsById(long id, ConsultationDAO consDAO){
		Consultation c = consDAO.getConsultationById(id);		
		return c;
	}

	public String rescheduleConsultation(long idConsultation, Date dateInit, Date dateEnd, String email, ConsultationDAO consDAO){
		Gson gson = new Gson();
		Response response = new Response();
		try{
			Consultation consultation = consDAO.getConsultationById(idConsultation);
			consultation.setDateInit(dateInit);
			consultation.setDateEnd(dateEnd);
			if(consultation.getPatient() != null && consultation.getPatient().getEmail() != null && !email.equals("")){
				emailService.sendEmail(consultation, email);
			}
			consDAO.update(consultation);
			response.setMessage("Consulta reagendada com sucesso!");
			response.setCode(Response.SUCCESS);
		}catch(Exception e){
			e.printStackTrace();
			response.setMessage("Não foi possível reagendar essa consulta!");
			response.setCode(Response.ERROR);
		}
		return gson.toJson(response);
	}

	public void updateConsultation(Consultation consultation, ConsultationDAO consDAO){
		consDAO.update(consultation);
	}

	public String updateConsultation(Consultation consultation, ConsultationDAO consDAO, Patient patient){


		Gson gson = new Gson();
		Response response = new Response();
		Date date = new Date();

		try{
			if(consultation.getState().equals(ConsultationState.FR) && consultation.getDateInit().after(date)){
				consultation.setState(ConsultationState.SC);
				consultation.setPatient(patient);
				response.setCode(Response.SUCCESS);
				response.setMessage("Consulta agendada com sucesso");
				consDAO.update(consultation);
			} 
			else{
				response.setCode(Response.ERROR);
				response.setMessage("A consulta não está mais disponível");

			}

			return gson.toJson(response);

		}catch(Exception e){
			response.setCode(Response.ERROR);
			response.setMessage("Ops, não foi possível agendar a consulta");
			System.out.println("Error at registerConsultation by id: "+e);
			return gson.toJson(response);
		}


	}
	
	public String cancelConsultationById(long id, String message, ConsultationDAO consDAO){

		Gson gson = new Gson();
		Response response = new Response();

		try{
			Consultation oldCons = getConsultationsById(id, consDAO);

			if(oldCons != null){
				Date today = new Date();

				DateFormat formatDate = new SimpleDateFormat("dd/MM/YYYY HH:mm");
				DateFormat formatHours = new SimpleDateFormat("HH:mm");
				if( message == "" || message == null){
					message = "Informamos que sua consulta do dia "+formatDate.format(oldCons.getDateInit())+" de "+formatHours.format(oldCons.getDateInit())+" às "+ formatHours.format(oldCons.getDateEnd()) +" foi cancelada!";
				}

				if(oldCons.getDateEnd().before(today)){
					response.setCode(Response.ERROR);
					response.setMessage("Ops, não é possível cancelar consultas anteriores a data de hoje");
				}else{
					consDAO.cancelConsultation(oldCons);
					response.setCode(Response.SUCCESS);
					response.setMessage("Consulta cancelada com sucesso!");

					if(oldCons.getPatient() != null){
						if(!message.equals(""))
							emailService.sendEmail(oldCons, message);
					}
				}
				return gson.toJson(response);
			}
			response.setCode(Response.ERROR);
			response.setMessage("Ops, não foi possível cancelar a consulta pois a mesma não existe");
			return gson.toJson(response);
		}catch(Exception e){
			response.setCode(Response.ERROR);
			response.setMessage("Ops, não foi possível cancelar a consulta");

			e.printStackTrace();

			System.out.println("Error at cancelConsultation by id: "+e);

			return gson.toJson(response);
		}
	}

	public String getRatingByConsultation(Consultation consultation, ConsultationDAO consultationDAO){
		String json = "";
		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
		Rating rating = consultationDAO.getRatingByIdConsultation(consultation.getId());
		json = gson.toJson(rating);
		return json;

	}

	public String cancelConsultation(Consultation consultation, ConsultationDAO consultationDAO, ReserveDAO reserveDAO){

		List<Reserve> reserves = reserveDAO.getActiveReservesByConsultation(consultation);

		if(reserves.isEmpty()){
			consultation.setState(ConsultationState.FR);
			consultation.setPatient(null);
			consultationDAO.update(consultation);

		}else{
			Collections.sort(reserves);
			Reserve reserve = reserves.get(0);
			reserve.setActive(false);
			reserveDAO.update(reserve);
			consultation.setPatient(reserve.getPatient());
			consultationDAO.update(consultation);
		}

		return "{'msg':Consulta cancelada com sucesso}";
	}

	public String reserveConsultation(Patient patient, Consultation consultation, ReserveDAO reserveDAO){

		Reserve reserve = new Reserve();
		reserve.setPatient(patient);
		reserve.setConsultation(consultation);
		reserve.setDate(new Date());
		reserve.setActive(true);

		reserveDAO.save(reserve);

		return "{'msg':Consulta reservada com sucesso}";
	}

	public String cancelReserve(Reserve reserve, ReserveDAO reserveDAO){
		reserve.setActive(false);
		reserveDAO.update(reserve);
		return "{'msg': Reserva cancelada com sucesso}";
	}

	public String updateRating(Consultation consultation, ConsultationDAO consultationDAO, Patient patient){

		Gson gson = new Gson();
		Response response = new Response();
		
		try{
			if(consultation.getState().equals(ConsultationState.RD) && consultation.getPatient().getCpf().equals(patient.getCpf())){
				response.setCode(Response.SUCCESS);
				response.setMessage("Consulta avaliada com sucesso");
				consultationDAO.update(consultation);
			} 
			else{
				response.setCode(Response.ERROR);
				response.setMessage("A consulta não pode ser avaliada");

			}
			return gson.toJson(response);


		}catch(Exception e){
			response.setCode(Response.ERROR);
			response.setMessage("Ops, não foi possível avaliar a consulta");
			System.out.println("Error at ratingConsultation by id: "+e);
			return gson.toJson(response);
		}

	}
	
	public String checkSchedules(Professional proTemp, String json, ConsultationDAO consDAO){
		Gson gson = new Gson();
		Response response = new Response();
		JsonObject schedules = (JsonObject) new JsonParser().parse(json);
		SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
		
		Date dateInit = new Date(Long.parseLong(schedules.get("dateInit").toString()));
		Date dateEnd = new Date(Long.parseLong(schedules.get("dateEnd").toString()));
		try{
			List<Consultation> cons = consDAO.getConsultationByPeriod(proTemp, dateInit, dateEnd);
			for(Consultation c: cons){
				Date di = new Date();
				String si = schedules.get("hourInit").toString().replaceAll("\"", "");
				di.setHours(Integer.parseInt(si.split(":")[0]));
				di.setMinutes(Integer.parseInt(si.split(":")[1]));
				di.setSeconds(Integer.parseInt(si.split(":")[2]));
				
				Date de = new Date();
				String se = schedules.get("hourEnd").toString().replaceAll("\"", "");
				de.setHours(Integer.parseInt(se.split(":")[0]));
				de.setMinutes(Integer.parseInt(se.split(":")[1]));
				de.setSeconds(Integer.parseInt(se.split(":")[2]));
				
				if(c.getDateInit().getHours() >= di.getHours() && c.getDateInit().getMinutes() >= di.getMinutes() && c.getDateEnd().getHours() <= de.getHours() && c.getDateEnd().getMinutes() <= de.getMinutes() ){
					System.out.println("Entrou!!");
					response.setCode(Response.ERROR);
					response.setMessage("Ops, outra(s) consulta(s) está(ão) agendada(s) neste período");
					return gson.toJson(response);
				}
			}
		}catch (Exception e) {
			e.printStackTrace();
			response.setCode(Response.ERROR);
			response.setMessage("Ops, não foi possível verificar os horários");
			return gson.toJson(response);
		}
		
		response.setCode(Response.SUCCESS);
		response.setMessage("Todos os horários estão vagos!");
		return gson.toJson(response);
		
	}

}
