package it.zeze.fanta.service.ejb;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.Query;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import it.zeze.fanta.db.DBManager;
import it.zeze.fanta.service.bean.GiocatoriMercato;
import it.zeze.fanta.service.bean.MessageSeverity;
import it.zeze.fanta.service.bean.ServiceResponse;
import it.zeze.fanta.service.definition.ejb.FormazioneMercatoLocal;
import it.zeze.fanta.service.definition.ejb.FormazioniFGLocal;
import it.zeze.fanta.service.definition.ejb.FormazioniGazzettaLocal;
import it.zeze.fanta.service.definition.ejb.FormazioniLocal;
import it.zeze.fanta.service.definition.ejb.FormazioniRemote;
import it.zeze.fanta.service.definition.ejb.GiornateLocal;
import it.zeze.fanta.service.definition.ejb.ProbabiliFormazioniLocal;
import it.zeze.fanta.service.definition.ejb.UtentiFormazioniLocal;
import it.zeze.fantaformazioneweb.entity.Formazioni;
import it.zeze.fantaformazioneweb.entity.FormazioniId;
import it.zeze.fantaformazioneweb.entity.Giocatori;
import it.zeze.fantaformazioneweb.entity.ProbabiliFormazioniFg;
import it.zeze.fantaformazioneweb.entity.ProbabiliFormazioniGazzetta;
import it.zeze.fantaformazioneweb.entity.UtentiFormazioni;
import it.zeze.util.Constants;

@Stateless
@LocalBean
public class FormazioniEJB implements FormazioniLocal, FormazioniRemote {

	private static final Logger log = LogManager.getLogger(FormazioniEJB.class);

	@EJB(name = "DBManager")
	private DBManager dbManager;

	@EJB(name = "UtentiFormazioniEJB")
	private UtentiFormazioniLocal utentiFormazioniEJB;

	@EJB(name = "FormazioniFGEJB")
	FormazioniFGLocal formazioniFGEJB;

	@EJB(name = "FormazioniGazzettaEJB")
	FormazioniGazzettaLocal formazioniGazzettaEJB;

	@EJB(name = "ProbabiliFormazioniEJB")
	ProbabiliFormazioniLocal probabiliFormazioniEJB;

	@EJB(name = "FormazioneMercatoEJB")
	FormazioneMercatoLocal formazioneBean;

	@EJB(name = "GiornateEJB")
	private GiornateLocal giornateEJB;

	private static final String SELECT_BY_ID_UTENTI_FORMAZIONI = "select formazioni from Formazioni formazioni where formazioni.id.idUtentiFormazioni=:idUtentiFormazioni";
	private static final String UPDATE_PROB_TIT_BY_ID_UTENTI_FORMAZIONI_ID_GIOCATORE = "update Formazioni formazioni set formazioni.id.probTitolare=:probTitolare where formazioni.id.idUtentiFormazioni=:idUtentiFormazioni and formazioni.id.idGiocatore=:idGiocatore";
	private static final String UPDATE_PROB_PANC_BY_ID_UTENTI_FORMAZIONI_ID_GIOCATORE = "update Formazioni formazioni set formazioni.id.probPanchina=:probPanchina where formazioni.id.idUtentiFormazioni=:idUtentiFormazioni and formazioni.id.idGiocatore=:idGiocatore";
	private static final String DELETE_GIOCATORE_BY_ID_UTENTI_FORMAZIONI_ID_GIOCATORE = "DELETE FROM Formazioni formazioni WHERE formazioni.id.idUtentiFormazioni=:idUtentiFormazioni and formazioni.id.idGiocatore=:idGiocatore";
	private static final String DELETE_GIOCATORE_BY_ID_UTENTI_FORMAZIONI = "DELETE FROM Formazioni formazioni WHERE formazioni.id.idUtentiFormazioni=:idUtentiFormazioni";

