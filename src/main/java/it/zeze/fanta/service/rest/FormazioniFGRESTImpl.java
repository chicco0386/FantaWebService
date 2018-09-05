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

import it.zeze.fanta.service.definition.FormazioniFGInterface;
import it.zeze.fanta.service.definition.ejb.FormazioniFGLocal;
import it.zeze.fantaformazioneweb.entity.ProbabiliFormazioniFg;

@Path("/formazioniFGRESTImpl")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class FormazioniFGRESTImpl implements FormazioniFGInterface {

	private static final Logger logger = LogManager.getLogger(FormazioniFGRESTImpl.class);

	private FormazioniFGLocal formazioniFGEJB;

	{
		try {
			formazioniFGEJB = JNDIUtils.getFormazioniFGEJB();
		} catch (NamingException e) {
			e.printStackTrace();
		}
	}

	@GET
	@Path("/unmarshallAndSaveFromHtmlFile")
	@Override
	public void unmarshallAndSaveFromHtmlFile(@QueryParam("stagione") String stagione) {
		formazioniFGEJB.unmarshallAndSaveFromHtmlFile(stagione);
	}

	@Override
	public ProbabiliFormazioniFg selectByIdGiocatoreIdGiornata(int idGiocatore, int idGiornata) {
		return formazioniFGEJB.selectByIdGiocatoreIdGiornata(idGiocatore, idGiornata);
	}

}
