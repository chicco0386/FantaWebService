package it.zeze.fanta.service.ejb;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import it.zeze.fanta.service.bean.FormazioneBeanCommon;
import it.zeze.fanta.service.bean.GiocatoriMercato;
import it.zeze.fanta.service.definition.ejb.FormazioneMercatoLocal;
import it.zeze.fanta.service.definition.ejb.FormazioneMercatoRemote;
import it.zeze.fanta.service.definition.ejb.FormazioniLocal;
import it.zeze.fanta.service.definition.ejb.UtentiFormazioniLocal;
import it.zeze.fantaformazioneweb.entity.Formazioni;
import it.zeze.fantaformazioneweb.entity.UtentiFormazioni;
import it.zeze.fantaformazioneweb.entity.wrapper.GiocatoriWrap;
import it.zeze.util.GiocatoriComparator;

@Stateless
@LocalBean
public class FormazioneMercatoEJB implements FormazioneMercatoLocal, FormazioneMercatoRemote {
	
	/*
	 * Classe FormazioniBea in SEAM
	 */

	private static final Logger log = LogManager.getLogger(FormazioneMercatoEJB.class);

	@EJB(name="UtentiFormazioniEJB")
	private UtentiFormazioniLocal utentiFormazioniEJB;

	@EJB(name="FormazioniEJB")
	private FormazioniLocal formazioniEJB;

	@Override
	public FormazioneBeanCommon initListaGiocatoriMercato(FormazioneBeanCommon common, int idUtente) {
		log.info("Init listaGiocatoriMercato doInit [" + common.isDoInit() + "]");
		if (common.getIdUtenteFormazioneToInit() != -1) {
			if (common.isDoInit()) {
				if (utentiFormazioniEJB.esisteUtentiFormazioni(common.getIdUtenteFormazioneToInit(), idUtente)) {
					common.getListaGiocatoriMercato().clear();
					List<Formazioni> formazioni = formazioniEJB.selectFormazioniByIdUtenteFormazioni(common.getIdUtenteFormazioneToInit());
					for (int i = 0; i < formazioni.size(); i++) {
						GiocatoriMercato giocatoreMercato = new GiocatoriMercato(formazioni.get(i).getGiocatori());
						if (formazioni.get(i).getPrezzoAcquisto() == null) {
							giocatoreMercato.setPrezzoAcquisto(BigDecimal.ZERO);
						} else {
							giocatoreMercato.setPrezzoAcquisto(formazioni.get(i).getPrezzoAcquisto());
						}
						common.getListaGiocatoriMercato().add(giocatoreMercato);
					}
					// lo setto in modo da nn rifare l'init se eseguo un action
					// ajax sulla pagina
					common.setDoInit(false);
				}
			}
		}
		Collections.sort(common.getListaGiocatoriMercato(), Collections.reverseOrder(new GiocatoriComparator()));
		return common;
	}

	@Override
	public FormazioneBeanCommon initListaGiocatori(FormazioneBeanCommon common, int idUtente) {
		log.info("Init listaGiocatori doInit [" + common.isDoInit() + "]");
		if (common.getIdUtenteFormazioneToInit() != -1) {
			if (common.isDoInit()) {
				if (utentiFormazioniEJB.esisteUtentiFormazioni(common.getIdUtenteFormazioneToInit(), idUtente)) {
					common.getListaGiocatori().clear();
					List<Formazioni> formazioni = formazioniEJB.selectFormazioniByIdUtenteFormazioni(common.getIdUtenteFormazioneToInit());
					for (int i = 0; i < formazioni.size(); i++) {
						common.getListaGiocatori().add(new GiocatoriWrap(formazioni.get(i).getGiocatori()));
					}
					// lo setto in modo da nn rifare l'init se eseguo un action
					// ajax sulla pagina
					common.setDoInit(false);
				}
			}
		}
		Collections.sort(common.getListaGiocatori(), Collections.reverseOrder(new GiocatoriComparator()));
		return common;
	}

	@Override
	public FormazioneBeanCommon initCreditiResidui(FormazioneBeanCommon common, int idUtentiFormazioni, int idUtente) {
		log.info("initCreditiResidui doInitCrediti [" + common.isDoInitCrediti() + "]");
		UtentiFormazioni utForm = utentiFormazioniEJB.getUtentiFormazioniByIdAndIdUtente(idUtentiFormazioni, idUtente);
		if (utForm.getCrediti() != null) {
			common.setCreditiResidui(utForm.getCrediti());
		} else {
			common.setCreditiResidui(BigDecimal.ZERO);
		}
		common.setDoInitCrediti(false);
		return common;
	}
}
