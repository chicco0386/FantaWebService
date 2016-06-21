package it.zeze.fanta.service.ejb.proxy.seam;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import it.zeze.fanta.service.definition.ejb.StatisticheLocal;
import it.zeze.fanta.service.definition.ejb.proxy.seam.StatisticheSeamRemote;
import it.zeze.fantaformazioneweb.entity.Statistiche;

@Stateless
@LocalBean
public class StatisticheSeamEJB implements StatisticheSeamRemote {

	private static final Logger log = LogManager.getLogger(StatisticheSeamEJB.class);

	@EJB(name = "StatisticheEJB")
	private StatisticheLocal statisticheEJB;

	@Override
	public void unmarshallAndSaveFromHtmlFile(String stagione) {
		statisticheEJB.unmarshallAndSaveFromHtmlFile(stagione);
	}
	
	@Override
	public Statistiche getStatisticheIdGiocatoreIdGiornata(int idGiocatore, int idGiornata){
		return statisticheEJB.getStatisticheIdGiocatoreIdGiornata(idGiocatore, idGiornata);
	}

}
