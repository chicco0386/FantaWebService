package it.zeze.fanta.service.ejb.proxy.seam;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

import it.zeze.fanta.service.definition.ejb.StatisticheLocal;
import it.zeze.fanta.service.definition.ejb.proxy.seam.StatisticheSeamRemote;
import it.zeze.fantaformazioneweb.entity.Giocatori;
import it.zeze.fantaformazioneweb.entity.Giornate;
import it.zeze.fantaformazioneweb.entity.Statistiche;
import it.zeze.fantaformazioneweb.entity.wrapper.StatisticheWrap;

@Stateless
@LocalBean
public class StatisticheSeamEJB implements StatisticheSeamRemote {

	@EJB(name = "StatisticheEJB")
	private StatisticheLocal statisticheEJB;

	@Override
	public void unmarshallAndSaveFromHtmlFile(String stagione) {
		statisticheEJB.unmarshallAndSaveFromHtmlFile(stagione);
	}

	@Override
	public StatisticheWrap getStatisticheIdGiocatoreIdGiornata(int idGiocatore, int idGiornata) {
		StatisticheWrap toReturn = null;
		Statistiche ejbResp = statisticheEJB.getStatisticheIdGiocatoreIdGiornata(idGiocatore, idGiornata);
		toReturn = new StatisticheWrap(ejbResp);
		return toReturn;
	}

	@Override
	public List<StatisticheWrap> initResultList(Giornate giornate, Giocatori giocatori, String orderColumn, String orderDir) {
		List<StatisticheWrap> toReturn = new ArrayList<StatisticheWrap>(); 
		List<Statistiche> ejbResult = statisticheEJB.initResultList(giornate, giocatori, orderColumn, orderDir);
		for (Statistiche current : ejbResult){
			toReturn.add(new StatisticheWrap(current));
		}
		return toReturn;
	}
	
	@Override
	public List<StatisticheWrap> resetResumeStatistiche(List<Statistiche> resultList, Giornate giornate, Giocatori giocatori, String orderColumn, String orderDir){
		List<StatisticheWrap> toReturn = new ArrayList<StatisticheWrap>(); 
		List<Statistiche> ejbResult = statisticheEJB.resetResumeStatistiche(resultList, giornate, giocatori, orderColumn, orderDir);
		for (Statistiche current : ejbResult){
			toReturn.add(new StatisticheWrap(current));
		}
		return toReturn;
	}

}
