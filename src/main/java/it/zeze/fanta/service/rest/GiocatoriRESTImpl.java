package it.zeze.fanta.service.rest;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import it.zeze.fanta.service.definition.GiocatoriInterface;
import it.zeze.fanta.service.definition.ejb.GiocatoriLocal;

@Path("/giocatoriRESTImpl")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Stateless
public class GiocatoriRESTImpl implements GiocatoriInterface {

	private static final Logger logger = LogManager.getLogger(GiocatoriRESTImpl.class);

	@EJB(name = "GiocatoriEJB")
	private GiocatoriLocal giocatoriEJB;

	@Override
	public void unmarshallAndSaveFromHtmlFile(boolean noLike) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void unmarshallAndSaveFromHtmlFileForUpdateStagione(boolean noLike) {
		// TODO Auto-generated method stub
		
	}
}
