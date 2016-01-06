package br.ufc.petsi.controller;

import javax.inject.Inject;

import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import br.ufc.petsi.dao.ServiceDAO;
import br.ufc.petsi.model.SocialService;
import br.ufc.petsi.service.ServiceService;

@Controller
@Transactional
public class ServiceController {
	
	@Inject
	private ServiceService serviceService;
	
	@Inject
	private ServiceDAO serviceDao;
	
	@RequestMapping("/registerService")
	@ResponseBody
	public String registerService(SocialService service){
		serviceService.registerService(service, this.serviceDao);
		return this.getServices();
	}
	
	@RequestMapping("/getActiveServices")
	@ResponseBody
	public String getActiveServices(){
		return serviceService.getActiveServices(this.serviceDao);
	}
	
	@RequestMapping("/getServices")
	@ResponseBody
	public String getServices(){
		return serviceService.getServices(this.serviceDao);
	}
	
	@RequestMapping("/setInactiveService")
	@ResponseBody
	public String setInactiveService(SocialService service){
		serviceService.setInactiveService(this.serviceDao, service);
		return this.getServices();
	}
	
	@RequestMapping("/setActiveService")
	@ResponseBody
	public String setActiveService(SocialService service){
		serviceService.setActiveService(this.serviceDao, service);
		return this.getServices();
	}
	
	@RequestMapping("/editService")
	@ResponseBody
	public String editService(SocialService service){
		serviceService.editService(serviceDao, service);
		return this.getServices();
	}
}
