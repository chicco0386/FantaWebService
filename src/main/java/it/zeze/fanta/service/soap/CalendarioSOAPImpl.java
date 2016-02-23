package it.zeze.fanta.service.soap;

import javax.jws.WebMethod;
import javax.jws.WebService;

import org.htmlcleaner.TagNode;

import it.zeze.fanta.service.definition.CalendarioInterface;
import it.zeze.fantaformazioneweb.entity.Calendario;
import it.zeze.fantaformazioneweb.entity.Giornate;

@WebService(name="FantaFormazioneSOAP")
public class CalendarioSOAPImpl implements CalendarioInterface {

	@WebMethod
	@Override
	public void inizializzaCalendario() {
		
	}

	@WebMethod
	@Override
	public Giornate getGiornate() {
		// TODO Auto-generated method stub
		return null;
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