	@Override
	public ServiceResponse insertFormazioneMercato(String nomeFormazione, List<GiocatoriMercato> listaGiocatori, int idUtente, int idUtenteFormazioneToUpdate, BigDecimal crediti) {
		log.info("Start inserimento [" + listaGiocatori.size() + "] giocatori per la formazione [" + nomeFormazione + "] per ID utente [" + idUtente + "] idFormazioneToUpdate [" + idUtenteFormazioneToUpdate + "]");
		ServiceResponse toReturn = new ServiceResponse();
		Boolean result = false;
		String messageString;
		if (listaGiocatori == null || listaGiocatori.isEmpty()) {
			// StatusMessages.instance().add(Severity.ERROR, "Devi inserire
			// almeno un giocatore per creare la tua formazione");
			messageString = "Devi inserire almeno un giocatore per creare la tua formazione";
			toReturn.addMessage(MessageSeverity.ERROR, messageString);
		} else if (controllaGiocatoriDuplicatiMercato(listaGiocatori)) {
			// StatusMessages.instance().add(Severity.ERROR, "Hai inserito dei
			// giocatori duplicati");
			messageString = "Hai inserito dei giocatori duplicati";
			toReturn.addMessage(MessageSeverity.ERROR, messageString);
		} else {
			GiocatoriMercato currentGiocatore;
			UtentiFormazioni utenteFormazione;
			List<GiocatoriMercato> listaGiocatoriMod = new ArrayList<GiocatoriMercato>(listaGiocatori);
			formazioneBean.setDoInit(true);
			formazioneBean.initListaGiocatoriMercato(idUtente);
			List<GiocatoriMercato> listGiocatoriAttualiInFormazione = formazioneBean.getListaGiocatoriMercato();
			log.info("Init [" + listGiocatoriAttualiInFormazione.size() + "]");
			if (idUtenteFormazioneToUpdate == -1) {
				utenteFormazione = (UtentiFormazioni) utentiFormazioniEJB.insertUtenteFormazione(nomeFormazione, idUtente, crediti).getObjectResponse();
				listGiocatoriAttualiInFormazione.clear();
			} else {
				utenteFormazione = (UtentiFormazioni) utentiFormazioniEJB.updateUtenteFormazione(idUtenteFormazioneToUpdate, nomeFormazione, idUtente, crediti).getObjectResponse();
			}
			if (utenteFormazione != null) {
				// Cancellazione utenti
				List<GiocatoriMercato> listGiocToRemove = (List<GiocatoriMercato>) CollectionUtils.subtract(listGiocatoriAttualiInFormazione, listaGiocatoriMod);
				for (int i = 0; i < listGiocToRemove.size(); i++) {
					currentGiocatore = listGiocToRemove.get(i);
					log.info("currentGiocatoreToRemove: " + currentGiocatore.getNome());
					deleteGiocatoreByIdFormazioneAndIdGiocatore(utenteFormazione.getId(), currentGiocatore.getId());
				}
				// Inserisco eventuali nuovi giocatori
				listaGiocatoriMod = (List<GiocatoriMercato>) CollectionUtils.subtract(listaGiocatoriMod, listGiocToRemove);
				for (int i = 0; i < listaGiocatoriMod.size(); i++) {
					currentGiocatore = listaGiocatoriMod.get(i);
					if (!listGiocatoriAttualiInFormazione.contains(currentGiocatore)) {
						Formazioni toInsert = new Formazioni();
						toInsert.setId(new FormazioniId(currentGiocatore.getId(), utenteFormazione.getId()));
						toInsert.setPrezzoAcquisto(currentGiocatore.getPrezzoAcquisto());
						dbManager.persist(toInsert);
					}
				}
				result = true;
				log.info("End inserimento [" + listaGiocatoriMod.size() + "] e cancellazione [" + listGiocToRemove.size() + "] giocatori per la formazione [" + nomeFormazione + "] per ID utente [" + idUtente + "]");
			}
		}
		if (result) {
			if (idUtenteFormazioneToUpdate == -1) {
				messageString = "Formazione [" + nomeFormazione + "] inserita correttamente";
				toReturn.addMessage(MessageSeverity.INFO, messageString);
				// StatusMessages.instance().add(Severity.INFO, "Formazione [" +
				// nomeFormazione + "] inserita correttamente");
			} else {
				messageString = "Formazione [" + nomeFormazione + "] aggiornata correttamente";
				toReturn.addMessage(MessageSeverity.INFO, messageString);
				// StatusMessages.instance().add(Severity.INFO, "Formazione [" +
				// nomeFormazione + "] aggiornata correttamente");
			}
		}
		toReturn.setObjectResponse(result);
		return toReturn;
	}

