package it.zeze.fanta.service.ejb.proxy.seam;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Remote;
import javax.ejb.Stateless;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.htmlcleaner.TagNode;

import it.zeze.fanta.service.definition.ejb.CalendarioLocal;
import it.zeze.fanta.service.definition.ejb.proxy.seam.CalendarioSeamRemote;
import it.zeze.fantaformazioneweb.entity.Calendario;
import it.zeze.fantaformazioneweb.entity.Giornate;
import it.zeze.fantaformazioneweb.entity.wrapper.GiornateWrap;

@Stateless
@LocalBean
public class CalendarioSeamEJB implements CalendarioSeamRemote {
	
	private static final Logger log = LogManager.getLogger(CalendarioSeamEJB.class);
	
	@EJB(name="CalendarioEJB")
	private CalendarioLocal calendarioEJB;
	
	@Override
	public void inizializzaCalendario(String stagione) {
		calendarioEJB.inizializzaCalendario(stagione);
	}

	@Override
	public GiornateWrap getGiornate() {
		Giornate ejbResult = calendarioEJB.getGiornate();
		GiornateWrap toReturn = null;
		return toReturn;
	}

	@Override
	public void unmarshallAndSaveFromNodeCalendario(int idGiornata, TagNode calendarNode) {
		calendarioEJB.unmarshallAndSaveFromNodeCalendario(idGiornata, calendarNode);
	}

	@Override
	public Calendario getCalendarioByIdGiornataIdSquadra(int idGiornata, int idSquadra) {
		return calendarioEJB.getCalendarioByIdGiornataIdSquadra(idGiornata, idSquadra);
	}

	@Override
	public String getNomeSquadraAvversaria(int idGiornata, int idSquadra) {
		return calendarioEJB.getNomeSquadraAvversaria(idGiornata, idSquadra);
	}

	@Override
	public boolean isSquadraFuoriCasa(int idGiornata, int idSquadra) {
		return calendarioEJB.isSquadraFuoriCasa(idGiornata, idSquadra);
	}

}
