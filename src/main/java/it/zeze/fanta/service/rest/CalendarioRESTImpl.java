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
import org.htmlcleaner.TagNode;

import it.zeze.fanta.service.definition.CalendarioInterface;
import it.zeze.fanta.service.definition.ejb.CalendarioLocal;
import it.zeze.fantaformazioneweb.entity.Calendario;
import it.zeze.fantaformazioneweb.entity.Giornate;

@Path("/calendarioRESTImpl")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class CalendarioRESTImpl implements CalendarioInterface {

	private static final Logger logger = LogManager.getLogger(CalendarioRESTImpl.class);

	private CalendarioLocal calendarioEJB;

	{
		try {
			calendarioEJB = JNDIUtils.getCalendarioEJB();
		} catch (NamingException e) {
			e.printStackTrace();
		}
	}

	@GET
	@Path("/initCalendario")
	@Override
	public void inizializzaCalendario(@QueryParam("stagione") String stagione) {
		logger.info("inizializzaCalendario - CALLED");
		calendarioEJB.inizializzaCalendario(stagione);
	}

	@GET
	@Path("/getGiornate")
	@Override
	public Giornate getGiornate() {
		logger.info("getGiornate - CALLED");
		return calendarioEJB.getGiornate();
	}

	@Override
	public void unmarshallAndSaveFromNodeCalendario(int idGiornata, TagNode calendarNode) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Calendario getCalendarioByIdGiornataIdSquadra(int idGiornata, int idSquadra) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getNomeSquadraAvversaria(int idGiornata, int idSquadra) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isSquadraFuoriCasa(int idGiornata, int idSquadra) {
		// TODO Auto-generated method stub
		return false;
	}

}
