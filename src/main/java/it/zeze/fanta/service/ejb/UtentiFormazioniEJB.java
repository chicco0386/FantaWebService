package it.zeze.fanta.service.ejb;

import java.math.BigDecimal;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.Query;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import it.zeze.fanta.db.DBManager;
import it.zeze.fanta.service.bean.MessageResponse;
import it.zeze.fanta.service.bean.MessageSeverity;
import it.zeze.fanta.service.bean.ServiceResponse;
import it.zeze.fanta.service.definition.ejb.FormazioniLocal;
import it.zeze.fanta.service.definition.ejb.ProbabiliFormazioniLocal;
import it.zeze.fanta.service.definition.ejb.UtentiFormazioniLocal;
import it.zeze.fanta.service.definition.ejb.UtentiFormazioniRemote;
import it.zeze.fantaformazioneweb.entity.Utenti;
import it.zeze.fantaformazioneweb.entity.UtentiFormazioni;

@Stateless
@LocalBean
public class UtentiFormazioniEJB implements UtentiFormazioniLocal, UtentiFormazioniRemote {

	private static final Logger log = LogManager.getLogger(UtentiFormazioniEJB.class);
	
	@EJB(name = "DBManager")
	private DBManager dbManager;

	@EJB(name = "FormazioniEJB")
	FormazioniLocal formazioniEJB;

	@EJB(name = "ProbabiliFormazioniEJB")
	ProbabiliFormazioniLocal probabiliFormazioniEJB;

	private static final String SELECT_BY_NOME_AND_UTENTE_ID = "select utentiFormazioni from UtentiFormazioni utentiFormazioni where utentiFormazioni.nomeFormazione=:nomeFormazione and utentiFormazioni.utenti.id=:idUtente";
	private static final String SELECT_BY_ID_AND_UTENTE_ID = "select utentiFormazioni from UtentiFormazioni utentiFormazioni where utentiFormazioni.id=:idUtenteFormazione and utentiFormazioni.utenti.id=:idUtente";
	private static final String DELETE_BY_ID_AND_UTENTE_ID = "delete from UtentiFormazioni utentiFormazioni where utentiFormazioni.id=:idUtenteFormazione and utentiFormazioni.utenti.id=:idUtente";

	private UtentiFormazioni utentiFormazioni = new UtentiFormazioni();

	@Override
	public ServiceResponse insertUtenteFormazione(String nomeFormazione, int idUtente, BigDecimal crediti) {
		log.info("Start inserimento formazione [" + nomeFormazione + "] dell'utente con id [" + idUtente + "]");
		ServiceResponse toReturn  = new ServiceResponse();
		UtentiFormazioni utentiFormazioni = null;
		MessageResponse message = new MessageResponse();
		if (nomeFormazione == null || StringUtils.isBlank(nomeFormazione)) {
//			StatusMessages.instance().add(Severity.ERROR, "Devi inserire il nome della tua formazione");
			message.setSeverity(MessageSeverity.ERROR);
			message.setMessage("Devi inserire il nome della tua formazione");
		} else if (esisteUtentiFormazioni(nomeFormazione, idUtente)) {
//			StatusMessages.instance().add(Severity.ERROR, "Hai gi� inserito una formazione con questo nome");
			message.setSeverity(MessageSeverity.ERROR);
			message.setMessage("Hai gia' inserito una formazione con questo nome");
		} else {
			UtentiFormazioni toInsert = new UtentiFormazioni();
			Utenti utenteToInsert = new Utenti();
			utenteToInsert.setId(idUtente);
			toInsert.setUtenti(utenteToInsert);
			toInsert.setNomeFormazione(nomeFormazione);
			if (crediti != null) {
				toInsert.setCrediti(crediti);
			}
			dbManager.persist(toInsert);
			dbManager.getEm().flush();
			utentiFormazioni = getUtentiFormazioniId(nomeFormazione, idUtente);
			log.info("End inserita utente formazione con ID [" + utentiFormazioni.getId() + "]");
		}
		toReturn.addMessage(message);
		toReturn.setObjectResponse(utentiFormazioni);
		return toReturn;
	}

