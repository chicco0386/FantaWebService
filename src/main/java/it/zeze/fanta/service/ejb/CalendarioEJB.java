package it.zeze.fanta.service.ejb;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.htmlcleaner.TagNode;

import it.zeze.fanta.service.definition.ejb.CalendarioLocal;
import it.zeze.fanta.service.definition.ejb.CalendarioRemote;
import it.zeze.fanta.service.rest.CalendarioRESTImpl;
import it.zeze.fantaformazioneweb.entity.Calendario;
import it.zeze.fantaformazioneweb.entity.Giornate;

@Stateless
@LocalBean
public class CalendarioEJB implements CalendarioLocal, CalendarioRemote{
	
	private static final Logger logger = LogManager.getLogger(CalendarioRESTImpl.class);
	
	@PersistenceContext(unitName = "FantaFormazioneService")
	EntityManager em;

	@Override
	public void inizializzaCalendario() {
		logger.info("inizializzaCalendario - CALLED 1");
	}

	@Override
	public Giornate getGiornate() {
		return (Giornate) em.createQuery("SELECT g FROM Giornate g").getResultList().get(0);
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
