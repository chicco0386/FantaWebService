package it.zeze.fanta.service.rest;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.naming.NamingException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import it.zeze.fanta.ejb.util.JNDIUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import it.zeze.fanta.service.definition.GiocatoriInterface;
import it.zeze.fanta.service.definition.ejb.GiocatoriLocal;
import it.zeze.fantaformazioneweb.entity.Giocatori;

@Path("/giocatoriRESTImpl")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class GiocatoriRESTImpl implements GiocatoriInterface {

	private static final Logger logger = LogManager.getLogger(GiocatoriRESTImpl.class);

	private GiocatoriLocal giocatoriEJB;

	{
		try {
			giocatoriEJB = JNDIUtils.getGiocatoriEJB();
		} catch (NamingException e) {
			e.printStackTrace();
		}
	}

	@Override
	@GET
	@Path("/unmarshallAndSaveFromHtmlFile")
	public void unmarshallAndSaveFromHtmlFile(@QueryParam("stagione") String stagione, @QueryParam("noLike") boolean noLike) {
		giocatoriEJB.unmarshallAndSaveFromHtmlFile(stagione, noLike);
		
	}

	@Override
	public void unmarshallAndSaveFromHtmlFileForUpdateStagione(boolean noLike) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Giocatori getGiocatoreByNomeSquadraRuolo(String nomeGiocatore, String squadra, String ruolo, String stagione, boolean noLike) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void insertOrUpdateGiocatore(String nomeSquadra, String nomeGiocatore, String ruolo, String stagione, boolean noLike) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Giocatori getGiocatoreByNomeSquadra(String nomeGiocatore, String squadra, String stagione, boolean noLike) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Giocatori getGiocatoreById(int idGiocatore) {
		// TODO Auto-generated method stub
		return null;
	}
}
