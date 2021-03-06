package br.ufc.petsi.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import br.ufc.petsi.model.Professional;

import javax.annotation.Generated;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import br.ufc.petsi.enums.ConsultationState;

//Adicionar id ou cpf do profissional e o cpf ou id do pacientes

@Entity
@Table( name = "consultation" )
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
public class Consultation implements Serializable{
	
	//private static final long serialVersionUID = 852069081696127745L;

	@Id
	@GeneratedValue
	@JsonProperty("id")
	private Long id;
	
	@OneToOne(targetEntity = SocialService.class, 
			  cascade = { CascadeType.MERGE }, 
			  fetch = FetchType.EAGER )
	@JoinColumn( name = "id_service" )
	@JsonProperty("socialService")
	private SocialService socialService;
	
	@ManyToOne(cascade=CascadeType.MERGE)
	@JsonProperty("professional")
	private Professional professional;
	
	@ManyToOne(cascade=CascadeType.MERGE)
	@JsonProperty("patient")
	private Patient patient;
	
	@Column(name="date_init")
	@Temporal(value = TemporalType.TIMESTAMP)
	@JsonProperty("dateInit")
	private Date dateInit;
	
	@Column(name="date_end")
	@Temporal(value = TemporalType.TIMESTAMP)
	@JsonProperty("dateEnd")
	private Date dateEnd;
	
	@Enumerated( EnumType.STRING )
	@JsonProperty("state")
	private ConsultationState state;
	
	@OneToOne(cascade = CascadeType.ALL, optional = true, 
			fetch = FetchType.EAGER, orphanRemoval = true)
	@JoinColumn(name="id_rating")
	@JsonProperty("rating")
	private Rating rating;
	
	@OneToMany(fetch = FetchType.EAGER, mappedBy="consultation")
	@JsonProperty("reserves")
	private List<Reserve> reserves;
	
	public Consultation(Long id, SocialService socialService, Professional profesisonal, Patient patient, 
			ConsultationState state, Date dateInit, Date dateEnd) {
		this.id = id;
		this.socialService = socialService;
		this.state = state;
		this.professional = profesisonal;
		this.patient = patient;
		this.dateEnd = dateEnd;
		this.dateInit = dateInit;
	}

	public Consultation() {}
	
	@JsonProperty("id")
	public Long getId() {
		return id;
	}

	@JsonProperty("id")
	public void setId(Long id) {
		this.id = id;
	}
	
	@JsonProperty("professional")
	public Professional getProfessional() {
		return professional;
	}
	
	@JsonProperty("patient")
	public Patient getPatient() {
		return patient;
	}
	
	@JsonProperty("patient")
	public void setPatient(Patient patient) {
		this.patient = patient;
	}

	@JsonProperty("dateInit")
	public Date getDateInit() {
		return dateInit;
	}

	@JsonProperty("dateInit")
	public void setDateInit(Date dateInit) {
		this.dateInit = dateInit;
	}

	@JsonProperty("dateEnd")
	public Date getDateEnd() {
		return dateEnd;
	}

	@JsonProperty("dateEnd")
	public void setDateEnd(Date dateEnd) {
		this.dateEnd = dateEnd;
	}

	@JsonProperty("professional")
	public void setProfessional(Professional professional) {
		this.professional = professional;
	}

	@JsonProperty("socialService")
	public SocialService getService() {
		return socialService;
	}

	@JsonProperty("socialService")
	public void setService(SocialService service) {
		this.socialService = service;
	}

	@JsonProperty("state")
	public ConsultationState getState() {
		return state;
	}

	@JsonProperty("state")
	public void setState(ConsultationState state) {
		this.state = state;
	}

	@JsonProperty("rating")
	public Rating getRating() {
		return rating;
	}

	@JsonProperty("rating")
	public void setRating(Rating rating) {
		this.rating = rating;
	}

	@JsonProperty("reserves")
	public List<Reserve> getReserves() {
		return reserves;
	}

	@JsonProperty("reserves")
	public void setReserves(List<Reserve> reserves) {
		this.reserves = reserves;
	}
	
	@Override
	public String toString() {
		return "Consultation: [ id:"+id+"; socialService: "+socialService.getName()+"; dataInit: "+dateInit.toString()+"; dataEnd: "+dateEnd.toString()+"]";
	}

}
