package it.zeze.fanta.service.rest;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

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
@Stateless
public class CalendarioRESTImpl implements CalendarioInterface {

	private static final Logger logger = LogManager.getLogger(CalendarioRESTImpl.class);

	@EJB(name = "CalendarioEJB")
	private CalendarioLocal calendarioEJB;

	@GET
	@Path("/initCalendario")
	@Override
	public void inizializzaCalendario() {
		logger.info("inizializzaCalendario - CALLED");
		calendarioEJB.inizializzaCalendario();
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