	@Override
	public ServiceResponse updateUtenteFormazione(int idUtentiFormazioni, String nomeFormazione, int idUtente, BigDecimal crediti) {
		log.info("Start update formazione id [" + idUtentiFormazioni + "] dell'utente con id [" + idUtente + "] con [" + nomeFormazione + "]");
		UtentiFormazioni utentiFormazioni = null;
		ServiceResponse toReturn  = new ServiceResponse();
		MessageResponse message = new MessageResponse();
		if (nomeFormazione == null || StringUtils.isBlank(nomeFormazione)) {
//			StatusMessages.instance().add(Severity.ERROR, "Devi inserire il nome della tua formazione");
			message.setSeverity(MessageSeverity.ERROR);
			message.setMessage("Devi inserire il nome della tua formazione");
		} else if (idUtentiFormazioni != -1) {
			utentiFormazioni = getUtentiFormazioniByIdAndIdUtente(idUtentiFormazioni, idUtente);
			utentiFormazioni.setNomeFormazione(nomeFormazione);
			if (crediti != null) {
				utentiFormazioni.setCrediti(crediti);
			}
			dbManager.persist(utentiFormazioni);
			log.info("End update utente formazione con ID [" + utentiFormazioni.getId() + "]");
		}
		toReturn.addMessage(message);
		toReturn.setObjectResponse(utentiFormazioni);
		return toReturn;
	}

	@Override
	public boolean esisteUtentiFormazioni(String nomeFormazione, int idUtente) {
		boolean exist = false;
		UtentiFormazioni utenteFormazioneReturned = getUtentiFormazioniId(nomeFormazione, idUtente);
		if (utenteFormazioneReturned != null) {
			exist = true;
		}
		return exist;
	}

	@Override
	public boolean esisteUtentiFormazioni(int idUtenteFormazione, int idUtente) {
		boolean exist = false;
		UtentiFormazioni utenteFormazioneReturned = getUtentiFormazioniByIdAndIdUtente(idUtenteFormazione, idUtente);
		if (utenteFormazioneReturned != null) {
			exist = true;
		}
		return exist;
	}

	@Override
	public UtentiFormazioni getUtentiFormazioniId(String nomeFormazione, int idUtente) {
		UtentiFormazioni toReturn = null;
		Query query = dbManager.getEm().createQuery(SELECT_BY_NOME_AND_UTENTE_ID);
		query.setParameter("nomeFormazione", nomeFormazione);
		query.setParameter("idUtente", idUtente);
		List<UtentiFormazioni> resultList = query.getResultList();
		if (resultList.isEmpty()) {
			log.error("Nessun utentiFormazione trovato con nome [" + nomeFormazione + "] ID utente [" + idUtente + "]");
		} else {
			toReturn = resultList.get(0);
		}
		return toReturn;
	}

	@Override
	public UtentiFormazioni getUtentiFormazioniByIdAndIdUtente(int idUtenteFormazione, int idUtente) {
		UtentiFormazioni toReturn = null;
		Query query = dbManager.getEm().createQuery(SELECT_BY_ID_AND_UTENTE_ID);
		query.setParameter("idUtenteFormazione", idUtenteFormazione);
		query.setParameter("idUtente", idUtente);
		List<UtentiFormazioni> resultList = query.getResultList();
		if (resultList != null && resultList.size() == 0) {
			log.error("Nessun utentiFormazione trovato con ID [" + idUtenteFormazione + "] ID utente [" + idUtente + "]");
		} else {
			toReturn = resultList.get(0);
		}
		return toReturn;
	}

	private void copiaUtentiFormazioni(int idUtenteFormazione, int idUtente) {
		UtentiFormazioni currentUF = getUtentiFormazioniByIdAndIdUtente(idUtenteFormazione, idUtente);
		String nomeCopia = currentUF.getNomeFormazione().concat("_copy");
		log.info("TODO COPY");
	}

	@Override
	public UtentiFormazioni getUtentiFormazioni() {
		return utentiFormazioni;
	}

	@Override
	public void delete(int idUtentiFormazioni, int idUtente) {
		formazioniEJB.deleteGiocatoreByIdFormazione(idUtentiFormazioni);
		probabiliFormazioniEJB.deleteProbFormazioniByUtentiFormazione(idUtentiFormazioni);
		Query query = dbManager.getEm().createQuery(DELETE_BY_ID_AND_UTENTE_ID);
		query.setParameter("idUtenteFormazione", idUtentiFormazioni);
		query.setParameter("idUtente", idUtente);
		query.executeUpdate();
	}
}
