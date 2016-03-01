package it.zeze.fanta.service.rest;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import it.zeze.fanta.service.definition.FormazioniGazzettaInterface;
import it.zeze.fanta.service.definition.ejb.FormazioniGazzettaLocal;

@Path("/formazioniGazzettaRESTImpl")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Stateless
public class FormazioniGazzettaRESTImpl implements FormazioniGazzettaInterface {

	private static final Logger logger = LogManager.getLogger(FormazioniGazzettaRESTImpl.class);

	@EJB(name = "FormazioniGazzettaEJB")
	private FormazioniGazzettaLocal formazioniGazzettaEJB;

	@GET
	@Path("/unmarshallAndSaveFromHtmlFile")
	@Override
	public void unmarshallAndSaveFromHtmlFile(@QueryParam("stagione") String stagione) {
		formazioniGazzettaEJB.unmarshallAndSaveFromHtmlFile(stagione);		
	}

}