	@Override
	public ServiceResponse insertFormazione(String nomeFormazione, List<Giocatori> listaGiocatori, int idUtente, int idUtenteFormazioneToUpdate) {
		log.info("Start inserimento [" + listaGiocatori.size() + "] giocatori per la formazione [" + nomeFormazione + "] per ID utente [" + idUtente + "] idFormazioneToUpdate [" + idUtenteFormazioneToUpdate + "]");
		ServiceResponse toReturn = new ServiceResponse();
		Boolean result = false;
		String messageString;
		if (listaGiocatori == null || listaGiocatori.isEmpty()) {
			// StatusMessages.instance().add(Severity.ERROR,
			// "StatusMessages.instance().add(Severity.ERROR, "Devi inserire
			// almeno un giocatore per creare la tua formazione");");
			messageString = "Devi inserire almeno un giocatore per creare la tua formazione";
			toReturn.addMessage(MessageSeverity.ERROR, messageString);
		} else if (controllaGiocatoriDuplicati(listaGiocatori)) {
			// StatusMessages.instance().add(Severity.ERROR, "Hai inserito dei
			// giocatori duplicati");
			messageString = "Hai inserito dei giocatori duplicati";
			toReturn.addMessage(MessageSeverity.ERROR, messageString);
		} else {
			Giocatori currentGiocatore;
			UtentiFormazioni utenteFormazione;
			List<Giocatori> listaGiocatoriMod = new ArrayList<Giocatori>(listaGiocatori);
			formazioneBean.setDoInit(true);
			formazioneBean.initListaGiocatori(idUtente);
			List<Giocatori> listGiocatoriAttualiInFormazione = formazioneBean.getListaGiocatori();
			log.info("Init [" + listGiocatoriAttualiInFormazione.size() + "]");
			if (idUtenteFormazioneToUpdate == -1) {
				utenteFormazione = (UtentiFormazioni) utentiFormazioniEJB.insertUtenteFormazione(nomeFormazione, idUtente, null).getObjectResponse();
				listGiocatoriAttualiInFormazione.clear();
			} else {
				utenteFormazione = (UtentiFormazioni) utentiFormazioniEJB.updateUtenteFormazione(idUtenteFormazioneToUpdate, nomeFormazione, idUtente, null).getObjectResponse();
			}
			if (utenteFormazione != null) {
				// Cancellazione utenti
				List<Giocatori> listGiocToRemove = (List<Giocatori>) CollectionUtils.subtract(listGiocatoriAttualiInFormazione, listaGiocatoriMod);
				for (int i = 0; i < listGiocToRemove.size(); i++) {
					currentGiocatore = listGiocToRemove.get(i);
					log.info("currentGiocatoreToRemove: " + currentGiocatore.getNome());
					deleteGiocatoreByIdFormazioneAndIdGiocatore(utenteFormazione.getId(), currentGiocatore.getId());
				}
				// Inserisco eventuali nuovi giocatori
				listaGiocatoriMod = (List<Giocatori>) CollectionUtils.subtract(listaGiocatoriMod, listGiocToRemove);
				for (int i = 0; i < listaGiocatoriMod.size(); i++) {
					currentGiocatore = listaGiocatoriMod.get(i);
					if (!listGiocatoriAttualiInFormazione.contains(currentGiocatore)) {
						Formazioni toInsert = new Formazioni();
						toInsert.setId(new FormazioniId(currentGiocatore.getId(), utenteFormazione.getId()));
						dbManager.persist(toInsert);
					}
				}
				result = true;
				log.info("End inserimento [" + listaGiocatoriMod.size() + "] e cancellazione [" + listGiocToRemove.size() + "] giocatori per la formazione [" + nomeFormazione + "] per ID utente [" + idUtente + "]");
			}
		}
		if (result) {
			if (idUtenteFormazioneToUpdate == -1) {
				// StatusMessages.instance().add(Severity.INFO, "Formazione [" +
				// nomeFormazione + "] inserita correttamente");
				messageString = "Formazione [" + nomeFormazione + "] inserita correttamente";
				toReturn.addMessage(MessageSeverity.INFO, messageString);
			} else {
				// StatusMessages.instance().add(Severity.INFO, "Formazione [" +
				// nomeFormazione + "] aggiornata correttamente");
				messageString = "Formazione [" + nomeFormazione + "] aggiornata correttamente";
				toReturn.addMessage(MessageSeverity.INFO, messageString);
			}
		}
		toReturn.setObjectResponse(result);
		return toReturn;
	}

