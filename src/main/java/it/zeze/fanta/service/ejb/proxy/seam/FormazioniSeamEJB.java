package it.zeze.fanta.service.ejb.proxy.seam;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

import it.zeze.fanta.service.bean.GiocatoriMercato;
import it.zeze.fanta.service.bean.ServiceResponse;
import it.zeze.fanta.service.definition.ejb.FormazioniLocal;
import it.zeze.fanta.service.definition.ejb.proxy.seam.FormazioniSeamRemote;
import it.zeze.fantaformazioneweb.entity.Formazioni;
import it.zeze.fantaformazioneweb.entity.Giocatori;
import it.zeze.fantaformazioneweb.entity.wrapper.FormazioniWrap;
import it.zeze.fantaformazioneweb.entity.wrapper.GiocatoriWrap;

@Stateless
@LocalBean
public class FormazioniSeamEJB implements FormazioniSeamRemote {

	@EJB(name = "FormazioniEJB")
	private FormazioniLocal formazioniEJB;

	@Override
	public ServiceResponse insertFormazioneMercato(String nomeFormazione, List<GiocatoriMercato> listaGiocatori, int idUtente, int idUtenteFormazioneToUpdate, BigDecimal crediti) {
		return formazioniEJB.insertFormazioneMercato(nomeFormazione, listaGiocatori, idUtente, idUtenteFormazioneToUpdate, crediti);
	}

	@Override
	public ServiceResponse insertFormazione(String nomeFormazione, List<GiocatoriWrap> listaGiocatori, int idUtente, int idUtenteFormazioneToUpdate) {
		List<Giocatori> toPass = new ArrayList<Giocatori>();
		for (GiocatoriWrap current : listaGiocatori) {
			toPass.add(current.unwrap());
		}
		return formazioniEJB.insertFormazione(nomeFormazione, toPass, idUtente, idUtenteFormazioneToUpdate);
	}

	@Override
	public ServiceResponse calcolaFormazione(int idUtentiFormazioni, String stagioneDaCalcolare, int numeroGiornataDaCalcolare) {
		return formazioniEJB.calcolaFormazione(idUtentiFormazioni, stagioneDaCalcolare, numeroGiornataDaCalcolare);
	}

	@Override
	public List<FormazioniWrap> selectFormazioniByIdUtenteFormazioni(int idUtentiFormazioni) {
		List<FormazioniWrap> toReturn = new ArrayList<FormazioniWrap>();
		List<Formazioni> ejbResp = formazioniEJB.selectFormazioniByIdUtenteFormazioni(idUtentiFormazioni);
		for (Formazioni current : ejbResp) {
			toReturn.add(new FormazioniWrap(current));
		}
		return toReturn;
	}

	@Override
	public void deleteGiocatoreByIdFormazione(int idUtentiFormazioni) {
		formazioniEJB.deleteGiocatoreByIdFormazione(idUtentiFormazioni);
	}
}
