package br.ufc.petsi.controller;

import java.util.Date;

import javax.inject.Inject;
import javax.servlet.http.HttpSession;

import net.sf.jasperreports.engine.JREmptyDataSource;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import br.ufc.petsi.constants.Constants;
import br.ufc.petsi.dao.ReportDAO;
import br.ufc.petsi.model.GeneralReport;
import br.ufc.petsi.model.Professional;
import br.ufc.petsi.model.RatingReport;
import br.ufc.petsi.model.ServiceReport;

@Controller
public class ReportController {
	
	@Inject
	private ReportDAO reportDAO;
	
	@RequestMapping("/relatorio/servico")
	 public String gerarRelatorioPorServico(Model model, HttpSession session,
			 @DateTimeFormat(pattern="dd/MM/yyyy") Date dateBegin, 
			 @DateTimeFormat(pattern="dd/MM/yyyy") Date dateEnd, 
			 int serviceId, int professionalId){ 
		
		ServiceReport serviceReport = reportDAO.getServiceReport(serviceId, professionalId, dateBegin, dateEnd);

		model.addAttribute("format", "pdf");
		model.addAttribute("dateBegin", dateBegin);
		model.addAttribute("dateEnd", dateEnd);
		model.addAttribute("service", serviceReport.getService());
		model.addAttribute("professional", serviceReport.getProfessional());
		model.addAttribute("total", serviceReport.getTotal());
		model.addAttribute("scheduled", serviceReport.getScheduled());
		model.addAttribute("unscheduled", serviceReport.getUnscheduled());
		model.addAttribute("canceled", serviceReport.getCanceled());
		model.addAttribute("byMonth", serviceReport.getByMonth());
		model.addAttribute("SubReportByServiceLocation", "./ByService_subreport.jasper");
		model.addAttribute("datasource", new JREmptyDataSource());

		return "byservice";
	 }	
	
	@RequestMapping("/relatorio/avaliacao")
	 public String gerarRelatorioAvaliacao(Model model, 
			 HttpSession session, 
			 @DateTimeFormat(pattern="dd/MM/yyyy") Date dateBegin, 
			 @DateTimeFormat(pattern="dd/MM/yyyy") Date dateEnd){ 
		
		dateBegin.setHours(0);
		dateEnd.setHours(23);
		
		Professional professional = ((Professional)session.getAttribute(Constants.USER_SESSION));
		
		Long professionalId = professional.getId();
		
		RatingReport ratingReport = reportDAO.getRatingReport(professionalId, dateBegin, dateEnd);
		
		model.addAttribute("format", "pdf");
		model.addAttribute("dateBegin", dateBegin);
		model.addAttribute("name", professional.getName());
		model.addAttribute("dateEnd", dateEnd);
		model.addAttribute("average", ratingReport.getAverage());
		model.addAttribute("ratings", ratingReport.getRatings());
		model.addAttribute("RatingSubReportLocation", "./Rating_subreport.jasper");
		model.addAttribute("datasource", new JREmptyDataSource());

		return "rating";
	 }	

	@RequestMapping("/relatorio/geral")
	 public String gerarRelatorioGeral(Model model, HttpSession session, @DateTimeFormat(pattern="dd/MM/yyyy") Date dateBegin, @DateTimeFormat(pattern="dd/MM/yyyy") Date dateEnd){ 
		
		GeneralReport generalReport = reportDAO.getGeneralReport(dateBegin, dateEnd);
		
		model.addAttribute("format", "pdf");
		model.addAttribute("dateBegin", dateBegin);
		model.addAttribute("dateEnd", dateEnd);
		model.addAttribute("byMonth", generalReport.getByMonth());
		model.addAttribute("GeneralSubReportLocation", "./General_subreport.jasper");
		model.addAttribute("datasource", new JREmptyDataSource());

		return "geral";
	 }
}