	@Override
	public ServiceResponse calcolaFormazione(int idUtentiFormazioni, String stagioneDaCalcolare, int numeroGiornataDaCalcolare) {
		ServiceResponse toReturn = new ServiceResponse();
		Boolean result = false;
		log.info("Calcolo probabili formazioni per IdUtentiFormazioe [" + idUtentiFormazioni + "] stagione [" + stagioneDaCalcolare + "] e giornata [" + numeroGiornataDaCalcolare + "]");
		String messageString;
		if (stagioneDaCalcolare == null || stagioneDaCalcolare.trim().isEmpty()) {
			// StatusMessages.instance().add(Severity.ERROR, "Selezionare la
			// stagione e la giornata relativa");
			messageString = "Selezionare la stagione e la giornata relativa";
			toReturn.addMessage(MessageSeverity.ERROR, messageString);
			log.error("Selezionare la stagione e la giornata relativa");
		} else if (numeroGiornataDaCalcolare < 1) {
			// StatusMessages.instance().add(Severity.ERROR, "Selezionare la
			// stagione e la giornata relativa");
			messageString = "Selezionare la stagione e la giornata relativa";
			toReturn.addMessage(MessageSeverity.ERROR, messageString);
			log.error("Selezionare la stagione e la giornata relativa");
		} else {
			int idGiornata = giornateEJB.getIdGiornata(numeroGiornataDaCalcolare, stagioneDaCalcolare);
			ServiceResponse responseCalcolaFormazione = calcolaFormazione(idUtentiFormazioni, idGiornata);
			result = (Boolean) responseCalcolaFormazione.getObjectResponse();
			if (responseCalcolaFormazione.getMessageResponse() != null && !responseCalcolaFormazione.getMessageResponse().isEmpty()) {
				toReturn.addMessage(responseCalcolaFormazione.getMessageResponse().get(0));
			}
		}
		toReturn.setObjectResponse(result);
		return toReturn;
	}

	private ServiceResponse calcolaFormazione(int idUtentiFormazioni, int idGiornata) {
		log.info("Calcolo probabili formazioni per IdUtentiFormazioe [" + idUtentiFormazioni + "] e idGiornata [" + idGiornata + "]");
		ServiceResponse toReturn = new ServiceResponse();
		Boolean result = false;
		List<Formazioni> listFormazioni = selectFormazioniByIdUtenteFormazioni(idUtentiFormazioni);
		if (listFormazioni != null && !listFormazioni.isEmpty()) {
			Formazioni currentFormazioni;
			int currentIdGiocatore;
			ProbabiliFormazioniFg currentProbFormazioniFg;
			ProbabiliFormazioniGazzetta currentProbFormazioniGazzetta;
			int currentProbTit = 0;
			int currentProbPanc = 0;
			// Cancello eventuali formazioni calcolate in precedenza per la
			// stessa giornata e stesso idUtentiFormazioni
			probabiliFormazioniEJB.deleteProbFormazioniByGiornataUtentiFormazione(idGiornata, idUtentiFormazioni);
			for (int i = 0; i < listFormazioni.size(); i++) {
				currentProbTit = 0;
				currentProbPanc = 0;
				currentFormazioni = listFormazioni.get(i);
				currentIdGiocatore = currentFormazioni.getId().getIdGiocatore();
				// Prendo probabilit� FG
				currentProbFormazioniFg = formazioniFGEJB.selectByIdGiocatoreIdGiornata(currentIdGiocatore, idGiornata);
				if (currentProbFormazioniFg != null) {
					if (currentProbFormazioniFg.getId().isTitolare()) {
						currentProbTit = currentProbTit + Constants.PROB_FANTA_GAZZETTA;
					} else if (currentProbFormazioniFg.getId().isPanchina()) {
						currentProbPanc = currentProbPanc + +Constants.PROB_FANTA_GAZZETTA;
					}
				}
				// Prendo probabilita' Gazzetta
				currentProbFormazioniGazzetta = formazioniGazzettaEJB.selectByIdGiocatoreIdGiornata(currentIdGiocatore, idGiornata);
				if (currentProbFormazioniGazzetta != null) {
					if (currentProbFormazioniGazzetta.getId().isTitolare()) {
						currentProbTit = currentProbTit + +Constants.PROB_GAZZETTA;
					} else if (currentProbFormazioniGazzetta.getId().isPanchina()) {
						currentProbPanc = currentProbPanc + +Constants.PROB_GAZZETTA;
					}
				}
				probabiliFormazioniEJB.insertProbFormazione(idGiornata, idUtentiFormazioni, currentIdGiocatore, currentProbTit, currentProbPanc, null);
			}
			result = true;
			// StatusMessages.instance().add(Severity.INFO, "Formazione calcata
			// correttamente");
			toReturn.addMessage(MessageSeverity.INFO, "Formazione calcata correttamente");
			log.info("Fine calcolo probabili formazioni per IdUtentiFormazione [" + idUtentiFormazioni + "] e idGiornata [" + idGiornata + "]");
		} else {
			log.error("Nessuna formazione trovata per IdUtentiFormazione [" + idUtentiFormazioni + "] e idGiornata [" + idGiornata + "]");
		}
		toReturn.setObjectResponse(result);
		return toReturn;
	}

