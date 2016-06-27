package it.zeze.fanta.service.ejb.proxy.seam;

import java.math.BigDecimal;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

import it.zeze.fanta.service.bean.ServiceResponse;
import it.zeze.fanta.service.definition.ejb.UtentiFormazioniLocal;
import it.zeze.fanta.service.definition.ejb.proxy.seam.UtentiFormazioniSeamRemote;
import it.zeze.fantaformazioneweb.entity.UtentiFormazioni;
import it.zeze.fantaformazioneweb.entity.wrapper.UtentiFormazioniWrap;

@Stateless
@LocalBean
public class UtentiFormazioniSeamEJB implements UtentiFormazioniSeamRemote {
	
	@EJB(name = "UtentiFormazioniEJB")
	private UtentiFormazioniLocal utentiFormazioniEJB;

	@Override
	public ServiceResponse insertUtenteFormazione(String nomeFormazione, int idUtente, BigDecimal crediti) {
		return utentiFormazioniEJB.insertUtenteFormazione(nomeFormazione, idUtente, crediti);
	}

	@Override
	public ServiceResponse updateUtenteFormazione(int idUtentiFormazioni, String nomeFormazione, int idUtente, BigDecimal crediti) {
		return utentiFormazioniEJB.updateUtenteFormazione(idUtentiFormazioni, nomeFormazione, idUtente, crediti);
	}

	@Override
	public boolean esisteUtentiFormazioni(String nomeFormazione, int idUtente) {
		return utentiFormazioniEJB.esisteUtentiFormazioni(nomeFormazione, idUtente);
	}

	@Override
	public boolean esisteUtentiFormazioni(int idUtenteFormazione, int idUtente) {
		return utentiFormazioniEJB.esisteUtentiFormazioni(idUtenteFormazione, idUtente);
	}

	@Override
	public UtentiFormazioniWrap getUtentiFormazioniId(String nomeFormazione, int idUtente) {
		UtentiFormazioni ejbResp = utentiFormazioniEJB.getUtentiFormazioniId(nomeFormazione, idUtente);
		UtentiFormazioniWrap toReturn = new UtentiFormazioniWrap(ejbResp);
		return toReturn;
	}

	@Override
	public UtentiFormazioniWrap getUtentiFormazioniByIdAndIdUtente(int idUtenteFormazione, int idUtente) {
		UtentiFormazioni ejbResp = utentiFormazioniEJB.getUtentiFormazioniByIdAndIdUtente(idUtenteFormazione, idUtente);
		UtentiFormazioniWrap toReturn = new UtentiFormazioniWrap(ejbResp);
		return toReturn;
	}

	@Override
	public void delete(int idUtentiFormazioni, int idUtente) {
		utentiFormazioniEJB.delete(idUtentiFormazioni, idUtente);
	}
}
