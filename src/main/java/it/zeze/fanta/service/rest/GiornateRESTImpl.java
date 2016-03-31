package it.zeze.fanta.service.rest;

import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import it.zeze.fanta.service.bean.SelectItem;
import it.zeze.fanta.service.definition.GiornateInterface;
import it.zeze.fanta.service.definition.ejb.GiornateLocal;
import it.zeze.fantaformazioneweb.entity.Giornate;

@Path("/giornateRESTImpl")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Stateless
public class GiornateRESTImpl implements GiornateInterface {

	private static final Logger logger = LogManager.getLogger(GiornateRESTImpl.class);

	@EJB(name = "GiornateEJB")
	private GiornateLocal giornateEJB;

	@GET
	@Path("/unmarshallAndSaveFromHtmlFile")
	@Override
	public void unmarshallAndSaveFromHtmlFile() {
		logger.info("unmarshallAndSaveFromHtmlFile CALLED");
		giornateEJB.unmarshallAndSaveFromHtmlFile();		
	}

	@Override
	public String getStagione(String stagioneInput) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getIdGiornata(int numeroGiornata, String stagione) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Giornate getGiornataById(int idGiornata) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getIdGiornata(String dataString) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Giornate getGiornata(String dataString) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Giornate getLastGiornata() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Giornate> getGiornateByStagione(String stagione) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Giornate> getGiornateAll() {
		// TODO Auto-generated method stub
		return null;
	}

	@GET
	@Path("/getStagioniAll")
	@Override
	public List<SelectItem> getStagioniAll() {
		return giornateEJB.getStagioniAll();
	}
	
	

}