	@Override
	public List<Formazioni> selectFormazioniByIdUtenteFormazioni(int idUtentiFormazioni) {
		Query query = dbManager.getEm().createQuery(SELECT_BY_ID_UTENTI_FORMAZIONI);
		query.setParameter("idUtentiFormazioni", idUtentiFormazioni);
		List<Formazioni> toReturn = query.getResultList();
		return toReturn;
	}

	private void updateFormazioniProbTitByUIdUtenteFormazioneIdGiocatore(int idUtentiFormazioni, int idGiornata) {
		Query query = dbManager.getEm().createQuery(UPDATE_PROB_TIT_BY_ID_UTENTI_FORMAZIONI_ID_GIOCATORE);
		query.setParameter("idUtentiFormazioni", idUtentiFormazioni);
		query.setParameter("idUtentiFormazioni", idGiornata);
	}

	private void deleteGiocatoreByIdFormazioneAndIdGiocatore(int idUtentiFormazioni, int idGiocatore) {
		Query query = dbManager.getEm().createQuery(DELETE_GIOCATORE_BY_ID_UTENTI_FORMAZIONI_ID_GIOCATORE);
		query.setParameter("idUtentiFormazioni", idUtentiFormazioni);
		query.setParameter("idGiocatore", idGiocatore);
		query.executeUpdate();
	}

	@Override
	public void deleteGiocatoreByIdFormazione(int idUtentiFormazioni) {
		Query query = dbManager.getEm().createQuery(DELETE_GIOCATORE_BY_ID_UTENTI_FORMAZIONI);
		query.setParameter("idUtentiFormazioni", idUtentiFormazioni);
		query.executeUpdate();
	}

	private boolean controllaGiocatoriDuplicati(List<Giocatori> listaGiocatori) {
		boolean trovatoDuplicati = false;
		Giocatori currentGiocatori;
		for (int i = 0; i < listaGiocatori.size() && !trovatoDuplicati; i++) {
			currentGiocatori = listaGiocatori.get(i);
			int frequency = Collections.frequency(listaGiocatori, currentGiocatori);
			if (frequency > 1) {
				trovatoDuplicati = true;
			}
		}
		return trovatoDuplicati;
	}

	private boolean controllaGiocatoriDuplicatiMercato(List<GiocatoriMercato> listaGiocatori) {
		boolean trovatoDuplicati = false;
		GiocatoriMercato currentGiocatori;
		for (int i = 0; i < listaGiocatori.size() && !trovatoDuplicati; i++) {
			currentGiocatori = listaGiocatori.get(i);
			int frequency = Collections.frequency(listaGiocatori, currentGiocatori);
			if (frequency > 1) {
				trovatoDuplicati = true;
			}
		}
		return trovatoDuplicati;
	}
}
