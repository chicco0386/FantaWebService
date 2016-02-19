package it.zeze.fanta.service.soap;

import javax.jws.WebMethod;
import javax.jws.WebService;

import it.zeze.fanta.service.definition.CalendarioInterface;
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

}
