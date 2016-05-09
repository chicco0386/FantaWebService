package it.zeze.fanta.service.ejb.proxy.seam;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import it.zeze.fanta.service.definition.ejb.GiocatoriLocal;
import it.zeze.fanta.service.definition.ejb.proxy.seam.GiocatoriSeamRemote;
import it.zeze.fantaformazioneweb.entity.Giocatori;
import it.zeze.fantaformazioneweb.entity.wrapper.GiocatoriWrap;

@Stateless
@LocalBean
public class GiocatoriSeamEJB implements GiocatoriSeamRemote {

	private static final Logger log = LogManager.getLogger(GiornateSeamEJB.class);

	@EJB(name = "GiocatoriEJB")
	private GiocatoriLocal giocatoriEJB;

	public void unmarshallAndSaveFromHtmlFile(String stagione, boolean noLike) {
		giocatoriEJB.unmarshallAndSaveFromHtmlFile(stagione, noLike);
	}

	public void unmarshallAndSaveFromHtmlFileForUpdateStagione(boolean noLike) {
		giocatoriEJB.unmarshallAndSaveFromHtmlFileForUpdateStagione(noLike);
	}

	public void insertOrUpdateGiocatore(String nomeSquadra, String nomeGiocatore, String ruolo, String stagione, boolean noLike) {
		giocatoriEJB.insertOrUpdateGiocatore(nomeSquadra, nomeGiocatore, ruolo, stagione, noLike);
	}

	@Override
	public GiocatoriWrap getGiocatoreByNomeSquadraRuolo(String nomeGiocatore, String squadra, String ruolo, String stagione, boolean noLike) {
		Giocatori toWrap = giocatoriEJB.getGiocatoreByNomeSquadraRuolo(nomeGiocatore, squadra, ruolo, stagione, noLike);
		GiocatoriWrap toReturn = new GiocatoriWrap(toWrap);
		return toReturn;
	}

	@Override
	public GiocatoriWrap getGiocatoreByNomeSquadra(String nomeGiocatore, String squadra, String stagione, boolean noLike) {
		Giocatori toWrap = giocatoriEJB.getGiocatoreByNomeSquadra(nomeGiocatore, squadra, stagione, noLike);
		GiocatoriWrap toReturn = new GiocatoriWrap(toWrap);
		return toReturn;
	}

	@Override
	public GiocatoriWrap getGiocatoreById(int idGiocatore) {
		Giocatori toWrap = giocatoriEJB.getGiocatoreById(idGiocatore);
		GiocatoriWrap toReturn = new GiocatoriWrap(toWrap);
		return toReturn;
	}
}
