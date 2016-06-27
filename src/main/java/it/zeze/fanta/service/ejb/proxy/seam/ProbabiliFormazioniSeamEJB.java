package it.zeze.fanta.service.ejb.proxy.seam;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

import it.zeze.fanta.service.definition.ejb.ProbabiliFormazioniLocal;
import it.zeze.fanta.service.definition.ejb.proxy.seam.ProbabiliFormazioniSeamRemote;
import it.zeze.fantaformazioneweb.entity.ProbabiliFormazioni;
import it.zeze.fantaformazioneweb.entity.wrapper.ProbabiliFormazioniWrap;

@Stateless
@LocalBean
public class ProbabiliFormazioniSeamEJB implements ProbabiliFormazioniSeamRemote {
	
	@EJB(name = "ProbabiliFormazioniEJB")
	private ProbabiliFormazioniLocal probabiliFormazioniEJB;

	@Override
	public List<ProbabiliFormazioniWrap> getRisultati(int idUtentiFormazione, String stagione, int numeroGiornata) {
		List<ProbabiliFormazioni> responseEJB = probabiliFormazioniEJB.getRisultati(idUtentiFormazione, stagione, numeroGiornata);
		List<ProbabiliFormazioniWrap> toReturn = new ArrayList<ProbabiliFormazioniWrap>();
		for (ProbabiliFormazioni current : responseEJB){
			toReturn.add(new ProbabiliFormazioniWrap(current));
		}
		return toReturn;
	}

	@Override
	public List<ProbabiliFormazioniWrap> getProbFormazioniByGiornataUtentiFormazione(int idGiornata, int idUtentiFormazione) {
		List<ProbabiliFormazioni> responseEJB = probabiliFormazioniEJB.getProbFormazioniByGiornataUtentiFormazione(idGiornata, idUtentiFormazione);
		List<ProbabiliFormazioniWrap> toReturn = new ArrayList<ProbabiliFormazioniWrap>();
		for (ProbabiliFormazioni current : responseEJB){
			toReturn.add(new ProbabiliFormazioniWrap(current));
		}
		return toReturn;
	}

	@Override
	public void deleteProbFormazioniByGiornataUtentiFormazione(int idGiornata, int idUtentiFormazione) {
		probabiliFormazioniEJB.deleteProbFormazioniByGiornataUtentiFormazione(idGiornata, idUtentiFormazione);
	}
	
	@Override
	public void deleteProbFormazioniByUtentiFormazione(int idUtentiFormazione) {
		probabiliFormazioniEJB.deleteProbFormazioniByUtentiFormazione(idUtentiFormazione);
	}

	@Override
	public void insertProbFormazione(int idGiornata, int idUtentiFormazione, int idGiocatore, int probTit, int probPanc, String note) {
		probabiliFormazioniEJB.insertProbFormazione(idGiornata, idUtentiFormazione, idGiocatore, probTit, probPanc, note);
	}

	@Override
	public boolean isFantaGazzettaSource(int probabilita) {
		return probabiliFormazioniEJB.isFantaGazzettaSource(probabilita);
	}

	@Override
	public boolean isGazzettaSource(int probabilita) {
		return probabiliFormazioniEJB.isGazzettaSource(probabilita);
	}
}
