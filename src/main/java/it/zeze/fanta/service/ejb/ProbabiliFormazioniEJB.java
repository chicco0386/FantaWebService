package it.zeze.fanta.service.ejb;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.Query;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import it.zeze.fanta.db.DBManager;
import it.zeze.fanta.service.definition.ejb.GiornateLocal;
import it.zeze.fanta.service.definition.ejb.ProbabiliFormazioniLocal;
import it.zeze.fanta.service.definition.ejb.ProbabiliFormazioniRemote;
import it.zeze.fantaformazioneweb.entity.ProbabiliFormazioni;
import it.zeze.fantaformazioneweb.entity.ProbabiliFormazioniId;
import it.zeze.util.Constants;

@Stateless
@LocalBean
public class ProbabiliFormazioniEJB implements ProbabiliFormazioniLocal, ProbabiliFormazioniRemote {

	private static final Logger log = LogManager.getLogger(ProbabiliFormazioniEJB.class);
	
	@EJB(name = "DBManager")
	private DBManager dbManager;
	
	@EJB(name="GiornateEJB")
	private GiornateLocal giornateEJB;

	private static final String SELECT_BY_ID_GIORNATA_ID_UTENTIFORMAZIONE = "select probabiliFormazioni from ProbabiliFormazioni probabiliFormazioni where probabiliFormazioni.id.idGiornate=:idGiornata and probabiliFormazioni.id.idUtentiFormazioni=:idUtentiFormazione order by probabiliFormazioni.giocatori.ruolo desc";
	private static final String DELETE_BY_ID_GIORNATA_ID_UTENTIFORMAZIONE = "delete from ProbabiliFormazioni probabiliFormazioni where probabiliFormazioni.id.idGiornate=:idGiornata and probabiliFormazioni.id.idUtentiFormazioni=:idUtentiFormazione";
	private static final String DELETE_BY_ID_UTENTI_FORMAZIONI = "DELETE FROM ProbabiliFormazioni WHERE id.idUtentiFormazioni=:idUtentiFormazioni";

	private List<ProbabiliFormazioni> resultList;

	@Override
	public List<ProbabiliFormazioni> getRisultati(int idUtentiFormazione, String stagione, int numeroGiornata) {
		List<ProbabiliFormazioni> response = new ArrayList<ProbabiliFormazioni>();
		if (idUtentiFormazione > -1 && (stagione != null && !stagione.isEmpty()) && numeroGiornata > -1) {
			int idGiornata = giornateEJB.getIdGiornata(numeroGiornata, stagione);
			response = getProbFormazioniByGiornataUtentiFormazione(idGiornata, idUtentiFormazione);
		}
		this.resultList = response;
		return response;
	}

	@Override
	public List<ProbabiliFormazioni> getProbFormazioniByGiornataUtentiFormazione(int idGiornata, int idUtentiFormazione) {
		Query query = dbManager.getEm().createQuery(SELECT_BY_ID_GIORNATA_ID_UTENTIFORMAZIONE);
		query.setParameter("idGiornata", idGiornata);
		query.setParameter("idUtentiFormazione", idUtentiFormazione);
		List<ProbabiliFormazioni> toReturn = query.getResultList();
		log.info("------ " + toReturn.size());
		return toReturn;
	}

	@Override
	public void deleteProbFormazioniByGiornataUtentiFormazione(int idGiornata, int idUtentiFormazione) {
		Query query = dbManager.getEm().createQuery(DELETE_BY_ID_GIORNATA_ID_UTENTIFORMAZIONE);
		query.setParameter("idGiornata", idGiornata);
		query.setParameter("idUtentiFormazione", idUtentiFormazione);
		log.info("Cancellati [" + query.executeUpdate() + "] record da ProbabiliFormazioni");
	}
	
	@Override
	public void deleteProbFormazioniByUtentiFormazione(int idUtentiFormazione) {
		Query query = dbManager.getEm().createQuery(DELETE_BY_ID_UTENTI_FORMAZIONI);
		query.setParameter("idUtentiFormazioni", idUtentiFormazione);
		log.info("Cancellati [" + query.executeUpdate() + "] record da ProbabiliFormazioni");
	}

	@Override
	public void insertProbFormazione(int idGiornata, int idUtentiFormazione, int idGiocatore, int probTit, int probPanc, String note) {
		log.debug("Inserisco probabile formazione idGiornata [" + idGiornata + "] idUtentiFormazione [" + idUtentiFormazione + "] idGiocatore [" + idGiocatore + "] probTit [" + probTit + "] probPanc [" + probPanc + "] note [" + note + "]");
		ProbabiliFormazioniId probabiliFormazioniId = null;
		if (note != null) {
			probabiliFormazioniId = new ProbabiliFormazioniId(idGiornata, idUtentiFormazione, idGiocatore, probTit, probPanc, note);
		} else {
			probabiliFormazioniId = new ProbabiliFormazioniId(idGiornata, idUtentiFormazione, idGiocatore, probTit, probPanc);
		}
		ProbabiliFormazioni probabiliFormazioni = new ProbabiliFormazioni();
		probabiliFormazioni.setId(probabiliFormazioniId);
		dbManager.persist(probabiliFormazioni);
	}

	@Override
	public List<ProbabiliFormazioni> getResultList() {
		return resultList;
	}

	@Override
	public void setResultList(List<ProbabiliFormazioni> resultList) {
		this.resultList = resultList;
	}

	/**
	 * Per le probabilita' di fantagazzetta hanno valore 3
	 * 
	 * @param probabilita
	 * @return
	 */
	@Override
	public boolean isFantaGazzettaSource(int probabilita) {
		boolean fantaGazzetta = false;
		if (probabilita != 0) {
			if (probabilita % Constants.PROB_FANTA_GAZZETTA == 0) {
				fantaGazzetta = true;
			} else {
				int probTemp = probabilita - Constants.PROB_GAZZETTA;
				if (probTemp != 0 && probTemp % Constants.PROB_FANTA_GAZZETTA == 0) {
					fantaGazzetta = true;
				}
			}
		}
		return fantaGazzetta;
	}

	/**
	 * Per le probabilita' di fantagazzetta hanno valore 5
	 * 
	 * @param probabilita
	 * @return
	 */
	@Override
	public boolean isGazzettaSource(int probabilita) {
		boolean gazzetta = false;
		if (probabilita != 0) {
			if (probabilita % Constants.PROB_GAZZETTA == 0) {
				gazzetta = true;
			} else {
				int probTemp = probabilita - Constants.PROB_FANTA_GAZZETTA;
				if (probTemp != 0 && probTemp % Constants.PROB_GAZZETTA == 0) {
					gazzetta = true;
				}
			}
		}
		return gazzetta;
	}
}
