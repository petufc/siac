package br.ufc.petsi.service;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.Gson;

import br.ufc.petsi.dao.ServiceDAO;
import br.ufc.petsi.model.SocialService;

@Named
public class ServiceService {
	
	public void registerService(SocialService service, ServiceDAO serviceDao){
		service.setActive(true);
		serviceDao.save(service);
	}
	
	public String getActiveServices(ServiceDAO serviceDao){
		List<SocialService> services = serviceDao.getActiveServices();
		
		Gson gson = new Gson();
		
		String json = "";
		json = gson.toJson(services);
		
		return json;
	}
	
	public String getServices(ServiceDAO serviceDao){
		
		List<SocialService> services = serviceDao.getAllServices();
		
		Gson gson = new Gson();
		
		String json = "";
		json = gson.toJson(services);
		
		return json;
	}
	
	public void setInactiveService(ServiceDAO serviceDao, SocialService service){
		
		SocialService service2 = serviceDao.getServiceById(service.getId());
		
		service2.setActive(false);
		serviceDao.edit(service2);
		
	}
	
	public void setActiveService(ServiceDAO serviceDao, SocialService service){
		
		SocialService service2 = serviceDao.getServiceById(service.getId());
		
		service2.setActive(true);
		serviceDao.edit(service2);
	}
	
	public void editService(ServiceDAO serviceDao, SocialService service){
		
		SocialService service2 = serviceDao.getServiceById(service.getId());
		
		service2.setName(service.getName());
		serviceDao.edit(service2);
	}

}
